package com.jhu.ads.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

public class DataCenterMgr implements Runnable,Constants {
	
	// HashMap contains the information for all the Wowza Media Servers
	private HashMap<String, WowzaServer> wowzaServerMap = new HashMap<String, WowzaServer>();
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// Thread polls all the Wowza Servers and updates the Data Center
		// TODO
	}

	public void init(InputStream wowzaServerConfiguration) {
		// TODO
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		
		try {
			db = dbf.newDocumentBuilder();
			Document dom = db.parse(wowzaServerConfiguration);
			NodeList wowzaServerList = dom.getElementsByTagName(WOWZA_SERVER_LIST_TAG);
			Element wowServerList = (Element) wowzaServerList.item(0);
			
			NodeList wowzaServers = wowServerList.getElementsByTagName(WOWZA_SERVER_TAG);
			for(int count = 0; count < wowzaServers.getLength(); count++) {
			    Element wowzaServer = (Element) wowzaServers.item(count);
			    WowzaServer wowzaServerObject = new WowzaServer();
			    wowzaServerObject.setWowzaId(getTextValue(wowzaServer, WOWZA_SERVER_ID_TAG));
			    wowzaServerObject.setWowzaIp(getTextValue(wowzaServer, WOWZA_SERVER_IP_TAG));
                wowzaServerObject.setWowzaCapacity(new AtomicInteger(Integer
                        .parseInt(getTextValue(wowzaServer,WOWZA_SERVER_INITIAL_CAPACITY_TAG))));
                this.wowzaServerMap.put(wowzaServerObject.getWowzaId(), wowzaServerObject);
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
        if(nl != null && nl.getLength() > 0) {
            Element el = (Element)nl.item(0);
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
		// TODO
		return 0;
	}

	public WowzaServer assignWowzaServer() {
		// TODO
		return null;
	}

	public WowzaServer getWowzaInfo(String wowzaId) {
		// TODO
		return null;
	}
	
	public static void main(String[] args) {
	    DataCenterMgr d = new DataCenterMgr();
	    InputStream wowzaServerConfiguration;
        try {
            wowzaServerConfiguration = new FileInputStream("C:\\JHU\\Sem4\\AdvDistributed\\proj\\controller\\conf\\wowzaservers-config.xml");
            d.init(wowzaServerConfiguration );
            System.out.println(d.wowzaServerMap);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}