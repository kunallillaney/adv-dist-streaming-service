package com.jhu.ads.controller;

import java.util.concurrent.atomic.AtomicInteger;

public class WowzaServer {
	private String wowzaId;
	private String wowzaIp;
	private int streamingPort;
	private AtomicInteger currentCapacity = new AtomicInteger();
	private int maxCapacity;

	public String getWowzaId() {
		return wowzaId;
	}

	public void setWowzaId(String wowzaId) {
		this.wowzaId = wowzaId;
	}

	public String getWowzaIp() {
		return wowzaIp;
	}

    public void setWowzaIp(String wowzaIp) {
		this.wowzaIp = wowzaIp;
	}
    
    public int getStreamingPort() {
        return streamingPort;
    }

    public void setStreamingPort(int streamingPort) {
        this.streamingPort = streamingPort;
    }
    
	public AtomicInteger getCurrentCapacity() {
		return currentCapacity;
	}

	public void setCurrentCapacity(AtomicInteger currentCapacity) {
		this.currentCapacity = currentCapacity;
	}
	
	public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @Override
	public String toString() {
	    return "[WowzaId:" + wowzaId + "; WowzaIp:" + wowzaIp + "; WowzaCapacity:" + currentCapacity + "]";
	}
}
