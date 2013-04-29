package com.jhu.ads.controller;

public class Token {
	private int tokenId;
	private long recvdTime;

	public Token(int id) {
	    this.tokenId = id;
        this.recvdTime = 0;
    }
	
	public int getTokenId() {
		return tokenId;
	}

	public void setTokenId(int tokenId) {
		this.tokenId = tokenId;
	}

	public long getRecvdTime() {
		return recvdTime;
	}

	public void setRecvdTime(long recvdTime) {
		this.recvdTime = recvdTime;
	}
}
