package com.jhu.ads.controller.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jhu.ads.controller.DataCenterMgr;
import com.jhu.ads.controller.TokenMgr;
import com.jhu.ads.controller.WowzaServer;

/**
 * Servlet implementation class UserRequestServlet
 */
@WebServlet("/Request")
public class UserRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public UserRequestServlet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	    String tokenStr = request.getParameter("Token");
	    try {
            TokenMgr.getInstance().handleToken(tokenStr);
        } catch (Exception e) {
            throw new ServletException(e);
        }
	    
	    WowzaServer wowzaServer = DataCenterMgr.getInstance().assignWowzaServer();
	    if(wowzaServer == null) {
	        throw new ServletException("No free Wowza Server Instance found. Please try again later.");
	    }
	    String wowzaIp = wowzaServer.getWowzaIp();
	    int wowzaStreamingPort = wowzaServer.getStreamingPort();
	    
        response.setContentType("text/html");
        PrintWriter printWriter  = response.getWriter();

        printWriter.println("<embed width='768' height='456' " +
                		    "src='http://www.focusonthefamily.com/family/JWPlayer/mediaplayer.swf'        " +
                		    "flashvars='file=sample.mp4&streamer=rtmp://"+wowzaIp+":"+wowzaStreamingPort+"/vod/'" +
                		    "/>");
        printWriter.println("<h2>Video is being served from "+wowzaIp+":"+wowzaStreamingPort+"</h2>");
	    
	}
}
