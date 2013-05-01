package com.jhu.ads.webserver.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigMgr {
    
    private Properties props;
    
    private String dHomePath;
    
    private static String GEO_IP_FILE_PATH_PROPERTY = "MaxMindGeoIPFilePath";
    private static String DATACENTER_INFO_FILE_PATH_PROPERTY = "DataCenterInfoFilePath";
    private static String SPREAD_DEAMON_ADDR_PROPERTY = "SpreadDeamonAddr";
    private static String SPREAD_DEAMON_PORT_PROPERTY = "SpreadDeamonPort";
    private static String WEB_SERVER_NAME_PROPERTY = "WebServerName";
    private static String REQUEST_TOKENS_WHEN_NUMBER_OF_TOKENS_REMAINING_PROPERTY = "RequestTokensWhenNumberOfTokensRemaining";
    private static String REQUEST_TOKENS_THREAD_INTERVAL_PROPERTY = "RequestTokenThreadInterval";
    
    private volatile static ConfigMgr _instance = null;
    
    public static ConfigMgr getInstance() {
        if(_instance == null) {
            synchronized (ConfigMgr.class) {
                if(_instance == null) {
                    _instance = new ConfigMgr();
                }
            }
        }
        return _instance;
    }
    
    public String getGeoIPFilePath() {
        String retPath = props.getProperty(GEO_IP_FILE_PATH_PROPERTY);
        if(retPath == null || retPath.trim().equals("")) {
            retPath = dHomePath + "/conf/GeoLiteCity.dat";
        }
        return retPath;
    }
    
    public String getDataCentersInfoFilePath() {
        String retPath = props.getProperty(DATACENTER_INFO_FILE_PATH_PROPERTY);
        if(retPath == null || retPath.trim().equals("")) {
            retPath = dHomePath + "/conf/datacenter-config.xml";
        }
        return retPath;
    }
    
    public String getSpreadDeamonAddress() {
        return props.getProperty(SPREAD_DEAMON_ADDR_PROPERTY);
    }
    
    public int getSpreadDeamonPort() {
        return Integer.parseInt(props.getProperty(SPREAD_DEAMON_PORT_PROPERTY));
    }

    public String getWebServerName() {
        return props.getProperty(WEB_SERVER_NAME_PROPERTY);
    }
    
    public int getRequestTokensWhenNumberOfTokensRemaining() {
        return Integer.parseInt(props.getProperty(REQUEST_TOKENS_WHEN_NUMBER_OF_TOKENS_REMAINING_PROPERTY));
    }
    
    public int getRequestTokenThreadInterval() {
        return Integer.parseInt(props.getProperty(REQUEST_TOKENS_THREAD_INTERVAL_PROPERTY));
    }
    
    public void init(String configFilePath, String dHomePath) {
        this.props = new Properties();
        this.dHomePath = dHomePath;
        InputStream inStream;
        try {
            inStream = new FileInputStream(configFilePath);
            props.load(inStream);
            inStream.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

}
