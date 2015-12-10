/**
 * Created by fl_xu on 2015/12/10.
 * 读取配置文件
 */


import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

public class Configuration {
    private static final Logger log = Logger.getLogger(Configuration.class.getName());
    private static Properties p;

    static {
        p = new Properties();

        String path = getWebRoot() + File.separator + "WEB-INF" + File.separator + "conf" + File.separator + "conf.properties";
        try {
            FileInputStream in = new FileInputStream(path);
            p.load(in);
            in.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getConfigValue(String name) {
        return p.getProperty(name);
    }

    @SuppressWarnings("unchecked")
    public static Enumeration<String> getConfigValuesEnum() {
        return (Enumeration<String>) p.propertyNames();
    }

    public static String getWebRoot() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        URL url = classLoader.getResource("struts.xml");
        String filepath = url.getPath();
        filepath = filepath.substring(0, filepath.lastIndexOf("/"));
        filepath = filepath.substring(0, filepath.lastIndexOf("/"));
        filepath = filepath.substring(0, filepath.lastIndexOf("/"));
        return filepath;
    }
}
