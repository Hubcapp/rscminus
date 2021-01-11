package rscminus.scraper;


import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class ScraperDatabasePooledDataSource {
    private ComboPooledDataSource cpds;
    boolean init() {
        try {
            // Loading sql settings from file
            Properties properties = new Properties();
            InputStream inputStream = new FileInputStream("sqlCredentials.txt");
            properties.load(inputStream);
            String connectionPrefix = properties.getProperty("DB_CONNECTION_URL_PREFIX");
            ScraperDatabase.sqlIP = properties.getProperty("DB_IP");
            ScraperDatabase.sqlPort = properties.getProperty("DB_PORT");
            ScraperDatabase.sqlDatabaseName = properties.getProperty("SCRAPER_DATABASE_NAME");
            ScraperDatabase.sqlUsername = properties.getProperty("DB_USERNAME");
            ScraperDatabase.sqlPassword = properties.getProperty("DB_PASSWORD");
            inputStream.close();

            Properties p = new Properties(System.getProperties());
            p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
            p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF"); // Off or any other level
            System.setProperties(p);

            cpds = new ComboPooledDataSource();
            cpds.setJdbcUrl(connectionPrefix + ScraperDatabase.sqlIP + ":" + ScraperDatabase.sqlPort + "/" + ScraperDatabase.sqlDatabaseName);
            cpds.setUser(ScraperDatabase.sqlUsername);
            cpds.setPassword(ScraperDatabase.sqlPassword);

            // the settings below are optional
            // c3p0 can work with defaults
            cpds.setInitialPoolSize(5);
            cpds.setMinPoolSize(5);
            cpds.setAcquireIncrement(5);
            cpds.setMaxPoolSize(20);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public javax.sql.DataSource getDataSource() {
        return cpds;
    }
}