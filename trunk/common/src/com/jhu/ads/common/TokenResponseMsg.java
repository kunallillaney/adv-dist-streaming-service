package com.jhu.ads.common;

import java.io.Serializable;

public class TokenResponseMsg implements Serializable{

	private static final long serialVersionUID = -3910913710019308955L;

	private int maxCount;

	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}
	
}
