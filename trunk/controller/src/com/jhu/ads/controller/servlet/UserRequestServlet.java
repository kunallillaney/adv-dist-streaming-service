package com.jhu.ads.controller.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.SerializationUtils;
import org.bouncycastle.util.encoders.Base64;

import com.jhu.ads.common.UserInfo;
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
                		    "flashvars='autostart=true&allowfullscreen=true&file=sample.mp4&streamer=rtmp://"+wowzaIp+":"+wowzaStreamingPort+"/vod/'" +
                		    "/>");
        printWriter.println("<h2>Video is being served from "+wowzaIp+":"+wowzaStreamingPort+"</h2>");
        
        String userInfoStr = request.getParameter("UserInfo");
        if(userInfoStr!=null && !userInfoStr.equals("") ) {
            UserInfo userInfo = (UserInfo) SerializationUtils.deserialize(Base64.decode(userInfoStr.getBytes()));
            printWriter.println("<h3>More details about Client</h3>");
            printWriter.println("<table>");
            printWriter.println("<tr> <td> Client Addr: </td> <td>" + request.getRemoteAddr() + "</td> </tr>");
            printWriter.println("<tr> <td> Latitude: </td> <td>" + userInfo.getLatitude() + "</td> </tr>");
            printWriter.println("<tr> <td> Longitude: </td> <td>" + userInfo.getLongitude() + "</td> </tr>");
            printWriter.println("<tr> <td> Zipcode: </td> <td>" + userInfo.getPostalCode() + "</td> </tr>");
            printWriter.println("<tr> <td> City: </td> <td>" + userInfo.getCityName() + "</td> </tr>");
            printWriter.println("<tr> <td> Country: </td> <td>" + userInfo.getCountryName() + "</td> </tr>");
            printWriter.println("<tr> <td> Token Being Used: </td> <td>" + tokenStr + "</td> </tr>");
            printWriter.println("</table>");
        }
        
	    
	}
}
