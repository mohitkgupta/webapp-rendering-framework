package com.vedantatree.comps.dof.web.actions;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.vedantatree.utils.BeanUtils;
import org.vedantatree.utils.StringUtils;
import org.vedantatree.utils.exceptions.ApplicationException;
import org.vedantatree.utils.exceptions.ComponentException;
import org.vedantatree.utils.exceptions.ExceptionUtils;
import org.vedantatree.utils.exceptions.db.RelationExistException;
import org.vedantatree.utils.exceptions.server.ServerBusinessException;

import com.vedantatree.comps.dof.DOFUtils;
import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.DOSchemaManager;
import com.vedantatree.comps.dof.JNDILookupManager;
import com.vedantatree.comps.dof.ViewHelper;
import com.vedantatree.comps.dof.enterprise.clients.MasterTable;
import com.vedantatree.comps.dof.enterprise.clients.MasterTableHome;
import com.vedantatree.comps.dof.pagination.client.PaginationManagerDelegate;
import com.vedantatree.comps.dof.web.WebAppConstants;
import com.vedantatree.comps.dof.web.WebUIUtils;
import com.vedantatree.comps.dof.web.security.SecurityServletFilter;
import com.vedantatree.comps.dof.web.vdo.Column;


/**
 * MasterTableAction class is used for getting the Classname and forward the request to the specified class
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */

public class MasterTableAction extends Action implements WebAppConstants
{

	private static Log			LOGGER					= LogFactory.getLog( MasterTableAction.class );
	public static final String	MASTERTABLEBEAN			= "MasterTableBean";

	private final static String	ACTION					= "action";
	private final static String	CLASS_NAME				= "className";
	private final static String	SORT_COLUMN				= "sortColumn";

	// private final static String TILES_DEFINITION = "tilesDefinition";
	private final static String	PARENT_ID				= "parentId";
	// private final static String ROOT = "root";
	private final static String	IS_DESCENDING			= "isDescending";
	private final static String	SEARCH					= "search";
	private final static String	SEARCH_PATH_VALUE		= "masterTable.do?action=list&className=";
	private final static String	MASTERTABLE_HELP_LIST	= "master_list_";
	private final static String	MASTERTABLE_PAGE_ID		= "masterTablePageId";
	private final static String	MASTERTABLEDATAPROVIDER	= "com.vedantatree.comps.dof.enterprise.dao.pagination.MasterTableDataProvider";
	private final static String	MASTERTABLE_TILES_DEF	= "CommonDef";
	private final static String	SUPER_PARENT_ID			= "superParentId";
	private final static String	LIST_PAGE				= "listPage";
	private final static String	ADD_PAGE				= "addPage";
	private final static String	EDIT_PAGE				= "editPage";
	private final static String	EDITPAGE				= "fromEditAction";																// Add

	private final static String	CURRENT_NAV_OBJECT		= "currentNavigationObject";

