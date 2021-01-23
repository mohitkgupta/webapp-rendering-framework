package com.vedantatree.comps.dof.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.comps.securitymanager.model.Menu;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;

import com.vedantatree.comps.dof.DOFUtils;
import com.vedantatree.comps.dof.ViewMetaData;
import com.vedantatree.comps.dof.web.WebAppConstants;


/**
 * Servlet which handle the request to switch the menu. It receive the request whenever user choose any new menu on UI.
 * 
 * <p>
 * Role of this servlet is to get the menu, build the URL based on this menu and forward the request to this URL.
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */

public class MenuSwitchingServlet extends HttpServlet implements WebAppConstants
{

	private static final long	serialVersionUID	= -4183177275988279207L;
	private static Log			LOGGER				= LogFactory.getLog( MenuSwitchingServlet.class );
	private static final String	ID					= "menu-id";

	@Override
	protected final void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
	{
		LOGGER.debug( "MenuSwitchingServlet: request-menuId[" + req.getParameter( ID ) + "]" );

		res.setContentType( "text/html" );

		ViewMetaData viewMetaData = (ViewMetaData) req.getSession().getAttribute( VIEW_METADATA );
		Menu currentMenu = viewMetaData.getCurrentMenu();

		if( currentMenu == null )
		{
			SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"Current menu should have been set by now from security chains, but it is null" );
			LOGGER.error( se );
			DOFUtils.setExceptionPageParameters( req, se );
			throw se;
		}

		LOGGER.debug( "internal forward to menu[" + currentMenu + "]" );
		preMenuSwitching( req, res, currentMenu );

		req.getSession().setAttribute( CALL_FROM_MENUITEM_SWITCHING, "no" );

		String urlToForward = getURLToForward( currentMenu );
		LOGGER.debug( "MenuSwitchingServlet forwarding the request to >>>>>>> " + urlToForward );

		req.getRequestDispatcher( urlToForward ).forward( req, res );
	}

	@Override
	protected final void doPost( HttpServletRequest arg0, HttpServletResponse arg1 )
			throws ServletException, IOException
	{
		doGet( arg0, arg1 );
	}

	/**
	 * This method build the URL where the request should be forwarded next using the current menu.
	 * 
	 * @param currentMenu Current menu for which we need to build the URL
	 * @return URL of the menu where the request should be forwarded
	 */
	private String getURLToForward( Menu currentMenu )
	{

		String urlToForward = currentMenu.getUrl();
		/*
		 * Adding / to url if it is not starting with this. Otherwise url starts with current contextual url of the
		 * requesting page. Suppose if
		 * list page is in jsp/ap, so this url will also be loaded in context of that i.e. jsp/ap/menuurl.do
		 * 
		 * However struts servlet mapping is only for /*.do. So we are appending / to url so that it can be absolute
		 * from application root.
		 */
		if( !urlToForward.trim().startsWith( "/" ) )
		{
			urlToForward = "/" + urlToForward.trim();
		}

		return urlToForward;

	}

	/**
	 * A hook, which different module developers can override before forwarding the request to a menu.
	 * 
	 * <p>
	 * Use case could be like in one application, developers found the need to clean the data stored in session on menu
	 * switching.
	 * 
	 * @param req HTTP Request
	 * @param res HTTP Response
	 */
	protected void preMenuSwitching( HttpServletRequest req, HttpServletResponse res, Menu CurrentMenu )
			throws ServletException
	{
	}

}
