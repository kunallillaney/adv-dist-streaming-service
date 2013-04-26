package com.jhu.ads.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
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
			NodeList mainElms = dom.getElementsByTagName(MAIN_TAG);
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
}
