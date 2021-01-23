package com.vedantatree.comps.dof;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.ResourceFinder;
import org.vedantatree.utils.StringUtils;
import org.vedantatree.utils.config.ConfigurationManager;
import org.vedantatree.utils.exceptions.ApplicationException;
import org.vedantatree.utils.exceptions.ComponentException;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;
import org.vedantatree.utils.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.vedantatree.comps.dof.common.bdo.DisplayData;
import com.vedantatree.comps.dof.web.vdo.Column;
import com.vedantatree.comps.dof.web.vdo.EditAction;
import com.vedantatree.comps.dof.web.vdo.ListAction;
import com.vedantatree.comps.dof.web.vdo.PageAction;


/**
 * The XMLSchemaManager is responsible for parsing the XML files containing the metadata information for various Data
 * Objects and UI, and create the Metadata. This metadata is used to render the UI and also to faciliates the operation
 * logic for CRUD operations.
 * 
 * @author Sarthak Mishra
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */

class XMLSchemaManager
{

	private static Log				LOGGER						= LogFactory.getLog( XMLSchemaManager.class );

	/*
	 * Following final variables are the tags used as key-words in XMLSchemaManager . These variables are used for XML
	 * parsing in this class.
	 */
	private static final String		TABLE						= "object";
	private static final String		TABLE_NAME					= "name";
	private static final String		OBJECT_CLASS				= "object-class";
	private static final String		ENFORCE						= "enforce";
	private static final String		VIEW_ALL					= "viewAll";
	private static final String		EDIT_ALL					= "editAll";

	private static final String		COLUMN_DB_NAME				= "db-name";
	private static final String		COLUMN_DISPLAY_NAME			= "display-name";
	private static final String		COLUMN						= "property";
	private static final String		VIEW						= "view";
	private static final String		EDIT						= "edit";
	private static final String		VALIDATION_RULES			= "validation-rule";
	private static final String		COLUMN_VIEW_LINK			= "view-link";
	private static final String		COLUMN_SEARCHABLE			= "searchable";
	private static final String		COLUMN_SORTABLE				= "sortable";
	private static final String		COLUMN_DYNAMIC				= "dynamic";
	private static final String		UI_VIEW_HELPER				= "ui-view-helper";
	private static final String		CUSTOM_SEARCH_PANEL			= "custom-search-panel";
	private static final String		CUSTOM_SEARCH_PANEL_PATH	= "path";
	// class name used for view helper
	private static final String		CLASS_NAME					= "class-name";

	// TODO: It is not added in XSD, update XSD. It seems to be a property for Object node
	private static final String		PARENT_COL_NAME				= "parentColumn";

	/**
	 * MTMetadatXMLPath is the path of XML file containing the metadata of master table
	 */
	private static String			CONF_DIR_PATH_PROPERTY		= "view-metadata-path";
	private static String			CONF_DIR_PATH				= null;
	public static String			DO_METADATA_CONFIG_PATH		= "DOMetaData.xml";

	/**
	 * MTMetadatXSDPath is the path of XSD that validates the XML file containing the metadata of master table
	 */
	private static String			DOMETADATA_XSD_PATH			= "DOMetaData.xsd";
	private static String			SINGLE_OBJECT_XSD_PATH		= "SingleObject.xsd";

	/**
	 * Below are the properties belonging to a jsp page (Basically page leval properties).
	 */
	private static final String		TILES_DEF					= "page.tilesdef";
	private static final String		ADD_PAGE_TITLE				= "add.page.title";
	private static final String		ADD_PAGE_DESCRIPTION		= "add.page.desc";
	private static final String		ADD_PAGE_FOOTER				= "add.page.footer";
	private static final String		ADD_PAGE_HELP				= "add.page.help";
	private static final String		EDIT_PAGE_TITLE				= "edit.page.title";
	private static final String		EDIT_PAGE_DESCRIPTION		= "edit.page.desc";
	private static final String		EDIT_PAGE_FOOTER			= "edit.page.footer";
	private static final String		EDIT_PAGE_HELP				= "edit.page.help";
	private static final String		LIST_PAGE_TITLE				= "list.page.title";
	private static final String		LIST_PAGE_DESCRIPTION		= "list.page.desc";
	private static final String		LIST_PAGE_FOOTER			= "list.page.footer";
	private static final String		LIST_PAGE_HELP				= "list.page.help";
	private static final String		PARENT_JS_FUNCTION			= "popup.parentjsfunction";
	private static final String		PROPERTIES					= "properties";

	/**
	 * Constasnts used to deal with actions given in XML.
	 */
	private static final String		ACTIONS						= "actions";
	private static final String		FORM_ACTIONS				= "form-actions";
	private static final String		EDIT_ACTIONS				= "edit-actions";
	private static final String		EDIT_ACTION					= "edit-action";
	private static final String		FORM_ACTION					= "form-action";
	private static final String		POPUP						= "popup";
	private static final String		SELECTION					= "selection";
	private static final String		CLASS_NAME_FOR_ACTION		= "className";
	private static final String		URL							= "url";
	private static final String		ROLES						= "roles";
	private static final String		PAGE_ACTION					= "page.action";
	private static final String		LIST_ACTIONS				= "list-actions";
	private static final String		LIST_ACTION					= "list-action";
	private static final String		IMG							= "img";
	private static final String		AJAX						= "ajax";
	private static final String		CONFIRMATION_MESSAGE		= "confirmation-message";
	private static final String		ERROR_PAGE					= "error-page";
	private static final String		LIST_ACTION_ORIENTATION		= "orientation";
	private static final String		ADD_ACTION					= "addAction";
	private static final String		JS_FUNCTION					= "jsFunction";
	private static final String		JS_INFO						= "jsInfo";
	private static final String		JS_RETURN					= "jsReturn";
	private static final String		ACTION_TYPE					= "actionType";
	private static final String		ACCESS_KEY					= "access-key";
	private static final String		DISABLED					= "disabled";
	private static final String		STYLE_ID					= "style-id";
	private static final String		TAB_INDEX					= "tab-index";
	private static final String		TITLE						= "title";
	private static final String		STYLE						= "style";
	private static final String		STYLE_CLASS					= "style-class";

	private static XMLSchemaManager	xmlSchemaManager			= new XMLSchemaManager();

