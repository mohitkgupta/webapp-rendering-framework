package com.vedantatree.comps.dof;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.comps.securitymanager.model.User;
import org.vedantatree.utils.BeanUtils;
import org.vedantatree.utils.DateUtils;
import org.vedantatree.utils.Utilities;
import org.vedantatree.utils.exceptions.ComponentException;
import org.vedantatree.utils.exceptions.IErrorCodes;

import com.vedantatree.comps.dof.web.WebAppConstants;
import com.vedantatree.comps.dof.web.vdo.Column;
import com.vedantatree.comps.dof.web.vdo.EditAction;
import com.vedantatree.comps.dof.web.vdo.ListAction;
import com.vedantatree.comps.dof.web.vdo.PageAction;


/**
 * View Helper is an object which helps the view for rendering purpose.
 * 
 * <p>
 * It comes in existence to remove the processing logic from JSP page or tags. This is a helper for View and perform all
 * processing which view needs for rendering.
 * 
 * <p>
 * Every view can specify a separate view helper class in metadata. Framework will load the desired view helper class
 * and then further uses this to get various rendering and operation time properties for view. User should override this
 * default implementation for their views, if they want to customize any property at runtime, like visibility of actions
 * etc.
 * 
 * <p>
 * Every framework component, involved in rendering the UI, must communicate to view helper for getting any view related
 * metadata information, it should never be directly to metadata. It facilitates the developer to override the metadata
 * information at runtime also.
 * 
 * <p>
 * TODO - Remove the logic to store and get the metadata information from request. It should be handled by overriding
 * the view helper, wherever required. This is not removed yet, because some of the legacy code is using this. We are
 * deprecating this.
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class ViewHelper
{

	private static final Log	LOGGER			= LogFactory.getLog( ViewHelper.class );
	private final String		SHOW_ID			= "showId";
	private final static String	USER			= "user";
	private final static String	ADD_PAGE		= "addPage";
	private final static String	EDIT_PAGE		= "editPage";
	private final static String	LIST_PAGE		= "listPage";

	/**
	 * HTTP Request object for current web request. View helper may need it to retrieve various properties.
	 * 
	 * <p>
	 * It is set and reset from AbstractAction.
	 */
	private HttpServletRequest	request;

	/**
	 * DOMetadata object for current view. It contains the metadata information for current view. Currently metadata
	 * is being defined with XML files for each view. AbstractAction loads the right metadata linked with each UI,
	 * (based on metadata identifier given by Implementing action) and make it available for other components.
	 */
	private DOMetaData			doMetadata;

	/**
	 * Data for current row, if we are rendering the list page.
	 * 
	 * Generally pagination manager returns the list of data,
	 * which will be iterated by list page for rendering each row. The current row index and associated data is being
	 * set by list page to view helper.
	 * 
	 * So this property contains the data for current row (which is set by list page generation logic before rendering
	 * of each row). It can be an object with properties for each data column, or an array of values for each data
	 * column.
	 */
	private Object				currentRowData;

	/**
	 * Index of current column while rendering a single record from the list of records.
	 * 
	 * List page rendering logic iterate over the current row data and paint columns one by one. This property is set
	 * from that iteration with each progress.
	 */
	private int					currentColumnIndex;

	/**
	 * Serial number of records. It is used to print the serial number or rows on list page.
	 */
	private int					serialNumber	= 0;

	/**
	 * Metadata object for view which contains information for whole view for current session
	 * 
	 * DOMetadata is to keep the metadata information for one View (UI/JSP page) shown on one user action. ViewMetadata
	 * contains the metadata for whole Web UI which is shown to user at any point of time including headers, footers,
	 * menus etc.
	 */
	private ViewMetaData		viewMetaData;

	/**
	 * @deprecated This constructor will be removed soon. So please don't use it.
	 */
	@Deprecated
	public ViewHelper()
	{
		// TODO: It should be made private as it is not in use now
		UnsupportedOperationException uoe = new UnsupportedOperationException(
				"Viewhelper new instance should be created using constructor with DoMetadata." );
		LOGGER.error( uoe );
		throw uoe;
	}

	/**
	 * Constructor to create the view helper. It is always created from DOMetadata with a reference of DOMetadata.
	 * 
	 * @param doMetadata metadata object for current UI
	 */
	public ViewHelper( DOMetaData doMetadata )
	{
		setDoMetadata( doMetadata );
	}

	/**
	 * This method will provide all the editActions as addActions for which in XML file attribute addAction value is set
	 * as true and addActions are valid according to current user and other validation criteria that is defined in
	 * override methods
	 * 
	 * @return all valid addActions
	 * @throws ComponentException
	 */
	public final List getAddActions() throws ComponentException
	{
		/*
		 * Give the chance to users by checking edit actinos from request. If any developer wants to override the
		 * default actions in dometadata, she can set the edit actions in request
		 * 
		 * TODO: Provision to update the edit actions using request should be removed. If anyone want to provide custom
		 * actions, she should override view helper.
		 */
		List editActions = (ArrayList) request.getAttribute( DOMetaData.EDIT_ACTIONS );
		LOGGER.debug( "editActions-request[" + editActions + "]" );

		// if edit actions are not found with request, let us get it from dometadata
		if( editActions == null )
		{
			editActions = doMetadata.getEditActions();
			LOGGER.debug( "editActions-DOMetadata[" + editActions + "]" );
		}
		// if still there is no action so it return null
		if( editActions == null )
		{
			LOGGER.debug( "No Actions Found So return null" );
			return null;
		}

		// TODO addActions can be maintained separate in DOMetadata
		List addActions = null;

		// TODO : @ Tyagi : Remove this case later
		if( editActions.get( 0 ).getClass().isArray() )
		{
			if( true )
			{
				UnsupportedOperationException uoe = new UnsupportedOperationException(
						"Earlier we were using array of action information, however this approach has been changed now. Please use Array of Actions objects" );
				LOGGER.error( uoe );
				throw uoe;
			}
			if( addActions == null )
			{
				addActions = new ArrayList();
			}
			// Because we can't identify the add action in case of array
			addActions.addAll( editActions );
		}
		else
		{
			for( Iterator iter = editActions.iterator(); iter.hasNext(); )
			{

				EditAction editAction = (EditAction) iter.next();
				if( editAction.isAddAction() )
				{
					if( addActions == null )
					{
						addActions = new ArrayList();
					}
					addActions.add( editAction );
				}
			}
		}
		LOGGER.debug( "addActions[" + addActions.size() );
		return getValidActions( addActions, DOMetaData.ADD_ACTIONS );
	}

	/**
	 * Return the class name of the main object associated with this view helper. This object will be rendered on UI.
	 * 
	 * @return Name of the class for main data object for view
	 */
	public final String getClassName()
	{
		// give a chance to developer to set class name, otherwise we shall get it from doMetadata
		String className = (String) request.getAttribute( DOMetaData.CLASSNAME );
		if( className == null )
		{
			className = doMetadata.getClassName();
		}
		return className;
	}

	/**
	 * It returns the data for current column of list page, which we are rendering right now. Actually the view set the
	 * current column index in view helper and then ask for the data of that column.
	 * 
	 * @return data for current column
	 * @throws ComponentException if there is any problem
	 */
	public final Object getCurrentColumnData() throws ComponentException
	{

		/*
		 * Get the current index Check if the current row data is array currentColumnData = get value for current index
		 * from array Else get Property db name from dometadata for current index currentColumnData = Use bean utils and
		 * get the property value using current row data and dbname return current columnData
		 */
		Object dataValue = null;

		if( currentRowData.getClass().isArray() )
		{

			Object[] dataValues = (Object[]) currentRowData;

			dataValue = dataValues[currentColumnIndex];

			if( !isShowIdExist() )
			{

				dataValue = dataValues[currentColumnIndex + 1];
			}

			if( dataValue != null )
			{
				if( dataValue instanceof Timestamp )
				{
					Date data = (Date) dataValue;
					dataValue = DateUtils.convertDateToString( data );
				}
			}
			else
			{
				dataValue = new String( "&nbsp;" );
			}
		}
		else
		{
			List dbNameList = getDBNameList();
			if( dbNameList == null || dbNameList.size() < ( currentColumnIndex + 1 ) )
			{
				ComponentException ce = new ComponentException( IErrorCodes.ILLEGAL_STATE_ERROR,
						"Column data property is not found. It seems like . columnIndex[" + ( currentColumnIndex + 1 )
								+ "] dataPropertyNames[" + dbNameList + "]" );
				LOGGER.error( ce );
				throw ce;
			}

			try
			{
				dataValue = BeanUtils.getPropertyValue( currentRowData, getDBNameList().get( currentColumnIndex ) );
			}
			catch( Exception e )
			{
				ComponentException ce = new ComponentException( IErrorCodes.COMPONENT_INITIALIZATION_ERROR,
						"Exception while getting propertyValue from BeanUtils", e );
				LOGGER.debug( ce );
				throw ce;
			}
		}

		return getCurrentColumnDataImpl( dataValue );
	}

	/**
	 * This method can be override if any extended class want to change the data representation for current column
	 * 
	 * @param currentColumnData The original data for current row+column
	 * @return Modified data for current row+column
	 */
	protected Object getCurrentColumnDataImpl( Object currentColumnData )
	{
		return currentColumnData;
	}

	public final Object getCurrentDBName()
	{

		return getDBNameList().get( currentColumnIndex );
	}

	public final int getCurrentIndex()
	{
		return currentColumnIndex;
	}

	public final Column getCurrentColumn()
	{
		return doMetadata.getColumns().get( getCurrentIndex() );
	}

	public final Object getCurrentRowData()
	{
		return currentRowData;
	}

	public final User getCurrentUser()
	{
		LOGGER.debug( "Got request[" + request + "] request.getSession()[" + request.getSession()
				+ "] request.getSession().getAttribute( USER )[" + request.getSession().getAttribute( USER ) + "]" );

		return (User) request.getSession().getAttribute( USER );
	}

	public final List<String> getDBNameList()
	{
		// give a chance to developer to set dbnames list in request
		List<String> dbNameList = (List<String>) request.getAttribute( DOMetaData.DBNAMELIST );
		if( dbNameList == null || dbNameList.size() == 0 )
		{
			dbNameList = doMetadata.getListColumnDBNames();
		}

		return dbNameList;
	}

	public final DOMetaData getDoMetadata()
	{
		return doMetadata;
	}

	/**
	 * This method will provide all the editActions that are valid according to current user and other validation
	 * criteria that is defined in override methods
	 * 
	 * @return all valid editActions
	 * @throws ComponentException
	 */
	public final List getEditActions() throws ComponentException
	{
		List editActions = (ArrayList) request.getAttribute( DOMetaData.EDIT_ACTIONS );
		if( editActions == null )
		{
			editActions = doMetadata.getEditActions();
			LOGGER.debug( "Got doMetadate [" + doMetadata + "] and editActions [" + editActions + "]" );
		}
		return getValidActions( editActions, DOMetaData.EDIT_ACTIONS );
	}

	/**
	 * This method will provide all the formActions that are valid according to current user and other validation
	 * criteria that is defined in override methods
	 * 
	 * @return all valid formActions
	 * @throws ComponentException
	 */
	public final List getFormActions() throws ComponentException
	{
		List formActions = (ArrayList) request.getAttribute( DOMetaData.FORM_ACTIONS );
		if( formActions == null )
		{
			formActions = doMetadata.getFormActions();
			LOGGER.debug( "doMetadate [" + doMetadata + "] and formActions [" + formActions + "]" );
		}
		return getValidActions( formActions, DOMetaData.FORM_ACTIONS );
	}

	public final List getHeaderList()
	{
		List headerList = (List) request.getAttribute( DOMetaData.HEADERLIST );
		if( headerList == null || headerList.size() <= 0 )
		{
			headerList = doMetadata.getListHeaderLabels();
		}
		return getHeaderListImpl( headerList );
	}

	/**
	 * This method can be overriden to modify the list of headers names. It is useful when any developer wants to modify
	 * the header names based on some business logic.
	 * 
	 * @param headerList list of the header names
	 * @return modified list of header names
	 */
	protected List getHeaderListImpl( List headerList )
	{
		return headerList;
	}

	public final List<Column> getColumns()
	{
		List<Column> columns = doMetadata.getColumns();
		return getColumnsImpl( columns );
	}

	/**
	 * This method can be override by developers to make any change to page columns properties based on some business
	 * logic.
	 * 
	 * Please consider that any change done to the column will be permanant in current user session.
	 * 
	 * @param columns list of columns as per defined DOMetadata
	 * @return list of modified columns
	 */
	protected List<Column> getColumnsImpl( List<Column> columns )
	{
		return columns;
	}

	public final List<Column> getListColumns()
	{
		List columns = getColumns();
		List listColumns = new ArrayList();
		for( Iterator iterator = columns.iterator(); iterator.hasNext(); )
		{
			Column column = (Column) iterator.next();
			if( column.isListPageColumn() )
			{
				listColumns.add( column );
			}
		}
		return listColumns;
	}

	public final Object getIDDataValue() throws ComponentException
	{

		String idField = (String) request.getAttribute( "idField" );
		if( idField == null )
		{
			idField = doMetadata.getIdColumn().getDbName();
		}

		LOGGER.debug( ( new StringBuilder() ).append( "Got idField [" ).append( idField ).append( "]" ).toString() );
		Object dataValue;

		if( currentRowData.getClass().isArray() )
		{
			Object dataValues[] = (Object[]) currentRowData;
			dataValue = dataValues[0];
		}
		else
		{
			try
			{
				dataValue = BeanUtils.getPropertyValue( currentRowData, idField );
			}
			catch( Exception e )
			{
				LOGGER.error( "Exception while getting propertyValue from BeanUtils", e );
				throw new ComponentException( IErrorCodes.COMPONENT_INITIALIZATION_ERROR,
						"Exception while getting propertyValue from BeanUtils", e );
			}
		}
		return dataValue;
	}

	public String getJavaScriptFunction()
	{
		String popupParentJSFunctionName = (String) request.getAttribute( WebAppConstants.JAVASCRIPT_FUNCTION );
		if( popupParentJSFunctionName == null )
		{
			popupParentJSFunctionName = doMetadata.getPopupParentJSFunctionName();
		}
		return popupParentJSFunctionName;
	}

	/**
	 * This method will provide all the listActions that are valid according to current user role and rights and other
	 * validation criteria that may be defined in overridden methods
	 * 
	 * @return all valid listActions
	 * @throws ComponentException
	 * @TODO change the name to getValidListActions
	 */
	public final List getListActions() throws ComponentException
	{
		List listActions = (ArrayList) request.getAttribute( DOMetaData.LIST_ACTIONS );
		if( listActions == null )
		{
			listActions = doMetadata.getListActions();
			LOGGER.debug( "Got doMetadate [" + doMetadata + "] and listActions [" + listActions + "]" );
		}
		return getValidActions( listActions, DOMetaData.LIST_ACTIONS );
	}

	/**
	 * This method tells whether there is any list action exist or not. It does not check whether current user has
	 * rights on action or not. It simply checks for existence.
	 * 
	 * @return true if any list action exists without check the user roles / rights etc, false if there is no list
	 *         action
	 */
	public final boolean hasListActions()
	{

		List listActions = (ArrayList) request.getAttribute( DOMetaData.LIST_ACTIONS );

		LOGGER.debug( "List Actions in View Helper " + listActions );

		if( listActions == null )
		{
			listActions = doMetadata.getListActions();
		}
		if( listActions == null || listActions.size() == 0 )
		{
			return false;
		}
		return true;
	}

	/**
	 * This method tells us whether list actions stored for current UI will have any action matching with given
	 * orientation. This method does not consider any rights for the list actions. So it go through with all the actions
	 * available without applying roles and rights and return true/false based on any action found for given orientation
	 * or not. It is mainly used while rendering the list page, where we need to show the actions column depending upon
	 * whether actions present or not. We also show the action column even if current user does not rights on any of the
	 * action, in that case, it shows like 'no action available'
	 * 
	 * @param orientation tells us the orientation of action, left or right
	 * @return true if action matching with given orientation found, false otherwise
	 */
	public final boolean hasListActionsWithoutRightsConsideration( String orientation )
	{

		List listActions = (ArrayList) request.getAttribute( DOMetaData.LIST_ACTIONS );

		LOGGER.debug( "List Actions in View Helper " + listActions );

		if( listActions == null )
		{
			listActions = doMetadata.getListActions();
		}
		if( listActions == null || listActions.size() == 0 )
		{
			return false;
		}
		if( ListAction.LIST_ACTION_ORIENTATION_ANY.equalsIgnoreCase( orientation ) )
		{
			return true;
		}
		for( Iterator<ListAction> iterator = listActions.iterator(); iterator.hasNext(); )
		{
			ListAction listAction = iterator.next();
			if( listAction.getOrientation().equalsIgnoreCase( orientation ) )
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * It returns the next serial number for data rows on list page. It is used from list page rendering procedure to
	 * render the sequence number of the rows on UI.
	 * 
	 * @return next serial number for data row to be rendered as sequence number of rows on UI
	 */
	public final int getNextSerialNumber()
	{
		return ++serialNumber;
	}

	/**
	 * This method will return the description key according to type of page - Add/Edit/List by default : It will return
	 * description key for list page
	 * 
	 * @param pageType - Type of page for which you need description key
	 * @return Description key
	 */
	public String getPageDescKey( String pageType )
	{

		if( ADD_PAGE.equalsIgnoreCase( pageType ) )
		{
			return doMetadata.getAddPageDescKey();
		}
		else if( EDIT_PAGE.equalsIgnoreCase( pageType ) )
		{
			return doMetadata.getEditPageDescKey();
		}
		else
		{
			if( request.getAttribute( WebAppConstants.SUBTITLE ) != null )
			{
				return (String) request.getAttribute( WebAppConstants.SUBTITLE );
			}
			return doMetadata.getListPageDescKey();
		}
	}

	/**
	 * This method will return the help key according to type of page - Add/Edit/List by default : It will return help
	 * key for list page
	 * 
	 * @param pageType - Type of page for which you need help key
	 * @return Help key
	 */
	public String getPageHelpKey( String pageType )
	{
		if( ADD_PAGE.equalsIgnoreCase( pageType ) )
		{
			LOGGER.debug( "getPageHelpKey:ADD_PAGE" );
			return doMetadata.getAddPageHelpKey();
		}
		else if( EDIT_PAGE.equalsIgnoreCase( pageType ) )
		{
			LOGGER.debug( "getPageHelpKey:EDIT_PAGE" );
			return doMetadata.getEditPageHelpKey();
		}
		else
		{
			LOGGER.debug( "getPageHelpKey" + request.getAttribute( WebAppConstants.HELP ) );
			if( request.getAttribute( WebAppConstants.HELP ) != null )
			{
				LOGGER.debug( "getPageHelpKey:WebAppConstants.HELP" );
				return (String) request.getAttribute( WebAppConstants.HELP );
			}
			LOGGER.debug( "lIST PAGE HELP KEY[" + doMetadata.getListPageHelpKey() );
			return doMetadata.getListPageHelpKey();
		}
	}

	/**
	 * This method will return Tiles Definition.
	 * 
	 * if tilesdef is found in request then return tiledef else find tilesdef according to dometadata and then return
	 * 
	 * We are taking tilesdef from request while we need to override default tilesdef means we want to show some records
	 * in our module that are coming from other module.
	 * 
	 * @return
	 */
	public String getPageTilesDef()
	{

		String tilesDef = (String) request.getAttribute( DOMetaData.TILES_DEFINITION );
		if( tilesDef == null )
		{
			tilesDef = doMetadata.getPagetilesDef();
		}

		return tilesDef;
	}

	/**
	 * This method will return the title key according to type of page - Add/Edit/List by default : It will return title
	 * key for list page
	 * 
	 * @param pageType - Type of page for which you need title key
	 * @return Title key
	 */
	public String getPageTitleKey( String pageType )
	{

		if( ADD_PAGE.equalsIgnoreCase( pageType ) )
		{
			return doMetadata.getAddPageTitleKey();
		}
		else if( EDIT_PAGE.equalsIgnoreCase( pageType ) )
		{
			return doMetadata.getEditPageTitleKey();
		}
		else
		{
			if( request.getAttribute( WebAppConstants.TITLE ) != null )
			{
				return (String) request.getAttribute( WebAppConstants.TITLE );
			}
			return doMetadata.getListPageTitleKey();
		}
	}

	public HttpServletRequest getRequest()
	{
		return request;
	}

	public final List getSearchColumnList()
	{
		List searchColumnList = (List) request.getAttribute( DOMetaData.SEARCHLIST );
		if( searchColumnList == null || searchColumnList.size() <= 0 )
		{
			searchColumnList = doMetadata.getSearchColumnNames();
		}
		return searchColumnList;
	}

	public String getSelectionType()
	{

		return (String) request.getAttribute( WebAppConstants.SELECTION_MODE );
	}

	/**
	 * It returns the web page specific URL for a action URL. This method is generally called from Button Tag for
	 * rendering the actions on UI.
	 * 
	 * This web page URL is required so that we can show encrypted URL on UI and moreover a URL which framework want to
	 * show on UI. Here framework can also manage the starting of URL like if it wants to make it relative to context
	 * base etc.
	 * 
	 * @param actionURL URL of an action
	 * @return Encrypted web page URL
	 */
	public String getWebPageURL( String actionURL )
	{
		org.vedantatree.utils.StringUtils.assertQualifiedArgument( actionURL );
		ViewMetaData viewMetaData = (ViewMetaData) request.getSession().getAttribute( "viewmetadata" );
		return viewMetaData.getWebPageURL( actionURL );
	}

	/*
	 * These methods are to fulfill all the requirements of list page
	 */

	private List getValidActions( List actions, String actionType ) throws ComponentException
	{
		/*
		 * Ask current user from session Get roles for current action from dometadata Use StringUtils and get String
		 * Array from comma separated roles string apply loop on array call user.isinrole for current role if any of the
		 * loop return true from user.inrole return true
		 */

		LOGGER.debug( "Get Valid Action  action [ " + actions + " ]" );

		if( actions == null )
		{
			return null;
		}
		LOGGER.debug( "Size of actions are [ " + actions.size() + " ]" );
		User currentUser = getCurrentUser();

		PageAction pageAction = null;
		PageAction cloneAction = null;
		List validActions = null;

		if( actions != null && actions.size() > 0 && actions.get( 0 ).getClass().isArray() )
		{
			if( true )
			{
				ComponentException ce = new ComponentException( IErrorCodes.ILLEGAL_STATE_ERROR,
						"Flow should not come to this legacy code block. This code is here just for legacy Procurement application, which is not in use right now." );
				LOGGER.fatal( ce );
				throw ce;
			}
			// TODO : remove it after everybody shift the actions to XML
			for( int i = 0; i < actions.size(); i++ )
			{
				if( isValidActionImpl( currentColumnIndex ) )
				{
					if( validActions == null )
					{
						validActions = new ArrayList();
					}
					validActions.add( actions.get( i ) );
				}
			}
			LOGGER.debug( "Got validAction in case Array [" + validActions + "]" );
			return validActions;
		}

		ViewMetaData viewMetaData = (ViewMetaData) request.getSession().getAttribute( "viewmetadata" );
		List validAct = new ArrayList();
		DOMetaData _tmp = doMetadata;

		if( DOMetaData.LIST_ACTIONS.equals( actionType ) )
		{
			List listAct = doMetadata.getListActions();
			LOGGER.debug( "listActions[" + listAct + "]" );

			if( listAct != null && listAct.size() > 0 )
			{
				for( int i = 0; i < listAct.size(); i++ )
				{
					ListAction listAction = (ListAction) listAct.get( i );
					LOGGER.debug( ( new StringBuilder() ).append( "Got listAction with URL[" )
							.append( listAction.getUrl() ).append( "] and Display Name[" )
							.append( listAction.getDisplayData().getTextKey() ).append( "]" ).toString() );
					if( viewMetaData.isAuthroizedAction( listAction.getUrl() ) )
						validAct.add( listAction );
				}
			}
		}
		else if( "editActions".equals( actionType ) )
		{
			List editAct = doMetadata.getEditActions();
			LOGGER.debug( "editActions[" + editAct + "]" );

			if( editAct != null && editAct.size() > 0 )
			{
				for( int i = 0; i < editAct.size(); i++ )
				{
					EditAction editAction = (EditAction) editAct.get( i );
					LOGGER.debug( ( new StringBuilder() ).append( "Got editAction with URL[" )
							.append( editAction.getUrl() ).append( "] and Display Name[" )
							.append( editAction.getDisplayData().getTextKey() ).append( "]" ).toString() );
					if( editAction.getUrl() != null && editAction.getUrl().equals( "default" )
							|| viewMetaData.isAuthroizedAction( editAction.getUrl() ) )
						validAct.add( editAction );
				}
			}
		}
		// TODO review this case.. add actions are not given in samplemetadata.xml
		else if( "addActions".equals( actionType ) )
		{
			List addAct = doMetadata.getEditActions();
			LOGGER.debug( "addActions[" + addAct + "]" );

			if( addAct != null && addAct.size() > 0 )
			{
				for( int i = 0; i < addAct.size(); i++ )
				{
					EditAction addAction = (EditAction) addAct.get( i );
					LOGGER.debug( ( new StringBuilder() ).append( "Got addAction with URL[" )
							.append( addAction.getUrl() ).append( "] and Display Name[" )
							.append( addAction.getDisplayData().getTextKey() ).append( "]" ).toString() );
					if( addAction.isAddAction() )
					{
						LOGGER.debug( ( new StringBuilder() ).append( "when encounter Add Action:URL[" )
								.append( addAction.getUrl() ).append( "]" ).toString() );
						if( addAction.getUrl() != null && addAction.getUrl().equals( "default" )
								|| viewMetaData.isAuthroizedAction( addAction.getUrl() ) )
							validAct.add( addAction );
					}
				}
			}
		}
		else if( "formActions".equals( actionType ) )
		{
			List formAct = doMetadata.getFormActions();
			LOGGER.debug( "listActions[" + formAct + "]" );

			if( formAct != null && formAct.size() > 0 )
			{
				for( int i = 0; i < formAct.size(); i++ )
				{
					LOGGER.debug( ( new StringBuilder() ).append( "formAct [" ).append( formAct.size() ).append( "]" )
							.toString() );
					PageAction formAction = (PageAction) formAct.get( i );
					LOGGER.debug( ( new StringBuilder() ).append( "Got formAction with URL[" )
							.append( formAction.getUrl() ).append( "] and Display Name[" )
							.append( formAction.getDisplayData().getTextKey() ).append( "]" ).toString() );
					if( viewMetaData.isAuthroizedAction( formAction.getUrl() ) )
						validAct.add( formAction );
				}
			}
		}

		if( validAct != null && validAct.size() > 0 )
		{
			LOGGER.debug( ( new StringBuilder() ).append( "Size of validAct:Finally [" ).append( validAct.size() )
					.append( "]" ).toString() );
			for( int i = 0; i < validAct.size(); i++ )
			{
				PageAction currAct = (PageAction) validAct.get( i );
				PageAction currActClone;
				try
				{
					currActClone = (PageAction) currAct.clone();
				}
				catch( CloneNotSupportedException exc )
				{
					LOGGER.error( ( new StringBuilder() ).append( "Exception while creating clone for Actions[" )
							.append( exc.getMessage() ).append( "]" ).toString(), exc );
					throw new ComponentException( 9, "Exception while creating clone for Actions", exc );
				}
				if( !isValidActionImpl( currActClone, actionType, request ) )
				{
					// please refer to documentation of enabled property in PageAction for details of setting enabled
					// false here. List page will show the action as disabled based on this property.

					// earlier we have thought to show the icon however disabled these or to show a message that it is
					// not available. However later we realized that it is not in sync with user expectations. So
					// reversing it back to hiding the actions.
					// currActClone.setEnabled( false );
					LOGGER.debug( "current action is not valid as per extended view helper[" + currActClone + "]" );
					continue;
				}
				if( validActions == null )
					validActions = new ArrayList();
				validActions.add( currActClone );
			}

		}
		LOGGER.debug( ( new StringBuilder() ).append( "Got validAction in case of PageAction [" ).append( validActions )
				.append( "]" ).toString() );
		return validActions;
	}

	public final ViewMetaData getViewMetaData()
	{
		return viewMetaData;
	}

	public final boolean isShowIdExist()
	{
		Boolean showId = (Boolean) request.getAttribute( SHOW_ID );
		return showId != null && showId.booleanValue();
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	private boolean isValidActionImpl( int index )
	{
		return true;
	}

	/**
	 * 
	 * @param pageAction
	 * @param actionType TODO
	 * @param request TODO
	 * @return
	 */
	protected boolean isValidActionImpl( PageAction pageAction, String actionType, HttpServletRequest request )
	{
		return true;
	}

	/**
	 * This method is to reset viewHelper object.
	 * 
	 * Need : When viewHelper object found in session then we need to reset viewHelper object once and then set new
	 * information according to new request etc.
	 * 
	 */
	public void reset()
	{

		this.currentColumnIndex = 0;
		this.currentRowData = null;
		this.request = null;
		this.serialNumber = 0;
	}

	public final void setCurrentColumnIndex( int currentIndex )
	{
		if( currentIndex < 0 )
		{
			LOGGER.error( "Current row index must not be less than zero" );
			throw new IllegalArgumentException( "Current row index must not be less than zero" );
		}
		this.currentColumnIndex = currentIndex;
	}

	public final void setCurrentRowData( Object currentRowData )
	{
		LOGGER.debug( "entering : setCurrentRowData . currentRowData[" + currentRowData + "]" );
		if( currentRowData == null )
		{
			LOGGER.error( "Current row data must not be null" );
			throw new IllegalArgumentException( "Current row data must not be null" );
		}
		this.currentRowData = currentRowData;
	}

	public final void setDoMetadata( DOMetaData doMetadata )
	{
		Utilities.assertNotNullArgument( doMetadata, "metadata" );
		this.doMetadata = doMetadata;
	}

	public void setRequest( HttpServletRequest request )
	{
		this.request = request;
		setViewMetaData( (ViewMetaData) request.getSession().getAttribute( "viewmetadata" ) );
	}

	private void setViewMetaData( ViewMetaData viewMetaData )
	{
		this.viewMetaData = viewMetaData;
	}

}
