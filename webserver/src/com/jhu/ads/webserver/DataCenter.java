package com.jhu.ads.webserver;

import java.util.concurrent.atomic.AtomicInteger;

import com.jhu.ads.webserver.common.ConfigMgr;

public class DataCenter {
	private String name;
	private String controllerIP;
	private AtomicInteger currentToken; /* Token Available to be used currently */
	private int maxToken = 100; // TODO
	private String spreadGroupName;
	private boolean isAlive = true; //TODO /* Set this in the Token Manager when it starts up. */

    public DataCenter(String name, String controllerIP, String spreadGroupName) {
        this.name = name;
        this.controllerIP = controllerIP;
        this.spreadGroupName = spreadGroupName;
        currentToken = new AtomicInteger(0);
    }

    public int getAndIncrementCurrentToken() {
        return currentToken.getAndIncrement();
    }
    
    public boolean isDataCenterFull() {
        return (currentToken.get() < maxToken) ? false : true;
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

    @Override
	public String toString() {
	    return "[ " + name + "; " + controllerIP + "; " + spreadGroupName + " ]";
	}

}