	@Override
	public ActionForward execute( ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response )
	{
		LOGGER.trace( "execute of MasterTableAction" );
		String actionName = request.getParameter( ACTION );
		String uiMetadataName = request.getParameter( CLASS_NAME );
		String sortColumnName = request.getParameter( SORT_COLUMN );
		String searchColumnName = request.getParameter( SEARCH_COLUMN );
		String searchColumnValue = request.getParameter( SEARCH_COLUMN_VALUE );
		String parentId = request.getParameter( PARENT_ID );
		String isDescending = request.getParameter( IS_DESCENDING );
		MasterTable masterTable = null;

		// TODO it should be looked up once
		try
		{
			masterTable = (MasterTable) JNDILookupManager.lookupRemoteEJB( MASTERTABLEBEAN, MasterTableHome.class );
		}
		catch( ComponentException e1 )
		{
			LOGGER.debug( "Exception Occured while bounding MasterTable Bean " + e1 );
			WebUIUtils.setExceptionPageParameters( request, e1, "settingsMain.jsp", MASTERTABLE_TILES_DEF );
			mapping.findForward( "error" );
		}

		// TODO: Probably it should be passed further, as down in code, we are getting it again and again from request
		DOMetaData doMetadata;
		try
		{
			doMetadata = DOSchemaManager.getDOMetadata( uiMetadataName );
		}
		catch( ComponentException e1 )
		{
			ExceptionUtils.logException( LOGGER, e1.getMessage(), e1 );
			WebUIUtils.setExceptionPageParameters( request, e1, "settingsMain.jsp", MASTERTABLE_TILES_DEF );
			return mapping.findForward( "error" );
		}
		try
		{
			DOFUtils.initializeRequestCache( request, doMetadata );
		}
		catch( ApplicationException e2 )
		{
			LOGGER.error( "Exception Occured while configuring request with dometadata", e2 );
			WebUIUtils.setExceptionPageParameters( request, e2, "settingsMain.jsp", MASTERTABLE_TILES_DEF );
			return mapping.findForward( "error" );
		}

		ViewHelper viewHelper = (ViewHelper) request.getSession().getAttribute( doMetadata.getViewHelperKey() );
		LOGGER.trace( "viewHelper[" + viewHelper + "]" );
		HttpSession session = request.getSession();

		if( !StringUtils.isQualifiedString( searchColumnName ) )
		{
			searchColumnName = null;
		}
		if( !StringUtils.isQualifiedString( searchColumnValue ) )
		{
			searchColumnValue = null;
		}
		if( !StringUtils.isQualifiedString( sortColumnName ) )
		{
			sortColumnName = null;
		}

		request.setAttribute( LIVE_RELATIONS_DETECTED, null );
		// reset values
		request.setAttribute( SEARCH_COLUMN, searchColumnName );
		request.setAttribute( SEARCH_COLUMN_VALUE, searchColumnValue );

		LOGGER.debug( "searchColumnName[" + searchColumnName );
		LOGGER.debug( "searchColumnValue[" + searchColumnValue );
		LOGGER.debug( "actionName[ " + actionName + " ] className-uiMetadataName[" + uiMetadataName + "]" );

		String objectId = request.getParameter( "id" );
		LOGGER.debug( "objectId[" + objectId + "]" );

		Long id = null;
		if( StringUtils.isQualifiedString( objectId ) )
		{
			id = Long.valueOf( objectId );
		}

		if( actionName.equalsIgnoreCase( ACTION_LIST ) )
		{
			LOGGER.debug( "In List Method" );

			String popup = request.getParameter( POPUP );
			LOGGER.debug( "popup[ " + popup + " ] " );
			try
			{
				request.setAttribute( LIST_PAGE, "master.listing." + viewHelper.getClassName() );
				return getListPage( mapping, request, doMetadata, masterTable, sortColumnName, isDescending,
						searchColumnValue, searchColumnName, parentId );
			}
			catch( Exception e )
			{
				ExceptionUtils.logException( LOGGER, e.getMessage(), e );
				WebUIUtils.setExceptionPageParameters( request, e, "settingsMain.jsp", MASTERTABLE_TILES_DEF );
				return mapping.findForward( "error" );
			}

		}
		else if( actionName.equalsIgnoreCase( ACTION_SAVE ) )
		{
			LOGGER.trace( "In Edit method of MasterTable" );
			request.setAttribute( LIST_PAGE, "master.listing." + viewHelper.getClassName() );
			try
			{
				return getSavePage( mapping, request, doMetadata, masterTable, id );
			}
			catch( ComponentException e )
			{
				ExceptionUtils.logException( LOGGER, e.getMessage(), e );
				WebUIUtils.setExceptionPageParameters( request, e, "settingsMain.jsp", MASTERTABLE_TILES_DEF );
				e.printStackTrace();
				return mapping.findForward( "error" );
			}
		}
		else if( actionName.equalsIgnoreCase( ACTION_ADD ) || actionName.equalsIgnoreCase( ACTION_EDIT ) )
		{
			return getAddEdit( mapping, request, doMetadata, masterTable, id );
		}
		else if( actionName.equalsIgnoreCase( SEARCH ) )
		{
			try
			{
				request.setAttribute( LIST_PAGE, "master.listing." + viewHelper.getClassName() );
				return getListPage( mapping, request, doMetadata, masterTable, sortColumnName, isDescending,
						searchColumnValue, searchColumnName, parentId );
			}
			catch( Exception e )
			{
				ExceptionUtils.logException( LOGGER, e.getMessage(), e );
				WebUIUtils.setExceptionPageParameters( request, e, "masterTable.do?action=list",
						MASTERTABLE_TILES_DEF );
				return mapping.findForward( "error" );
			}
		}
		else if( actionName.equalsIgnoreCase( ACTION_DELETE ) )
		{
			LOGGER.trace( "In delete method of Master Table Action" );
			try
			{
				masterTable.deleteData( viewHelper.getClassName(), id );
			}
			catch( RemoteException e )
			{
				LOGGER.error( "JNDI Exception while bounding MasterTable In Delete" );
				WebUIUtils.setExceptionPageParameters( request, e, "masterTable.do?action=list",
						MASTERTABLE_TILES_DEF );
				request.setAttribute( LIST_PAGE, "master.listing." + viewHelper.getClassName() );
				return mapping.findForward( "error" );

			}
			catch( ServerBusinessException e )
			{
				ExceptionUtils.logException( LOGGER, e.getMessage(), e );
				WebUIUtils.setExceptionPageParameters( request, e, "masterTable.do?action=list",
						MASTERTABLE_TILES_DEF );
				request.setAttribute( LIST_PAGE, "master.listing." + viewHelper.getClassName() );
				return mapping.findForward( "error" );
			}
			catch( RelationExistException e )
			{
				LOGGER.debug( "In ChildREcordExixtException Catch block" );
				ExceptionUtils.logException( LOGGER, e.getMessage(), e );
				request.setAttribute( LIST_PAGE, "master.listing." + viewHelper.getClassName() );
				request.setAttribute( LIVE_RELATIONS_DETECTED, "delete.exception.message" );
				try
				{
					return getListPage( mapping, request, doMetadata, masterTable, sortColumnName, isDescending,
							searchColumnValue, searchColumnName, parentId );
				}
				catch( Exception e1 )
				{
					ExceptionUtils.logException( LOGGER, e1.getMessage(), e1 );
					WebUIUtils.setExceptionPageParameters( request, e, "masterTable.do?action=list",
							MASTERTABLE_TILES_DEF );
					return mapping.findForward( "error" );
				}
			}
			try
			{
				request.setAttribute( LIST_PAGE, "master.listing." + viewHelper.getClassName() );
				return getListPage( mapping, request, doMetadata, masterTable, sortColumnName, isDescending,
						searchColumnValue, searchColumnName, parentId );

			}
			catch( Exception e )
			{
				ExceptionUtils.logException( LOGGER, e.getMessage(), e );
				WebUIUtils.setExceptionPageParameters( request, e, "settingsMain.jsp", MASTERTABLE_TILES_DEF );
				return mapping.findForward( "error" );
			}
		}
		else
		{
			return mapping.findForward( "faliure" );
		}
	}

