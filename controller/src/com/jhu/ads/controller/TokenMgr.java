package com.jhu.ads.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.SerializationUtils;

import spread.AdvancedMessageListener;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import com.jhu.ads.common.TokenResponseMsg;
import com.jhu.ads.controller.common.ConfigMgr;

public class TokenMgr implements AdvancedMessageListener, Runnable {
	// Add the new token to list of corresponding Web server
	// A thread to clean up expired tokens
	private SpreadConnection connection;
    private AtomicInteger tokenCount;  // modify this inside sync
	
	String GLOBAL_SPREAD_GROUP_NAME = "GLOBAL_GROUP";

	HashMap<String, TokenInfo> tokenHolder = new HashMap<String, TokenInfo>();
	HashMap<SpreadGroup, Integer> webserveTokenCount = new HashMap<SpreadGroup, Integer>();

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
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Joined " + globalGroup + ".");

		// Set the total token count to the total wowza server capacity
		tokenCount.set(DataCenterMgr.getInstance().determineCurrentCapacity());
	}

	/*
	 *  Identify the wowza media server and webserver membership change and set the flag accordingly
	 */
	@Override
	public void membershipMessageReceived(SpreadMessage membershipMsg) {  
		//Currently ignoring all membership messages
	}

	@Override
	public void regularMessageReceived(SpreadMessage regularMsg) { // handle token requests
		sendTokens(regularMsg.getSender());
	}
	
	public void sendTokens(SpreadGroup sender) {
    	TokenResponseMsg tokenResponseMsg = new TokenResponseMsg();
    	int currentBatchCount, newTokenCount;
		if(tokenCount.get() < Integer.parseInt(ConfigMgr.getInstance().getTokenBatchCount())){
			currentBatchCount = tokenCount.get();
		}else{
			currentBatchCount = Integer.parseInt(ConfigMgr.getInstance().getTokenBatchCount());
		}
		if(!webserveTokenCount.containsKey(sender)){
			webserveTokenCount.put(sender, currentBatchCount);
		}else{
			newTokenCount = webserveTokenCount.get(sender) + currentBatchCount;
			webserveTokenCount.put(sender, newTokenCount);
		}
		tokenCount.addAndGet(-currentBatchCount);
    	tokenResponseMsg.setMaxCount(webserveTokenCount.get(sender));  
    	byte[] data = SerializationUtils.serialize(tokenResponseMsg);
    	SpreadMessage message = new SpreadMessage();
    	message.setData(data);
    	message.addGroup(sender);
    	message.setReliable();
    	// TODO: Add Type of message
    	try {
			connection.multicast(message);
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public void handleToken(String webserver_tokenID) {
		//Check if the token is valid
		String sender = webserver_tokenID.substring(0,webserver_tokenID.lastIndexOf("_"));  
		String tokenIDStr = webserver_tokenID.substring(sender.indexOf("_")+1);
		Token token = new Token();
		int tokenID = Integer.parseInt(tokenIDStr);
		if( tokenID > tokenHolder.get(sender).lastExpiredToken){
			token.setRecvdTime(System.currentTimeMillis());
			token.setTokenId(tokenID);
			//get current index
				
//			tokenHolder.get(sender[0].toString()).tokenList.add(index, token);	
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	public AtomicInteger getTokenCount() {
		return tokenCount;
	}

	public void setTokenCount(AtomicInteger tokenCount) {
		this.tokenCount = tokenCount;
	}

}
