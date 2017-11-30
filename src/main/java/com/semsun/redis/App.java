package com.semsun.redis;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Response;

/**
 * Hello world!
 *
 */
public class App 
{
	private JedisPool mPool = null;
	
	/**
	 * 读取Redis配置
	 * @param propertieName	Redis配置文件名，Redis配置文件需要放在CLASS_PATH下
	 * @return
	 */
	private Properties getRedisProperties(String propertieName) {
        
        Properties properties = new Properties();
        try{
        	
	        InputStream in = this.getClass().getClassLoader().getResourceAsStream(propertieName);
	        properties.load(in);
	        in.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
		
        return properties;
	}
	
	private void initRedis(String propertieName) {
		Properties properties = this.getRedisProperties(propertieName);
		
        int maxActive = Integer.parseInt( String.valueOf(properties.get("redis.pool.maxActive")) );
        int maxIdle = Integer.parseInt( String.valueOf(properties.get("redis.pool.maxIdle")) );
        int maxWait = Integer.parseInt( String.valueOf(properties.get("redis.pool.maxWait")) );
        boolean testOnBorrow = Boolean.parseBoolean( String.valueOf(properties.get("redis.pool.testOnBorrow")) );
        boolean testOnReturn = Boolean.parseBoolean( String.valueOf(properties.get("redis.pool.testOnReturn")) );
        
        String host = String.valueOf(properties.get("redis1.ip"));
        int port = Integer.parseInt( String.valueOf(properties.get("redis1.port")) );
        int database = Integer.parseInt( String.valueOf(properties.get("redis1.database")) );
        
        String msg = String.format("maxActive:%d\nmaxIdle:%d\nmaxWait:%d\ntestOnBorrow:%b\ntestOnReturn:%b\nhost:%s\nport:%d\ndatabase:%d"
        		, maxActive, maxIdle, maxWait, testOnBorrow, testOnReturn, host, port, database);
        System.out.println( msg );
        JedisPoolConfig redisPoolConf = new JedisPoolConfig();
        redisPoolConf.setMaxTotal(maxActive);
        redisPoolConf.setMaxIdle(maxIdle);
        redisPoolConf.setMaxWaitMillis(maxWait);
        redisPoolConf.setTestOnReturn(testOnReturn);
        redisPoolConf.setTestOnBorrow(testOnBorrow);
        
        mPool = new JedisPool(redisPoolConf, host, port, Protocol.DEFAULT_TIMEOUT, null, database);
//      JedisPool pool = new JedisPool(redisPoolConf, host, port);
	}
	
	public Jedis getRedisResource() {
		if( null == mPool ) return null;
		
		return mPool.getResource();
	}
	
	public void closeRedisPool() {
		if( null == mPool ) return;
		
		mPool.close();
	}
	
    public static void main( String[] args )
    {
    	App app = new App();
    	app.initRedis("redis.properties");
        Jedis redis = app.getRedisResource();
        
        Pipeline pipe = redis.pipelined();

    	System.out.println();
        List<Object> list = pipe.syncAndReturnAll();
    	System.out.println("---> Begin " + list.size());
        for(Object o:list) {
        	byte[] tmp = (byte[])o;
        	
        	System.out.println("--->" + new String(tmp));
        }
    	System.out.println("---> End");

    	System.out.println();
    	
    	/* 通过管道获取Redis数据 */
    	Response<String> res1 = pipe.get("1");
    	Response<String> res2 = pipe.get("2");
    	Response<String> res3 = pipe.get("3");
    	Response<String> res4 = pipe.get("4");
    	Response<String> res5 = pipe.get("5");
    	
    	pipe.sync();
    	
    	System.out.println( res1.get() );
    	System.out.println( res2.get() );
    	System.out.println( res3.get() );
    	System.out.println( res4.get() );
    	System.out.println( res5.get() );
    	
    	app.closeRedisPool();
    }
}