	private ActionForward getAddEdit( ActionMapping mapping, HttpServletRequest request, DOMetaData doMetadata,
			MasterTable masterTableBean, Long objectId )
	{
		Object dataValueList = new Object();

		// add case
		if( objectId != null )
		{
			try
			{
				dataValueList = masterTableBean.getDataById( doMetadata, objectId );
			}
			catch( RemoteException e )
			{
				LOGGER.error( "JNDI Exception while bounding MasterTable In Add Edit" );
				WebUIUtils.setExceptionPageParameters( request, e, "masterTable.do?action=list",
						MASTERTABLE_TILES_DEF );
				return mapping.findForward( "error" );
			}
			catch( ServerBusinessException e )
			{
				ExceptionUtils.logException( LOGGER, e.getMessage(), e );
				WebUIUtils.setExceptionPageParameters( request, e, "masterTable.do?action=list",
						MASTERTABLE_TILES_DEF );
				return mapping.findForward( "error" );
			}
		}

		List<Map<Object, Object>> dataList = new ArrayList<>();
		List<com.vedantatree.comps.dof.web.vdo.Column> columnNames = doMetadata.getColumns();
		String actionName = null;

		for( int i = 0; i < columnNames.size(); i++ )
		{
			Column column = columnNames.get( i );

			LOGGER.debug( "column-editable[ " + column.isEditable() + " ]" );
			if( column.isEditable() )
			{
				Map columnMap = new HashMap();

				String displayName = column.getDisplayName();
				String dataType = column.getDataType();
				String dataObjectPropertyName = column.getDbName();
				LOGGER.debug( "diaplayName[" + displayName + "] dataType[" + dataType + "] propertyName["
						+ dataObjectPropertyName + "]" );

				String validations = column.getValidationRules();
				if( dataType.contains( "Long" ) || dataType.contains( "Integer" ) || dataType.contains( "Float" )
						|| dataType.contains( "Double" ) )
				{
					dataType = "Numeric";
				}
				else if( dataType.contains( "String" ) )
				{
					dataType = "String";
				}
				else if( dataType.contains( "Date" ) )
				{
					dataType = "Date";
				}
				else if( dataType.contains( "ManyToOne" ) )
				{
					dataType = "Object";
					displayName = "";
				}
				LOGGER.debug( "manipulated-dataType[" + dataType + "] displayName[" + displayName + "]" );

				Object dataValue = null;
				String methodName = "get" + dataObjectPropertyName;

				if( objectId != null )
				{
					try
					{
						dataValue = BeanUtils.invokeMethod( dataValueList, methodName, null );
					}
					catch( ApplicationException e )
					{
						ExceptionUtils.logException( LOGGER, e.getMessage(), e );
						WebUIUtils.setExceptionPageParameters( request, e, "masterTable.do?action=list",
								MASTERTABLE_TILES_DEF );
						return mapping.findForward( "error" );
					}
					actionName = "edit";

				}
				else
				{
					dataValue = "";
					actionName = "add";
				}
				columnMap.put( "validations", validations );
				columnMap.put( "value", dataValue );
				columnMap.put( "propertyname", dataObjectPropertyName );
				columnMap.put( "name", displayName );
				columnMap.put( "type", dataType );
				dataList.add( columnMap );
			}

		}
		request.setAttribute( "action", actionName );
		request.setAttribute( "listProperties", dataList );
		request.setAttribute( "className", doMetadata.getClassName() );
		// req.setAttribute( TILES_DEFINITION, "SettingDef" );

		if( objectId != null )
		{
			request.setAttribute( EDIT_PAGE, "master.edit." + doMetadata.getClassName() );
			request.setAttribute( EDITPAGE, "yes" );
			// differentiate on ADDEDIT.JSP
			// PAGE
		}
		else
		{
			request.setAttribute( ADD_PAGE, "master.add." + doMetadata.getClassName() );
		}
		return mapping.findForward( "add" );
	}

