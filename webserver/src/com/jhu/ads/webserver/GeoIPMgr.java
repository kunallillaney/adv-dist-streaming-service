package com.jhu.ads.webserver;

import java.io.IOException;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

public class GeoIPMgr {
	LookupService lookUp;
	private volatile static GeoIPMgr geoSingleton = null;

	private GeoIPMgr() {
	}

	public void init(String fileName) {
		try {
			lookUp = new LookupService(fileName,
					LookupService.GEOIP_MEMORY_CACHE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static GeoIPMgr getInstance() {
		if (geoSingleton == null) {
			synchronized (GeoIPMgr.class) {
				if (geoSingleton == null) {
					geoSingleton = new GeoIPMgr();
				}
			}
		}
		return geoSingleton;
	}

	public int getZipCode(String locationIp) {
		Location centerIp = lookUp.getLocation(locationIp);
		return (centerIp.postalCode != null) ? Integer
				.parseInt(centerIp.postalCode) : 21210;
	}

	public static void main(String[] args) {
		GeoIPMgr test = new GeoIPMgr();
		test.init("C:\\Users\\klillaney\\Desktop\\Spring 2013\\Advanced Distributed Systems\\GeoIPJava-1.2.9\\GeoLiteCity.dat");
		System.out.println(test.getZipCode("125.220.221.123"));
	}

}
