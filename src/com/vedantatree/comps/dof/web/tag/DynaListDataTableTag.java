package com.vedantatree.comps.dof.web.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.taglib.TagUtils;
import org.vedantatree.utils.StringUtils;
import org.vedantatree.utils.exceptions.ExceptionUtils;

import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.ViewHelper;
import com.vedantatree.comps.dof.pagination.PaginationData;
import com.vedantatree.comps.dof.web.WebAppConstants;
import com.vedantatree.comps.dof.web.WebUIUtils;
import com.vedantatree.comps.dof.web.servlets.DynaListItemSelectionServlet;
import com.vedantatree.comps.dof.web.vdo.Column;
import com.vedantatree.comps.dof.web.vdo.ListAction;


/**
 * Tag to generated the Data List on list page using metadata and view helper.
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class DynaListDataTableTag extends BodyTagSupport implements WebAppConstants
{

	private static Log			LOGGER							= LogFactory.getLog( DynaListDataTableTag.class );
	private static final byte	ANCHOR_WITH_JAVA_SCRIPT_METHOD	= 0;
	private static final byte	ANCHOR_WITH_URL					= 1;
	private static final byte	ANCHOR_WITH_DISABLED_ACTION		= 2;

	// private ResourceBundle bundle;
	private ViewHelper			viewHelper;
	private PaginationData		dataList;
	private boolean				popup;
	private String				sortPath;
	private String				selectionMode;
	private String				parentId;
	private String				superParentId;
	private String				url;
	private List<ListAction>	listActions;
	private boolean				hasLeftActions;
	private boolean				hasRightActions;

	@Override
	public int doStartTag() throws JspException
	{
		LOGGER.trace( "in button tag" );

		try
		{
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

			Locale locale = request.getLocale();
			// bundle = ResourceBundle.getBundle( "ApplicationResources", locale );

			Boolean popupAtt = (Boolean) request.getAttribute( "popup" );
			popup = popupAtt != null ? popupAtt.booleanValue() : false;

			// To handle paren-child relationship on list page
			parentId = request.getParameter( "parentId" );
			superParentId = (String) request.getAttribute( "superParentId" );

			DOMetaData doMetaData = (DOMetaData) request.getAttribute( DOMetaData.DOMEATADATA_KEY );
			LOGGER.trace( "doMetadata[" + doMetaData + "]" );

			String viewHelperKey = doMetaData.getViewHelperKey();
			viewHelper = (ViewHelper) request.getSession().getAttribute( viewHelperKey );
			LOGGER.debug( "viewHelper[" + viewHelper + "]" );

			// Specify selection 'single' or 'multiple'
			// it is used if we are showing checkboxes or radio buttons for list records for selection purpose
			// Currently it is used only in popup case, however it may be used at base window if later we implement to
			// show the detail view on top for selected reocord
			selectionMode = (String) request.getAttribute( WebAppConstants.SELECTION_MODE );
			if( selectionMode == null )
			{
				selectionMode = request.getParameter( WebAppConstants.SELECTION_MODE );
			}

			// creating path for sort link by adding search clause to base url, like we have done in pagination
			sortPath = (String) request.getAttribute( WebAppConstants.BASE_URL );

			if( request.getAttribute( WebAppConstants.SEARCH_CLAUSE_URL ) != null )
			{
				sortPath += (String) request.getAttribute( WebAppConstants.SEARCH_CLAUSE_URL );
			}

			// if current request is for popup, add popup, selection, pagination and sort parameters to url
			if( popup )
			{
				sortPath = sortPath + "&popup=true&selection=" + selectionMode;
			}

			// PaginationData object which contains the list of objects and other info also
			dataList = (PaginationData) request.getAttribute( "dataList" );

			// initialize state whether we have left or right actions or not. This method calling of ViewHelper will not
			// check the data specific rights or customized behavior of extended view helpers. Rather, it will only
			// check the default list actions provided by Security Service i.e. Supervision. We can not initialize the
			// list of actions here, because that needs to be initialized for every row separately as extended view
			// helpers can customize the behavior based on the state of current row data.

			hasLeftActions = viewHelper
					.hasListActionsWithoutRightsConsideration( ListAction.LIST_ACTION_ORIENTATION_LEFT );
			hasRightActions = viewHelper
					.hasListActionsWithoutRightsConsideration( ListAction.LIST_ACTION_ORIENTATION_RIGHT );

			LOGGER.debug( "leftActions[" + hasLeftActions + "] rightActions[" + hasRightActions + "]" );

			pageContext.getOut().print(
					"<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"list_table_border\">" );

			generateDataTableHeader();

			generateDataTable();

			pageContext.getOut().print( "</table>" );
		}
		catch( Exception e )
		{
			LOGGER.error( "Exception in DynaListDataTable Tag implementation ", e );
			throw new JspTagException( "updated Exception in DynaListDataTable Tag implementation " + e.getMessage()
					+ "/n/n" + ExceptionUtils.getStackTrace( e ) );
		}

		return SKIP_BODY;
	}

	private void generateDataTableHeader() throws Exception
	{
		// List of headers of columns which we need to show on listing
		writeContents( "<tr>" );

		// writing column for selection field or for parent data field
		LOGGER.debug( "selectionMode[" + selectionMode + "] parentId[" + parentId + "] popup[" + popup + "]" );
		if( selectionMode != null || parentId != null )
		{
			writeHeaderColumn( "dynalist.selection", null );
		}
		writeHeaderColumn( "dynalist.sno", null );

		// will show the left list actions label if we are not in popup and has left actions
		if( !isPopup() && hasLeftActions )
		{
			writeHeaderColumn( "dynalist.listactions", null );
		}

		List columnList = viewHelper.getColumns();
		if( columnList == null || columnList.size() == 0 )
		{
			writeHeaderColumn( "dynalist.noheaderfound", null );
		}
		else
		{
			// column on which sorting is already done
			String sortColumn = (String) pageContext.getRequest().getAttribute( "sortColumn" );
			String isDescending = pageContext.getRequest().getParameter( "isDescending" );
			LOGGER.debug( "sortColumn[" + sortColumn + "] descending[" + isDescending + "]" );

			for( Iterator iterator = columnList.iterator(); iterator.hasNext(); )
			{
				Column headerColumn = (Column) iterator.next();
				LOGGER.debug( "headerColumn[" + headerColumn + "]" );

				if( !headerColumn.isListPageColumn() )
				{
					continue;
				}

				StringBuffer sortLink = null;
				String headerColumnLabel = headerColumn.getDisplayName();

				LOGGER.debug( "adding column header >name[" + headerColumnLabel + "] sortable["
						+ headerColumn.isSortable() + "] dbName[" + headerColumn.getDbName() + "]" );

				if( headerColumn.isSortable() )
				{
					sortLink = new StringBuffer();

					sortLink.append( "<a href=\"" );
					sortLink.append( "javascript:callSort('" );
					sortLink.append( sortPath );
					sortLink.append( "','" );
					sortLink.append( headerColumn.getDbName() );
					sortLink.append( "'," );

					// if sorting is done for current column, then pass attribute to java script method as per desc/asc
					// attribute
					if( headerColumnLabel.equals( sortColumn ) && isDescending != null
							&& isDescending.equals( "true" ) )
					{
						sortLink.append( "'false')" );
					}
					else
					{
						sortLink.append( "'true')" );
					}
					sortLink.append( "\">" );

					sortLink.append( "&nbsp;" );

					sortLink.append( "<img " );
					sortLink.append( "src=\"style/images/shorting_img.gif\"" );
					sortLink.append( " class=\"shorting_img\"" );
					sortLink.append( "border=\"0\">" );

					sortLink.append( "</a>" );
				}

				writeHeaderColumn( headerColumnLabel, sortLink == null ? null : sortLink.toString() );
			}
		}

		// will show the right list actions label if we are not in popup and has right actions
		if( !isPopup() && hasRightActions )
		{
			writeHeaderColumn( "dynalist.listactions", null );
		}

		pageContext.getOut().print( "</tr>" );
	}

	private void generateDataTable() throws Exception
	{
		if( dataList == null || dataList.size() == 0 )
		{
			writeContents( "<tr><td>&nbsp;</td></tr>" );
			writeContents( "<tr><td>&nbsp;</td></tr>" );
			writeContents( "<tr><td>" );
			writeContents( WebUIUtils.getLocaleMessage( "dynalist.nodatafound" ) );
			writeContents( "</td></tr>" );
			LOGGER.debug( "No record found to generate data table." );
			return;
		}

		LOGGER.debug( "selectionMode[" + selectionMode + "] parentId[" + parentId + "] popup[" + popup + "]" );

		// getting the selected item ids from session, if any item was selected previously
		Collection<String> selectedItemIdsList = (Collection<String>) pageContext.getSession()
				.getAttribute( DynaListItemSelectionServlet.LIST_ITEM_SELECTION_SESSION_ITEM_IDS + "_"
						+ dataList.getPaginationUserId() );

		int rowIndex = 0;
		for( Iterator iterator = dataList.iterator(); iterator.hasNext(); )
		{
			viewHelper.setCurrentRowData( iterator.next() );

			writeContents( "<tr class=\"" );

			writeContents( rowIndex % 2 == 0 ? "table_tr_bgcolor_even" : "table_tr_bgcolor_odd" );
			writeContents( "\" >" );

			StringBuffer columnData = null;

			// writing selection column based on selection mode and for recursive parent data
			if( selectionMode != null )
			{
				columnData = new StringBuffer();
				// if multiple selection, show the check boxes
				if( selectionMode.equals( "multiple" ) )
				{
					columnData.append( "<input " );
					columnData.append( "type=\"checkbox\" " );
					columnData.append( "name=\"selectList\"" );
					columnData.append( "value=\"" );
					columnData.append( viewHelper.getIDDataValue() );
					columnData.append( "\"" );
					// if any previous selection is being done, select that record
					if( selectedItemIdsList != null && selectedItemIdsList.contains( viewHelper.getIDDataValue() ) )
					{
						columnData.append( "checked=\"true\"" );
					}

					// work for multiple selection on list page
					// columnData.append( "onChange=\"" );
					// columnData.append( "callAjaxFunction(" );
					// columnData.append( "/dynaListItemSelection.do?" );
					// columnData.append( DynaListItemSelectionServlet.LIST_ITEM_SELECTION_UI_ID );
					// columnData.append( "=" );
					// columnData.append( dataList.getPaginationUserId() );
					// columnData.append( "&" );
					// columnData.append( DynaListItemSelectionServlet.LIST_ITEM_SELECTION_OPERATION_MODE);
					// columnData.append( "=" );
					// columnData.append( "this.value=" )

					columnData.append( "/>" );
				}
				// else simply show the single selection link
				else
				{
					// TODO - radio button should be shown in case of single selection. Currently a hyper link is being
					// shown
					List<Object> jsParams = new ArrayList<>();
					jsParams.add( viewHelper.getIDDataValue() );
					jsParams.add( viewHelper.getClassName() );

					generateAnchorTagContents( columnData, ANCHOR_WITH_JAVA_SCRIPT_METHOD, false, "selectSingle", null,
							jsParams, "select", "style/images/select_link.gif", null, null );
				}

				// appending parent data link if parent id is not null, using same column for both as of now
				if( parentId != null )
				{

					Map<String, Object> urlParams = new HashMap<>();
					urlParams.put( "action", "list" );
					urlParams.put( "parentId", viewHelper.getIDDataValue() );
					urlParams.put( "superParentId", parentId );
					urlParams.put( "className", viewHelper.getClassName() );
					urlParams.put( "popup", "true" );
					urlParams.put( "selection", "single" );
					generateAnchorTagContents( columnData, ANCHOR_WITH_URL, false, url, urlParams, null, "child",
							"style/images/tree.gif", null, null );
				}
				writeDataColumn( columnData.toString() );
			}

			// writing serial no
			writeDataColumn( viewHelper.getNextSerialNumber() + "" );

			// get list actions valid for this row. Validity will depend upon user right on particular action and
			// developer logic which might be implemented in view helper. Thats why list actions are taken after setting
			// the row data, so that if developer wants, can apply some logic based on current row data
			listActions = viewHelper.getListActions();

			LOGGER.debug( "listActions[" + listActions + "] hasLeftActions[" + hasLeftActions + "] hasRightActions["
					+ hasRightActions + "]" );

			// generating left oriented list actions
			generateListActions( viewHelper, true );

			LOGGER.debug( "Rendering data" );
			List columnList = viewHelper.getDoMetadata().getColumns();
			if( columnList == null || columnList.size() == 0 )
			{
				LOGGER.debug( "No column found with dometadata" );
				writeDataColumn( WebUIUtils.getLocaleMessage( "dynalist.noheaderfound" ) );
			}
			else
			{
				int columnIndex = 0;
				for( Iterator columnsIter = columnList.iterator(); columnsIter.hasNext(); )
				{
					Column headerColumn = (Column) columnsIter.next();
					if( !headerColumn.isListPageColumn() )
					{
						continue;
					}
					viewHelper.setCurrentColumnIndex( columnIndex );
					Object data = viewHelper.getCurrentColumnData();
					++columnIndex;
					LOGGER.debug( "columnIndex[" + columnIndex + "] data[" + data + "]" );

					writeDataColumn( data == null ? " " : data + "" );
				}
			}

			// generating right oriented list actions
			generateListActions( viewHelper, false );

			writeContents( "</tr>" );
			++rowIndex;
		}
	}

	private void generateListActions( ViewHelper viewHelper, boolean leftOriented ) throws Exception
	{
		// rendering list actions
		LOGGER.debug( "Rendering list actions" );

		if( popup )
		{
			LOGGER.debug( "returning without rendering the list actions, as no action is required in popup" );
			return;
		}

		LOGGER.debug( "is-left-oriented[" + leftOriented + "] all-listaction-size["
				+ ( listActions != null ? listActions.size() : -1 + "]" ) );

		// to track whether any action is rendered or not. Action may not be rendered if we dont get any action for
		// current orientation
		boolean actionRendered = false;

		// buffer to store action data
		StringBuffer columnData = new StringBuffer();

		if( listActions != null && listActions.size() > 0 )
		{
			// iterate over actions and render if we have actions to render for current orientation
			for( ListAction listAction2 : listActions )
			{
				// we are not handling the case when action information used to be in array of info previously
				ListAction listAction = listAction2;
				LOGGER.debug( "rendering-list-action[" + listAction + "]" );

				if( leftOriented != listAction.isLeftOriented() )
				{
					LOGGER.debug( "skipping rendering of list action because orientation is different. "
							+ "isRenderingLeftOriented[" + leftOriented + "] " + "action-orientation"
							+ listAction.getOrientation() + "]" );
					continue;
				}

				// if we crossed above if, it means that at least one action is rendered
				actionRendered = true;

				if( "true".equals( listAction.getPopup() ) )
				{
					List<String> jsParams = new ArrayList<>();
					StringBuffer param = new StringBuffer();
					param.append( listAction.getUrl() );
					param.append( "&id=" );
					param.append( viewHelper.getIDDataValue() );
					param.append( "&className" );
					param.append( listAction.getClassName() );
					jsParams.add( param.toString() );

					generateAnchorTagContents( columnData,
							listAction.isEnabled() ? ANCHOR_WITH_JAVA_SCRIPT_METHOD : ANCHOR_WITH_DISABLED_ACTION, true,
							"openPopup", null, jsParams, listAction.getDisplayData().getTextKey(),
							"style/images/" + listAction.getDisplayData().getImageName(), null,
							"&nbsp;&nbsp;&nbsp;&nbsp" );
				}
				else
				{
					Map<String, Object> urlParams = new HashMap<>();
					urlParams.put( "id", viewHelper.getIDDataValue() );
					urlParams.put( "className", viewHelper.getClassName() );

					String imageClickMethod = null;

					// if any confirmation message is given in metadata xml
					if( StringUtils.isQualifiedString( listAction.getConfirmationMessage() ) )
					{
						String confirmMessage = WebUIUtils.getLocaleMessage( listAction.getConfirmationMessage() );
						LOGGER.debug( "list-confirm-Message[" + confirmMessage + "]" );
						imageClickMethod = "confirmDynaActionSubmit('" + confirmMessage + "')";
					}
					// for backward compatability, if delete or remove action - show confirmation
					else if( listAction.getUrl().toLowerCase().contains( "delete" )
							|| listAction.getUrl().toLowerCase().contains( "remove" ) )
					{
						LOGGER.debug( "list-delete-confirm" );
						imageClickMethod = "confirmDelete()";
					}

					generateAnchorTagContents( columnData,
							listAction.isEnabled() ? ANCHOR_WITH_URL : ANCHOR_WITH_DISABLED_ACTION, false,
							listAction.getUrl(), urlParams, null, listAction.getDisplayData().getTextKey(),
							"style/images/" + listAction.getDisplayData().getImageName(), imageClickMethod,
							"&nbsp;&nbsp;&nbsp;&nbsp" );
				}
			}
		}

		// if not even one action is rendered, it means that no one qualify the orientation check or extended view
		// helper filtering criteria. So let us render the message that no action is available

		if( !actionRendered && ( ( leftOriented && hasLeftActions ) || ( !leftOriented && hasRightActions ) ) )
		{
			columnData.append( WebUIUtils.getLocaleMessage( "dynalist.no.listaction" ) ); // no action available.
		}

		// write the collected row action data to page output
		if( columnData.length() != 0 )
		{
			writeDataColumn( columnData.toString() );
		}
	}

	private void writeDataColumn( String data ) throws Exception
	{
		writeContents( "<td class=\"table_left_border\">" );
		writeContents( data );
		writeContents( "</td>" );
	}

	private void writeHeaderColumn( String columnHeadingKey, String extraHeaderContents ) throws Exception
	{
		writeContents( "<td class=\"inner_table_heading\">" );
		writeContents( WebUIUtils.getLocaleMessage( columnHeadingKey ) );
		if( extraHeaderContents != null )
		{
			writeContents( extraHeaderContents );
		}
		writeContents( "</td>" );
	}

	private void writeContents( String contents ) throws Exception
	{
		TagUtils.getInstance().write( pageContext, contents );
	}

	/**
	 * Method to generate anchor tag
	 * 
	 * TODO should be two separate methods for js method and url, will increase the readability
	 * 
	 * @param columnData data of column to add in anchor tag
	 * @param mode mode, possible values are anchor with url, anchor with java script method, and anchor with disabled
	 *        action
	 * @param insertJSVoid true if we need to insert java script void function with specified JS method
	 * @param linkOrJSMethod link or java script method name
	 * @param urlParams parameters in case if we are working for url
	 * @param jsParams parameters in case if we are working for java script method
	 * @param titleKey key of title for anchor
	 * @param imageSource source of image
	 * @param imageClickMethod java script method, with no parameter, which should be called on click on image and value
	 *        should be returned
	 * @param extraContents any extra contents which we may need to add at the end
	 * @return final contents for anchor tag
	 * @throws Exception if there is any problem
	 */
	private StringBuffer generateAnchorTagContents( StringBuffer columnData, byte mode, boolean insertJSVoid,
			String linkOrJSMethod, Map urlParams, List jsParams, String titleKey, String imageSource,
			String imageClickMethod, String extraContents ) throws Exception
	{

		if( columnData == null )
		{
			columnData = new StringBuffer();
		}
		columnData.append( "<a " );
		columnData.append( "href=\"" );

		switch( mode )
		{
			case ANCHOR_WITH_DISABLED_ACTION:
			{
				LOGGER.debug( "generating anchor tag with disabled action" );
				columnData.append( "javascript:" );
				columnData.append( "alert('" );
				columnData.append( WebUIUtils.getLocaleMessage( "dynalist.actionNotAvailable" ) );
				columnData.append( "');" );
				break;
			}
			case ANCHOR_WITH_JAVA_SCRIPT_METHOD:
			{
				LOGGER.debug( "generating anchor tag with java script method" );
				columnData.append( "javascript:" );
				columnData.append( linkOrJSMethod );
				columnData.append( "(" );
				if( jsParams != null && jsParams.size() > 0 )
				{
					boolean firstIteration = true;
					for( Iterator iterator = jsParams.iterator(); iterator.hasNext(); )
					{
						Object jsParam = iterator.next();
						if( !firstIteration )
						{
							columnData.append( ", " );
						}
						else
						{
							firstIteration = false;
						}
						columnData.append( "'" );
						columnData.append( jsParam );
						columnData.append( "'" );
					}
				}
				columnData.append( ");" );
				if( insertJSVoid )
				{
					columnData.append( "void(0);" );
				}
				break;
			}
			default: // Anchor with url
			{
				LOGGER.debug( "generating anchor tag with url" );
				columnData.append( linkOrJSMethod );

				if( urlParams != null && urlParams.size() > 0 )
				{
					boolean firstIteration = true;
					for( Iterator iterator = urlParams.entrySet().iterator(); iterator.hasNext(); )
					{
						Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
						// append ? if it is first iteration and ? is not already appended
						if( firstIteration && columnData.indexOf( "?" ) < 0 )
						{
							columnData.append( "?" );
							firstIteration = false;
						}
						else
						{
							columnData.append( "&" );
						}
						columnData.append( entry.getKey() );
						columnData.append( "=" );
						columnData.append( entry.getValue() );
					}
				}
			}
		}
		columnData.append( "\" " );

		columnData.append( "title=\"" );
		columnData.append( WebUIUtils.getLocaleMessage( titleKey ) );
		columnData.append( "\">" );

		columnData.append( "<img " );
		columnData.append( "src=\"" );
		columnData.append( imageSource );
		columnData.append( "\" " );
		columnData.append( "border=\"0\" " );
		if( imageClickMethod != null )
		{
			columnData.append( "onclick=\"return " );
			columnData.append( imageClickMethod );
			columnData.append( ";\"" );
		}
		columnData.append( "/>" );

		if( extraContents != null )
		{
			columnData.append( extraContents );
		}

		columnData.append( "</a>" );

		return columnData;
	}

	@Override
	public int doEndTag()
	{
		return EVAL_PAGE;
	}

	private boolean isPopup()
	{
		return popup;
	}

	private String getSortPath()
	{
		return sortPath;
	}

	private ViewHelper getViewHelper()
	{
		return viewHelper;
	}

	private String getSelectionMode()
	{
		return selectionMode;
	}

	private List getDataList()
	{
		return dataList;
	}

	private String getParentId()
	{
		return parentId;
	}

	private String getSuperParentId()
	{
		return superParentId;
	}

	private String getURL()
	{
		return url;
	}

	/**
	 * It will tell whether list actions retrieved from view helper are having actions of given orientation or not.
	 * 
	 * @param leftOriented true if method should look for left oriented actions
	 * @return true if any list action found for given orientation, false otherwise
	 */
	private boolean hasListActions( boolean leftOriented )
	{
		if( listActions == null || listActions.size() == 0 )
		{
			return false;
		}
		for( Object element : listActions )
		{
			ListAction listAction = (ListAction) element;
			if( leftOriented && listAction.isLeftOriented() )
			{
				return true;
			}
			if( !leftOriented && listAction.isRightOriented() )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void release()
	{
		viewHelper = null;
		dataList = null;
		popup = false;
		sortPath = null;
		selectionMode = null;
		parentId = null;
		superParentId = null;
		url = null;
		listActions = null;
		hasLeftActions = false;
		hasRightActions = false;
		super.release();
	}

}
