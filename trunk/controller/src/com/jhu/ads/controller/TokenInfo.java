package com.jhu.ads.controller;

import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenInfo {

	private int lastExpiredToken;
	private Lock lock;
	public Lock getLock() {
		return lock;
	}
	TreeMap<Integer , Token> tokenList;
	
	public TokenInfo() {
		tokenList = new TreeMap<Integer , Token>();
		lastExpiredToken = 0;
		lock = new ReentrantLock();
	}
	
	public int getLastExpiredToken() {
		return lastExpiredToken;
	}
	public void setLastExpiredToken(int lastExpiredToken) {
		this.lastExpiredToken = lastExpiredToken;
	}
}
