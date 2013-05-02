package com.jhu.ads.webserver;

import java.io.IOException;

import com.jhu.ads.common.UserInfo;
import com.jhu.ads.webserver.common.ConfigMgr;
import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

/**
 * This class interfaces with the Maxmind API's and provides the user info based
 * on the IP address of the request
 * 
 * @author klillaney
 */
public class GeoIPMgr {
	LookupService lookUp;
	private volatile static GeoIPMgr geoSingleton = null;

	private GeoIPMgr() {
	}

	public void init() {
		try {
			lookUp = new LookupService(ConfigMgr.getInstance().getGeoIPFilePath(),
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

	/**
	 * Gets the zipcode from the Maxmind Database
	 * 
	 * @param locationIp
	 * @return int
	 */
	public int getZipCode(String locationIp) {
		System.out.println("Query for:"+locationIp);
		Location centerIp = lookUp.getLocation(locationIp);
		if (centerIp == null)
			return 21210;
		return (centerIp.postalCode != null) ? Integer
				.parseInt(centerIp.postalCode) : 21210;
	}

	/**
	 * Gets the user information from the Maxmind Database and inserts into
	 * UserInfo
	 * 
	 * @param locationIp
	 * @return UserInfo
	 */
	public UserInfo getUserInfo(String locationIp) {
		Location centerIp = lookUp.getLocation(locationIp);
		UserInfo user = new UserInfo();
		if (centerIp == null) {
			return null;
		}
		
		user.setLocationIp(locationIp);
		user.setCountryName(centerIp.countryName);
		user.setCityName(centerIp.city);
		user.setPostalCode(centerIp.postalCode);
		user.setLatitude((double) centerIp.latitude);
		user.setLongitude((double) centerIp.longitude);
		
		return user;
	}


}
