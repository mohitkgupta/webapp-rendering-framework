package com.vedantatree.comps.dof;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.spi.ErrorCode;
import org.vedantatree.utils.BeanUtils;
import org.vedantatree.utils.exceptions.ApplicationException;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;

import com.vedantatree.comps.dof.web.WebAppConstants;
import com.vedantatree.comps.dof.web.vdo.Column;


/**
 * Utility classes having generic functionality methods to be used by DO Framework
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class DOFUtils
{

	private static final Log LOGGER = LogFactory.getLog( DOFUtils.class );

	/**
	 * This method is used to flush the information from request cache, whenever a requested is served completely by the
	 * server.
	 * 
	 * @param request Request object for current operation
	 * @param doMetadata Metadata object
	 */
	public static void flushRequestCache( HttpServletRequest request, DOMetaData doMetadata )
	{
		LOGGER.trace( "flushRequestCache: request[" + request + "]  classObject[" + doMetadata + "]" );
		request.setAttribute( DOMetaData.DOMEATADATA_KEY, null );
		ViewHelper viewHelper = (ViewHelper) request.getSession().getAttribute( doMetadata.getViewHelperKey() );
		if( viewHelper != null )
		{
			viewHelper.reset();
		}
		request.getSession().setAttribute( doMetadata.getViewHelperKey(), null );
		request.getSession().setAttribute( doMetadata.getUIMetadataName(), null );
	}

	/**
	 * This method is used to initialize the information cache with request, whenever a requested is received by server.
	 * 
	 * @param request Request object for current operation
	 * @param doMetadata Metadata object
	 */
	public static void initializeRequestCache( HttpServletRequest request, DOMetaData doMetadata )
			throws ApplicationException
	{
		LOGGER.trace( "configureHTTPRequestFromDOMetadata: request[" + request + "]  classObject[" + doMetadata + "]" );

		request.setAttribute( DOMetaData.DOMEATADATA_KEY, doMetadata );
		String viewHelperKey = doMetadata.getViewHelperKey();
		ViewHelper viewHelper = (ViewHelper) request.getSession().getAttribute( viewHelperKey );
		if( viewHelper != null )
		{
			viewHelper.reset();
		}
		else
		{
			viewHelper = doMetadata.createViewHelperInstance();
			request.getSession().setAttribute( viewHelperKey, viewHelper );
		}

		viewHelper.setRequest( request );

		LOGGER.debug( "viewHelper[" + viewHelper + "] with key[" + viewHelperKey + "]" );

		// TODO : should be removed as further only view helper should be used
		// request.setAttribute( DOMetaData.DBNAMELIST, doMetadata.getListColumnDBNames() );
		// if( request.getAttribute( DOMetaData.PAGE_ACTION ) == null )
		// {
		// request.setAttribute( DOMetaData.PAGE_ACTION, doMetadata.getPageAction() );
		// }
	}

	/**
	 * It returns the database name for a column corresponding to its display name on UI
	 * 
	 * @param doMetadata metadata object
	 * @param columnDisplayName Display name of the column
	 * @return Database name if found
	 */
	public static String getColumnDBName( DOMetaData doMetadata, String columnDisplayName )
	{
		List<Column> columns = doMetadata.getColumns();

		for( Column column : columns )
		{
			if( column.getDisplayName().equals( columnDisplayName ) )
			{
				LOGGER.debug( "columnDisplayName[ " + columnDisplayName + " ]displayName[ " + column.getDisplayName()
						+ " ] dbName[" + column.getDbName() + "]" );
				String dbName = column.getDbName();
				if( dbName == null )
				{
					if( !column.isDynamic() )
					{
						SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
								"DBName of a column MUST not be null if it is not a dynamic column. column-displayName["
										+ column.getDisplayName() + "]" );
						LOGGER.error( se );
						throw se;
					}
					return null;
				}
				return dbName.trim();
			}
		}

		// returning null from here as abstract action then try to use the specified column name with sort string in
		// request as column name
		// this is because now we have changed the implementation and specifying the actual DBName of column with sort
		// links on UI
		LOGGER.debug( "No Matching column found in METADATA dbColumnName[ " + columnDisplayName + " ]" );
		// throw new IllegalStateException( "No Matching column found in METADATA dbColumnName[ " + columnDisplayName
		// + " ]" );
		return null;
	}

	/**
	 * Method to return comma separated string of list items
	 * 
	 * <p>
	 * TODO - Should be moved to collection utils
	 * 
	 * @param idList Collection of ids
	 * @return String having comma separated ids
	 */
	public static String getCommaSeparatedStringForCollection( List idList )
	{
		String commaSeparatedList = "";
		for( int i = 0; i < idList.size(); i++ )
		{
			if( i == 0 )
			{
				commaSeparatedList = idList.get( i ).toString();
			}
			else
			{
				commaSeparatedList = ( new StringBuilder() ).append( commaSeparatedList ).append( " ," )
						.append( idList.get( i ).toString() ).toString();
			}
		}

		LOGGER.debug( "commaSeparatedList[ " + commaSeparatedList + " ]" );
		return commaSeparatedList;
	}

	/**
	 * It returns the column list part of a query for specified metadata
	 * 
	 * @param classObject Metadata object
	 * @param idColumnName Name of the id column, TODO - it can be get from metadata also
	 * @return Formed query
	 */
	public static String getListColumnQueryString( DOMetaData classObject, String idColumnName )
	{
		List columns = classObject.getColumns();
		String queryString = ( new StringBuilder() ).append( "a." ).append( idColumnName ).append( " , " ).toString();
		Iterator i$ = columns.iterator();
		do
		{
			if( !i$.hasNext() )
				break;
			Column column = (Column) i$.next();
			if( column.isListPageColumn() && !column.isDynamic() )
			{
				queryString = ( new StringBuilder() ).append( queryString ).append( "a." )
						.append( column.getDbName().trim() ).append( "," ).toString();
			}
		} while( true );
		queryString = queryString.substring( 0, queryString.lastIndexOf( ',' ) );
		return queryString;
	}

	/**
	 * Get searchClause by providing DometaData and searchColumn and SearchValue
	 * 
	 * @param doMetaData
	 * @param searchColumnValue
	 * @param searchColumnName
	 * @return searchClause
	 * @author Arvind
	 * 
	 *         http://stackoverflow.com/questions/1031844/oracle-db-how-can-i-write-query-ignoring-case
	 */
	public static String getPaginationSearchString( DOMetaData doMetaData, String searchColumnValue,
			String searchColumnName )
	{

		if( searchColumnValue != null && searchColumnValue.contains( "'" ) )
		{
			searchColumnValue = "@";
		}
		String searchQueryString = "";
		List columns = doMetaData.getColumns();
		if( searchColumnName != null )
			// kind of full text search through logic for search-able columns
			if( searchColumnName.equalsIgnoreCase( "default" ) )
			{
				Iterator i$ = columns.iterator();
				do
				{
					if( !i$.hasNext() )
						break;
					Column column = (Column) i$.next();
					if( !column.isSearchable() )
						continue;
					// handling for data type of data. Here we are using trunc method to truncate the time information
					// from timestamp
					if( column.getDataType() != null && column.getDataType().toLowerCase().contains( "date" ) )
					{
						searchQueryString = searchQueryString + " (trunc(a." + column.getDbName() + ") = "
								+ ( searchColumnValue == null ? "null" : searchColumnValue ) + ")";
					}
					else
					{
						// added upper clause for making query case insensitive
						searchQueryString = ( new StringBuilder() ).append( searchQueryString ).append( " upper(a." )
								.append( column.getDbName().trim() ).append( ") like upper('%" )
								.append( searchColumnValue != null && !searchColumnValue.equalsIgnoreCase( "null" )
										? searchColumnValue.trim()
										: "" )
								.append( "%') or" ).toString();
					}
					break;
				} while( true );
				LOGGER.debug( ( new StringBuilder() ).append( "SERACH QUERY STRING in DofUtil[ " )
						.append( searchQueryString ).append( " ]" ).toString() );

				searchQueryString = searchQueryString.substring( 0, searchQueryString.lastIndexOf( "or" ) );
			}
			else
			{
				Column column = doMetaData.getColumnByDisplayName( searchColumnName );
				if( column.isDynamic() )
				{
					SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
							"A dynamic column can not be 'searchable'. There is some problem is system. column["
									+ column + "]" );
					LOGGER.error( se );
					throw se;
				}

				// handling for data type of data. Here we are using trunc method to truncate the time information from
				// timestamp
				if( column.getDataType() != null && column.getDataType().toLowerCase().contains( "date" ) )
				{
					searchQueryString = searchQueryString + " (trunc(a." + column.getDbName() + ") = "
							+ ( searchColumnValue == null ? "null" : searchColumnValue ) + ")";
				}
				else
				{
					// added upper clause for making query case insensitive
					searchQueryString = ( new StringBuilder() ).append( searchQueryString ).append( " upper(a." )
							.append( getColumnDBName( doMetaData, searchColumnName ).trim() )
							.append( ") like upper('%" )
							.append( searchColumnValue != null && !searchColumnValue.equalsIgnoreCase( "null" )
									? searchColumnValue.trim()
									: "" )
							.append( "%') " ).toString();
				}

				LOGGER.debug( "SERACH QUERY STRING[ " + searchQueryString + " ]" );
			}
		return searchQueryString;
	}

	public static List getSearchList( DOMetaData doMetaData, String searchColumnValue, String searchColumnName,
			List dataList )
	{

		LOGGER.debug( "searchColumnValue[ " + searchColumnValue + " ] searchColumnName[ " + searchColumnName + " ]" );
		String searchQueryString = "";
		List<Column> columns = doMetaData.getColumns();
		List valueList = new ArrayList();
		if( searchColumnName.equalsIgnoreCase( "default" ) )
		{

			for( int i = 0; i < dataList.size(); i++ )
			{
				for( Column column : columns )
				{
					if( column.isSearchable() )
					{
						if( column.isDynamic() )
						{
							SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
									"A dynamic column can not be 'searchable'. There is some problem is system. column["
											+ column + "]" );
							LOGGER.error( se );
							throw se;
						}

						searchQueryString = column.getDbName().trim();
						LOGGER.debug( "searchQueryString " + searchQueryString );
						Object object;
						try
						{
							object = BeanUtils.invokeMethod( dataList.get( i ), "get" + searchQueryString, null );

							if( object.toString().contains( searchColumnValue ) )
							{
								LOGGER.debug( "in if condition in searchList()" );
								valueList.add( dataList.get( i ) );
							}
						}
						catch( ApplicationException e )
						{
							// TODO improve the exception
							LOGGER.error( "Exception occured in DOFUtils", e );
							throw new SystemException( ErrorCode.GENERIC_FAILURE, "Exception occured in DOFUtils", e );
						}
					}
				}
			}
			LOGGER.debug( "Size of returned list is in searchList::" + valueList.size() );
			return valueList;
		}
		else
		{
			for( int i = 0; i < dataList.size(); i++ )
			{
				for( Column column : columns )
				{
					if( column.isSearchable() )
					{
						if( column.isDynamic() )
						{
							SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
									"A dynamic column can not be 'searchable'. There is some problem is system. column["
											+ column + "]" );
							LOGGER.error( se );
							throw se;
						}
						if( column.getDisplayName().trim().equalsIgnoreCase( searchColumnName ) )
						{
							searchQueryString = column.getDbName().trim();
							Object object;
							try
							{
								object = BeanUtils.invokeMethod( dataList.get( i ), "get" + searchQueryString, null );

								if( object.toString().contains( searchColumnValue ) )
								{
									valueList.add( dataList.get( i ) );
								}
							}
							catch( ApplicationException e )
							{
								// TODO improve the exception
								LOGGER.error( "Exception occured in DOFUtils", e );
								throw new SystemException( ErrorCode.GENERIC_FAILURE, "Exception occured in DOFUtils",
										e );
							}
						}
					}
				}
			}
			LOGGER.debug( "Size of returned list is searchList::" + valueList.size() );
			return valueList;
		}
	}

	public static String getSearchPath( String searchColumn, String searchValue, String sortString, String isDescending,
			String searchAct )
	{
		String searchPath = "";

		if( searchColumn != null && !searchColumn.equalsIgnoreCase( "default" ) && searchValue != null )
		{
			searchPath = "&searchColumn=" + searchColumn + "&searchValue=" + searchValue;

		}
		else if( searchColumn == null && searchValue != null )
		{
			searchPath = "&searchColumn=" + searchColumn + "&searchValue=" + searchValue;

		}
		else if( searchColumn != null && !searchColumn.equalsIgnoreCase( "default" ) && searchValue == null )
		{
			searchPath = "&searchColumn=" + searchColumn;

		}
		if( sortString != null && !sortString.equalsIgnoreCase( "" ) )
		{
			searchPath += "&sortColumn=" + sortString + "&isDescending=" + isDescending;
		}
		if( searchAct != null && !searchAct.equalsIgnoreCase( "" ) )
		{
			searchPath += "&searchAct=" + searchAct;
		}
		LOGGER.debug( "Value of searchPath[ " + searchPath + " ]" );
		return searchPath;
	}

	public static String getSearchString( DOMetaData doMetaData, String searchColumnValue, String searchColumnName )
	{
		if( searchColumnValue != null && searchColumnValue.contains( "'" ) )
			searchColumnValue = "@";
		String searchQueryString = "";
		List columns = doMetaData.getColumns();
		if( searchColumnName != null )
			if( searchColumnName.equalsIgnoreCase( "default" ) )
			{
				Iterator i$ = columns.iterator();
				do
				{
					if( !i$.hasNext() )
						break;
					Column column = (Column) i$.next();
					if( !column.isSearchable() )
						continue;

					// handling for data type of data. Here we are using trunc method to truncate the time information
					// from
					// timestamp
					if( column.getDataType() != null && column.getDataType().toLowerCase().contains( "date" ) )
					{
						searchQueryString = searchQueryString + " (trunc(" + column.getDbName() + ") = "
								+ ( searchColumnValue == null ? "null" : searchColumnValue ) + ")";
					}
					else
					{
						searchQueryString = searchQueryString = ( new StringBuilder() ).append( searchQueryString )
								.append( column.getDbName().trim() ).append( " like '%" )
								.append( searchColumnValue == null ? "" : searchColumnValue ).append( "%' or" )
								.toString();
					}
					break;
				} while( true );
				searchQueryString = searchQueryString.substring( 0, searchQueryString.lastIndexOf( "or" ) );
			}
			else
			{
				Column column = doMetaData.getColumnByDisplayName( searchColumnName );
				if( column.isDynamic() )
				{
					SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
							"A dynamic column can not be 'searchable'. There is some problem is system. column["
									+ column + "]" );
					LOGGER.error( se );
					throw se;
				}
				// handling for data type of data. Here we are using trunc method to truncate the time information from
				// timestamp
				if( column.getDataType() != null && column.getDataType().toLowerCase().contains( "date" ) )
				{
					searchQueryString = searchQueryString + " (trunc(" + column.getDbName() + ") = "
							+ ( searchColumnValue == null ? "null" : searchColumnValue ) + ")";
				}
				else
				{
					searchQueryString = searchQueryString = ( new StringBuilder() ).append( searchQueryString )
							.append( getColumnDBName( doMetaData, searchColumnName ).trim() ).append( " like '%" )
							.append( searchColumnValue == null ? "" : searchColumnValue ).append( "%' " ).toString();
				}
			}
		LOGGER.debug( ( new StringBuilder() ).append( "SERACH QUERY STRING[ " ).append( searchQueryString )
				.append( " ]" ).toString() );
		return searchQueryString;
	}

	/**
	 * Returns array of all columns requied to show on list page
	 * 
	 * @param doMetaData
	 * @return array of all columns requied to show on list page
	 * @author Arvind
	 */
	public static String[] getSelectStringArray( DOMetaData doMetaData )
	{

		LOGGER.trace( ( new StringBuilder() ).append( "IN getSelectStringArray doMetaData[" ).append( doMetaData )
				.append( "]" ).toString() );

		List columns = doMetaData.getColumns();
		String selectColumns[] = new String[columns.size() + 1];
		LOGGER.debug( ( new StringBuilder() ).append( "DO meta data id column's DB name[" )
				.append( doMetaData.getIdColumn().getDbName() ).append( "]" ).toString() );

		selectColumns[0] = doMetaData.getIdColumn().getDbName().trim();
		int i = 1;
		LOGGER.debug( ( new StringBuilder() ).append( "Size of column list[" ).append( columns.size() ).append( "]" )
				.toString() );

		Iterator i$ = columns.iterator();
		do
		{
			if( !i$.hasNext() )
				break;
			Column column = (Column) i$.next();
			if( column.isListPageColumn() && !column.isDynamic() )
			{
				selectColumns[i] = column.getDbName().trim();
				i++;
			}
		} while( true );
		String resultColumns[] = new String[i];
		for( int j = 0; j < i; j++ )
		{
			LOGGER.debug(
					( new StringBuilder() ).append( "COLUMN[" ).append( selectColumns[j] ).append( "]" ).toString() );
			resultColumns[j] = selectColumns[j];
		}

		return resultColumns;
	}

	public static void setExceptionPageParameters( ServletRequest request, Object exceptionObject )
	{
		setExceptionPageParameters( request, exceptionObject, null, null, null );
	}

	/**
	 * this method will be used by the action class to set the request attreibute for the Error page when any exception
	 * occured in the action classes and these ttribute will be fetched on error page and will display the message
	 * accordingly and will go back after hiting the back button
	 * 
	 * @param request request Object used in action class
	 * @param exceptionObject object of exception occured
	 * @param previousPageUrl url of page at which we will go after hitting OK button
	 * @param tilesDefinition of the action
	 */
	public static void setExceptionPageParameters( ServletRequest request, Object exceptionObject,
			String previousPageUrl, String tilesDefinition )
	{
		setExceptionPageParameters( request, exceptionObject, previousPageUrl, tilesDefinition, null );
	}

	/**
	 * this method will be used by the action class to set the request attreibute for the Error page when any exception
	 * occured in the action classes and these ttribute will be fetched on error page and will display the message
	 * accordingly and will go back after hiting the back button
	 * 
	 * @param request request Object used in action class
	 * @param exceptionObject object of exception occured
	 * @param previousPageUrl url of page at which we will go after hitting OK button
	 * @param tilesDefinition of the action
	 * @param message for servlets
	 */
	public static void setExceptionPageParameters( ServletRequest request, Object exceptionObject,
			String previousPageUrl, String tilesDefinition, String errorMessage )
	{
		LOGGER.trace( "setExceptionPageParameters: exceptionObject[ " + exceptionObject + " ]previousPageUrl[ "
				+ previousPageUrl + " ]tilesDefinition[ " + tilesDefinition + " ]request[ " + request
				+ " ]errorMessage[ " + errorMessage + " ]" );

		if( !WebAppConstants.DEFAULT_VALUE.equals( tilesDefinition ) )
		{
			request.setAttribute( WebAppConstants.EXCEPTION_TILES_DEFINITION, tilesDefinition );
		}
		if( !WebAppConstants.DEFAULT_VALUE.equals( exceptionObject ) )
		{
			request.setAttribute( WebAppConstants.EXCEPTION_OBJECT, exceptionObject );
		}
		if( !WebAppConstants.DEFAULT_VALUE.equals( previousPageUrl ) )
		{
			request.setAttribute( WebAppConstants.EXCEPTION_PREV_PAGE_URL, previousPageUrl );
		}
		if( !WebAppConstants.DEFAULT_VALUE.equals( errorMessage ) )
		{
			request.setAttribute( WebAppConstants.ERRORMESSAGE, errorMessage );
		}
	}

	public static void resetExceptionPageParameters( ServletRequest request )
	{
		LOGGER.trace( "Resettign exception page Parameters in DOFUtils" );
		setExceptionPageParameters( request, null, null, null, null );
	}

	/**
	 * Returns Query for fetching data from database for given doMetaData, search column information
	 * 
	 * @param doMetaData Metadata object
	 * @param searchColumnValue Value for search column
	 * @param searchColumnName Name of the search column
	 * @return Full Query string to fetch data from database for given do metadata
	 * @author Arvind
	 * 
	 * @deprecated Not in Use, use getPaginationSearchQuery instead
	 * @see getPaginationSearchQuery
	 */
	@Deprecated
	public static String getPaginationQuery( DOMetaData doMetaData, String searchColumnValue, String searchColumnName )
	{

		if( searchColumnValue != null && searchColumnValue.contains( "'" ) )
		{
			searchColumnValue = "@";
		}

		String searchQueryString = "Select ";
		List columns = doMetaData.getColumns();
		for( int i = 0; i < columns.size(); i++ )
		{
			Column col = (Column) columns.get( i );
			if( i == columns.size() - 1 )
				searchQueryString = ( new StringBuilder() ).append( searchQueryString ).append( " a." )
						.append( col.getDbName() ).toString();
			else
				searchQueryString = ( new StringBuilder() ).append( searchQueryString ).append( " a." )
						.append( col.getDbName() ).append( "," ).toString();
		}

		String objectClassName = org.vedantatree.utils.StringUtils.getSimpleClassName( doMetaData.getClassName() );
		searchQueryString = ( new StringBuilder() ).append( searchQueryString ).append( " " ).append( objectClassName )
				.append( " a " ).toString();
		if( searchColumnName != null )
		{
			searchQueryString = ( new StringBuilder() ).append( searchQueryString ).append( " where " ).toString();
			if( searchColumnName.equalsIgnoreCase( "default" ) )
			{
				Iterator i$ = columns.iterator();
				do
				{
					if( !i$.hasNext() )
					{
						break;
					}
					Column column = (Column) i$.next();
					if( !column.isSearchable() )
					{
						// it means dynamic column will also not pass through this, as dynamic column can not be set as
						// searchable or sortable
						continue;
					}
					if( column.getDataType() != null && column.getDataType().toLowerCase().contains( "date" ) )
					{
						// handling for data type of data. Here we are using trunc method to truncate the time
						// information from
						// timestamp
						searchQueryString = searchQueryString + " (trunc(a." + column.getDbName() + ") = "
								+ ( searchColumnValue == null ? "null" : searchColumnValue ) + ")";
					}
					else
					{
						searchQueryString = ( new StringBuilder() ).append( searchQueryString ).append( " a." )
								.append( column.getDbName().trim() ).append( " like '%" )
								.append( searchColumnValue == null ? "null" : searchColumnValue ).append( "%' or " )
								.toString();
					}
					break;
				} while( true );

				searchQueryString = searchQueryString.substring( 0, searchQueryString.lastIndexOf( "or" ) );
			}
			else
			{
				Column column = doMetaData.getColumnByDisplayName( searchColumnName );
				if( column.isDynamic() )
				{
					SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
							"A dynamic column can not be 'searchable'. There is some problem is system. column["
									+ column + "]" );
					LOGGER.error( se );
					throw se;
				}
				// handling for data type of data. Here we are using trunc method to truncate the time information from
				// timestamp
				if( column.getDataType() != null && column.getDataType().toLowerCase().contains( "date" ) )
				{
					searchQueryString = searchQueryString + " (trunc(a." + column.getDbName() + ") = "
							+ ( searchColumnValue == null ? "null" : searchColumnValue ) + ")";
				}
				else
				{
					searchQueryString = ( new StringBuilder() ).append( searchQueryString ).append( " a." )
							.append( getColumnDBName( doMetaData, searchColumnName ).trim() ).append( " like '%" )
							.append( searchColumnValue == null ? "null" : searchColumnValue ).append( "%' " )
							.toString();
				}
			}
		}
		LOGGER.debug( "search-query[ " + searchQueryString + " ]" );
		return searchQueryString;
	}

}
