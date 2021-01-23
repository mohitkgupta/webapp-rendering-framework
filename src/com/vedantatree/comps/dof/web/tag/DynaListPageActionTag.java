package com.vedantatree.comps.dof.web.tag;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.ViewHelper;
import com.vedantatree.comps.dof.web.WebAppConstants;
import com.vedantatree.comps.dof.web.WebUIUtils;
import com.vedantatree.comps.dof.web.vdo.PageAction;


/**
 * This tag generates the page actions for list page.
 * 
 * TODO we can merge ButtonTag and DynaListPageActionTag
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public class DynaListPageActionTag extends BodyTagSupport implements WebAppConstants
{

	private static Log LOGGER = LogFactory.getLog( DynaListPageActionTag.class );

	public int doEndTag()
	{
		return EVAL_PAGE;
	}

	public int doStartTag() throws JspException
	{

		LOGGER.trace( "in button tag" );

		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

		Locale locale = request.getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle( "ApplicationResources", locale );

		DOMetaData doMetaData = (DOMetaData) request.getAttribute( DOMetaData.DOMEATADATA_KEY );
		LOGGER.trace( "doMetadata[" + doMetaData + "]" );

		String viewHelperKey = doMetaData.getViewHelperKey();
		ViewHelper viewHelper = (ViewHelper) request.getSession().getAttribute( viewHelperKey );
		LOGGER.debug( "viewHelper[" + viewHelper + "]" );

		try
		{
			List formActions = viewHelper.getFormActions();
			if( formActions != null )
			{
				for( int j = 0; j < formActions.size(); j++ )
				{
					LOGGER.debug( "formAction[" + formActions.get( j ) + "]" );
					PageAction formAction = (PageAction) formActions.get( j );
					writePageAction( formAction, bundle, viewHelper );
				}
			}
		}
		catch( Exception e )
		{
			LOGGER.error( "Exception in DynaListPageAction Tag implementation ", e );
			JspTagException jspEx = new JspTagException(
					"Exception in DynaListPageAction Tag implementation " + e.getMessage() );
			jspEx.initCause( e );
			throw jspEx;
		}

		return SKIP_BODY;
	}

	private void writePageAction( PageAction listPageAction, ResourceBundle bundle, ViewHelper viewHelper )
			throws IOException
	{
		LOGGER.debug( "writting-action[" + listPageAction + "]" );

		// div is not required here, as div is putting the buttons in new line

		// pageContext.getOut().print( "<div class=\"button_inner_border\">" );
		// pageContext.getOut().print( "<div>" );

		String masterTable = (String) pageContext.getRequest().getAttribute( "masterTable" );

		/*
		 * Here we are getting the web page URL for give action URL. Web Page URL means framwork may decide
		 * to return an encrypted URL for given simple URL. Other modification can be, that, framework may
		 * decide to return a uniform URL which starts from Application context root with some encryption
		 * 
		 * TODO: We should merge button tag with this tag, or/and further we should keep the web page url with
		 * pageAction itself. It can be set while initializing the metadata by passing current User to DOSchemaManager
		 * or by setting the url from viewHelper while validating the action
		 */
		String actionURL = viewHelper.getWebPageURL( listPageAction.getUrl() );
		String displayDataKey = listPageAction.getDisplayData().getTextKey();
		String className = listPageAction.getClassName();
		boolean popUp = listPageAction.getPopup() != null && listPageAction.getPopup().equals( "true" );
		String buttonBody = null;

		buttonBody = "<input type=\"" + "button" + "\"";
		if( masterTable == null || listPageAction.getDisplayData().getTextKey().contains( "back" ) )
		{
			buttonBody += "value=\"" + bundle.getString( displayDataKey );
		}
		else
		{
			buttonBody += "value=\"" + bundle.getString( displayDataKey ) + " " + masterTable;
		}

		buttonBody += "\" onclick=" + "\"";

		// if action is disabled, just add an java script alert to tell user that this action is not available
		if( !listPageAction.isEnabled() )
		{
			buttonBody += "javascript:";
			buttonBody += "alert('";
			buttonBody += WebUIUtils.getLocaleMessage( "dynalist.actionNotAvailable" );
			buttonBody += "');";
		}
		else
		{
			buttonBody += popUp ? "openPopup('" : "openLink('";
			buttonBody += actionURL;
			buttonBody += actionURL.contains( "?" ) ? "&className=" : "?className=";
			buttonBody += className;
			buttonBody += "')";
		}
		buttonBody += "\" ";

		buttonBody += "class=\"save_buttontext\"";
		buttonBody += "/>";

		pageContext.getOut().print( "\n" + buttonBody + "\n" );
		// pageContext.getOut().print( "</div>" );
	}
}