	private XMLSchemaManager()
	{
		CONF_DIR_PATH = ConfigurationManager.getSharedInstance().getPropertyValueAsSystemPath( CONF_DIR_PATH_PROPERTY );
		if( !StringUtils.isQualifiedString( CONF_DIR_PATH ) )
		{
			SystemException se = new SystemException( IErrorCodes.INSTANTIATION_ERROR,
					"Path for Viewmetadata configuration files should be specified in propoerties, however it is not found. property-name[view-metadata-path]" );
			LOGGER.fatal( se );
			throw se;
		}
		DO_METADATA_CONFIG_PATH = CONF_DIR_PATH + DO_METADATA_CONFIG_PATH;
		DOMETADATA_XSD_PATH = CONF_DIR_PATH + DOMETADATA_XSD_PATH;
		SINGLE_OBJECT_XSD_PATH = CONF_DIR_PATH + SINGLE_OBJECT_XSD_PATH;
	}

	/** Singleton class */

	static XMLSchemaManager getSharedInstance()
	{
		if( xmlSchemaManager == null )
		{
			xmlSchemaManager = new XMLSchemaManager();
		}
		return xmlSchemaManager;
	}

	/**
	 * 
	 * @param tableName It indicates the name attribute property of meta data
	 * @return
	 * @throws ComponentException
	 */
	DOMetaData getDOMetaData( String tableName ) throws ComponentException
	{

		StringUtils.assertQualifiedArgument( tableName );

		/*
		 * Psuedo code
		 * 
		 * Create a new method with getDOMetaDataNew throws ComponentException
		 * 
		 * Get the desired node from DO Metadata XML using specified table name Create DO Metadata object from XML
		 * Create a new DOMetadata object Get the node list from XML node Iterate over node list Create new column for
		 * every property type node set all the property type node attributes to Column Object add the object to
		 * DOMetadata add various roles and other properties to DOMetadata object return DOMetadata
		 */

		LOGGER.trace( "entering :getDOMetaData . tableName[" + tableName + "" );

		if( tableName.contains( "." ) )
		{
			tableName = StringUtils.getSimpleClassName( tableName );
			LOGGER.debug( "unqualifyTableName[" + tableName + "]" );
		}

		// It will search the file having metadata information for given object name. This can be a separate file or
		// metadata can be in common file. If it find the separate file, it will return the xml nodes form this file.
		// Else it will return all the nodes from common metadata file to check
		NodeList tableNodeList = loadMTMetadata( tableName );
		LOGGER.debug( "tableNodeListCount[" + tableNodeList.getLength() + "]" );

		DOMetaData metaData = new DOMetaData();

		// Loop on table nodes to find the node for specified name
		for( int counter = 0; counter < tableNodeList.getLength(); counter++ )
		{
			Node tableNode = tableNodeList.item( counter );

			// comparing table name to find the right metadata node from given XML nodes. It matches the 'name'
			// attribute
			if( tableNode.getNodeName() != null && tableNode.getNodeName().trim().equalsIgnoreCase( TABLE )
					&& tableNode.getAttributes().getNamedItem( TABLE_NAME ).getNodeValue().trim()
							.equalsIgnoreCase( tableName.trim() ) )
			{
				LOGGER.debug( "Found metadata for [" + tableName + "]" );

				metaData.setUIMetadataName( tableName.trim() );
				/*
				 * Putting the table level attributes in a map. Later this map will be used in business logic.
				 */
				Map<String, String> tableProperties = getTableProerties( tableNode );
				setParentColName( tableNode, metaData );

				NodeList tableInnerTagList = tableNode.getChildNodes();

				/*
				 * Loop for iterating other nodes of table node XMLColList would contain all those columns (returned by
				 * HibernateSchemaManager) whose display name is changed.
				 */
				List<Column> XMLColList = new ArrayList<>();
				for( int i = 0; i < tableInnerTagList.getLength(); i++ )
				{
					Node tableInnerNode = tableInnerTagList.item( i );
					String tableInnerNodeName = tableInnerNode.getNodeName();

					LOGGER.debug( "tableInnerNodeName[" + tableInnerNodeName + "]" );

					/*
					 * If tag-name is column then get this node and send it to change the display name etc.
					 */
					if( tableInnerNodeName.equalsIgnoreCase( PROPERTIES ) )
					{
						// manageColumn( XMLColList, tableProperties, tableInnerNode );
						dealPropertiesNode( XMLColList, tableProperties, tableInnerNode );
						LOGGER.debug( "XMLColList size is [" + XMLColList.size() + "]" );
					}
					else
					{
						setDOMetadataProperty( metaData, tableNode, tableInnerNodeName );
					}
				}

				/*
				 * If enforseBoolean is true then remove all those columns from column list that does not exist in
				 * XMLColList .
				 */
				String enforcePrp = tableProperties.get( ENFORCE );

				boolean enforceBoolean = enforcePrp != null ? Boolean.parseBoolean( enforcePrp ) : false;
				metaData.setEnforce( enforceBoolean );

				if( enforceBoolean )
				{
					metaData.setColumns( XMLColList );
				}

				/*
				 * Set Id column and useOrmSchema and class name from xml file
				 */
				String objectClass = tableProperties.get( OBJECT_CLASS );
				metaData.setObjectClassName( StringUtils.isQualifiedString( objectClass ) ? objectClass : tableName );
				LOGGER.debug( "className[" + metaData.getClassName() + "]" );

				String ormSchemaPrp = tableProperties.get( "ormschema" );
				boolean ormschema = ormSchemaPrp != null ? Boolean.parseBoolean( ormSchemaPrp ) : false;
				metaData.setUseORMSchema( ormschema );

				String id = tableProperties.get( "id" );
				LOGGER.debug( "metaData-Id[" + id + "]" );
				Column idColumn = new Column();
				idColumn.setDataType( "Long" );
				idColumn.setDbName( id );
				metaData.setIdColumn( idColumn );

				// lets go out as we have found the metadata
				break;
			}
		}

		if( metaData == null )
		{
			ComponentException ce = new ComponentException( IErrorCodes.RESOURCE_NOT_FOUND,
					"No metadata found for [" + tableName + "]" );
			LOGGER.error( ce );
			throw ce;
		}

		LOGGER.trace( "exiting :getDOMetaData[ " + metaData + "]" );
		return metaData;
	}

	/**
	 * This method basically deals with properties tag in XML .It iterates all the inner nodes. Dyuring iteration if
	 * node name is found "property" then manageColumns() is invoked.
	 * 
	 * @param XMLColList
	 * @param tableProperties
	 * @param propertiesNode
	 */
	private void dealPropertiesNode( List XMLColList, Map<String, String> tableProperties, Node propertiesNode )
	{
		LOGGER.trace( "entering : dealPropertiesNode . XMLColList[" + XMLColList + "] tableProperties["
				+ tableProperties + "] propertiesNode[" + propertiesNode + "]" );

		NodeList propertyList = propertiesNode.getChildNodes();
		LOGGER.debug( "propertyList is [" + propertyList + "] and size is [" + propertyList.getLength() + "]" );

		for( int i = 0; i < propertyList.getLength(); i++ )
		{
			Node propertyNode = propertyList.item( i );

			if( propertyNode != null && propertyNode.getNodeName().trim().equals( COLUMN ) )
			{
				LOGGER.debug( "Managing the property ie. column" );
				manageColumn( XMLColList, tableProperties, propertyNode );
			}
		}

		LOGGER.trace( "exiting : dealPropertiesNode." );
	}

