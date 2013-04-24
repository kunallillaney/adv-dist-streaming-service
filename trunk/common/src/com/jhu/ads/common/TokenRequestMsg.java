package com.jhu.ads.common;

import java.io.Serializable;

public class TokenRequestMsg implements Serializable{

	private static final long serialVersionUID = 7344483183436326082L;
	
	private String webserverName;

	public String getWebserverName() {
		return webserverName;
	}

	public void setWebserverName(String webserverName) {
		this.webserverName = webserverName;
	}
	
}
