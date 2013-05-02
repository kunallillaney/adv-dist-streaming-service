package com.jhu.ads.webserver.servlet;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.SerializationUtils;
import org.bouncycastle.util.encoders.Base64;

import com.jhu.ads.common.UserInfo;
import com.jhu.ads.webserver.DataCenter;
import com.jhu.ads.webserver.DataCenterMgr;
import com.jhu.ads.webserver.GeoIPMgr;


@WebServlet("/")
public class StreamingServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("Remote Addr: " + req.getRemoteAddr());
        UserInfo userInfo = GeoIPMgr.getInstance().getUserInfo(req.getRemoteAddr());
        System.out.println("UserInfo: " + userInfo);
        
        DataCenter dataCenter = null;
        String token = null;
        synchronized(StreamingServlet.class) {
            try {
                dataCenter = DataCenterMgr.getInstance().getDataCenter(userInfo);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ServletException(e);
            }
            
            token = DataCenterMgr.getInstance().getNewToken(dataCenter);
        }
        
        String urlEncodedToken = URLEncoder.encode(token, "ISO-8859-1");
        
        System.out.println("Data Center IP: "+dataCenter.getControllerIP());
        System.out.println("Token: "+token);
        
        resp.setContentType("text/html");
        
        byte[] userInfoBytes = SerializationUtils.serialize(userInfo);
        
        byte[] userInfoB64EncodedBytes = Base64.encode(userInfoBytes);
        String userInfoStr = new String(userInfoB64EncodedBytes);
        String urlEncodeduserInfo = URLEncoder.encode(userInfoStr, "ISO-8859-1");
        
        String link = "http://"+dataCenter.getControllerIP()+":"+dataCenter.getControllerPort()+"/"+"Controller"+"/Request"
        		                + "?Token=" + urlEncodedToken
        		                + "&UserInfo=" + urlEncodeduserInfo;
        System.out.println("Redirecting user to: " + link);
        resp.sendRedirect(link);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
    
}