	private Node getSubNode( Node tableNode, String subNodeName )
	{
		LOGGER.trace( "entering : getSubNode . tableNode[" + tableNode + "] subNodeName[" + subNodeName + "]" );

		Node subNode = null;
		NodeList childNodeList = tableNode.getChildNodes();
		LOGGER.debug( "childNodeList  is [" + childNodeList + "]" );

		for( int i = 0; i < childNodeList.getLength(); i++ )
		{
			Node tempNode = childNodeList.item( i );
			LOGGER.debug( "Node at index is [" + tempNode + "] and node name is [" + tempNode.getNodeName() + "]" );
			if( tempNode.getNodeName().trim().equals( subNodeName.trim() ) )
			{
				LOGGER.debug( "Subnode found :::::" );
				subNode = tempNode;
				break;
			}
		}

		LOGGER.debug( "subNode is [" + subNode + "]" );
		LOGGER.trace( "exiting : getSubNode." );
		return subNode;
	}

	/**
	 * The default valyes of enforce/EDIT_ALL/VIEW_ALL is false
	 * 
	 * @param node
	 * @return
	 */
	private Map<String, String> getTableProerties( Node node )
	{
		Map<String, String> tableProperties = new HashMap<>();

		/*
		 * putting ENFORCE property in table
		 */
		Node attributeNode = node.getAttributes().getNamedItem( ENFORCE );
		String enforceString = "false";
		if( attributeNode != null )
		{
			enforceString = attributeNode.getNodeValue();
		}
		tableProperties.put( ENFORCE, enforceString );

		/*
		 * putting OBJECT_CLASS property in table
		 */
		attributeNode = node.getAttributes().getNamedItem( OBJECT_CLASS );
		String objectClass = null;
		if( attributeNode != null )
		{
			objectClass = attributeNode.getNodeValue();
		}
		LOGGER.debug( "objectClass  [" + objectClass + "]" );
		tableProperties.put( OBJECT_CLASS, objectClass );

		/*
		 * putting VIEW_ALL property in table
		 */
		attributeNode = node.getAttributes().getNamedItem( VIEW_ALL );
		String listAllString = "false";
		if( attributeNode != null )
		{
			listAllString = attributeNode.getNodeValue();
		}
		LOGGER.debug( "listAllString  [" + listAllString + "]" );
		tableProperties.put( VIEW_ALL, listAllString );

		/*
		 * putting EDIT_ALL property in table
		 */
		attributeNode = node.getAttributes().getNamedItem( EDIT_ALL );
		String editAllString = "false";
		if( attributeNode != null )
		{
			editAllString = attributeNode.getNodeValue();
		}
		tableProperties.put( EDIT_ALL, editAllString );

		/*
		 * Added by arvind -id and ormschema
		 */

		attributeNode = node.getAttributes().getNamedItem( "ormschema" );

		String ormschema = null;
		if( attributeNode != null )
		{
			ormschema = attributeNode.getNodeValue();
			if( !StringUtils.isQualifiedString( ormschema ) )
			{
				ormschema = null;
			}
		}
		LOGGER.debug( "metadata-ormSchema[" + ormschema + "]" );
		tableProperties.put( "ormschema", ormschema );

		// Reading id attribute
		attributeNode = node.getAttributes().getNamedItem( "id" );
		String id = null;
		if( attributeNode != null )
		{
			id = attributeNode.getNodeValue();
			if( !StringUtils.isQualifiedString( id ) )
			{
				id = null;
			}
		}
		LOGGER.debug( "metadata-id[" + id + "]" );
		tableProperties.put( "id", id );

		return tableProperties;
	}

	/**
	 * It take a xml element and the tag name, look for the tag and get the text content
	 */
	private String getTextValue( Element ele, String tagName )
	{
		String textVal = null;
		NodeList nl = ele.getElementsByTagName( tagName );
		if( nl != null && nl.getLength() > 0 )
		{
			Element el = (Element) nl.item( 0 );
			Node node = el.getFirstChild();
			LOGGER.debug( "Node is  [" + node + "]" );
			if( node != null )
			{
				textVal = node.getNodeValue();
			}
		}
		LOGGER.debug( "Text value rerurned is [" + textVal + "]" );
		return textVal != null ? textVal.trim() : textVal;
	}

	/**
	 * Loads xml and validates it by xsd
	 * 
	 * @return
	 * @throws ComponentException
	 */
	private NodeList loadMTMetadata( String fileName ) throws ComponentException
	{
		String xsdPath = DOMETADATA_XSD_PATH;

		LOGGER.debug( "File name in loadMetaData is  [" + fileName + "]" );

		InputStream is;
		try
		{
			boolean separateFile = false;
			// try to find separate file for given name
			is = ConfigurationManager.loadConfigurationFile( CONF_DIR_PATH + fileName + ".xml" );

			// if separate file not found, load the single file to check metadata node in that
			if( is == null )
			{
				LOGGER.debug( "Loading common metadata file." );
				is = ConfigurationManager.loadConfigurationFile( DO_METADATA_CONFIG_PATH );
			}
			else
			{
				xsdPath = SINGLE_OBJECT_XSD_PATH;
				separateFile = true;
				LOGGER.debug( "Independent file found for " + fileName );
			}
			if( is == null )
			{
				LOGGER.fatal( "No DOMetadata config file found." );
				throw new SystemException( IErrorCodes.RESOURCE_NOT_FOUND,
						"No DOMetadata config file found. metadata-name[" + fileName + " ]" );
			}

			// Document document = XMLUtils.parseXML( is, xsdPath );
			// TODO: XSD need to be corrected
			Document document = XMLUtils.parseXML( is, null );

			if( separateFile )
			{
				return document.getChildNodes();
			}
			else
			{
				Node rootNode = document.getChildNodes().item( 0 );
				return rootNode.getChildNodes();
			}
		}
		catch( ApplicationException e )
		{
			LOGGER.error( "Problem while loading config file. file[" + fileName + "]", e );
			throw new ComponentException( IErrorCodes.RESOURCE_NOT_FOUND,
					"Problem while loading config file. file[" + fileName + "]", e );
		}

	}

