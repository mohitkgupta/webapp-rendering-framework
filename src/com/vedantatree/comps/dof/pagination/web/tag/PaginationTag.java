package com.vedantatree.comps.dof.pagination.web.tag;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vedantatree.comps.dof.pagination.PaginationData;
import com.vedantatree.comps.dof.web.WebAppConstants;


/**
 * This tag renders the Pagination links on Dynamic List Page
 * 
 * @author Tyagi
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class PaginationTag extends BodyTagSupport
{

	private static Log			LOGGER			= LogFactory.getLog( PaginationTag.class );

	private final static String	ACTION_FIRST	= "first";
	private final static String	ACTION_PREVIOUS	= "previous";
	private final static String	ACTION_NEXT		= "next";
	private final static String	ACTION_LAST		= "last";

	private PaginationData		paginationData;
	private String				paginationPath;

	@Override
	public int doEndTag()
	{
		return EVAL_PAGE;
	}

	@Override
	public int doStartTag() throws JspException
	{
		Boolean showPaginationTag = (Boolean) pageContext.getRequest().getAttribute( "showPaginationTag" );
		LOGGER.debug( "showPaginationTag[" + showPaginationTag + "]" );

		if( showPaginationTag != null && showPaginationTag.booleanValue() )
		{
			ServletRequest request = pageContext.getRequest();
			String paginationLink = (String) request.getAttribute( WebAppConstants.BASE_URL );

			// Creating the pagination URL. Base URL is the base path for the list page.
			// here we are adding search clause to base url, so that search filter can be maintained with pagination
			// Actually pagination link is the URL where we shall send the request every time for next page
			if( request.getAttribute( WebAppConstants.SEARCH_CLAUSE_URL ) != null )
			{
				paginationLink += (String) request.getAttribute( WebAppConstants.SEARCH_CLAUSE_URL );
			}

			// adding sort clause to pagination url so that pagination can retain the sorting also
			if( request.getAttribute( WebAppConstants.SORT_CLAUSE_URL ) != null )
			{
				paginationLink += (String) request.getAttribute( WebAppConstants.SORT_CLAUSE_URL );
			}

			// if current request is for popup, add popup, selection, pagination and sort parameters to url
			String selection = (String) request.getAttribute( WebAppConstants.SELECTION_MODE );
			if( selection == null )
			{
				selection = request.getParameter( WebAppConstants.SELECTION_MODE );
			}
			Boolean popup = (Boolean) request.getAttribute( "popup" );
			if( popup != null && popup.booleanValue() )
			{
				paginationLink = paginationLink + "&popup=true&selection=" + selection;
			}

			this.paginationPath = paginationLink;
			LOGGER.debug( "pagination-path[" + paginationPath + "]" );

			this.paginationData = (PaginationData) request.getAttribute( "dataList" );

			try
			{

				String content = "<div class='pagination' align='right'>"
						+ "<table border='0' cellspacing='0' cellpadding='0'>" + "<tr>"
						+ "<td  style='font-size:15px;'>Showing " + getStartRecordIndex() + " - " + getLastRecordIndex()
						+ " of " + paginationData.getTotalRecordsSize() + " </td><td>&nbsp;&nbsp;</td><td>";
				if( isValidPaginationAction( ACTION_FIRST ) )
				{
					content += "<a href='" + getPath() + "&pagination=" + ACTION_FIRST
							+ "'><img src='style/images/left_single_navig.gif' border='0' class='arrow_navigation_button' /></a>";
				}
				else
				{
					content += "<img src='style/images/left_double_arrow_disable.gif' border='0' class='arrow_navigation_button' />";
				}
				content += "</td>" + "<td>";
				if( isValidPaginationAction( ACTION_PREVIOUS ) )
				{
					content += "<a href='" + getPath() + "&pagination=" + ACTION_PREVIOUS
							+ "'><img src='style/images/left_doble_navi.gif' border='0' class='arrow_navigation_button1' /></a>";
				}
				else
				{
					content += "<img src='style/images/left_single_arrow_disable.gif' border='0' class='arrow_navigation_button1' />";
				}

				content += "</td>" + "<td><input type='text' id='page' name='page' class='navigation_text_box' value='"
						+ getCurrentPageValue()
						+ "' size='3' maxlength='3' onkeyup='pagination(this.value,event);' /><input type='hidden' name='pagination' id='pagination' value='"
						+ getPath() + "' /><input type='hidden' name='totalPage' id='totalPage' value='"
						+ getTotalNumberOfPage()
						+ "' /><input type='hidden' name='currentPage' id='currentPage' value='" + getCurrentPageValue()
						+ "' /></td>" + "<td>";
				if( isValidPaginationAction( ACTION_NEXT ) )
				{
					content += "<a href='" + getPath() + "&pagination=" + ACTION_NEXT
							+ "'><img src='style/images/right_duble_navi.gif' border='0' class='arrow_navigation_button1' /></a>";
				}
				else
				{
					content += "<img src='style/images/right_single_arrow_disable.gif' border='0' class='arrow_navigation_button1' />";
				}

				content += "</td>" + "<td>";
				if( isValidPaginationAction( ACTION_LAST ) )
				{
					content += "<a href='" + getPath() + "&pagination=" + ACTION_LAST
							+ "'><img src='style/images/right_single_naviga.gif' border='0' class='arrow_navigation_button' /></a>";
				}
				else
				{
					content += "<img src='style/images/right_double_arrow_disable.gif' border='0' class='arrow_navigation_button' />";
				}

				content += "</td>" + "</tr>" + "</table>" + "</div>";

				pageContext.getOut().print( content );
			}
			catch( Exception ex )
			{
				LOGGER.error( ex );
				throw new JspTagException( "SimpleTag: " + ex.getMessage() );
			}
		}
		return SKIP_BODY;
	}

	private int getCurrentPageValue()
	{

		return paginationData.getCurrentPageIndex();
	}

	public PaginationData getDataObject()
	{
		return paginationData;
	}

	private int getLastRecordIndex()
	{

		int pageSize = paginationData.getPageSize();
		int currentPage = paginationData.getCurrentPageIndex();
		int totalRecord = paginationData.getTotalRecordsSize();

		int lastRecordIndex = ( currentPage * pageSize );
		if( lastRecordIndex > totalRecord )
		{
			return totalRecord;
		}

		return lastRecordIndex;
	}

	public String getPath()
	{
		return paginationPath;
	}

	private int getStartRecordIndex()
	{

		int pageSize = paginationData.getPageSize();
		int currentPage = paginationData.getCurrentPageIndex();
		int totalRecord = paginationData.getTotalRecordsSize();

		int startRecordIndex = ( ( currentPage - 1 ) * pageSize ) + 1;
		if( startRecordIndex > totalRecord )
		{
			return totalRecord;
		}

		return startRecordIndex;
	}

	private int getTotalNumberOfPage()
	{

		int totalRecords = paginationData.getTotalRecordsSize();
		int pageSize = paginationData.getPageSize();
		int totalPage = totalRecords / pageSize;
		if( ( totalRecords % pageSize ) > 0 )
		{
			totalPage++;
		}

		return totalPage;
	}

	private boolean isValidPaginationAction( String link )
	{

		int currentPage = paginationData.getCurrentPageIndex();
		int totalRecords = paginationData.getTotalRecordsSize();
		int pageSize = paginationData.getPageSize();
		int totalPage = getTotalNumberOfPage();

		if( link.equals( ACTION_FIRST ) )
		{

			if( currentPage == 1 )
			{
				return false;
			}
			if( totalRecords <= 0 )
			{
				return false;
			}
			if( totalRecords <= pageSize )
			{
				return false;
			}
		}
		else if( link.equals( ACTION_PREVIOUS ) )
		{

			if( currentPage == 1 )
			{
				return false;
			}
			if( totalRecords <= 0 )
			{
				return false;
			}
			if( totalRecords <= pageSize )
			{
				return false;
			}
		}
		else if( link.equals( ACTION_NEXT ) )
		{

			if( totalRecords <= 0 )
			{
				return false;
			}
			if( totalRecords <= pageSize )
			{
				return false;
			}
			if( currentPage == totalPage )
			{
				return false;
			}
		}
		else if( link.equals( ACTION_LAST ) )
		{

			if( totalRecords <= 0 )
			{
				return false;
			}
			if( totalRecords <= pageSize )
			{
				return false;
			}
			if( currentPage == totalPage )
			{
				return false;
			}
		}
		return true;
	}

	public void setDataObject( PaginationData dataObject )
	{
		this.paginationData = dataObject;
	}

	public void setPath( String path )
	{
		this.paginationPath = path;
	}

}
