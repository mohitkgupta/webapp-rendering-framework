package com.vedantatree.comps.dof.web.tag;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.comps.securitymanager.model.MenuItem;
import org.vedantatree.comps.securitymanager.model.User;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;

import com.vedantatree.comps.dof.DOFUtils;
import com.vedantatree.comps.dof.ViewMetaData;
import com.vedantatree.comps.dof.web.WebAppConstants;


/**
 * Tag to generate the left navigation for view
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */

public class LeftNavigationTag extends BodyTagSupport implements WebAppConstants
{

	private static final long	serialVersionUID	= 1L;

	private static Log			LOGGER				= LogFactory.getLog( LeftNavigationTag.class );
	public ResourceBundle		bundle;
	public ViewMetaData			viewMetaData;

	@Override
	public int doStartTag() throws JspException
	{

		LOGGER.trace( "LeftNavigationTag" );

		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		HttpSession session = request.getSession();

		User currentUser = (User) session.getAttribute( USER );
		LOGGER.debug( "user-from-session[ " + currentUser + " ]" );

		// control can come here for home and login page, when user is not logged in
		if( currentUser == null )
		{
			LOGGER.debug( "User is not logged in, hence we shall not render the left navigation" );
			return SKIP_BODY;
		}

		viewMetaData = (ViewMetaData) session.getAttribute( VIEW_METADATA );
		if( viewMetaData == null )
		{
			SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"Viewmetadata found null. It seems like a development bug. ViewMetadata is set at the time of login. session["
							+ session + "]" );
			DOFUtils.setExceptionPageParameters( request, se, null, null );
			LOGGER.error( se );
			throw se;
		}

		Collection<MenuItem> leftNavigationTopMenuItems = viewMetaData.getLeftNavigation();
		LOGGER.debug( "left-navigation-top-menu-items[" + leftNavigationTopMenuItems + "]" );

		if( leftNavigationTopMenuItems != null && leftNavigationTopMenuItems.size() > 0 )
		{
			try
			{
				pageContext.getOut().print( "<div class=\"leftnav\">\n<ul>" );
				Iterator<MenuItem> menuItemIter = leftNavigationTopMenuItems.iterator();

				while( menuItemIter.hasNext() )
				{
					MenuItem topLevelLeftNavItem = menuItemIter.next();
					LOGGER.debug( "top-level-left-navigation-item[" + topLevelLeftNavItem + "]" );

					if( LOGGER.isDebugEnabled()
							&& !( WebAppConstants.LEFT_NAV_ACTION == topLevelLeftNavItem.getActionType() ) )
					{
						SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
								"Menu Items should be of Left Navigation type here, but found a different type. menuActionType["
										+ topLevelLeftNavItem.getActionType()
										+ "]. It seems like a development bug. Menu Items are filtered at the time of building the user context." );
						DOFUtils.setExceptionPageParameters( request, se, null, null );
						LOGGER.error( se );
						throw se;
					}

					Collection<MenuItem> subMenuItems = viewMetaData.getValidSubMenuItems( topLevelLeftNavItem );

					// if sub menu items exist, generate navigation for these
					if( subMenuItems != null && subMenuItems.size() > 0 )
					{
						generateSubMenu( topLevelLeftNavItem, subMenuItems );
					}
					// otherwise create the navigation for current item
					else
					{
						pageContext.getOut().print( "<li>" );

						pageContext.getOut()
								.print( "<a  id='" + topLevelLeftNavItem.getId() + "' href=\""
										+ topLevelLeftNavItem.getWebPageURL() + "\">"
										+ topLevelLeftNavItem.getDisplayName() + "</a>" );

						pageContext.getOut().print( "</li>" );
					}
				}
				pageContext.getOut().print( "\n</ul>\n</div>" );
			}
			catch( IOException e )
			{
				DOFUtils.setExceptionPageParameters( request, e, null, null );
				LOGGER.error( e );
				throw new JspTagException( "LeftNavigationTag: " + e.getMessage() );
			}

		}
		else
		{
			JspTagException jte = new JspTagException(
					"LeftNavigationTag: No menu items found for current logged in user. Please check in supervision database." );
			DOFUtils.setExceptionPageParameters( request, jte, null, null );
			LOGGER.error( jte );
			throw jte;
		}

		return SKIP_BODY;
	}

	public void generateSubMenu( MenuItem menuItem, Collection<MenuItem> subMenuItems ) throws JspTagException
	{
		Long id = menuItem.getId();
		try
		{
			pageContext.getOut().print( "<li>" );

			pageContext.getOut().print( "<a href=\"javascript:void(0);\" onclick=\"showHide('" + id
					+ "')\"	style=\"cursor:pointer;\">" + menuItem.getDisplayName() + "</a>" );
			pageContext.getOut().print( " <div id=\"" + id + "\" style=\"display:none;\" class=\"leftnav_tree\">" );
			pageContext.getOut().print( "<ul>" );

			if( subMenuItems != null && subMenuItems.size() > 0 )
			{
				Iterator<MenuItem> iterSubMenuItem = subMenuItems.iterator();

				// iterate over sub menu items
				while( iterSubMenuItem.hasNext() )
				{
					MenuItem subMenuItem = iterSubMenuItem.next();
					if( WebAppConstants.LEFT_NAV_ACTION == subMenuItem.getActionType() )
					{
						// get valid sub menu items
						Collection<MenuItem> subHierarchyMenuItems = viewMetaData.getValidSubMenuItems( subMenuItem );

						if( subHierarchyMenuItems != null && subHierarchyMenuItems.size() > 0 )
						{
							generateSubMenu( subMenuItem, subHierarchyMenuItems );
						}
						else
						{
							pageContext.getOut()
									.print( " <li><a  id='" + subMenuItem.getId() + "' href=\""
											+ subMenuItem.getWebPageURL() + "\">" + subMenuItem.getDisplayName()
											+ "</a></li>" );
						}
					}
				}
			}
			pageContext.getOut().print( "\n</ul>\n</div>\n</li>" );

		}
		catch( IOException e )
		{
			LOGGER.error( e );
			throw new JspTagException( "LeftNavigationTag: " + e.getMessage() );
		}
		// ++id;

	}

	@Override
	public int doEndTag() throws JspException
	{

		return EVAL_PAGE;
	}

	public ResourceBundle getBundle()
	{
		return bundle;
	}

	public void setBundle( ResourceBundle bundle )
	{
		this.bundle = bundle;
	}

	@Override
	public void release()
	{
		viewMetaData = null;
		super.release();
	}
}
