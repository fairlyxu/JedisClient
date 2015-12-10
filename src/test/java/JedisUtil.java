

/**
 * Created by fl_xu on 2015/12/9.
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import redis.clients.jedis.*;
import redis.clients.util.Pool;

@SuppressWarnings("deprecation")
public class JedisUtil {
    private static Logger log = Logger.getLogger(JedisUtil.class.getName());

    //分片客户端连接
    private static ShardedJedis shardedJedis;
    //分片客户端连接池
    private static ShardedJedisPool shardedJedisPool;
    //jedispool配置
    private static JedisPoolConfig config;
    //默认过期时间为一天
    private static final int DEFAULT_EXPIRED_TIME = 60 * 60 * 24; // 1 day
    // 创建全局的唯一实例
    public static JedisUtil ju = new JedisUtil();

    /**
     * 设置与缓存服务器的连接池
     * 初始化配置
     *
     */
    static {
        initJedisPoolConfig();
        Set set = initHostAndPorts();
        initShardedJedisPool(set);
    }

    /**
     * 初始化jedisconfig的基本配置
     */
    private static void initJedisPoolConfig() {
        config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(10);
        config.setMaxWaitMillis(1000L);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
    }

    /**
     * 从数据库读取服务器ip，port
     */
    private static Set initHostAndPorts() {
        Set<HostAndPort> set = new HashSet<HostAndPort>();
        try {
            String hosts1 = Configuration.getConfigValue("redis.server");
            String[] tempHosts = hosts1.split(",");
            if (tempHosts == null || tempHosts.length < 1) {
                return null;
            }
            for (String temp : tempHosts) {
                String[] server = temp.split(":");
                HostAndPort host = new HostAndPort(server[0], Integer.parseInt(server[1]));
                set.add(host);
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
        }
        return set;
    }

    /**
     * 将数据切片到不同服务器中
     *
     * @param shardsSet
     * @return
     */
    private static void initShardedJedisPool(Set<HostAndPort> shardsSet) {
        initJedisPoolConfig();
        List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
        for (HostAndPort temp : shardsSet) {
            shards.add(new JedisShardInfo(temp.getHost(), temp.getPort()));
        }
        shardedJedisPool = new ShardedJedisPool(config, shards);
    }

    public static ShardedJedis getShardedJedis() {
        shardedJedis = shardedJedisPool.getResource();
        return shardedJedis;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void returnResource(Pool pool, Object redis) {
        if (redis != null) {
            pool.returnResourceObject(redis);
        }
    }

    /**
     * 构造方法，不允许实例化！
     */
    private JedisUtil() {
    }

    /**
     * 添加一个指定的值到缓存中.
     *
     * @param key
     * @param value
     * @return
     */
    //设置  key-value
    public static void set(String key, String value) {
        try {
            shardedJedis = getShardedJedis();
            shardedJedis.setex(key, DEFAULT_EXPIRED_TIME, value);
        } catch (Exception e) {
            log.error(e);
            shardedJedisPool.returnBrokenResource(shardedJedis);
        } finally {
            returnResource(shardedJedisPool, shardedJedis);
        }
    }


    /**
     * 设置  key-value //设置过期时间
     *
     * @param key
     * @param value
     * @param sec
     * @return
     */
    public static void set(String key, String value, int sec) {
        try {
            shardedJedis = getShardedJedis();
            shardedJedis.setex(key, sec, value);
        } catch (Exception e) {
            log.error(e);
            shardedJedisPool.returnBrokenResource(shardedJedis);
        } finally {
            returnResource(shardedJedisPool, shardedJedis);
        }
    }


    /**
     * 得到shard key-value
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        String value = null;
        try {
            shardedJedis = getShardedJedis();
            value = shardedJedis.getShard(key).get(key);
        } catch (Exception e) {
            log.error(e);
            // 释放资源
            shardedJedisPool.returnBrokenResource(shardedJedis);
        } finally {
            returnResource(shardedJedisPool, shardedJedis);
        }
        return value;
    }

}