	private ActionForward getSavePage( ActionMapping mapping, HttpServletRequest req, DOMetaData doMetadata,
			MasterTable masterTableBean, Long id ) throws ComponentException
	{
		// TODO: Datalist is not required. remove it.
		List<Object> dataList = new ArrayList<>();
		Map<String, Object> dataMap = new HashMap<>();

		Enumeration<String> requestParams = req.getParameterNames();
		List<Column> columnNames = doMetadata.getColumns();

		LOGGER.debug( "columnSize[" + columnNames.size() + "]" );

		while( requestParams.hasMoreElements() )
		{
			String reqParam = requestParams.nextElement();
			LOGGER.debug( "currentReqParam[" + reqParam + "]" );

			for( int i = 0; i < columnNames.size(); i++ )
			{
				Column column = columnNames.get( i );
				String columnName = column.getDbName().trim();
				LOGGER.debug( "columnName[ " + columnName + " ]" );

				String columnDataType = column.getDataType();
				LOGGER.debug( "columnType[ " + columnDataType + " ]" );

				Object columnValue = null;
				if( columnName.equalsIgnoreCase( "objectGroup" ) )
				{
					columnValue = SecurityServletFilter.getObjectGroup();
				}
				else
				{
					columnValue = req.getParameter( reqParam );
				}
				LOGGER.debug( "columnValue[" + columnValue + "]" );

				dataMap.put( columnName, columnValue );
				dataList.add( columnValue );
			}

		}

		try
		{
			LOGGER.debug( "Size of List in Master Table DAO " + dataList.size() );

			masterTableBean.saveOrUpdateData( doMetadata, dataList, id, dataMap );
			return getListPage( mapping, req, doMetadata, masterTableBean, null, null, null, null, null );
		}
		catch( RemoteException e )
		{
			LOGGER.error( "JNDI Exception while bounding MasterTable In Edit" );

			if( id == null )
			{
				WebUIUtils.setExceptionPageParameters( req, e, "masterTable.do?action=add", MASTERTABLE_TILES_DEF );
			}
			else
			{
				WebUIUtils.setExceptionPageParameters( req, e, "masterTable.do?action=edit", MASTERTABLE_TILES_DEF );
			}
			return mapping.findForward( "error" );
		}
		catch( ServerBusinessException e )
		{
			ExceptionUtils.logException( LOGGER, e.getMessage(), e );
			if( id == null )
			{
				WebUIUtils.setExceptionPageParameters( req, e, "masterTable.do?action=add", MASTERTABLE_TILES_DEF );
			}
			else
			{
				WebUIUtils.setExceptionPageParameters( req, e, "masterTable.do?action=edit", MASTERTABLE_TILES_DEF );
			}
			return mapping.findForward( "error" );
		}
	}

