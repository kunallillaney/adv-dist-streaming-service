package com.jhu.ads.common;

import java.io.Serializable;

/**
 * UserInfo contains the information about the user copied from the Maxmind
 * Database
 * 
 * @author klillaney
 * 
 */
public class UserInfo implements Serializable {
    
    private static final long serialVersionUID = 848031025199035002L;
    
    private String locationIp;
	private String countryName;
	private String cityName;
	private String postalCode;
	private double latitude;
	private double longitude;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "IP:" + this.locationIp + ":Country:" + this.countryName
				+ ":City:" + this.cityName + ":PostalCode:" + this.postalCode
				+ ":Latitude:" + this.latitude + ":Longitude:" + this.longitude;
	}

	public String getLocationIp() {
		return locationIp;
	}

	public void setLocationIp(String locationIp) {
		this.locationIp = locationIp;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

}
