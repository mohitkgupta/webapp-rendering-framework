package com.vedantatree.comps.dof.web.jsptags;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.exceptions.ExceptionUtils;

import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.ViewHelper;
import com.vedantatree.comps.dof.web.WebAppConstants;


/**
 * This tag generates the search fields on Dynamic List Page
 * 
 * TODO change the name, captialize first letter
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public class SearchTag extends BodyTagSupport
{

	private static Log		LOGGER	= LogFactory.getLog( SearchTag.class );

	public List<String>		searchList;
	public String			path;
	public ResourceBundle	bundle;

	// TODO change name to current searchColumn and searchValue
	public String			searchColumn;
	public String			searchValue;

	@Override
	public int doStartTag() throws JspException
	{
		Boolean showSearchTag = (Boolean) pageContext.getRequest().getAttribute( "showSearchTag" );
		LOGGER.debug( "show-search[" + showSearchTag + "]" );

		if( showSearchTag != null && showSearchTag.booleanValue() )
		{
			ServletRequest request = pageContext.getRequest();
			searchColumn = (String) request.getAttribute( "searchColumn" );
			searchValue = (String) request.getAttribute( "searchValue" );
			LOGGER.debug( "searchColumn[" + searchColumn + "] searchValue[" + searchValue + "]" );

			DOMetaData doMetaData = (DOMetaData) request.getAttribute( DOMetaData.DOMEATADATA_KEY );
			String viewHelperKey = doMetaData.getViewHelperKey();
			ViewHelper viewHelper = (ViewHelper) ( (HttpServletRequest) request ).getSession()
					.getAttribute( viewHelperKey );
			searchList = viewHelper.getSearchColumnList();

			Locale locale = request.getLocale();
			bundle = ResourceBundle.getBundle( "ApplicationResources", locale );

			path = (String) request.getAttribute( WebAppConstants.BASE_URL );
			// if current request is for popup, add popup, selection, pagination and sort parameters to url
			String selection = (String) request.getAttribute( WebAppConstants.SELECTION_MODE );
			if( selection == null )
			{
				selection = request.getParameter( WebAppConstants.SELECTION_MODE );
			}
			Boolean popup = (Boolean) request.getAttribute( "popup" );
			if( popup != null && popup.booleanValue() )
			{
				path = path + "&popup=true&selection=" + selection;
			}

			LOGGER.debug( "path-4-search-action[" + path + "]" );

			try
			{
				String form = "<form action='" + path + "' method='post'>";
				String formEnd = "</form>";

				pageContext.getOut().print( "<table>" + form
						+ "\n<tr>\n<td class='form_headingtext'>Search By:</td><td class='form_headingtext'><select name='searchColumn' id='searchColumn' class='drop_down_small' onChange='javascript:checkSelection()'>"
						+ "\n<option value='default'>-- Select any --</option>" );

				LOGGER.debug( "search-columns-size[" + searchList.size() + "]" );

				for( String searchColumnLabel : searchList )
				{
					if( searchColumnLabel.equalsIgnoreCase( searchColumn ) )
					{
						pageContext.getOut().print( "\n<option value=" + searchColumnLabel + " selected='true'>"
								+ bundle.getString( searchColumnLabel ) + "</option>" );
					}
					else
					{
						pageContext.getOut().print( "\n<option value=" + searchColumnLabel + ">"
								+ bundle.getString( searchColumnLabel ) + "</option>" );
					}
				}

				pageContext.getOut()
						.print( "</select>\n</td><td class='form_headingtext'>Search:</td><td>"
								+ "<input class='text_box_small' type='text' " + "value='"
								+ ( searchValue != null ? searchValue : "" )
								+ "' name='searchValue' maxlength='255' id='searchValue'/></td>\n<td>"
								+ "<input type='submit' value='Search' id='Search' "
								+ "class='save_buttontext1'/></td></tr>" + formEnd + "</table>" );

			}
			catch( Exception ex )
			{
				LOGGER.error( ex );
				throw new JspTagException( "DynaListSearchTag Error: " + ex.getMessage() + ":: errorTrace\n"
						+ ExceptionUtils.getStackTraceForWebPage( ex ) );
			}
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag()
	{
		return EVAL_PAGE;
	}

	public String getSearchColumn()
	{
		return searchColumn;
	}

	public void setSearchColumn( String searchColumn )
	{
		if( searchColumn != null )
		{
			this.searchColumn = searchColumn;
		}
		else
		{
			this.searchColumn = "";
		}
	}

	public String getSearchValue()
	{
		return searchValue;
	}

	public void setSearchValue( String searchValue )
	{
		if( searchValue != null )
		{
			this.searchValue = searchValue;
		}
		else
		{
			this.searchValue = "";
		}
	}

	public ResourceBundle getBundle()
	{
		return bundle;
	}

	public void setBundle( ResourceBundle bundle )
	{
		this.bundle = bundle;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath( String path )
	{
		this.path = path;
	}

	public List<String> getSearchList()
	{
		return searchList;
	}

	public void setSearchList( List<String> searchList )
	{
		this.searchList = searchList;
	}

}
