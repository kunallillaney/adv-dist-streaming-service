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
	private AtomicInteger remainingTokenCount = new AtomicInteger(); // modify this inside sync
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
	        System.out.println("DataCenter Joined " + globalGroup + ".");
			// TODO: Do the local datacenter spread group
		} catch (SpreadException e) {
			e.printStackTrace();
		}

        SpreadGroup localGroup = new SpreadGroup();
        try {
            localGroup.join(connection, ConfigMgr.getInstance().getDataCenterName());
            System.out.println("DataCenter Joined " + ConfigMgr.getInstance().getDataCenterName() + ".");
        } catch (SpreadException e) {
            e.printStackTrace();
        }
		
        connection.add(TokenMgr.getInstance());
        
        // Set the total token count to the total wowza server capacity
		remainingTokenCount.set(DataCenterMgr.getInstance()
				.determineCurrentCapacity());
		recoverUnusedTokens = new Thread(TokenMgr.getInstance(), "RecoverUnusedTokensThread");
		recoverUnusedTokens.start();
	}

	/*
	 * Identify the wowza media server and webserver membership change and set
	 * the flag accordingly
	 */
	@Override
	public void membershipMessageReceived(SpreadMessage membershipMsg) {
		// Currently ignoring all membership messages
        try {
            System.out.println("Data Center: Received membership change msg - "+membershipMsg.toString());
        } catch(Throwable t) {
            t.printStackTrace();
        }
	}

	@Override
	public void regularMessageReceived(SpreadMessage regularMsg) {
		// handle token requests
	    try {
	        System.out.println("Data Center: Received token Request before Deserialzie");
    		TokenRequestMsg tokenRequestMsg = (TokenRequestMsg) SerializationUtils
    				.deserialize(regularMsg.getData());
            System.out.println("Data Center: Received Received token Request from:" + tokenRequestMsg.getWebserverName());
    		handleRequestTokenMessage(tokenRequestMsg.getWebserverName(), regularMsg.getSender().toString()); // send the tokens
	    } catch (Throwable t) {
	        t.printStackTrace();
	    }
	}

	public void handleRequestTokenMessage(String webserverName, String webServerPrivateSpreadGroupName) {
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
		if(currentCount == null) {
		    currentCount = 0;
		}
		newTokenCount = currentCount + currentBatchCount;
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
				webserverTokenInfo.tokenList.put(currentCount + i, new Token(currentCount + i));
				i++;
			}
		} finally {
			webserverTokenInfo.getLock().unlock();
		}

		tokenResponseMsg.setMaxCount(webserveTokenCount.get(webserverName));
		byte[] data = SerializationUtils.serialize(tokenResponseMsg);
		SpreadMessage message = new SpreadMessage();
		message.setData(data);
		message.addGroup(webServerPrivateSpreadGroupName);
		message.setReliable();
		// TODO: Add Type of message
		try {
			connection.multicast(message);
		} catch (SpreadException e) {
			e.printStackTrace();
		}
	}

	public void handleToken(String webserver_tokenID) throws Exception {
		// Check if the token is valid
		String sender = webserver_tokenID.substring(0,
				webserver_tokenID.lastIndexOf("_"));
		String tokenIDStr = webserver_tokenID
				.substring(webserver_tokenID.indexOf("_") + 1);
		int tokenID = Integer.parseInt(tokenIDStr);
		TokenInfo tokenInfo = tokenHolder.get(sender);
		if (tokenInfo == null || tokenID <= tokenInfo.getLastExpiredToken()) {
				throw new Exception("Token " + tokenID + " Expired");
		}

		Token token = tokenInfo.tokenList.get(tokenID);
		token.setTokenId(tokenID);
		token.setRecvdTime(System.currentTimeMillis());
		// exists in the list
		addToList(token, tokenInfo.tokenList);
	}

	private void addToList(Token token, TreeMap<Integer, Token> tokenList) {
		tokenList.get(token.getTokenId()).setRecvdTime(
				System.currentTimeMillis());
	}

	@Override
	public void run() {
		for (;;) {
		    try {
    			int unusedTokenCount = 0;
    			Collection<TokenInfo> values = tokenHolder.values();
    			for (Iterator<TokenInfo> iterator = values.iterator(); iterator.hasNext();) {
    				TokenInfo tokenInfo = (TokenInfo) iterator.next();
    				NavigableSet<Integer> tokenKeys = tokenInfo.tokenList
    						.navigableKeySet();
    
    				Iterator<Integer> iterator2 = tokenKeys.descendingIterator();
    				Token currentToken = null;
    				boolean isFound = false;
    				while (iterator2.hasNext()) {
    				    int currentTokenId = iterator2.next();
    					currentToken = tokenInfo.tokenList.get(currentTokenId);
    					if (currentToken.getRecvdTime() != 0 &&
    					        (System.currentTimeMillis() - currentToken.getRecvdTime()) > ConfigMgr.getInstance().getTokenExpiryTime()) {
    					    isFound = true;
    						break;
    					}
    				}
                    if (isFound == true) {
                        try {
                            int prevLastExpiredToken = tokenInfo.getLastExpiredToken();
                            tokenInfo.setLastExpiredToken(currentToken.getTokenId()); // Set the last expired token to the token that was found
                            tokenInfo.getLock().lock();
                            for(int currentTokenId = currentToken.getTokenId(); currentTokenId > prevLastExpiredToken; currentTokenId--) {
                                // through the rest of the items and count the unused ones.
                                currentToken = tokenInfo.tokenList.get(currentTokenId);
                                if (currentToken.getRecvdTime() == 0) {
                                    unusedTokenCount++;
                                }
                                tokenInfo.tokenList.remove(currentTokenId); // Remove that token element. Do NOT use the navigable key set iterator to remove the element.
                            }
                        } finally {
                            tokenInfo.getLock().unlock();
                        }
                        remainingTokenCount.getAndAdd(unusedTokenCount); // Do this only when atleast one token was expired.
    				}
    			}
    			try {
    				Thread.sleep(ConfigMgr.getInstance().getTokenExpiryPollingInterval());
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
		    } catch(Throwable t) {
		        t.printStackTrace();
		    }
		}
	}

	public AtomicInteger getTokenCount() {
		return remainingTokenCount;
	}

	public static void main(String[] args) {
        TokenMgr.getInstance().init();
        try {
            System.out.println("Sleeping.. ");
            Thread.sleep(100000000);
            System.out.println("Exiting.. ");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}