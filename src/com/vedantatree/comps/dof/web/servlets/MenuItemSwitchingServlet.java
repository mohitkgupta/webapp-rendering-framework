package com.vedantatree.comps.dof.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.comps.securitymanager.model.MenuItem;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;

import com.vedantatree.comps.dof.DOFUtils;
import com.vedantatree.comps.dof.ViewMetaData;
import com.vedantatree.comps.dof.web.WebAppConstants;


/**
 * Servlet which handle the request to switch the menu item. It receive the request whenever user choose any new menu
 * item on UI.
 * 
 * <p>
 * Role of this servlet is to get the right menu item, build the URL based on this menu item and forward the request to
 * this URL.
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */

public class MenuItemSwitchingServlet extends HttpServlet implements WebAppConstants
{

	private static final long	serialVersionUID	= -6176380455485347575L;
	private static Log			LOGGER				= LogFactory.getLog( MenuItemSwitchingServlet.class );
	private static final String	ID					= "item-id";

	@Override
	protected final void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
	{
		LOGGER.debug( "MenuItemSwitchingServlet: menu-item-id[" + req.getParameter( ID ) + "]" );

		res.setContentType( "text/html" );

		ViewMetaData viewMetaData = (ViewMetaData) req.getSession().getAttribute( VIEW_METADATA );
		MenuItem currentMenuItem = viewMetaData.getCurrentMenuItem();

		LOGGER.debug( "current-menu-item[" + currentMenuItem + "]" );

		if( currentMenuItem == null )
		{
			SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"Menu Item found. It should have been set by now by security filters. It might be some development bug. "
							+ "Please contact development team." );
			LOGGER.error( se );
			DOFUtils.setExceptionPageParameters( req, se );
			throw se;
		}

		LOGGER.debug( "calling pre-menu-switching hook for module developers >>>>>" );

		preMenuItemSwitching( req, res, currentMenuItem );

		LOGGER.debug( "<<<<< pre-menu-switching hook call finished" );

		req.getSession().setAttribute( CALL_FROM_MENUITEM_SWITCHING, "yes" );
		req.setAttribute( "internalrequest", Boolean.TRUE );
		if( currentMenuItem.getActionType() == WebAppConstants.LEFT_NAV_ACTION )
		{
			req.getSession().setAttribute( CURRENT_LEFT_MENUITEM, currentMenuItem );
		}

		String urlToForward = getURLToForward( currentMenuItem );
		LOGGER.debug( "MenuItemSwitchingServlet forwarding request to >>>>>>> " + urlToForward );

		req.getRequestDispatcher( urlToForward ).forward( req, res );
	}

	@Override
	protected final void doPost( HttpServletRequest arg0, HttpServletResponse arg1 )
			throws ServletException, IOException
	{
		doGet( arg0, arg1 );
	}

	/**
	 * This method build the URL where the request should be forwarded next using the current menu item.
	 * 
	 * @param currentMenuItem menu item selected currently
	 * @return URL of the menu where the request should be forwarded
	 */
	private String getURLToForward( MenuItem currentMenuItem )
	{
		String urlToForward = currentMenuItem.getUrl();
		LOGGER.debug( "urlToForward [" + urlToForward + "]" );
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
	 * A hook, which different module developers can override before forwarding the request to a menu item.
	 * 
	 * <p>
	 * Use case could be like in one application, developers found the need to clean the data stored in session on menu
	 * item switching.
	 * 
	 * @param req HTTP Request
	 * @param res HTTP Response
	 * @param currrentMenuItem Currently selected Menu Item
	 */
	protected void preMenuItemSwitching( HttpServletRequest req, HttpServletResponse res, MenuItem currentMenuItem )
			throws ServletException
	{
	}

}

// -------------------------------------------------------------------------------------------------------------------------------

// private MenuItem searchInSubMenuItems( MenuItem menuItem, Long menuItemId )
// {
// LOGGER.debug( "searchingMenuItemInHierarchy[" + menuItem + "] and idToSearch[" + menuItemId + "]" );
//
// Collection<MenuItem> subMenuItems = menuItem.getSubMenuItems();
// MenuItem itrMenuItem = null;
// if( subMenuItems != null && subMenuItems.size() > 0 )
// {
// Iterator<MenuItem> subMItemIter = subMenuItems.iterator();
// while( subMItemIter.hasNext() )
// {
// itrMenuItem = (MenuItem) subMItemIter.next();
// LOGGER.debug( "itrMenuItem[" + itrMenuItem + "]" );
//
// if( menuItemId == itrMenuItem.getId() )
// {
// return itrMenuItem;
// }
// else if( itrMenuItem.getSubMenuItems() != null && itrMenuItem.getSubMenuItems().size() > 0 )
// {
// return searchInSubMenuItems( itrMenuItem, menuItemId );
// }
// }
// }
// return null;
// }
//
