package com.jhu.ads.webserver.servlet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import com.jhu.ads.webserver.DataCenterMgr;
import com.jhu.ads.webserver.GeoIPMgr;
import com.jhu.ads.webserver.TokenMgr;
import com.jhu.ads.webserver.common.ConfigMgr;

@WebServlet( name="InitServlet", displayName="Initialization Servlet", urlPatterns = {"/init"}, loadOnStartup=1)
public class InitServlet extends HttpServlet {
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        // TODO Auto-generated method stub
        
        String dHome = System.getenv("DHOME");
        if(dHome == null || dHome.equals("")) {
            throw new ServletException("DHOME environment variable NOT set.");
        }
        
        // String path = config.getServletContext().getRealPath("/WEB-INF");
        ConfigMgr.getInstance().init(dHome + "/webserver-config.properties");
        
        InputStream is;
        try {
            is = new FileInputStream(dHome + "/datacenter-config.xml");
            DataCenterMgr.getInstance().init(is);
            is.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        TokenMgr.getInstance().init();
        GeoIPMgr.getInstance().init(ConfigMgr.getInstance().getGeoIPFilePath());
    }
    
}
