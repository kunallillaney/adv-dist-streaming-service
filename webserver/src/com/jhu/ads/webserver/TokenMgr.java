package com.jhu.ads.webserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.apache.commons.lang.SerializationUtils;

import spread.AdvancedMessageListener;
import spread.MembershipInfo;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import com.jhu.ads.common.TokenRequestMsg;
import com.jhu.ads.common.TokenResponseMsg;
import com.jhu.ads.webserver.common.ConfigMgr;

public class TokenMgr implements AdvancedMessageListener {
	private SpreadConnection connection;
	
	String GLOBAL_SPREAD_GROUP_NAME = "GLOBAL_GROUP";
	
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
    
    public void requestTokens(DataCenter dataCenter) {
    	TokenRequestMsg tokenRequestMsg = new TokenRequestMsg();
    	tokenRequestMsg.setWebserverName(ConfigMgr.getInstance().getWebServerName());
    	byte[] data = SerializationUtils.serialize(tokenRequestMsg);
    	SpreadMessage message = new SpreadMessage();
    	message.setData(data);
    	message.addGroup(dataCenter.getSpreadGroupName());
    	message.setReliable();
    	System.out.println("Webserver Reqesting tokens to: " + dataCenter.getSpreadGroupName());
    	// TODO: Add Type
    	try {
			connection.multicast(message);
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void init() {
    	// Establish the spread connection at address:port
    	String spreadUser = ConfigMgr.getInstance().getWebServerName(); //
    	String spreadAddr = ConfigMgr.getInstance().getSpreadDeamonAddress(); //  
    	int spreadPort = ConfigMgr.getInstance().getSpreadDeamonPort(); // 
    	
    	try
		{
			connection = new SpreadConnection();
			connection.connect(InetAddress.getByName(spreadAddr), spreadPort, spreadUser, false, true);
			connection.add(TokenMgr.getInstance());
		}
		catch(SpreadException e)
		{
			System.err.println("There was an error connecting to the daemon.");
			e.printStackTrace();
			System.exit(1);
		}
		catch(UnknownHostException e)
		{
			System.err.println("Can't find the daemon ");
			e.printStackTrace();
			System.exit(1);
		}
    	
    	// Join the group
    	SpreadGroup globalGroup = new SpreadGroup();
		try {
			globalGroup.join(connection, GLOBAL_SPREAD_GROUP_NAME);
	        System.out.println("WebServer Joined " + globalGroup + ".");
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Request the initial set of tokens from all the datacenter
		Iterator<DataCenter> datacenters = DataCenterMgr.getInstance().getAllDataCenters();
		while( datacenters.hasNext()){
			requestTokens(datacenters.next());
		}
    }

	@Override
	public void membershipMessageReceived(SpreadMessage membershipMsg) {  // Identify the sender and set the flag accordingly
		try {
		    System.out.println("Web Server: Received membership change msg - "+membershipMsg.toString());
    	    if(membershipMsg.getSender().toString().equals(GLOBAL_SPREAD_GROUP_NAME)) { // If this membership message belongs to the Global group
    			MembershipInfo memberInfo = membershipMsg.getMembershipInfo();
    			if(memberInfo.isCausedByJoin()){
    				SpreadGroup memberJoined= memberInfo.getJoined();
    				if(memberJoined.toString().contains("DC")) {
    				    DataCenter dataCenter = DataCenterMgr.getInstance().getDataCenterBasedOnSpreadName(memberJoined.toString());
                        dataCenter.setAlive(true);
    				    // If there were no tokens ever requested from this data center, then request for some tokens
    				    if(dataCenter.getMaxToken() == DataCenter.UNINITIALIZED) {
    				        requestTokens(dataCenter);
    				    }
    				}
    			}else if(memberInfo.isCausedByLeave()){
    				SpreadGroup memberLeft= memberInfo.getLeft();
    				if(memberLeft.toString().contains("DC")) {
    				    DataCenterMgr.getInstance().getDataCenterBasedOnSpreadName(memberLeft.toString()).setAlive(false);
    				}
    			}else if(memberInfo.isCausedByDisconnect()){
    				SpreadGroup memberDisconnected= memberInfo.getDisconnected();
    				if(memberDisconnected.toString().contains("DC")) { 
    				    DataCenterMgr.getInstance().getDataCenterBasedOnSpreadName(memberDisconnected.toString()).setAlive(false);
    				}
    			}
    		}
		} catch(Throwable t) {
		    t.printStackTrace();
		}
	}

	@Override
	public void regularMessageReceived(SpreadMessage regularMsg) {
		try {
		    System.out.println("Web Server: Received TokenResponseMsg - "+regularMsg.toString());
		    TokenResponseMsg tokenResponseMsg = (TokenResponseMsg) SerializationUtils.deserialize(regularMsg.getData());
	        DataCenter dataCenter = DataCenterMgr.getInstance().getDataCenterBasedOnSpreadName(regularMsg.getSender().toString());
	        dataCenter.setMaxToken(tokenResponseMsg.getMaxCount());
	        dataCenter.setAlive(true);
		} catch (Throwable t) {
		    t.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
        TokenMgr.getInstance().init();
    }
}
