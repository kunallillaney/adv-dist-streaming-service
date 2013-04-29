package com.jhu.ads.webserver;

import java.util.concurrent.atomic.AtomicInteger;

import com.jhu.ads.webserver.common.ConfigMgr;

public class DataCenter {
    
    public static final int UNINITIALIZED = -1;
    
	private String name;
	private String controllerIP;
	private int controllerPort;
	private AtomicInteger currentToken; /* Token Available to be used currently */
	private int maxToken;
	private String spreadGroupName;
	private boolean isAlive;
	private double longitude;
	private double latitude;
	private boolean isBuilt;

    public DataCenter(String name, String controllerIP, int port, String spreadGroupName,
            double longitude, double latitude, boolean isBuilt) {
        this.name = name;
        this.controllerIP = controllerIP;
        this.controllerPort = port;
        this.spreadGroupName = spreadGroupName;
        currentToken = new AtomicInteger(0);
        this.longitude = longitude;
        this.latitude = latitude;
        this.isBuilt = isBuilt;
        isAlive = false;
        maxToken = UNINITIALIZED;
    }

    public int getAndIncrementCurrentToken() {
        return currentToken.getAndIncrement();
    }
    
    public boolean isDataCenterFull() {
        return (currentToken.get() < maxToken) ? false : true; // Very correct. DO not change this. currentToken should exclude maxToken value.
    }
    
    public String buildToken(int tokenNum) {
        return ConfigMgr.getInstance().getWebServerName() + "_" + tokenNum;
    }
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getControllerIP() {
        return controllerIP;
    }

    public void setControllerIP(String controllerIP) {
        this.controllerIP = controllerIP;
    }
    
	public int getControllerPort() {
        return controllerPort;
    }

    public void setControllerPort(int controllerPort) {
        this.controllerPort = controllerPort;
    }

    public int getMaxToken() {
		return maxToken;
	}

	public void setMaxToken(int maxToken) {
		this.maxToken = maxToken;
	}

	public String getSpreadGroupName() {
		return spreadGroupName;
	}

	public void setSpreadGroupName(String spreadGroupName) {
		this.spreadGroupName = spreadGroupName;
	}
	
	public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }
    
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public boolean isBuilt() {
		return isBuilt;
	}

	public void setBuilt(boolean isBuilt) {
		this.isBuilt = isBuilt;
	}

	@Override
	public String toString() {
	    return "[ " + name + "; " + controllerIP + "; " + spreadGroupName + " ]";
	}

}
