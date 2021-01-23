package com.vedantatree.comps.dof.web.tag;

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
import org.vedantatree.comps.securitymanager.model.Menu;
import org.vedantatree.comps.securitymanager.model.User;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;

import com.vedantatree.comps.dof.DOFUtils;
import com.vedantatree.comps.dof.ViewMetaData;
import com.vedantatree.comps.dof.web.WebAppConstants;
import com.vedantatree.comps.dof.web.WebUIUtils;


/**
 * Tag to generate the header
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */

public class HeaderTag extends BodyTagSupport implements WebAppConstants
{

	private static final long	serialVersionUID	= 201102171030L;

	private static Log			LOGGER				= LogFactory.getLog( HeaderTag.class );
	public ResourceBundle		bundle;

	@Override
	public int doStartTag() throws JspException
	{
		LOGGER.trace( "HeaderTag" );

		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		HttpSession session = request.getSession();
		LOGGER.debug( "session-header[" + session + "]" );

		User currentUser = (User) session.getAttribute( USER );
		LOGGER.debug( "user-from-session[ " + currentUser + " ]" );

		// control can come here for home and login page, when user is not logged in
		if( currentUser == null )
		{
			LOGGER.debug( "User is not logged in, hence we shall not render the left navigation" );
			return SKIP_BODY;
		}

		ViewMetaData viewMetaData = (ViewMetaData) session.getAttribute( VIEW_METADATA );
		if( viewMetaData == null )
		{
			SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"Viewmetadata found null. It seems like a development bug. ViewMetadata is set at the time of login" );
			DOFUtils.setExceptionPageParameters( request, se, null, null );
			LOGGER.error( se );
			throw se;
		}

		Collection<Menu> menus = viewMetaData.getTopNavigation();
		LOGGER.debug( "top-navigation-size[" + menus.size() + "]" );

		if( menus != null && menus.size() > 0 )
		{
			try
			{
				pageContext.getOut().print(
						"<div id=\"navigation\">\n<table border=\"0\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">\n<tr>\n<td>\n<ul>" );
				Iterator menuIter = menus.iterator();
				while( menuIter.hasNext() )
				{
					Menu headerMenu = (Menu) menuIter.next();
					LOGGER.debug(
							"adding menus to top Navigation: menu[" + headerMenu.getDisplayName() + "] menu-moduleName["
									+ headerMenu.getModuleName() + "] menu-URL [" + headerMenu.getUrl() + "]" );

					// We are filtering the menus for Global Menus,as these are meant for top right corner only.
					// Global menus comes with every request
					if( !Menu.GLOBAL_MODULE_MENU.equals( headerMenu.getModuleName() ) )
					{
						// Commented for User Acees Right Report
						// pageContext.getOut().print(
						// "<li><a id='" + headerMenu.getIndex() + "' href=\"menu.do?menu-id="
						// + headerMenu.getIndex() + "\">"
						// + bundle.getString( headerMenu.getDisplayName() ) + "</a></li>" );

						// pageContext.getOut().print(
						// "<li><a id='" + headerMenu.getId() + "' href=\"menu.do?menu-id=" + headerMenu.getId()
						// + "\">" + headerMenu.getDisplayName() + "</a></li>" );

						pageContext.getOut().print( "<li><a id='" + headerMenu.getId() + "' href=\""
								+ headerMenu.getEncryptedURL() + "\">" + headerMenu.getDisplayName() + "</a></li>" );
					}
				}
				pageContext.getOut().print(
						"\n</ul>\n</td>\n</tr>\n<tr>\n<td class=\"navitation-bottombar\">&nbsp;</td>\n</tr>\n</table>\n</div>" );

			}
			catch( Exception e )
			{
				LOGGER.error( e );
				WebUIUtils.setExceptionPageParameters( request, e, null, null );
				throw new JspTagException( "HeaderTag: " + e.getMessage() );
			}
		}

		return SKIP_BODY;
	}

	@Override
	public int doEndTag()
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

}
