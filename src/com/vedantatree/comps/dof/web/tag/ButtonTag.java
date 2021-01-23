package com.vedantatree.comps.dof.web.tag;

import java.util.List;
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
import com.vedantatree.comps.dof.web.vdo.EditAction;


/**
 * 
 * This Tag is Used to Generate Buttons on Add/Edit page of application.
 * It picks the actions form DOMetadata and render the buttons according to their type and data on page.
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public class ButtonTag extends BodyTagSupport implements WebAppConstants
{

	private static Log			LOGGER	= LogFactory.getLog( ButtonTag.class );

	private static final String	SUBMIT	= "submit";
	private static final String	CANCEL	= "cancel";
	private ResourceBundle		bundle	= null;

	@Override
	public int doEndTag()
	{
		return EVAL_PAGE;
	}

	@Override
	public int doStartTag() throws JspException
	{

		LOGGER.trace( "in button tag" );

		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

		DOMetaData doMetaData = (DOMetaData) request.getAttribute( DOMetaData.DOMEATADATA_KEY );
		LOGGER.trace( "doMetadata[" + doMetaData + "]" );

		String viewHelperKey = doMetaData.getViewHelperKey();
		String pageAction = (String) request.getAttribute( PAGEACTION );
		ViewHelper viewHelper = (ViewHelper) request.getSession().getAttribute( viewHelperKey );

		try
		{
			List actionList = null;
			if( ACTION_ADD.equals( pageAction ) )
			{
				actionList = viewHelper.getAddActions();
			}
			else
			{
				actionList = viewHelper.getEditActions();
			}
			LOGGER.debug( "actions[" + actionList + "]" );

			pageContext.getOut().print( "<div id=\"save_button\">" + "\n<table>\n<tr>\n<td>" );

			if( actionList != null && actionList.size() > 0 )
			{
				pageContext.getOut().print( "<div class=\"button_inner_border\">" );
				for( int i = 0; i < actionList.size(); i++ )
				{
					EditAction action = (EditAction) actionList.get( i );
					LOGGER.debug( "action[" + action + "]" );

					// TODO review this check
					if( i == 2 )
					{
						LOGGER.debug( "if i==2 then close first <DIV>::Start Second <DIV> " );
						pageContext.getOut().print( "</div>" );
						pageContext.getOut().print( "<div class=\"button_inner_border\">" );
					}

					/*
					 * Value of action URL comes as 'default', if user wants to use the button as default submit button
					 * so that
					 * call goes to default action specified in page
					 * 
					 * Here we are getting the web page URL for give action URL. Web Page URL means framework may decide
					 * to return an encrypted URL for given simple URL. Other modification can be, that, framework may
					 * decide to return a uniform URL which starts from Application context root with some encryption
					 */

					String actionURL = action.getUrl(); // viewHelper.getWebPageURL( action.getUrl() );
					String displayDataKey = action.getDisplayData().getTextKey();
					String className = action.getClassName();
					String popUp = action.getPopup();
					String actionType = action.getActionType();
					String actionName = action.getName();
					String acessKey = action.getAccessKey();
					Boolean disabled = action.isDisabled();
					String tabIndex = action.getTabIndex();
					String title = action.getTitle();
					String style = action.getStyle();
					String styleId = action.getStyleId();
					String styleClass = action.getStyleClass();
					String jsFunction = action.getJsFunction();
					String jsInfo = action.getJsInfo();
					String jsMethodBody = null;
					String buttonBody = null;

					// handling java script function for simple type of buttons which may open popup
					if( POPUP.equals( popUp ) && actionURL != null && className != null )
					{
						LOGGER.debug( "creating JSMethod Body for popup" );
						jsMethodBody = "openPopup('" + actionURL + "','" + className + "');";
					}
					else if( jsFunction != null )
					{
						jsMethodBody = jsFunction + "(";
						if( actionURL != null )
						{
							jsMethodBody += "'" + actionURL + "'";

						}
						if( jsInfo != null && jsInfo.trim().length() != 0 )
						{
							jsMethodBody += jsInfo;
						}
						jsMethodBody += ");";
						if( action.isJsReturn() )
						{
							jsMethodBody = "return " + jsMethodBody;
						}
					}
					if( actionType == null )
					{
						actionType = "button";
					}
					else if( actionType.equals( CANCEL ) )
					{
						// handling of cancel for struts, as struts support submit type of button with bCancel=true
						// attribute
						actionType = SUBMIT;
						jsMethodBody += "bCancel=true;";
					}

					buttonBody = "<input type=\"" + actionType + "\"";
					if( actionName != null )
					{
						buttonBody += " name=\"" + actionName + "\"";
					}
					else
					{
						buttonBody += " name=\"\"";
					}
					if( styleClass != null )
					{
						buttonBody += " class=\"" + styleClass + "\"";
					}
					if( style != null )
					{
						buttonBody += " style=\"" + style + "\"";
					}
					if( title != null )
					{
						buttonBody += " title=\"" + title + "\"";
					}
					if( tabIndex != null )
					{
						buttonBody += " tabindex=\"" + tabIndex + "\"";
					}
					if( styleId != null )
					{
						buttonBody += " id=\"" + styleId + "\"";
					}
					if( disabled.booleanValue() )
					{
						buttonBody += " disabled=\"" + disabled + "\"";
					}
					if( acessKey != null )
					{
						buttonBody += " acesskey=\"" + acessKey + "\"";
					}
					buttonBody += "value=\"" + bundle.getString( displayDataKey );
					if( jsMethodBody != null )
					{
						buttonBody += "\" onclick=" + "\"" + jsMethodBody;
					}
					buttonBody += "\"/>";

					LOGGER.debug( "button created[" + buttonBody + "]" );
					pageContext.getOut().print( "\n" + buttonBody + "\n" );
				}
				LOGGER.debug( "End of <DIV> " );
				pageContext.getOut().print( "</div>" );
			}
			LOGGER.debug( "End of Div Containig Buttons " );
			pageContext.getOut().print( "</td>" + "\n</tr>\n</table></div>" );
		}
		catch( Exception e )
		{
			LOGGER.error( "Exception in ButtonTag implementation ", e );
			throw new JspTagException( "Exception in ButtonTag implementation " + e.getMessage() );
		}

		return SKIP_BODY;
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
		super.release();
		bundle = null;
	}
}
