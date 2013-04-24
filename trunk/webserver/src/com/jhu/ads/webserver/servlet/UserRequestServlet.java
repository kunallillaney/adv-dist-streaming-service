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

@WebServlet("/Request")
public class UserRequestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("Remote Addr: " + req.getRemoteAddr());
        int zipCode = GeoIPMgr.getInstance().getZipCode(req.getRemoteAddr());
        System.out.println("Zip Code: " + zipCode);
        
        DataCenter dataCenter = DataCenterMgr.getInstance().getDataCenter(zipCode);
        int tokenNum = dataCenter.getAndIncrementCurrentToken();
        String token = dataCenter.buildToken(tokenNum);
        
        System.out.println("Data Center IP: "+dataCenter.getControllerIP());
        System.out.println("Token: "+token);
        
        resp.setContentType("text/html");
        PrintWriter printWriter  = resp.getWriter();
        printWriter.println("<h1>" + dataCenter.getControllerIP() + "</h1>");
        printWriter.println("<h1>" + token + "</h1>");
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
    
}
