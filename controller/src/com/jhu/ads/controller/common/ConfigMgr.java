package com.jhu.ads.controller.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigMgr {
    
    private static volatile ConfigMgr _instance = null;
    
    private static final String SPREAD_DEAMON_ADDR_PROPERTY = "SpreadDeamonAddr";
    private static final String SPREAD_DEAMON_PORT_PROPERTY = "SpreadDeamonPort";
    private static final String DATA_CENTER_NAME_PROPERTY = "DataCenterName";
    private static final String TOKEN_BATCH_COUNT_PROPERTY = "TokenBatchCount";
    private static final String WOWZA_POLLING_INTERVAL_PROPERTY = "WowzaPollingInterval";
    private static final String TOKEN_EXPIRY_TIME_PROPERTY = "TokenExpiryTime";
    private static final String TOKEN_EXPIRY_POLLING_INTERVAL_PROPERTY = "TokenExpiryPollingInterval";
    private static final String WEB_SERVER_URL_PROPERTY = "WebServerURL";
    
    private Properties props;
    
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
    
    public void init(String filePath) {
        props = new Properties();
        InputStream fin;
        try {
            fin = new FileInputStream(filePath);
            props.load(fin);
            fin.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public String getSpreadDeamonAddress() {
        return props.getProperty(SPREAD_DEAMON_ADDR_PROPERTY);
    }
    
    public int getSpreadDeamonPort() {
        return Integer.parseInt(props.getProperty(SPREAD_DEAMON_PORT_PROPERTY));
    }

    public String getDataCenterName() {
        return props.getProperty(DATA_CENTER_NAME_PROPERTY);
    }
    
    public int getTokenBatchCount() {
        return Integer.parseInt(props.getProperty(TOKEN_BATCH_COUNT_PROPERTY));
    }
    
    public int getWowzaPollingInterval() {
        return Integer.parseInt(props.getProperty(WOWZA_POLLING_INTERVAL_PROPERTY));
    }
    
    public int getTokenExpiryTime() {
        return Integer.parseInt(props.getProperty(TOKEN_EXPIRY_TIME_PROPERTY));
    }

    public int getTokenExpiryPollingInterval() {
        return Integer.parseInt(props.getProperty(TOKEN_EXPIRY_POLLING_INTERVAL_PROPERTY));
    }

    public String getWebServerURL() {
        String webServerURL = props.getProperty(WEB_SERVER_URL_PROPERTY);
        if(webServerURL == null || webServerURL.equals("")) {
            webServerURL = "http://webserver1.cnds.jhu.edu:8012/WebServer/";
        }
        return webServerURL;
    }
    
}
