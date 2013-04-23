package com.jhu.ads.webserver;

public class DataCenter {
	private String name;
	private int controllerIP;
	private int currentToken;
	private int maxToken;
	private String spreadGroupName;

    public String getToken() {
        return null; // TODO
    }
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getControllerIP() {
		return controllerIP;
	}

	public void setControllerIP(int controllerIP) {
		this.controllerIP = controllerIP;
	}

	public int getCurrentToken() {
		return currentToken;
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

}
