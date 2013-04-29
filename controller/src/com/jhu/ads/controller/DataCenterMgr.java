package com.jhu.ads.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DataCenterMgr implements Runnable, Constants {

	// HashMap contains the information for all the Wowza Media Servers
	private HashMap<String, WowzaServer> wowzaServerMap = new HashMap<String, WowzaServer>();
	private ArrayList<String> wowzaList = new ArrayList<String>();

    private volatile static DataCenterMgr _instance;
    
    public static DataCenterMgr getInstance() {
        if(_instance == null) {
            synchronized (DataCenterMgr.class) {
                if(_instance == null) {
                    _instance = new DataCenterMgr();
                }
            }
        }
        return _instance;
    }	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// Thread polls all the Wowza Servers and updates the Data Center
		while (true) {

			int tokenChange = 0;
			for (WowzaServer wowzaObject : wowzaServerMap.values()) {
				// Getting the WowzaCount for each WowzaServer and updating the
				// value
				int currentConnections = getWowzaCount(wowzaObject.getWowzaIp());
				int wowzaRemoteValue = wowzaObject.getMaxCapacity() - currentConnections;
				int wowzaLocalValue = wowzaObject.getCurrentCapacity().get();
				if (wowzaRemoteValue != wowzaLocalValue) {
					// If the remote value is larger than local then increase
					// tokenChange
					tokenChange = tokenChange + (wowzaRemoteValue - wowzaLocalValue);
					wowzaObject.getCurrentCapacity().set(wowzaRemoteValue);
				}// end of outer if

				// set the tokenCount in token Mangaer to tokenChange
				// TODO
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public int getWowzaCount(String ip){
		
		try {
			// Establishing a url connection to the xml file
			URL url = new URL("http://"+ip+":8086/connectioncounts.xml");
			//URL url = new URL("http://localhost:8086/connectioncounts.xml");
			URLConnection urlConnection = url.openConnection();
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			
			// Parsing the XML file
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			
			db = dbf.newDocumentBuilder();
			Document dom = db.parse(in);
			NodeList countList = dom.getElementsByTagName(WOWZA_MEDIA_SERVER_TAG);
			Element element = (Element)countList.item(0);
			return Integer.parseInt(getTextValue(element, WOWZA_SERVER_COUNT_TAG));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
		
	}
	
	public void init(InputStream wowzaServerConfiguration) {
		// TODO
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			Document dom = db.parse(wowzaServerConfiguration);
			NodeList wowzaServerList = dom
					.getElementsByTagName(WOWZA_SERVER_LIST_TAG);
			Element wowServerList = (Element) wowzaServerList.item(0);

			NodeList wowzaServers = wowServerList
					.getElementsByTagName(WOWZA_SERVER_TAG);
			for (int count = 0; count < wowzaServers.getLength(); count++) {
				Element wowzaServer = (Element) wowzaServers.item(count);
				WowzaServer wowzaServerObject = new WowzaServer();
				wowzaServerObject.setWowzaId(getTextValue(wowzaServer,
						WOWZA_SERVER_ID_TAG));
				wowzaServerObject.setWowzaIp(getTextValue(wowzaServer,
						WOWZA_SERVER_IP_TAG));
				wowzaServerObject.setMaxCapacity(Integer
						.parseInt(getTextValue(wowzaServer,
								WOWZA_SERVER_MAX_CAPACITY_TAG)));
				wowzaServerObject.getCurrentCapacity().set(wowzaServerObject.getMaxCapacity()); // TODO: For now. Actually should poll all wowza instances and then initialize this count.
				this.wowzaServerMap.put(wowzaServerObject.getWowzaId(), wowzaServerObject);
				wowzaList.add(wowzaServerObject.getWowzaId());
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			Node firstChild = el.getFirstChild();
			if (firstChild != null) {
				textVal = firstChild.getNodeValue();
				if (textVal != null) {
					textVal = textVal.trim();
				}
			}
		}
		return textVal;
	}

	public int determineCurrentCapacity() {
		int totalCapacity = 0;
		// Iterating over the WowServer HashMap and incrementing Capacity
		for (WowzaServer iter : wowzaServerMap.values()) {
			totalCapacity = totalCapacity + iter.getCurrentCapacity().get();
		}
		return totalCapacity;
	}

	public WowzaServer assignWowzaServer() {
		// Iterating over the WowzaServer List for the Wowza id
		for (String id : wowzaList) {
			// Uses the wowza id to determine which one has empty capacity and
			// returns that wowza object else returns null
			if (wowzaServerMap.get(id).getCurrentCapacity().get() > 0) {
				wowzaServerMap.get(id).getCurrentCapacity().getAndDecrement();
				return wowzaServerMap.get(id);
			}
		}
		return null;
	}

	public WowzaServer getWowzaInfo(String wowzaId) {
		// Returns the WowzaServer object using the id
		return wowzaServerMap.get(wowzaId);
	}

	public static void main(String[] args) {
		DataCenterMgr d = new DataCenterMgr();
		d.getWowzaCount("127.0.0.1");
		/*
		InputStream wowzaServerConfiguration;
		try {
			
			wowzaServerConfiguration = new FileInputStream(
					"C:\\JHU\\Sem4\\AdvDistributed\\proj\\controller\\conf\\wowzaservers-config.xml");
			d.init(wowzaServerConfiguration);
			System.out.println(d.wowzaServerMap);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
}
