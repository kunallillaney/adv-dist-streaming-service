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
        
        DataCenter dataCenter = DataCenterMgr.getInstance().getDataCenter(userInfo);
        int tokenNum = dataCenter.getAndIncrementCurrentToken();
        String token = dataCenter.buildToken(tokenNum);
        
        System.out.println("Data Center IP: "+dataCenter.getControllerIP());
        System.out.println("Token: "+token);
        
        resp.setContentType("text/html");
        PrintWriter printWriter  = resp.getWriter();
        printWriter.println("<h2> Client Addr: " + req.getRemoteAddr() + "</h1>");
        printWriter.println("<h2> ClientInfo: " + userInfo + "</h1>");
        printWriter.println("<h2> ControllerIP: " + dataCenter.getControllerIP() + "</h1>");
        printWriter.println("<h2> Token: " + token + "</h1>");
        
        // resp.setStatus(302);
        // resp.setHeader("Location", "www.google.com");
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
    
}