	private void manageColumn( List<Column> XMLColList, Map<String, String> tableProperties, Node columnNode )
	{
		/**
		 * finding column name and display name
		 */
		// String columnName = columnNode.getAttributes().getNamedItem( COLUMN_DB_NAME ).getNodeValue();
		String columnName = getTextValue( (Element) columnNode, COLUMN_DB_NAME );
		String colDisplayName = getTextValue( (Element) columnNode, COLUMN_DISPLAY_NAME );
		Column column = new Column();

		column.setDisplayName( colDisplayName );
		column.setDbName( columnName );
		setColumnEditStatus( tableProperties, columnNode, column );
		setColumnViewStatus( tableProperties, columnNode, column );

		/*
		 * Setting validation rules
		 */
		String validationRules = getTextValue( (Element) columnNode, VALIDATION_RULES );
		LOGGER.debug( "validationRules  [" + validationRules + "]" );
		column.setValidationRules( validationRules );

		setSearchable( column, columnNode );
		setSortable( column, columnNode );
		setDynamic( column, columnNode );

		if( column.isDynamic() && ( column.isSearchable() || column.isSortable() ) )
		{
			SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"A dynamic column MUST not be set as 'searchable' or 'sortable'. column[" + column + "]" );
			LOGGER.error( se );
			throw se;
		}
		if( !StringUtils.isQualifiedString( column.getDbName() ) )
		{
			SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"A column must have a DB Name. Even if column is dynamic, system expect the 'getter' method for that property with data object, or a value in array if returned value is array from data provider. column["
							+ column + "]" );
			LOGGER.error( se );
			throw se;
		}

		setViewLink( column, columnNode );

		LOGGER.debug( "Column after managing is [ " + column + " ]" );
		XMLColList.add( column );
	}

	/**
	 * Sets the data for a action.
	 * 
	 * @param actionsNode
	 * @param actionNode
	 * @param action
	 */
	private void setActionData( Node actionsNode, Node actionNode, PageAction action )
	{
		LOGGER.trace( "entering : setActionData . " );
		LOGGER.debug( "nodeName[" + actionNode.getNodeName() + "]" );
		Node popupNode = actionNode.getAttributes().getNamedItem( POPUP );
		if( popupNode != null )
		{
			String popupValue = popupNode.getNodeValue();
			LOGGER.debug( "popupValue[" + popupValue + "]" );
			action.setPopup( popupValue );
			Node popupSelectionNode = actionNode.getAttributes().getNamedItem( SELECTION );
			if( popupSelectionNode != null )
			{
				String selectionValue = popupSelectionNode.getNodeValue();
				LOGGER.debug( "classNameValue[" + selectionValue + "]" );
				action.setSelection( selectionValue );
			}
			Node popupClassNameNode = actionNode.getAttributes().getNamedItem( CLASS_NAME_FOR_ACTION );
			if( popupClassNameNode != null )
			{
				String classNameValue = popupClassNameNode.getNodeValue();
				LOGGER.debug( "classNameValue[" + classNameValue + "]" );
				action.setClassName( classNameValue );
			}
		}

		String urlValue = getTextValue( (Element) actionNode, URL );
		LOGGER.debug( "url[" + urlValue + "]" );
		action.setUrl( urlValue );

		// NO more required as now we are getting roles related information from SM security service
		// String rolesValue = getTextValue( (Element) actionNode, ROLES );
		// LOGGER.debug( "roles[" + rolesValue + "]" );
		// action.setRoles( new HashSet( StringUtils.getTokenizedString( rolesValue, "," ) ) );

		/**
		 * Preparing displayData.
		 */
		setDisplayData( actionsNode, actionNode, action );
		LOGGER.trace( "exiting : setActionData . " );
	}

	/**
	 * Sets all the actions kept in XML to the metadata.
	 * 
	 * @param metaData
	 * @param tableNode
	 */
	private void setActions( DOMetaData metaData, Node tableNode )
	{
		LOGGER.trace( "entering : setActions . metaData[" + metaData + "] tableNode[" + tableNode + "]" );

		Node actinsNode = getSubNode( tableNode, ACTIONS );
		LOGGER.debug( "actinsNode is [" + actinsNode + "]" );

		NodeList childNodeList = actinsNode.getChildNodes();
		LOGGER.debug( "childNodeList is [" + childNodeList + "]" );

		/**
		 * Set form actions.
		 */
		Node formActionsNode = getSubNode( actinsNode, FORM_ACTIONS );
		if( formActionsNode != null )
		{
			setFormActions( metaData, formActionsNode );
		}

		/**
		 * Set list actions.
		 */
		Node listActionsNode = getSubNode( actinsNode, LIST_ACTIONS );
		if( listActionsNode != null )
		{
			setListActions( metaData, listActionsNode );
		}

		/**
		 * Set edit actions
		 */
		Node editActionsNode = getSubNode( actinsNode, EDIT_ACTIONS );
		LOGGER.debug( "editActionsNode is is [" + editActionsNode + "]" );
		if( editActionsNode != null )
		{
			setEditActions( metaData, editActionsNode );
		}
		LOGGER.trace( "exiting : setActions ." );
	}

	private void setColumnEditStatus( Map<String, String> tableProperties, Node columnNode, Column column )
	{
		boolean editAll = Boolean.parseBoolean( tableProperties.get( EDIT_ALL ) );
		Node attributeNode = columnNode.getAttributes().getNamedItem( EDIT );
		boolean editCol = false;
		if( attributeNode != null )
		{
			editCol = Boolean.parseBoolean( attributeNode.getNodeValue().trim() );
		}
		boolean colEditStatus = editAll || editCol;
		LOGGER.debug( "colEditStatus [" + colEditStatus + "]" );
		column.setEditable( colEditStatus );
	}

	private void setColumnViewStatus( Map<String, String> tableProperties, Node columnNode, Column column )
	{
		Node attributeNode;
		boolean listAll = Boolean.parseBoolean( tableProperties.get( VIEW_ALL ) );
		LOGGER.debug( "table map is [" + tableProperties + "]" );
		attributeNode = columnNode.getAttributes().getNamedItem( VIEW );
		boolean listCol = false;
		if( attributeNode != null )
		{
			listCol = Boolean.parseBoolean( attributeNode.getNodeValue().trim() );
		}
		boolean colListStatus = listAll || listCol;
		LOGGER.debug( "colListStatus [" + colListStatus + "]" );
		column.setListPageColumn( colListStatus );
	}

	/**
	 * Sets the description of all (add/edit/list) pages.
	 * 
	 * @param metaData
	 * @param tableNode
	 * @param tableInnerNodeName
	 */
	private void setDescriptions( DOMetaData metaData, Node tableNode, String tableInnerNodeName )
	{
		LOGGER.debug( "entering : setDescriptions . metaData[" + metaData + "] tableNode[" + tableNode
				+ "] tableInnerNodeName[" + tableInnerNodeName + "]" );
		if( tableInnerNodeName.equalsIgnoreCase( ADD_PAGE_DESCRIPTION ) )
		{
			String addDescription = getTextValue( (Element) tableNode, ADD_PAGE_DESCRIPTION );
			metaData.setAddPageDescKey( addDescription );
		}
		if( tableInnerNodeName.equalsIgnoreCase( EDIT_PAGE_DESCRIPTION ) )
		{
			String editDescription = getTextValue( (Element) tableNode, EDIT_PAGE_DESCRIPTION );
			metaData.setEditPageDescKey( editDescription );
		}
		if( tableInnerNodeName.equalsIgnoreCase( LIST_PAGE_DESCRIPTION ) )
		{
			String listDescription = getTextValue( (Element) tableNode, LIST_PAGE_DESCRIPTION );
			metaData.setListPageDescKey( listDescription );
		}
		LOGGER.trace( "exiting : setDescriptions." );
	}

	/**
	 * Preparing displayData.
	 */
	private void setDisplayData( Node actionsNode, Node actionNode, PageAction action )
	{
		LOGGER.trace( "entering : setDisplayData." );
		DisplayData displayData = new DisplayData();
		String displayText = getTextValue( (Element) actionNode, COLUMN_DISPLAY_NAME );
		displayData.setText( displayText );

		LOGGER.debug( "actionNode is [" + actionNode.getNodeName() + "] and child count is ["
				+ actionNode.getChildNodes().getLength() + "]" );
		Node displayNode = getSubNode( actionNode, COLUMN_DISPLAY_NAME );
		LOGGER.debug( "displayNode is [" + displayNode + "]" );

		if( displayNode.getAttributes().getNamedItem( IMG ) != null )
		{
			String image = displayNode.getAttributes().getNamedItem( IMG ).getNodeValue();
			displayData.setImageName( image );
		}
		LOGGER.debug( "displayData is [" + displayData + "]" );
		action.setDisplayData( displayData );

		LOGGER.trace( "exiting : setDisplayData." );
	}

	/**
	 * This method is responsible to set all the data in DOMetaData object except properties (Columns).
	 * 
	 * @param metaData instance of doMetaData to which values are to be set.
	 * @param tableNode the node representing a table(object) node of XML.
	 * @param tableInnerNodeName name of inner node of table to be delt with.
	 */
	private void setDOMetadataProperty( DOMetaData metaData, Node tableNode, String tableInnerNodeName )
	{
		LOGGER.debug( "tableInnerNodeName is [" + tableInnerNodeName + "]" );

		if( tableInnerNodeName.equalsIgnoreCase( TILES_DEF ) )
		{
			String tilesDef = getTextValue( (Element) tableNode, TILES_DEF );
			metaData.setPagetilesDef( tilesDef );
		}
		// Sets the addTitle,editTitle,listTitle .
		setTitles( metaData, tableNode, tableInnerNodeName );

		// Sets the descriptions.
		setDescriptions( metaData, tableNode, tableInnerNodeName );

		// Sets the footer of all the pages.
		setFooters( metaData, tableNode, tableInnerNodeName );

		// Sets the help-key of all the pages.
		setHelpKeys( metaData, tableNode, tableInnerNodeName );

		if( tableInnerNodeName.equalsIgnoreCase( PARENT_JS_FUNCTION ) )
		{
			String jsFunction = getTextValue( (Element) tableNode, PARENT_JS_FUNCTION );
			metaData.setPopupParentJSFunctionName( jsFunction );
		}
		else if( tableInnerNodeName.equalsIgnoreCase( UI_VIEW_HELPER ) )
		{
			Node viewHelperNode = getSubNode( tableNode, UI_VIEW_HELPER );
			LOGGER.debug( "viewHelperNode is [" + viewHelperNode + "]" );

			String viewHelper = viewHelperNode.getAttributes().getNamedItem( CLASS_NAME ).getNodeValue();

			LOGGER.debug( "UI_VIEW_HELPER is [" + viewHelper + "]" );
			metaData.setViewHelperClassName( viewHelper );
		}
		// Sets page actions
		else if( tableInnerNodeName.equalsIgnoreCase( PAGE_ACTION ) )
		{
			String pageAction = getTextValue( (Element) tableNode, PAGE_ACTION );
			metaData.setBaseURL( pageAction );
		}
		// Sets list and form actions
		else if( tableInnerNodeName.equalsIgnoreCase( ACTIONS ) )
		{
			setActions( metaData, tableNode );
		}
		else if( tableInnerNodeName.equalsIgnoreCase( CUSTOM_SEARCH_PANEL ) )
		{
			Node viewHelperNode = getSubNode( tableNode, CUSTOM_SEARCH_PANEL );
			LOGGER.debug( "customSearchNode[" + viewHelperNode + "]" );

			String path = viewHelperNode.getAttributes().getNamedItem( CUSTOM_SEARCH_PANEL_PATH ).getNodeValue();

			LOGGER.debug( "customSearchPanelPath[" + path + "]" );
			metaData.setCustomSearchPanelPath( path );
		}
	}

	/**
	 * Setting Edit Action values
	 * 
	 * @param actionsNode
	 * @param actionNode
	 * @param action
	 */
	private void setEditActionData( Node actionsNode, Node actionNode, EditAction action )
	{
		LOGGER.debug( "entering : setListActionData . actionsNode[" + actionsNode + "] actionNode[" + actionNode
				+ "] action[" + action + "]" );

		Node addActionNode = actionNode.getAttributes().getNamedItem( ADD_ACTION );
		String addAction = null;
		if( addActionNode != null )
		{
			addAction = addActionNode.getNodeValue();
		}
		LOGGER.debug( "addAction is [" + addAction + "]" );
		if( addAction != null && addAction.trim().equals( "true" ) )
		{
			action.setAddAction( true );
		}
		else
		{
			action.setAddAction( false );
		}

		Node jsFunctionNode = actionNode.getAttributes().getNamedItem( JS_FUNCTION );
		String jsFunction = null;
		if( jsFunctionNode != null )
		{
			jsFunction = jsFunctionNode.getNodeValue().trim();
		}
		LOGGER.debug( "jsFunction is [" + jsFunction + "]" );
		action.setJsFunction( jsFunction );

		Node jsInfoNode = actionNode.getAttributes().getNamedItem( JS_INFO );
		String jsInfo = null;
		if( jsInfoNode != null )
		{
			jsInfo = jsInfoNode.getNodeValue().trim();
		}
		LOGGER.debug( "jsInfo is [" + jsInfo + "]" );
		action.setJsInfo( jsInfo );

		Node jsReturnNode = actionNode.getAttributes().getNamedItem( JS_RETURN );
		String jsReturn = null;
		if( jsReturnNode != null )
		{
			jsReturn = actionNode.getAttributes().getNamedItem( JS_RETURN ).getNodeValue();
		}
		LOGGER.debug( "jsReturn is [" + jsReturn + "]" );
		if( jsReturn != null && jsReturn.trim().equals( "true" ) )
		{
			action.setJsReturn( true );
		}
		else
		{
			action.setJsReturn( false );
		}

		Node actionTypeNode = actionNode.getAttributes().getNamedItem( ACTION_TYPE );
		String actionType = null;
		if( actionTypeNode != null )
		{
			actionType = actionTypeNode.getNodeValue().trim();
		}
		LOGGER.debug( "actionType  is [" + actionType + "]" );
		action.setActionType( actionType );

		Node accessKeyNode = actionNode.getAttributes().getNamedItem( ACCESS_KEY );
		String accessKey = null;
		if( accessKeyNode != null )
		{
			accessKey = accessKeyNode.getNodeValue().trim();
		}
		LOGGER.debug( "accessKey is [" + accessKey + "]" );
		action.setAccessKey( accessKey );

		Node disabledNode = actionNode.getAttributes().getNamedItem( DISABLED );
		String disabled = null;
		if( disabledNode != null )
		{
			disabled = disabledNode.getNodeValue().trim();
		}
		LOGGER.debug( "disabled is [" + disabled + "]" );
		if( disabled != null && disabled.equals( "true" ) )
		{
			action.setDisabled( true );
		}
		else
		{
			action.setDisabled( false );
		}

		Node styleIdNode = actionNode.getAttributes().getNamedItem( STYLE_ID );
		String styleId = null;
		if( styleIdNode != null )
		{
			styleId = styleIdNode.getNodeValue().trim();
		}
		LOGGER.debug( "styleId is [" + styleId + "]" );
		action.setStyleId( styleId );

		Node tabIndexNode = actionNode.getAttributes().getNamedItem( TAB_INDEX );
		String tabIndex = null;
		if( tabIndexNode != null )
		{
			tabIndex = tabIndexNode.getNodeValue().trim();
		}
		LOGGER.debug( "tabIndex is [" + tabIndex + "]" );
		action.setTabIndex( tabIndex );

		Node titleNode = actionNode.getAttributes().getNamedItem( TITLE );
		String title = null;
		if( titleNode != null )
		{
			title = titleNode.getNodeValue().trim();
		}
		LOGGER.debug( "title is [" + title + "]" );
		action.setTitle( title );

		Node styleNode = actionNode.getAttributes().getNamedItem( STYLE );
		String style = null;
		if( styleNode != null )
		{
			style = styleNode.getNodeValue().trim();
		}
		LOGGER.debug( "style is [" + style + "]" );
		action.setStyle( style );

		Node styleClassNode = actionNode.getAttributes().getNamedItem( STYLE_CLASS );
		String styleClass = null;
		if( styleClassNode != null )
		{
			styleClass = styleClassNode.getNodeValue().trim();
		}
		LOGGER.debug( "styleClass is [" + styleClass + "]" );
		action.setStyleClass( styleClass );

		LOGGER.debug( "exiting : setEditActionData." );
	}

	/**
	 * setEditActions traverse all the edit actions kept inside the editActions and create a pageAction for each such
	 * editAction and adds this newly created edit action to the list of editActions .
	 * 
	 * 
	 * @param metaData
	 * @param editActionsNode node representing list-actions node in XML.
	 */
	private void setEditActions( DOMetaData metaData, Node editActionsNode )
	{
		LOGGER.trace(
				"entering : setEditActions  . metaData[" + metaData + "] editActionsNode[" + editActionsNode + "]" );

		NodeList editActionList = editActionsNode.getChildNodes();
		LOGGER.debug( "editActionList is [" + editActionList + "] and size is [" + editActionList.getLength() + "]" );

		// LOGGER.debug(
		// "edit actions before adding are ["+metaData.getEditActions()+"] and size is
		// ["+metaData.getEditActions().size()+"]"
		// );
		for( int i = 0; i < editActionList.getLength(); i++ )
		{
			Node editActionNode = editActionList.item( i );
			LOGGER.debug( "editActionNode is [" + editActionNode + "]" );

			/**
			 * Set the data for action.
			 */
			if( editActionNode != null && editActionNode.getNodeName().trim().equals( EDIT_ACTION ) )
			{
				LOGGER.debug( "seting actions for editactions" );
				if( metaData.getEditActions() == null )
				{
					metaData.setEditActions( new ArrayList() );
				}
				EditAction editAction = new EditAction();
				setActionData( editActionsNode, editActionNode, editAction );
				/**
				 * Adding the editAction to the list of editActions.
				 */
				setEditActionData( editActionsNode, editActionNode, editAction );
				metaData.getEditActions().add( editAction );
			}

		}
		// LOGGER.debug( "Form actions after adding are [" + metaData.getFormActions() + "] and size is ["
		// + metaData.getFormActions().size() + "]" );

		LOGGER.trace( "exiting : setEditActions ." );
	}

	// /**
	// * @param metaData
	// * @return
	// */
	// private void setAccessRights(DOMetaData metaData ,Node tableNode) {
	// LOGGER.trace( "entering : setAccessRights . metaData["+metaData+"]" );
	//
	// Map<String , String > accessRights = new HashMap<String, String>();
	//
	// Node accessRightsNode = getSubNode( tableNode, ACCESS_RIGHTS );
	// LOGGER.debug( "accessRightsNode is ["+accessRightsNode+"]" );
	//
	// NodeList rightsList = accessRightsNode.getChildNodes();
	//
	// /**
	// * Iterate all nodes of rightsList .
	// * if(nodeName == access-right){
	// * get the value of action and role.
	// * put it in map.
	// * }
	// */
	// for( int i = 0; i < rightsList.getLength(); i++ ) {
	// Node rightNode = (Node) rightsList.item( i );
	//
	// if(rightNode != null && rightNode.getNodeName().trim().equals( ACCESS_RIGHT )){
	// String action = rightNode.getAttributes().getNamedItem( ACTION).getNodeValue();
	// String role = rightNode.getAttributes().getNamedItem( ROLE).getNodeValue();
	//
	// LOGGER.debug( "action is ["+action+"] and role is ["+role+"]" );
	// accessRights.put( action, role );
	// }
	// }
	//
	// LOGGER.debug( "access rights map is ["+accessRights+"]" );
	// metaData.setAccesRights( accessRights );
	// LOGGER.trace( "exiting : setAccessRights." );
	// }

	/**
	 * Sets the footers of all (add/edit/list) pages.
	 * 
	 * @param metaData
	 * @param tableNode
	 * @param tableInnerNodeName
	 */
	private void setFooters( DOMetaData metaData, Node tableNode, String tableInnerNodeName )
	{
		LOGGER.debug( "entering : setFooters . metaData[" + metaData + "] tableNode[" + tableNode
				+ "] tableInnerNodeName[" + tableInnerNodeName + "]" );
		if( tableInnerNodeName.equalsIgnoreCase( ADD_PAGE_FOOTER ) )
		{
			String addFooter = getTextValue( (Element) tableNode, ADD_PAGE_FOOTER );
			metaData.setAddPageFooterKey( addFooter );
		}
		if( tableInnerNodeName.equalsIgnoreCase( EDIT_PAGE_FOOTER ) )
		{
			String editFooter = getTextValue( (Element) tableNode, EDIT_PAGE_FOOTER );
			metaData.setEditPageFooterKey( editFooter );
		}
		if( tableInnerNodeName.equalsIgnoreCase( LIST_PAGE_FOOTER ) )
		{
			String listFooter = getTextValue( (Element) tableNode, LIST_PAGE_FOOTER );
			metaData.setListPageFooterKey( listFooter );
		}
		LOGGER.trace( "exiting : setFooters." );
	}

	/**
	 * setFormActions traverse all the form actions kept inside the formActions and create a pageAction for each such
	 * formAction and adds this newly created form action to the list of formActions .
	 * 
	 * 
	 * @param metaData
	 * @param formActionsNode node representing form-actions node in XML.
	 */
	private void setFormActions( DOMetaData metaData, Node formActionsNode )
	{
		LOGGER.trace(
				"entering : setListActions  . metaData[" + metaData + "] formActionsNode[" + formActionsNode + "]" );

		NodeList fornActionList = formActionsNode.getChildNodes();
		LOGGER.debug( "fornActionList is [" + fornActionList + "]" );

		// LOGGER.debug(
		// "Form actions before adding are ["+metaData.getFormActions()+"] and size is
		// ["+metaData.getFormActions().size()+"]"
		// );
		for( int i = 0; i < fornActionList.getLength(); i++ )
		{
			Node formActionNode = fornActionList.item( i );

			/**
			 * Set the data for action.
			 */
			if( formActionNode != null && formActionNode.getNodeName().trim().equals( FORM_ACTION ) )
			{
				if( metaData.getFormActions() == null )
				{
					metaData.setFormActions( new ArrayList() );
				}
				LOGGER.debug( "seting actions for form" );
				PageAction formAction = new PageAction();
				setActionData( formActionsNode, formActionNode, formAction );
				/**
				 * Adding the formAction to the list of formActions.
				 */
				metaData.getFormActions().add( formAction );
			}

		}
		// LOGGER.debug( "Form actions after adding are [" + metaData.getFormActions() + "] and size is ["
		// + metaData.getFormActions().size() + "]" );

		LOGGER.trace( "exiting : setListActions ." );
	}

	/**
	 * Sets the help keys of all (add/edit/list) pages.
	 * 
	 * @param metaData
	 * @param tableNode
	 * @param tableInnerNodeName
	 */
	private void setHelpKeys( DOMetaData metaData, Node tableNode, String tableInnerNodeName )
	{
		LOGGER.debug( "entering : setHelpKeys . metaData[" + metaData + "] tableNode[" + tableNode
				+ "] tableInnerNodeName[" + tableInnerNodeName + "]" );
		if( tableInnerNodeName.equalsIgnoreCase( ADD_PAGE_HELP ) )
		{
			String addHelpKey = getTextValue( (Element) tableNode, ADD_PAGE_HELP );
			metaData.setAddPageHelpKey( addHelpKey );
		}
		if( tableInnerNodeName.equalsIgnoreCase( EDIT_PAGE_HELP ) )
		{
			String editHelpKey = getTextValue( (Element) tableNode, EDIT_PAGE_HELP );
			metaData.setEditPageHelpKey( editHelpKey );
		}
		if( tableInnerNodeName.equalsIgnoreCase( LIST_PAGE_HELP ) )
		{
			String listHelpKey = getTextValue( (Element) tableNode, LIST_PAGE_HELP );
			metaData.setListPageHelpKey( listHelpKey );
		}
		LOGGER.trace( "exiting : setHelpKeys." );
	}

	/**
	 * Setting list page specific data. Dafault value of sjaxEnabled is false.
	 * 
	 * @param actionsNode
	 * @param actionNode
	 * @param action
	 */
	private void setListActionData( Node actionsNode, Node actionNode, ListAction action )
	{
		LOGGER.debug( "entering : setListActionData . actionsNode[" + actionsNode + "] actionNode[" + actionNode
				+ "] action[" + action + "]" );

		Node ajaxEnabledNode = actionNode.getAttributes().getNamedItem( AJAX );
		String ajaxEnabled = null;
		if( ajaxEnabledNode != null )
		{
			ajaxEnabled = actionNode.getAttributes().getNamedItem( AJAX ).getNodeValue();
		}
		LOGGER.debug( "ajax enabled is [" + ajaxEnabled + "]" );
		if( ajaxEnabled != null && ajaxEnabled.trim().equals( "true" ) )
		{
			action.setAjaxEnabled( true );
		}
		else
		{
			action.setAjaxEnabled( false );
		}

		Node orientationNode = actionNode.getAttributes().getNamedItem( LIST_ACTION_ORIENTATION );
		String orientation = null;
		if( orientationNode != null )
		{
			orientation = orientationNode.getNodeValue();
		}
		LOGGER.debug( "orientation[" + orientation + "]" );

		if( orientation != null )
		{
			if( orientation != null && orientation.trim().equals( "left" ) )
			{
				action.setOrientation( ListAction.LIST_ACTION_ORIENTATION_LEFT );
			}
			else if( orientation != null && orientation.trim().equals( "right" ) )
			{
				action.setOrientation( ListAction.LIST_ACTION_ORIENTATION_RIGHT );
			}
			else
			{
				throw new SystemException( IErrorCodes.ILLEGAL_ARGUMENT_ERROR,
						"Orientation of a list action can be either left or right, but found something else. orientation["
								+ orientation + "]" );
			}
		}
		else
		{
			action.setOrientation( ListAction.LIST_ACTION_ORIENTATION_LEFT );
		}

		String confMsg = getTextValue( (Element) actionNode, CONFIRMATION_MESSAGE );
		LOGGER.debug( "confMsg is [" + confMsg + "]" );
		action.setConfirmationMessage( confMsg );

		String errorPage = getTextValue( (Element) actionNode, ERROR_PAGE );
		LOGGER.debug( "errorPage  is [" + errorPage + "]" );
		action.setErrorPage( errorPage );

		LOGGER.debug( "exiting : setListActionData." );
	}

	/**
	 * setListActions traverse all the list actions kept inside the listActions and create a pageAction for each such
	 * listAction and adds this newly created list action to the list of listActions .
	 * 
	 * 
	 * @param metaData
	 * @param listActionsNode node representing list-actions node in XML.
	 */
	private void setListActions( DOMetaData metaData, Node listActionsNode )
	{
		LOGGER.trace(
				"entering : setListActions  . metaData[" + metaData + "] listActionsNode[" + listActionsNode + "]" );

		NodeList listActionList = listActionsNode.getChildNodes();
		LOGGER.debug( "listActionList is [" + listActionList + "]" );

		// LOGGER.debug(
		// "list actions before adding are ["+metaData.getFormActions()+"] and size is
		// ["+metaData.getFormActions().size()+"]"
		// );
		for( int i = 0; i < listActionList.getLength(); i++ )
		{
			Node listActionNode = listActionList.item( i );

			/**
			 * Set the data for action.
			 */
			if( listActionNode != null && listActionNode.getNodeName().trim().equals( LIST_ACTION ) )
			{
				if( metaData.getListActions() == null )
				{
					metaData.setListActions( new ArrayList() );
				}
				LOGGER.debug( "seting actions for listactions" );
				ListAction listAction = new ListAction();

				/**
				 * Setting data of action.
				 */
				setActionData( listActionsNode, listActionNode, listAction );
				/**
				 * Setting list action specific data.
				 */
				setListActionData( listActionsNode, listActionNode, listAction );
				/**
				 * Adding the listAction to the list of listActions.
				 */
				metaData.getListActions().add( listAction );
			}

		}
		// LOGGER.debug( "Form actions after adding are [" + metaData.getFormActions() + "] and size is ["
		// + metaData.getFormActions().size() + "]" );

		LOGGER.trace( "exiting : setListActions ." );
	}

	private void setParentColName( Node tableNode, DOMetaData metaData )
	{
		Node attributeNode = tableNode.getAttributes().getNamedItem( PARENT_COL_NAME );
		if( attributeNode != null )
		{
			metaData.setParentColumn( attributeNode.getNodeValue() );
			LOGGER.debug( "Parent column name set in metadata is [" + metaData.getParentColumn() + "]" );
		}
	}

	/**
	 * default value of searchable is true
	 * 
	 * @param column
	 * @param columnNode
	 */
	private void setSearchable( Column column, Node columnNode )
	{
		Node tempNode = columnNode.getAttributes().getNamedItem( COLUMN_SEARCHABLE );
		boolean searchable = true;
		if( tempNode != null && tempNode.getNodeValue().trim().length() > 1 )
		{
			searchable = Boolean.parseBoolean( tempNode.getNodeValue() );
		}
		column.setSearchable( searchable );
	}

	/**
	 * default value of searchable is true
	 * 
	 * @param column
	 * @param columnNode
	 */
	private void setDynamic( Column column, Node columnNode )
	{
		Node tempNode = columnNode.getAttributes().getNamedItem( COLUMN_DYNAMIC );
		boolean dynamic = false;
		if( tempNode != null && tempNode.getNodeValue().trim().length() > 1 )
		{
			dynamic = Boolean.parseBoolean( tempNode.getNodeValue() );
		}
		column.setDynamic( dynamic );
	}

	/**
	 * default value of sortable is true
	 * 
	 * @param column
	 * @param columnNode
	 */
	private void setSortable( Column column, Node columnNode )
	{
		Node tempNode = columnNode.getAttributes().getNamedItem( COLUMN_SORTABLE );
		boolean sortable = true;
		if( tempNode != null && tempNode.getNodeValue().trim().length() > 1 )
		{
			sortable = Boolean.parseBoolean( tempNode.getNodeValue() );
		}
		column.setSortable( sortable );
	}

	/**
	 * Sets the addTitle,editTitle,listTitle .
	 * 
	 * @param metaData
	 * @param tableNode
	 * @param tableInnerNodeName
	 */
	private void setTitles( DOMetaData metaData, Node tableNode, String tableInnerNodeName )
	{
		LOGGER.debug( "entering : setTitles . metaData[" + metaData + "] tableNode[" + tableNode
				+ "] tableInnerNodeName[" + tableInnerNodeName + "]" );

		if( tableInnerNodeName.equalsIgnoreCase( ADD_PAGE_TITLE ) )
		{
			String addTitle = getTextValue( (Element) tableNode, ADD_PAGE_TITLE );
			metaData.setAddPageTitleKey( addTitle );
		}
		if( tableInnerNodeName.equalsIgnoreCase( EDIT_PAGE_TITLE ) )
		{
			String editTitle = getTextValue( (Element) tableNode, EDIT_PAGE_TITLE );
			metaData.setEditPageTitleKey( editTitle );
		}
		if( tableInnerNodeName.equalsIgnoreCase( LIST_PAGE_TITLE ) )
		{
			String listTitle = getTextValue( (Element) tableNode, LIST_PAGE_TITLE );
			metaData.setListPageTitleKey( listTitle );
		}
		LOGGER.trace( "exiting : setTitles." );
	}

	// default value of view-link is false
	private void setViewLink( Column column, Node columnNode )
	{
		Node tempNode = columnNode.getAttributes().getNamedItem( COLUMN_VIEW_LINK );
		boolean viewLink = false;
		if( tempNode != null && tempNode.getNodeValue().trim().length() > 1 )
		{
			viewLink = Boolean.parseBoolean( tempNode.getNodeValue() );
		}
		column.setViewLink( viewLink );
	}

	public static void main( String[] args ) throws SAXException, IOException, ApplicationException
	{
		// new XMLSchemaManager().getDOMetaData( "Supplier" );
		DOMetaData metaData = XMLSchemaManager.getSharedInstance().getDOMetaData( "Supplier" );
		LOGGER.debug( "Final dometadata is [" + metaData + "]" );
		ResourceFinder.findResource( "conf/DOMetaData.xml" );
	}

}
