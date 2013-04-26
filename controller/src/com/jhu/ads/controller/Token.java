package com.jhu.ads.controller;

public class Token {
	private int tokenId;
	private boolean valid;
	private long recvdTime;
	public int getTokenId() {
		return tokenId;
	}
	public void setTokenId(int tokenId) {
		this.tokenId = tokenId;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public long getRecvdTime() {
		return recvdTime;
	}
	public void setRecvdTime(long recvdTime) {
		this.recvdTime = recvdTime;
	}
}
