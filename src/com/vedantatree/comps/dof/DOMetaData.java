package com.vedantatree.comps.dof;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.BeanUtils;
import org.vedantatree.utils.StringUtils;
import org.vedantatree.utils.exceptions.ApplicationException;

import com.vedantatree.comps.dof.web.WebAppConstants;
import com.vedantatree.comps.dof.web.vdo.Column;
import com.vedantatree.comps.dof.web.vdo.EditAction;
import com.vedantatree.comps.dof.web.vdo.ListAction;
import com.vedantatree.comps.dof.web.vdo.PageAction;


/**
 * DOMetadata (Dynamic Object Metadata) is core data structure for Dynamic Object Framework.
 * 
 * <p>
 * Dynamic Object Framework facilitates to define the UI Metadata for an UI. Later this metadata can be used to render
 * the UI and to perform various crud operations on UI. It enables the Dynamic Object Framework to produce the list
 * pages automatically, and all the CRUD operation pages for master tables with simple columns as of now. Further, it
 * can be extended to any level to handle even complex objects also.
 * 
 * <p>
 * Metadata is defined in XML files as of now. Please refer to SampleMetadata.xml file for details. Later, framework can
 * be enhanced to store this information in datbase also, by providing an user interface to define the metadata.
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class DOMetaData implements Serializable
{

	private static final Log	LOGGER					= LogFactory.getLog( DOMetaData.class );

	/**
	 * It is key used to store the DOMetadata in request object. It is used in AbstractAction to store the metadata
	 * while forwarding requests to other actions or methods.
	 */
	public static final String	DOMEATADATA_KEY			= "doMetaData";

	/**
	 * XML tags which contains the list of actions we need to show on 'list' page for each record.
	 * 
	 * <p>
	 * Every action in list contains
	 * <ul>
	 * <li>URL where request should be sent for this action
	 * <li>Title of the action to display on UI
	 * <li>Name of the image, which should be displayed on UI for this action
	 * <li>and many other properties. Refer to EditAction/ListAction/PageAction for more details.
	 * </ul>
	 * 
	 * @see ListAction
	 * @see PageAction
	 */
	public final static String	LIST_ACTIONS			= "listActions";

	/**
	 * XML tags which contains the list of actions we need to show on 'edit' page for a record.
	 * 
	 * <p>
	 * Every action in list contains
	 * <ul>
	 * <li>URL where request should be sent for this action
	 * <li>Title of the action to display on UI
	 * <li>Name of the image, which should be displayed on UI for this action
	 * <li>and many other properties. Refer to EditAction/ListAction/PageAction for more details.
	 * </ul>
	 * 
	 * @see EditAction
	 * @see PageAction
	 */
	public final static String	EDIT_ACTIONS			= "editActions";

	/**
	 * XML tags which contains the list of actions we need to show on 'add' page for a record.
	 * 
	 * <p>
	 * Every action in list contains
	 * <ul>
	 * <li>URL where request should be sent for this action
	 * <li>Title of the action to display on UI
	 * <li>Name of the image, which should be displayed on UI for this action
	 * <li>and many other properties. Refer to EditAction/ListAction/PageAction for more details.
	 * </ul>
	 * 
	 * @see EditAction
	 * @see PageAction
	 */
	public final static String	ADD_ACTIONS				= "addActions";

	/**
	 * XML tag which contains the List of form actions we need to show on list
	 * 
	 * @see PageAction
	 */
	public final static String	FORM_ACTIONS			= "formActions";

	/**
	 * XML tag which contains the Key to internationalized value for showing the content on list page footer
	 */
	public static final String	LIST_PAGE_FOOTER		= "list.page.footer";

	/**
	 * XML tag which contains the Key to internationalized value for showing the page description contents
	 */
	public static final String	LIST_PAGE_DESCRIPTION	= "list.page.desc";

	/**
	 * XML tag name which contains the Key to internationalized value for showing the page title contents
	 */
	public static final String	LIST_PAGE_TITLE			= "list.page.title";

	/**
	 * XML tag name which contains the Key to internationalized value for showing the content on edit page footer
	 */
	public static final String	EDIT_PAGE_FOOTER		= "edit.page.footer";

	/**
	 * XML tag name which contains the Key to internationalized value for showing the edit page description contents
	 */
	public static final String	EDIT_PAGE_DESCRIPTION	= "edit.page.desc";

	/**
	 * XML tag name which contains the Key to internationalized value for showing the edit page title contents
	 */
	public static final String	EDIT_PAGE_TITLE			= "edit.page.title";

	/**
	 * XML tag name which contains the Key to internationalized value for showing the content on add page footer
	 */
	public static final String	ADD_PAGE_FOOTER			= "add.page.footer";

	/**
	 * XML tag name which contains the Key to internationalized value for showing the add page description contents
	 */
	public static final String	ADD_PAGE_DESCRIPTION	= "add.page.desc";

	/**
	 * XML tag name which contains the Key to internationalized value for showing the add page title contents
	 */
	public static final String	ADD_PAGE_TITLE			= "add.page.title";

	/**
	 * XML tag name which contains the actino name for the current page
	 */
	public final static String	PAGE_ACTION				= "page.action";

	// Refers the attribute names to be kept in request.Basically these are the properties of DOMetaData.

	/**
	 * XML tag name which contains the Key to store the tiles definition
	 */
	public final static String	TILES_DEFINITION		= "tilesDefinition";

	/**
	 * Key to store in request - those columns on which we need to search
	 */
	public final static String	SEARCHLIST				= "searchList";

	/**
	 * list of your class variables respective to your headerlist
	 */
	public final static String	DBNAMELIST				= "dbNameList";
	/**
	 * field name which represents your class id
	 */
	public final static String	IDFIELD					= "idField";
	/**
	 * Class name
	 */
	public final static String	CLASSNAME				= "objectClassName";
	/**
	 * List of keys of columns which we need to show on list page as header .
	 */
	public final static String	HEADERLIST				= "headerList";

	private String				addPageTitleKey;
	private String				addPageDescKey;
	private String				addPageFooterKey;
	private String				addPageHelpKey;

	private String				editPageTitleKey;
	private String				editPageDescKey;
	private String				editPageFooterKey;
	private String				editPageHelpKey;

	private String				listPageTitleKey;
	private String				listPageDescKey;
	private String				listPageFooterKey;
	private String				listPageHelpKey;

	private String				pagetilesDef;

	/**
	 * Name of the parent page JS function, which should be invoked on closing of this page. It is applicable, if
	 * current page is a popup.
	 */
	private String				popupParentJSFunctionName;

	/**
	 * Path of the action/servlet which will handle the requests related to this DOMetaData
	 */
	private String				baseURL;

	/**
	 * List of list actions parsed from metadata cofiguration
	 */
	private List<PageAction>	listActions;

	/**
	 * List of form actions parsed from metadata cofiguration
	 */
	private List<PageAction>	formActions;

	/**
	 * List of edit actions parsed from metadata cofiguration
	 */
	private List<PageAction>	editActions;

	/**
	 * List of columns parsed from metadata cofiguration
	 */
	private List<Column>		columns;

	/**
	 * ID column for current page data object
	 * 
	 * <p>
	 * TODO - replace with simple string, as it is a simple string in metadata now. Not sure why we made this change.
	 * Actually keeping id column property at column level seems good by marking it id column.
	 */
	private Column				idColumn;

	/**
	 * Fully qualified class name of the main data object associated with this UI metadata
	 */
	private String				objectClassName;

	/**
	 * Name of this metadata object, stored with name property
	 */
	private String				uiMetadataName;

	/**
	 * Indicates whether we should merge the columns defined in metadata with database schema or not
	 */
	private Boolean				useORMSchema;

	/**
	 * Indicates whether we should enforce that columns in metadata and database should have same properties
	 */
	private Boolean				enforce;

	/**
	 * Fully qualified class name of view helper
	 */
	private String				viewHelperClassName;

	/**
	 * Name of the parent objects id column, used in popup
	 */
	private String				parentColumn;

	private String				queryForList;

	/**
	 * Path of the custom search panel JSP page which we may want to show on list page for custom search function.
	 * 
	 * <p>
	 * Framework read it at runtime while rendering the list page, pick the jsp page, add it to top of list. On this
	 * page submit action, developer can call their own action to collect the search parameter and then can call the
	 * get list page of abstract action to refresh the page with new search result.
	 */
	private String				customSearchPanelPath;

	DOMetaData()
	{
	}

	/**
	 * Returns the new instance of ViewHelper
	 * 
	 * Remember, we can not cache the viewhelper here as Same DOMetadata can be used for multiple views and multiple
	 * users. However viewhelper should be only view specific as these contains runtime properties of views.
	 * 
	 * @return New Instance of ViewHelper
	 * @throws ApplicationException If there is any problem
	 */
	ViewHelper createViewHelperInstance() throws ApplicationException
	{
		String viewHelperClassName = getViewHelperClassName();
		ViewHelper viewHelperInstance = null;
		if( viewHelperClassName == null )
		{
			viewHelperInstance = new ViewHelper( this );
		}
		else
		{
			viewHelperInstance = (ViewHelper) BeanUtils.newInstance( viewHelperClassName, new Class[]
			{ DOMetaData.class }, new Object[]
			{ this } );
		}
		return viewHelperInstance;
	}

	public String getAddPageDescKey()
	{
		return addPageDescKey;
	}

	public String getAddPageFooterKey()
	{
		return addPageFooterKey;
	}

	public String getAddPageHelpKey()
	{
		return addPageHelpKey;
	}

	public String getAddPageTitleKey()
	{
		return addPageTitleKey;
	}

	public String getClassName()
	{
		return objectClassName;
	}

	public List<Column> getColumns()
	{
		return columns;
	}

	public Column getColumnByDisplayName( String displayName )
	{
		StringUtils.assertQualifiedArgument( displayName );
		if( columns != null && columns.size() > 0 )
		{
			for( Object element : columns )
			{
				Column column = (Column) element;
				if( column.getDisplayName().equalsIgnoreCase( displayName ) )
				{
					return column;
				}
			}
		}
		IllegalStateException ise = new IllegalStateException(
				"Unable to find any column for specified display name[" + displayName + "]" );
		LOGGER.error( ise );
		throw ise;
	}

	public Column getColumnByDBName( String dbName )
	{
		StringUtils.assertQualifiedArgument( dbName );
		if( columns != null && columns.size() > 0 )
		{
			for( Object element : columns )
			{
				Column column = (Column) element;
				if( column.getDbName().equalsIgnoreCase( dbName ) )
				{
					return column;
				}
			}
		}
		IllegalStateException ise = new IllegalStateException(
				"Unable to find any column for specified database column name[" + dbName + "]" );
		LOGGER.error( ise );
		throw ise;
	}

	public String getColumnDBName( String displayName )
	{
		return getColumnByDisplayName( displayName ).getDbName();
	}

	public List<PageAction> getEditActions()
	{
		return editActions;
	}

	public String getEditPageDescKey()
	{
		return editPageDescKey;
	}

	public String getEditPageFooterKey()
	{
		return editPageFooterKey;
	}

	public String getEditPageHelpKey()
	{
		return editPageHelpKey;
	}

	public String getEditPageTitleKey()
	{
		return editPageTitleKey;
	}

	public List<PageAction> getFormActions()
	{
		return formActions;
	}

	public Column getIdColumn()
	{
		return idColumn;
	}

	public List<PageAction> getListActions()
	{
		return listActions;
	}

	public List getListColumnDBNames()
	{
		List<String> dbNames = new ArrayList<>();
		Column column = null;
		int columnsSize = columns.size();
		for( int i = 0; i < columnsSize; i++ )
		{
			column = columns.get( i );
			if( column.isListPageColumn() )
			{
				String dbName = column.getDbName();
				dbNames.add( dbName );
			}
		}
		return dbNames;
	}

	public List getListHeaderLabels()
	{
		List<String> headerList = new ArrayList<>();
		Column column = null;
		int columnsSize = columns.size();
		for( int i = 0; i < columnsSize; i++ )
		{
			column = columns.get( i );
			if( column.isListPageColumn() )
			{
				String headerName = column.getDisplayName();
				headerList.add( headerName );
			}
		}
		return headerList;
	}

	public String getListPageDescKey()
	{
		return listPageDescKey;
	}

	public String getListPageFooterKey()
	{
		return listPageFooterKey;
	}

	public String getListPageHelpKey()
	{
		return listPageHelpKey;
	}

	public String getListPageTitleKey()
	{
		return listPageTitleKey;
	}

	/**
	 * It returns the path of main action (url) for associated UI. This is the URL which will actually resolve all the
	 * request associated with this UI
	 * 
	 * @return URL of main action/web component for associated UI
	 */
	public String getBaseURL()
	{
		return baseURL;
	}

	public String getPagetilesDef()
	{
		return pagetilesDef;
	}

	public String getParentColumn()
	{
		return parentColumn;
	}

	public String getPopupParentJSFunctionName()
	{
		return popupParentJSFunctionName;
	}

	/**
	 * @return It returns the query set by developer for list.
	 * 
	 * @deprecated This method is deprecated and will be removed soon. Please override
	 *             'AbstractAction.getQuerySearchClauseForListImpl' instead and use the custom search clause
	 */
	@Deprecated
	public String getQueryForList()
	{
		return queryForList;
	}

	public List getSearchColumnNames()
	{
		List<String> searchColumnNames = new ArrayList<>();
		Column column = null;
		int columnsSize = columns.size();
		for( int i = 0; i < columnsSize; i++ )
		{
			column = columns.get( i );
			if( column.isSearchable() )
			{
				String searchColumnName = column.getDisplayName();
				searchColumnNames.add( searchColumnName );
			}
		}
		return searchColumnNames;
	}

	public String getUIMetadataName()
	{
		// return getClassName();
		return uiMetadataName;
	}

	public String getViewHelperClassName()
	{
		return viewHelperClassName;
	}

	public String getViewHelperKey()
	{
		return getUIMetadataName() + "_" + WebAppConstants.VIEW_HELPER;
	}

	public void setAddPageDescKey( String addPageDescKey )
	{
		this.addPageDescKey = addPageDescKey;
	}

	public void setAddPageFooterKey( String addPageFooterKey )
	{
		this.addPageFooterKey = addPageFooterKey;
	}

	public void setAddPageHelpKey( String addPageHelpKey )
	{
		this.addPageHelpKey = addPageHelpKey;
	}

	public void setAddPageTitleKey( String addPageTitleKey )
	{
		this.addPageTitleKey = addPageTitleKey;
	}

	public void setObjectClassName( String className )
	{
		this.objectClassName = className;
	}

	public void setColumns( List<Column> columns )
	{
		this.columns = columns;
	}

	public void setEditActions( List<PageAction> editActions )
	{
		this.editActions = editActions;
	}

	public void setEditPageDescKey( String editPageDescKey )
	{
		this.editPageDescKey = editPageDescKey;
	}

	public void setEditPageFooterKey( String editPageFooterKey )
	{
		this.editPageFooterKey = editPageFooterKey;
	}

	public void setEditPageHelpKey( String editPageHelpKey )
	{
		this.editPageHelpKey = editPageHelpKey;
	}

	public void setEditPageTitleKey( String editPageTitleKey )
	{
		this.editPageTitleKey = editPageTitleKey;
	}

	public void setEnforce( boolean enforce )
	{
		this.enforce = enforce;
	}

	public void setFormActions( List<PageAction> formActions )
	{
		this.formActions = formActions;
	}

	public void setIdColumn( Column idColumnName )
	{
		this.idColumn = idColumnName;
	}

	public void setListActions( List<PageAction> listActions )
	{
		this.listActions = listActions;
	}

	public void setListPageDescKey( String listPageDescKey )
	{
		this.listPageDescKey = listPageDescKey;
	}

	public void setListPageFooterKey( String listPageFooterKey )
	{
		this.listPageFooterKey = listPageFooterKey;
	}

	public void setListPageHelpKey( String listPageHelpKey )
	{
		this.listPageHelpKey = listPageHelpKey;
	}

	public void setListPageTitleKey( String listPageTitleKey )
	{
		this.listPageTitleKey = listPageTitleKey;
	}

	public void setBaseURL( String pageAction )
	{
		this.baseURL = pageAction;
	}

	public void setPagetilesDef( String pagetilesDef )
	{
		this.pagetilesDef = pagetilesDef;
	}

	public void setParentColumn( String parentColumn )
	{
		this.parentColumn = parentColumn;
	}

	public void setPopupParentJSFunctionName( String popupParentJSFunctionName )
	{
		this.popupParentJSFunctionName = popupParentJSFunctionName;
	}

	/**
	 * It is used to set any query clause for data fetching at server in data provider.
	 * 
	 * @param queryForList query
	 * @deprecated This method is deprecated and will be removed soon. Please override
	 *             'AbstractAction.getQuerySearchClauseForListImpl' instead and use the custom search clause
	 */
	@Deprecated
	public void setQueryForList( String queryForList )
	{
		this.queryForList = queryForList;
	}

	// TODO: >> set me from xmldoschema manager
	void setUIMetadataName( String uiMetadataName )
	{
		this.uiMetadataName = uiMetadataName;
	}

	public void setUseORMSchema( boolean useORMSchema )
	{
		this.useORMSchema = useORMSchema;
	}

	public void setViewHelperClassName( String viewHelperClassName )
	{
		this.viewHelperClassName = viewHelperClassName;
	}

	public boolean shouldEnforce()
	{
		return enforce;
	}

	public boolean shouldUseORMSchema()
	{
		return useORMSchema;
	}

	public String getCustomSearchPanelPath()
	{
		return customSearchPanelPath;
	}

	public void setCustomSearchPanelPath( String customSearchPanelPath )
	{
		this.customSearchPanelPath = customSearchPanelPath;
	}

	@Override
	public String toString()
	{
		return "DOMetadata@" + hashCode() + ": columns[" + ( columns == null ? 0 : columns.size() ) + "] idColumn["
				+ idColumn + "] objectClassName[" + objectClassName + "] parentColumn[" + parentColumn
				+ "] parentJSMethod[" + popupParentJSFunctionName + "] listActions["
				+ ( listActions == null ? 0 : listActions.size() ) + "] formActions["
				+ ( formActions == null ? 0 : formActions.size() ) + "] baseURL[" + baseURL + "] editActions["
				+ ( editActions == null ? 0 : editActions.size() ) + "]";
	}

}
