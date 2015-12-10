import redis.clients.jedis.Jedis;

/**
 * Created by fl_xu on 2015/12/7.
 */
public class JedisDemo {
    public static void setDemo(){
        Jedis je = new Jedis("192.168.39.14",6379);
        je.set("name" , "xufl");
        je.set("university", "tongji");
        je.setex("exTime", 5, "5s times");
        je.mset("k1", "v1", "k1", "v2");
        je.setex("foo", 5, "haha");
        System.out.println(je.dbSize());
        System.out.println(je.get("k2"));
    }
    public static void main(String arg[]){
        JedisDemo.setDemo();
    }
}
