package com.jhu.ads.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.SerializationUtils;

import spread.AdvancedMessageListener;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import com.jhu.ads.common.TokenRequestMsg;
import com.jhu.ads.common.TokenResponseMsg;
import com.jhu.ads.controller.common.ConfigMgr;

public class TokenMgr implements AdvancedMessageListener, Runnable {
	private SpreadConnection connection;
	private AtomicInteger remainingTokenCount; // modify this inside sync
	String GLOBAL_SPREAD_GROUP_NAME = "GLOBAL_GROUP";
	Thread recoverUnusedTokens = null;
	HashMap<String, TokenInfo> tokenHolder = new HashMap<String, TokenInfo>();
	HashMap<String, Integer> webserveTokenCount = new HashMap<String, Integer>();

	private static TokenMgr _instance = null;

	public static TokenMgr getInstance() {
		if (_instance == null) {
			synchronized (TokenMgr.class) {
				if (_instance == null) {
					_instance = new TokenMgr();
				}
			}
		}
		return _instance;
	}

	public void init() {
		// Establish the spread connection at address:port
		String spreadUser = ConfigMgr.getInstance().getDataCenterName();
		String spreadAddr = ConfigMgr.getInstance().getSpreadDeamonAddress();
		int spreadPort = ConfigMgr.getInstance().getSpreadDeamonPort();

		try {
			connection = new SpreadConnection();
			connection.connect(InetAddress.getByName(spreadAddr), spreadPort,
					spreadUser, false, true);
			connection.add(TokenMgr.getInstance());
		} catch (SpreadException e) {
			System.err.println("There was an error connecting to the daemon.");
			e.printStackTrace();
			System.exit(1);
		} catch (UnknownHostException e) {
			System.err.println("Can't find the daemon ");
			e.printStackTrace();
			System.exit(1);
		}
		// Join the global group
		SpreadGroup globalGroup = new SpreadGroup();
		try {
			globalGroup.join(connection, GLOBAL_SPREAD_GROUP_NAME);
			// TODO: Do the local datacenter spread group
		} catch (SpreadException e) {
			e.printStackTrace();
		}
		System.out.println("Joined " + globalGroup + ".");

		// Set the total token count to the total wowza server capacity
		remainingTokenCount.set(DataCenterMgr.getInstance()
				.determineCurrentCapacity());
		recoverUnusedTokens = new Thread(TokenMgr.getInstance());
		recoverUnusedTokens.start();
	}

	/*
	 * Identify the wowza media server and webserver membership change and set
	 * the flag accordingly
	 */
	@Override
	public void membershipMessageReceived(SpreadMessage membershipMsg) {
		// Currently ignoring all membership messages
	}

	@Override
	public void regularMessageReceived(SpreadMessage regularMsg) {
		// handle token requests
		TokenRequestMsg tokenRequestMsg = (TokenRequestMsg) SerializationUtils
				.deserialize(regularMsg.getData());
		sendTokens(tokenRequestMsg.getWebserverName()); // send the tokens
	}

	public void sendTokens(String webserverName) {
		TokenResponseMsg tokenResponseMsg = new TokenResponseMsg();
		int currentBatchCount, newTokenCount;
		if (remainingTokenCount.get() < ConfigMgr.getInstance()
				.getTokenBatchCount()) {
			currentBatchCount = remainingTokenCount.get(); // Give away
															// everything
		} else {
			currentBatchCount = ConfigMgr.getInstance().getTokenBatchCount();
		}
		remainingTokenCount.addAndGet(-currentBatchCount);

		Integer currentCount = webserveTokenCount.get(webserverName);
		newTokenCount = (currentCount == null ? 0 : currentCount)
				+ currentBatchCount;
		webserveTokenCount.put(webserverName, newTokenCount);

		// Add the tokens to the list
		int i = 0;
		TokenInfo webserverTokenInfo = tokenHolder.get(webserverName);
		if (webserverTokenInfo == null) {
			webserverTokenInfo = new TokenInfo();
			tokenHolder.put(webserverName, webserverTokenInfo);
		}

		webserverTokenInfo.getLock().lock();
		try {
			while (i < currentBatchCount) {
				webserverTokenInfo.tokenList.put(currentCount + i, new Token());
				i++;
			}
		} finally {
			webserverTokenInfo.getLock().unlock();
		}

		tokenResponseMsg.setMaxCount(webserveTokenCount.get(webserverName));
		byte[] data = SerializationUtils.serialize(tokenResponseMsg);
		SpreadMessage message = new SpreadMessage();
		message.setData(data);
		message.addGroup(webserverName);
		message.setReliable();
		// TODO: Add Type of message
		try {
			connection.multicast(message);
		} catch (SpreadException e) {
			e.printStackTrace();
		}
	}

	public void handleToken(String webserver_tokenID) {
		// Check if the token is valid
		String sender = webserver_tokenID.substring(0,
				webserver_tokenID.lastIndexOf("_"));
		String tokenIDStr = webserver_tokenID
				.substring(sender.indexOf("_") + 1);
		int tokenID = Integer.parseInt(tokenIDStr);
		TokenInfo tokenInfo1 = tokenHolder.get(sender);
		if (tokenInfo1 == null || tokenID < tokenInfo1.getLastExpiredToken()) {
			try {
				throw new Exception("Token " + tokenID + " Expired");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Token token = tokenInfo1.tokenList.get(tokenID);
		token.setTokenId(tokenID);
		token.setRecvdTime(System.currentTimeMillis());
		// exists in the list
		addToList(token, tokenInfo1.tokenList);
	}

	private void addToList(Token token, TreeMap<Integer, Token> tokenList) {
		tokenList.get(token.getTokenId()).setRecvdTime(
				System.currentTimeMillis());
	}

	@Override
	public void run() {
		for (;;) {
			int unusedTokenCount = 0;
			Collection<TokenInfo> values = tokenHolder.values();
			for (Iterator iterator = values.iterator(); iterator.hasNext();) {
				TokenInfo tokenInfo = (TokenInfo) iterator.next();
				NavigableSet<Integer> tokenKeys = tokenInfo.tokenList
						.navigableKeySet();

				Iterator iterator2 = tokenKeys.descendingIterator();
				Token currentToken = null;
				while (iterator2.hasNext()) {
					currentToken = (Token) iterator2.next();
					if ((System.currentTimeMillis() - tokenInfo.tokenList.get(
							currentToken).getRecvdTime()) > ConfigMgr.getInstance().getTokenExpiryTime()) {
						break;
					}
				}
				boolean isFirst = true;
				tokenInfo.getLock().lock();
				try {
					while (iterator2.hasNext()) {
						currentToken = (Token) iterator2.next();
						if (isFirst) {
							tokenInfo.setLastExpiredToken(currentToken
									.getTokenId());
							isFirst = false;
						}
						if (currentToken.getRecvdTime() == 0) {
							unusedTokenCount++;
						}
						tokenInfo.tokenList.remove(currentToken.getTokenId());
					}
				} finally {
					tokenInfo.getLock().unlock();
				}
				remainingTokenCount.getAndAdd(unusedTokenCount);
			}
			try {
				Thread.sleep(ConfigMgr.getInstance().getTokenExpiryPollingInterval());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public AtomicInteger getTokenCount() {
		return remainingTokenCount;
	}

	public void setTokenCount(AtomicInteger tokenCount) {
		this.remainingTokenCount = tokenCount;
	}
}