package com.jhu.ads.webserver;

import com.maxmind.geoip.Location;

/**
 * UserInfo contains the information about the user copied from the Maxmind
 * Database
 * 
 * @author klillaney
 * 
 */
public class UserInfo {
	private String locationIp;
	private String countryName;
	private String cityName;
	private String postalCode;
	private double latitude;
	private double longitude;

	/**
	 * Sets the data members of UserInfo object
	 * 
	 * @param location
	 * @param locationIp
	 */
	public void setUserInfo(Location location, String locationIp) {
		this.setLocationIp(locationIp);
		this.setCountryName(location.countryName);
		this.setCityName(location.city);
		this.setPostalCode(location.postalCode);
		this.setLatitude((double) location.latitude);
		this.setLongitude((double) location.longitude);
	}

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
