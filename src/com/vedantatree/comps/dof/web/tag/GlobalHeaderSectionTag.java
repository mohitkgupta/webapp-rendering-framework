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
import org.vedantatree.utils.config.ConfigurationManager;

import com.vedantatree.comps.dof.ViewMetaData;
import com.vedantatree.comps.dof.web.WebAppConstants;
import com.vedantatree.comps.dof.web.WebUIUtils;


/**
 * Tag to generate the global section of the header
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 *
 */
public class GlobalHeaderSectionTag extends BodyTagSupport implements WebAppConstants
{

	private static final long	serialVersionUID	= 1L;

	private static Log			LOGGER				= LogFactory.getLog( GlobalHeaderSectionTag.class );
	private static final String	USER				= "user";
	public ResourceBundle		bundle;

	@Override
	public int doStartTag() throws JspException
	{
		LOGGER.trace( "In GlobalHeaderSectionTag" );

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

		ViewMetaData viewMetaData = (ViewMetaData) session.getAttribute( VIEW_METADATA );

		try
		{
			pageContext.getOut().print( "<div id=\"header\">" + "\n<table border=\"0\" width=\"100%\">\n<tr>" );
			pageContext.getOut().print( "<td>" + "<div class=\"banner\">" + "<table align=\"center\">" + "<tr>"
					+ "<td>&nbsp;</td>" + "</tr>" + "</table>" + "</div>" + "</td>" );
			pageContext.getOut().print( "<td align=\"right\" width=\"230\">" );

			pageContext.getOut().print( "<div class=\"welcome\" align=\"right\">" + bundle.getString( "hd.welcome" )
					+ ",<span class=\"rbold\">" + currentUser.getUserId() + "</span>" );

			// render global menus
			renderGlobalMenus( viewMetaData );

			// Render module version number and name
			renderAppInformation();

			// This method can be overridden to display any application specific information
			renderAppSpecificInformation();

			pageContext.getOut().print( "</div>\n</td>" );
			pageContext.getOut().print( "\n</tr>\n</table>\n</div>" );
		}
		catch( Exception e )
		{
			LOGGER.error( e );
			WebUIUtils.setExceptionPageParameters( request, e, null, null );
			throw new JspTagException( "Error in GlobalHeaderSectionTag while generating global header contents. error["
					+ e.getMessage() + "]" );
		}

		return SKIP_BODY;
	}

	protected void renderGlobalMenus( ViewMetaData viewMetaData ) throws Exception
	{
		Collection<Menu> globalMenus = viewMetaData.getGlobalMenus();
		LOGGER.debug( "globalMenus-size[ " + ( globalMenus != null ? globalMenus.size() : "null" ) + " ]" );

		if( globalMenus != null && globalMenus.size() > 0 )
		{
			Iterator globalMenuIter = globalMenus.iterator();

			while( globalMenuIter.hasNext() )
			{
				Menu globalMenu = (Menu) globalMenuIter.next();
				LOGGER.debug( " Display Name [" + globalMenu.getDisplayName() + " ]" );

				pageContext.getOut().print( "|<a id='" + globalMenu.getId() + "' href=\"" + globalMenu.getEncryptedURL()
						+ "\">" + bundle.getString( globalMenu.getDisplayName() ) + "</a>" );
			}
		}

	}

	protected void renderAppInformation() throws Exception
	{
		String versionNumber = ConfigurationManager.getSharedInstance().getPropertyValue( "app.version", false );
		LOGGER.debug( "appVersion[ " + versionNumber + " ]" );

		LOGGER.debug( "printing application version number" );
		pageContext.getOut()
				.print( "<br><font color=\"yellow\"><b>"
						+ ( versionNumber != null ? versionNumber : bundle.getString( "version.not.defined" ) )
						+ "</b></font>" );

		// Code to show the module name at the top
		String module = (String) ( (HttpServletRequest) pageContext.getRequest() ).getSession().getAttribute( MODULE );
		LOGGER.debug( "module-from-session[" + module + "]" );

		String moduleNameDisplayString = bundle.getString( "module" ) + " : "
				+ bundle.getString( "module.label." + module );

		pageContext.getOut().print( "<br><font color=\"yellow\"><b>" + moduleNameDisplayString + "</b></font>" );

	}

	protected void renderAppSpecificInformation() throws Exception
	{
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

}
