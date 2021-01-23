package com.vedantatree.comps.dof.web.servlets;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.StringUtils;

import com.vedantatree.comps.dof.web.WebAppConstants;


/**
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */

public class LocaleServlet extends HttpServlet implements WebAppConstants
{

	private static Log LOGGER = LogFactory.getLog( LocaleServlet.class );

	@Override
	protected void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
	{
		LOGGER.trace( "LocaleServlet: setting locale information" );

		String language = req.getParameter( LANGUAGE );
		LOGGER.debug( "language-to-set[" + language + "]" );

		StringUtils.assertQualifiedArgument( language );

		String url = req.getParameter( URL );
		String moduleName = req.getParameter( MODULE );

		LOGGER.debug( "request-url[" + url + "] moduleName[" + moduleName + "]" );

		String newUrl = StringUtils.isQualifiedString( moduleName ) ? ( url + "?" + MODULE + "=" + moduleName ) : url;
		req.getSession().setAttribute( LOCALE_STRING, new Locale( language ) );

		LOGGER.debug( "forwarding-to-after-setting-language[" + newUrl + "]" );
		res.sendRedirect( newUrl );
	}

}
