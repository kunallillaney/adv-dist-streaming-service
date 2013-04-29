package com.jhu.ads.webserver.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jhu.ads.webserver.DataCenter;
import com.jhu.ads.webserver.DataCenterMgr;
import com.jhu.ads.webserver.GeoIPMgr;
import com.jhu.ads.webserver.UserInfo;

@WebServlet("/Request")
public class UserRequestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("Remote Addr: " + req.getRemoteAddr());
        UserInfo userInfo = GeoIPMgr.getInstance().getUserInfo(req.getRemoteAddr());
        System.out.println("UserInfo: " + userInfo);
        
        DataCenter dataCenter = null;
        try {
            dataCenter = DataCenterMgr.getInstance().getDataCenter(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
        
        String token = DataCenterMgr.getInstance().getNewToken(dataCenter);
        
        System.out.println("Data Center IP: "+dataCenter.getControllerIP());
        System.out.println("Token: "+token);
        
        resp.setContentType("text/html");
        PrintWriter printWriter  = resp.getWriter();
        printWriter.println("<h2> Client Addr: " + req.getRemoteAddr() + "</h2>");
        printWriter.println("<h2> Latitude: " + userInfo.getLatitude() + "</h2>");
        printWriter.println("<h2> Longitude: " + userInfo.getLongitude() + "</h2>");
        printWriter.println("<h2> Zipcode: " + userInfo.getPostalCode() + "</h2>");
        printWriter.println("<h2> City: " + userInfo.getCityName() + "</h2>");
        printWriter.println("<h2> Country: " + userInfo.getCountryName() + "</h2>");
        printWriter.println("<h2> ControllerIP: " + dataCenter.getControllerIP() + "</h2>");
        printWriter.println("<h2> Token: " + token + "</h2>");
        
        // resp.setStatus(302);
        // resp.setHeader("Location", "www.google.com");
        
        printWriter.println("<br/><br/><h2> Click the following link to view the video: </h2>");
        String link = "http://"+dataCenter.getControllerIP()+":"+dataCenter.getControllerPort()+"/"+"ADSDataCenterController"+"/Request?Token="+token;
        printWriter.println("<a href='"+link+"'>"+link+"</a>");
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
    
}
