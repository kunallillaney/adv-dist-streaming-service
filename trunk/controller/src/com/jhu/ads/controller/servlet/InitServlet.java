package com.jhu.ads.controller.servlet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import com.jhu.ads.controller.DataCenterMgr;
import com.jhu.ads.controller.TokenMgr;
import com.jhu.ads.controller.common.ConfigMgr;

@WebServlet( name="InitServlet", displayName="Initialization Servlet", urlPatterns = {"/init"}, loadOnStartup=1)
public class InitServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        
        String dHome = System.getenv("DHOME");
        if(dHome == null || dHome.equals("")) {
            throw new ServletException("DHOME environment variable NOT set.");
        }
        
        ConfigMgr.getInstance().init(dHome + "/conf/datacenter-config.properties");
        
        InputStream wowzaServerConfiguration;
        try {
            wowzaServerConfiguration = new FileInputStream(dHome + "/conf/wowzaservers-config.xml");
            DataCenterMgr.getInstance().init(wowzaServerConfiguration);
            wowzaServerConfiguration.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        TokenMgr.getInstance().init();
    }
    
}
