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
    	String spreadUser = ConfigMgr.getInstance().getWebServerName();
    	String spreadAddr = ConfigMgr.getInstance().getSpreadDeamonAddress();
    	int spreadPort = ConfigMgr.getInstance().getSpreadDeamonPort();
    	
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
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Joined " + globalGroup + ".");
		
		// Request the initial set of tokens from all the datacenter
		Iterator<DataCenter> datacenters = DataCenterMgr.getInstance().getAllDataCenters();
		while( datacenters.hasNext()){
			requestTokens(datacenters.next());
		}
    }

	@Override
	public void membershipMessageReceived(SpreadMessage membershipMsg) {  // Identify the sender and set the flag accordingly
		if(membershipMsg.getSender().toString().contains("#Data")){
			MembershipInfo memberInfo = membershipMsg.getMembershipInfo();
			if(memberInfo.isCausedByJoin()){
				SpreadGroup memberJoined= memberInfo.getJoined();
				DataCenterMgr.getInstance().getDataCenterBasedOnSpreadName(memberJoined.toString()).setAlive(true);
			}else if(memberInfo.isCausedByLeave()){
				SpreadGroup memberLeft= memberInfo.getLeft();
				DataCenterMgr.getInstance().getDataCenterBasedOnSpreadName(memberLeft.toString()).setAlive(false);			
			}else if(memberInfo.isCausedByDisconnect()){
				SpreadGroup memberDisconnected= memberInfo.getDisconnected();
				DataCenterMgr.getInstance().getDataCenterBasedOnSpreadName(memberDisconnected.toString()).setAlive(false);			
			}
		}
	}

	@Override
	public void regularMessageReceived(SpreadMessage regularMsg) {
		TokenResponseMsg tokenResponseMsg = (TokenResponseMsg) SerializationUtils.deserialize(regularMsg.getData());
		DataCenterMgr.getInstance().getDataCenterBasedOnSpreadName(regularMsg.getSender().toString())
			.setMaxToken(tokenResponseMsg.getMaxCount());
	}
}
