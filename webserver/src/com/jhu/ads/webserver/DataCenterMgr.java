package com.jhu.ads.webserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jhu.ads.webserver.common.ConfigMgr;

public class DataCenterMgr implements Constants {

	private volatile static DataCenterMgr _instance;

	private HashMap<String, DataCenter> dataCenterMap;

	private DataCenterMgr() {
		dataCenterMap = new HashMap<String, DataCenter>();
	}

	public static DataCenterMgr getInstance() {
		if (_instance == null) {
			synchronized (DataCenterMgr.class) {
				if (_instance == null) {
					_instance = new DataCenterMgr();
				}
			}
		}
		return _instance;
	}

	public DataCenter getDataCenter(String name) {
		return dataCenterMap.get(name);
	}

	public DataCenter getDataCenter(UserInfo userInfo) {
		Collection<DataCenter> dataCenterValueSet = dataCenterMap.values();
		ArrayList<DataCenter> buildDataCenterList = new ArrayList<DataCenter>();
		ArrayList<DataCenter> rentedDataCenterList = new ArrayList<DataCenter>();

		for (Iterator<DataCenter> iterator = dataCenterValueSet.iterator(); iterator
				.hasNext();) {
			DataCenter dataCenter = iterator.next();
			if (dataCenter.isAlive() && !dataCenter.isDataCenterFull()) {
				if (dataCenter.isBuilt()) {
					buildDataCenterList.add(dataCenter);
				} else {
					rentedDataCenterList.add(dataCenter);
				}
			}
		}

		if (!buildDataCenterList.isEmpty()) {
			return getPreferredDatacenter(buildDataCenterList, userInfo);
		} else {
			return getPreferredDatacenter(rentedDataCenterList, userInfo);
		}
	}
	
	public String getNewToken(DataCenter dataCenter) {
        int tokenNum = dataCenter.getAndIncrementCurrentToken();
        if ( (dataCenter.getMaxToken() - tokenNum) 
                == ConfigMgr.getInstance().getRequestTokensWhenNumberOfTokensRemaining()) {
            TokenMgr.getInstance().requestTokens(dataCenter);
        }
        String token = dataCenter.buildToken(tokenNum);
        return token;
	}

	private DataCenter getPreferredDatacenter(
			ArrayList<DataCenter> dataCenterList, UserInfo userInfo) {
		DataCenter preferredDataCenter = null;
		double value = Double.MAX_VALUE;
		double distance = 0;
		for (int j = 0; j < dataCenterList.size(); j++) {
			distance = Math.abs(dataCenterList.get(j).getLongitude()
					- userInfo.getLongitude());
			if (distance < value) {
				value = distance;
				preferredDataCenter = dataCenterList.get(j);
			}
		}
		return preferredDataCenter;
	}

	public DataCenter getDataCenterBasedOnSpreadName(String spreadName) {
		DataCenter retDataCenter = null;
		Collection<DataCenter> dataCenterValueSet = dataCenterMap.values();
		for (Iterator<DataCenter> iterator = dataCenterValueSet.iterator(); iterator
				.hasNext();) {
			DataCenter dataCenter = iterator.next();
			if (dataCenter.getSpreadGroupName().equals(spreadName)) {
				retDataCenter = dataCenter;
				break;
			}
		}
		return retDataCenter;
	}

	public Iterator<DataCenter> getAllDataCenters() {
		return dataCenterMap.values().iterator();
	}

	public void init(InputStream datacentersConfigStream) {
		// Initializes two Hash Maps
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // get
																			// the
																			// factory
		DocumentBuilder db; // Using factory get an instance of document builder
		try {
			db = dbf.newDocumentBuilder();
			Document dom = db.parse(datacentersConfigStream); // parse using
																// builder to
																// get DOM
																// representation
																// of the XML
																// file

			NodeList mainElems = dom.getElementsByTagName(MAIN_TAG);
			Element mainElement = (Element) mainElems.item(0);

			/* Build Data Center List */
			NodeList dataCenterListNode = mainElement
					.getElementsByTagName(DATA_CENTER_LIST_TAG);
			Element dataCenterListElem = (Element) dataCenterListNode.item(0);

			NodeList dataCenterList = dataCenterListElem
					.getElementsByTagName(DATA_CENTER_TAG);

			for (int index = 0; index < dataCenterList.getLength(); index++) {
				Element dataCenter = (Element) dataCenterList.item(index);
				String name = getTextValue(dataCenter, DATA_CENTER_NAME_TAG);
				String controllerIP = getTextValue(dataCenter,
						DATA_CENTER_IP_TAG);
				String spreadGroupName = getTextValue(dataCenter,
						DATA_CENTER_SP_GROUPNAME_TAG);
				String longitude = getTextValue(dataCenter,
						DATA_CENTER_LONGITUDE_TAG);
				String latitude = getTextValue(dataCenter,
						DATA_CENTER_LATITUDE_TAG);
				String isBuilt = getTextValue(dataCenter,
						DATA_CENTER_IS_BUILT_TAG);

				DataCenter dataCenterObj = new DataCenter(name, controllerIP,
						spreadGroupName, Double.parseDouble(longitude),
						Double.parseDouble(latitude),
						Boolean.getBoolean(isBuilt));
				DataCenterMgr.getInstance().dataCenterMap.put(name,
						dataCenterObj);
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

	public static void main(String[] args) {
		String file = "C:\\JHU\\Sem4\\AdvDistributed\\proj\\webserver\\src\\datacenter-config.xml";
		try {
			FileInputStream is = new FileInputStream(file);
			DataCenterMgr.getInstance().init(is);
			System.out.println(DataCenterMgr.getInstance().dataCenterMap);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
