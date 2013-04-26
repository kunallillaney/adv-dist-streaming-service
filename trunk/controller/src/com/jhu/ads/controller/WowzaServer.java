package com.jhu.ads.controller;

import java.util.concurrent.atomic.AtomicInteger;

public class WowzaServer {
	private String wowzaId;
	private String wowzaIp;
	private AtomicInteger wowzaCapacity;

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

	public AtomicInteger getWowzaCapacity() {
		return wowzaCapacity;
	}

	public void setWowzaCapacity(AtomicInteger wowzaCapacity) {
		this.wowzaCapacity = wowzaCapacity;
	}
}
