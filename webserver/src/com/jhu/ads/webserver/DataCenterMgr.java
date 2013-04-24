package com.jhu.ads.webserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

public class DataCenterMgr implements Constants {
    
    private volatile static DataCenterMgr _instance;
    
    private HashMap<String, DataCenter> dataCenterMap;
    
    private HashMap<Integer, ArrayList<DataCenter>> zipCodeMap;
    
    private DataCenterMgr() {
        dataCenterMap = new HashMap<String, DataCenter>();
        zipCodeMap = new HashMap<Integer, ArrayList<DataCenter>>();
    }
    
    public static DataCenterMgr getInstance() {
        if(_instance == null) {
            synchronized (DataCenter.class) {
                if(_instance == null) {
                    _instance = new DataCenterMgr();
                }
            }
        }
        return _instance;
    }
    
    public DataCenter getDataCenter(int zipCode) {
        ArrayList<DataCenter> dataCenterList = zipCodeMap.get(zipCode);
        for (Iterator iterator = dataCenterList.iterator(); iterator.hasNext();) {
            DataCenter dataCenter = (DataCenter) iterator.next();
            if(dataCenter.isAlive() && !dataCenter.isDataCenterFull()) {
                return dataCenter;
            }
        }
        return null;
    }
    
    public DataCenter getDataCenter(String name) {
        return dataCenterMap.get(name);
    }
    
    
    public void init(InputStream datacentersConfigStream) {
        // Initializes two Hash Maps
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();         // get the factory
        DocumentBuilder db;         // Using factory get an instance of document builder
        try {
            db = dbf.newDocumentBuilder();
            Document dom = db.parse(datacentersConfigStream);         // parse using builder to get DOM representation of the XML file
            
            NodeList mainElems = dom.getElementsByTagName(MAIN_TAG);
            Element mainElement = (Element) mainElems.item(0);
            
            /* Build Data Center List */
            NodeList dataCenterListNode = mainElement.getElementsByTagName(DATA_CENTER_LIST_TAG);
            Element dataCenterListElem = (Element) dataCenterListNode.item(0); 
            
            NodeList dataCenterList = dataCenterListElem.getElementsByTagName(DATA_CENTER_TAG);
            
            for(int index = 0; index < dataCenterList.getLength(); index++) {
                Element dataCenter = (Element)dataCenterList.item(index);
                String name = getTextValue(dataCenter, DATA_CENTER_NAME_TAG);
                String controllerIP = getTextValue(dataCenter, DATA_CENTER_IP_TAG);
                String spreadGroupName = getTextValue(dataCenter, DATA_CENTER_SP_GROUPNAME_TAG);
                DataCenter dataCenterObj = new DataCenter(name, controllerIP , spreadGroupName);
                DataCenterMgr.getInstance().dataCenterMap.put(name, dataCenterObj);
            }
            
            /* Build <Zipcode, DataCenter> Map List */
            NodeList zipCodeMapNode = mainElement.getElementsByTagName(ZIPCODE_DC_MAP_TAG);
            Element zipCodeMapElem = (Element) zipCodeMapNode.item(0);
            
            NodeList keyValuePairList = zipCodeMapElem.getElementsByTagName(KEY_VALUE_PAIR_TAG);
            for(int index = 0; index < keyValuePairList.getLength(); index++) {
                Element keyValuePairItem = (Element) keyValuePairList.item(index);
                
                String dataCenters = getTextValue(keyValuePairItem, DATA_CENTER_ORDER_TAG);
                String[] dataCentersArr = dataCenters.split(",");
                ArrayList<DataCenter> dataCentersList = new ArrayList<DataCenter>();
                for (int i = 0; i < dataCentersArr.length; i++) {
                    dataCentersList.add(DataCenterMgr.getInstance().getDataCenter(dataCentersArr[i].trim()));
                }
                
                String zipCodes = getTextValue(keyValuePairItem, ZIPCODE_TAG);
                String[] zipCodesArr = zipCodes.split(",");
                for (int i = 0; i < zipCodesArr.length; i++) {
                    int zipCode = Integer.parseInt(zipCodesArr[i].trim());
                    DataCenterMgr.getInstance().zipCodeMap.put(zipCode, dataCentersList);
                }
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

    public static String getTextValue(Element ele, String tagName) {
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
    
    public static void main(String[] args) {
        String file = "C:\\JHU\\Sem4\\AdvDistributed\\proj\\webserver\\src\\datacenter-config.xml";
        try {
            FileInputStream is = new FileInputStream(file);
            DataCenterMgr.getInstance().init(is);
            System.out.println(DataCenterMgr.getInstance().dataCenterMap);
            System.out.println(DataCenterMgr.getInstance().zipCodeMap);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