	private ActionForward getListPage( ActionMapping mapping, HttpServletRequest request, DOMetaData doMetadata,
			MasterTable masterTableBean, String sortColumn, String isDescending, String searchColumnValue,
			String searchColumnName, String parentId )
			throws RemoteException, ServerBusinessException, ComponentException
	{

		List dataList = null;
		String searchClause = null;
		LOGGER.debug( "parentId[" + parentId + "]" );

		String superParent = request.getParameter( SUPER_PARENT_ID );
		if( superParent != null )
		{
			request.setAttribute( SUPER_PARENT_ID, superParent );
		}
		LOGGER.debug( "superParentId[" + superParent + "]" );

		String blankDef = request.getParameter( BLANK_TILES_DEF );
		String qualifier = StringUtils.getSimpleClassName( doMetadata.getClassName() );
		String searchPath = SEARCH_PATH_VALUE + qualifier;
		String baseURL = "masterTable.do?action=list&className=" + qualifier;

		String popup = request.getParameter( POPUP );
		LOGGER.debug( "popup" + popup + " ] " );
		if( popup != null && popup.equalsIgnoreCase( "true" ) )
		{
			request.setAttribute( POPUP, true );
			searchPath = searchPath + "&popup=true";
			request.setAttribute( SELECTION_MODE, request.getParameter( SELECTION_MODE ) );
			request.setAttribute( SUBTITLE, request.getAttribute( SUBTITLE ) );
		}
		LOGGER.debug( "tilesDefintion[" + blankDef + "]" );
		HttpSession session = request.getSession();
		if( parentId != null && parentId.equalsIgnoreCase( "root" ) )
		{
			searchClause = " a." + doMetadata.getParentColumn() + " is NULL";
		}
		else if( parentId != null )
		{
			searchClause = " a." + doMetadata.getParentColumn() + "=" + parentId;
		}
		LOGGER.debug( "searchClause[" + searchClause + "]" );

		// Set Base url in request for list page
		if( request.getAttribute( BASE_URL ) == null )
		{
			request.setAttribute( BASE_URL, baseURL );
		}

		if( request.getAttribute( DATALIST ) == null )
		{
			int paginationUserId = 0;

			PaginationManagerDelegate paginationManager = PaginationManagerDelegate.getSharedInstance();

			String pagination = request.getParameter( PAGINATION );
			if( pagination == null )
			{

				if( session.getAttribute( MASTERTABLE_PAGE_ID ) != null )
				{
					Integer pageId = (Integer) session.getAttribute( MASTERTABLE_PAGE_ID );
					paginationUserId = pageId.intValue();
					paginationManager.unregisterPaginationUser( paginationUserId );
					session.removeAttribute( MASTERTABLE_PAGE_ID );
				}
			}

			if( session.getAttribute( MASTERTABLE_PAGE_ID ) == null )
			{
				LOGGER.debug( "in list() in MasterTable classObject[ " + doMetadata + " ]" );
				paginationUserId = paginationManager.registerPaginationUser( MASTERTABLEDATAPROVIDER, null, qualifier,
						PAGE_SIZE, doMetadata );
				session.setAttribute( MASTERTABLE_PAGE_ID, paginationUserId );
			}
			else
			{
				Integer pageId = (Integer) session.getAttribute( MASTERTABLE_PAGE_ID );
				paginationUserId = pageId.intValue();
			}

			/*
			 * Setting searchQuery
			 */
			boolean flag = false;
			String searchClauseURL = "";
			String search = (String) request.getAttribute( SEARCH_CLAUSE );
			LOGGER.debug( "requestSearchClause[ " + search + " ]" );

			int pageIndex = 1;
			if( ( searchColumnName == null || "".equalsIgnoreCase( searchColumnName ) ) && parentId != null )
			{
				LOGGER.debug( "IN PARENT CHILD LIST" );
				pageIndex = Integer.MAX_VALUE;
				searchClauseURL += "&parentId=" + parentId;
				searchPath += "&parentId=" + parentId;
				flag = true;
			}
			else if( searchColumnName != null && !"".equalsIgnoreCase( searchColumnName ) && parentId != null )
			{
				String searchClauseTemp;
				LOGGER.debug( "SEARCH COLUMN VALUE " + searchColumnValue );
				LOGGER.debug( "SEARCH COLUMN NAME " + searchColumnName );
				searchClauseTemp = DOFUtils.getPaginationSearchString( doMetadata, searchColumnValue,
						searchColumnName );
				// To handle search case for child
				if( parentId != null )
				{
					searchClause = searchClauseTemp + " and " + searchClause;
				}
				else
				{
					searchClause = searchClauseTemp;
				}
				searchClauseURL += "&searchColumn=" + searchColumnName;
				searchClauseURL += "&searchValue=" + searchColumnValue;
				searchClauseURL += "&parentId=" + parentId;
				searchPath += "&parentId=" + parentId;
				flag = true;
			}
			else if( searchColumnName == null || searchColumnName.equalsIgnoreCase( "" ) || search != null )
			{
				LOGGER.debug( "IN DEFAULT LIST" );
				searchClause = search;
				pageIndex = Integer.MAX_VALUE;
				flag = true;
			}
			else
			{
				LOGGER.debug( "SEARCH COLUMN VALUE " + searchColumnValue );
				LOGGER.debug( "SEARCH COLUMN NAME " + searchColumnName );
				searchClause = DOFUtils.getPaginationSearchString( doMetadata, searchColumnValue, searchColumnName );
				searchClauseURL += "&searchColumn=" + searchColumnName;
				searchClauseURL += "&searchValue=" + searchColumnValue;
				ArrayList<String[]> formActions = new ArrayList<>();
				String faction2[] =
				{ SEARCH_PATH_VALUE + qualifier, "back" };
				formActions.add( faction2 );
				request.setAttribute( "masterTable", null );
				request.setAttribute( DOMetaData.FORM_ACTIONS, formActions );
				flag = true;
			}

			// Set Search url in request for list page
			if( request.getAttribute( SEARCH_CLAUSE_URL ) == null )
			{
				request.setAttribute( SEARCH_CLAUSE_URL, searchClauseURL );
			}
			if( pagination == null )
			{
				/*
				 * If user comes fron any other action rather than paging Then set the current page index to first page
				 */
				pageIndex = 0;
			}
			LOGGER.debug( "paginationUserId[" + paginationUserId + "] pageIndex[" + pageIndex + "] searchClause["
					+ searchClause + "] classObject[" + doMetadata + "]" );

			String orderBy = null;
			String sortClauseURL = "";
			if( sortColumn != null )
			{
				orderBy = DOFUtils.getColumnDBName( doMetadata, sortColumn );
			}
			/*
			 * Prepare order by clause
			 */
			if( isDescending != null && "true".equals( isDescending ) && orderBy != null )
			{
				orderBy += " asc";
				request.setAttribute( IS_DESCENDING, "true" );
				sortClauseURL += "&isDescending=true";
				flag = true;
			}
			else if( orderBy != null )
			{
				orderBy += " desc";
				request.setAttribute( IS_DESCENDING, "false" );
				sortClauseURL += "&isDescending=false";
				flag = true;
			}

			if( sortColumn != null && !sortColumn.equalsIgnoreCase( "" ) )
			{
				request.setAttribute( SORT_COLUMN, sortColumn );
				sortClauseURL += "&sortColumn=" + sortColumn;
				flag = true;
			}

			LOGGER.debug( "Order by [" + orderBy + "]" );
			LOGGER.debug( "Flag Vlaue[ " + flag + " ]" );

			// Set Sort url in request for list page
			if( request.getAttribute( SORT_CLAUSE_URL ) == null )
			{
				request.setAttribute( SORT_CLAUSE_URL, sortClauseURL );
			}
			if( flag )
			{
				LOGGER.debug( "SearchClause[ " + searchColumnName + " ]SearchValue[ " + searchColumnValue
						+ " ]SearchClause[ " + searchClause + " ]SortColumn[ " + sortColumn + " ]" );
				paginationManager.updateRegisteredUser( paginationUserId, pageIndex, searchClause, orderBy, doMetadata,
						null );
			}
			dataList = paginationManager.getPaginationData( paginationUserId, pagination );
			LOGGER.debug( "Fetch data from Supplier Bean List size[ " + dataList.size() + " ]" );
		}

		/**
		 * Prepare list actions for list page
		 */
		// ArrayList<String[]> listActions = getListActions( searchColumnName,
		// searchColumnValue,sortColumn,isDescending ,null);
		//
		if( request.getAttribute( DATALIST ) == null )
		{
			request.setAttribute( DATALIST, dataList );
		}
		if( request.getAttribute( URL ) == null || parentId != null )
		{
			request.setAttribute( "url", "masterTable.do" );
		}

		if( parentId != null && !parentId.equalsIgnoreCase( "root" ) )
		{
			request.setAttribute( PARENT_ID, parentId );
		}

		// Navigation Tag started @
		if( parentId != null && !parentId.equalsIgnoreCase( "root" ) )
		{

			Object navObject = masterTableBean.getDataById( doMetadata, Long.valueOf( parentId ) );

			// Set in request to get in Navigation Tag
			request.setAttribute( CURRENT_NAV_OBJECT, navObject );
		}

		// // Navigation Tag work ended

		// if( request.getAttribute( PAGINATION_LINK ) == null ) {
		// request.setAttribute( PAGINATION_LINK, paginationLink );
		// }

		// String viewHelperKey = classObject.getViewHelperKey();
		// ViewHelper viewHelperObject = (ViewHelper)
		// request.getSession().getAttribute( viewHelperKey );
		//
		// if(request.getAttribute( HELP )==null)
		// {
		// request.setAttribute( HELP,
		// viewHelperObject.getPageHelpKey("listPage") );
		// String helpKey=(String) request.getAttribute(HELP);
		// LOGGER.debug( "Help Key In Master Table Setting["+helpKey+"]" );
		// }
		if( request.getAttribute( "masterTable" ) == null )
		{

			request.setAttribute( "masterTable", qualifier );
		}
		if( request.getAttribute( SEARCH_PATH ) == null )
		{
			request.setAttribute( SEARCH_PATH, searchPath );
		}
		// if(request.getAttribute( BLANK_TILES_DEF )==null)
		// {
		// request.setAttribute( BLANK_TILES_DEF, blankDef );
		// }
		LOGGER.debug( "Attibute set in the request" );
		return mapping.findForward( "list" );

	}

}
