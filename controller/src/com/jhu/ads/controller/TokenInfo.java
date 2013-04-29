package com.jhu.ads.controller;

import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenInfo {

    private static final int UNINITIALIZED = -1;
    
	private int lastExpiredToken;
	private Lock lock;
	public Lock getLock() {
		return lock;
	}
	TreeMap<Integer , Token> tokenList;
	
	public TokenInfo() {
		tokenList = new TreeMap<Integer , Token>();
		lastExpiredToken = UNINITIALIZED;
		lock = new ReentrantLock();
	}
	
	public int getLastExpiredToken() {
		return lastExpiredToken;
	}
	public void setLastExpiredToken(int lastExpiredToken) {
		this.lastExpiredToken = lastExpiredToken;
	}
}
