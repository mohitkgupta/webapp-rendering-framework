package com.vedantatree.comps.dof.pagination.server;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.StringUtils;
import org.vedantatree.utils.exceptions.ApplicationException;
import org.vedantatree.utils.exceptions.ComponentException;
import org.vedantatree.utils.exceptions.IErrorCodes;

import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.JNDILookupManager;


/**
 * Object of this class represent the user/client for pagination service.
 * 
 * TODO
 * It can be exposed to client also, so that data can be sent using an object rather than in attributes.
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
class PaginationUser
{

	private static Log				LOGGER	= LogFactory.getLog( PaginationUser.class );
	/**
	 * Object which will provide the data for pagination service. User will provide the implementation for this
	 * object. It can be a session bean or a simple class.
	 */
	private IPaginationDataProvider	dataProvider;

	/**
	 * Default page size set by user or system
	 */
	private int						defaultPageSize;

	/**
	 * It indicates the total number of records existed for current parameter set. This field is set at the time of
	 * registration and at the time of updating the pagination user
	 */
	private int						totalRecords;

	/**
	 * Type of the data objects
	 */
	private Class					dataType;

	/**
	 * A data qualifier string which may be used by developers in their data provider implementation. Like
	 * developers can put any business logic check based on data qualifier
	 */
	private String					dataQualifier;

	/**
	 * Index of the current page returned by pagination service
	 */
	private int						currentPageIndex;

	/**
	 * Search clause which should be used by data provider while returning the data
	 */
	private String					searchClause;

	/**
	 * Order by clause which should be used by data provider while returning the data
	 */
	private String					orderByClause;

	/**
	 * The time when user access the pagination service last time. This property is mainly used to deactivate the
	 * pagination user data from pagination manager cache if user is not activated since long. This property is
	 * configurable in property files
	 */
	private long					lastAccess;

	/**
	 * DOMetadata for the pagination request
	 */
	private DOMetaData				doMetaData;

	/**
	 * User can also set complete hql query instead of search and order by clauses. Pagination manager and data
	 * provider will use this query in that case
	 */
	private String					completeHql;

	/**
	 * It denotes the current group of data. It is used to implement the entity based system where applcation can
	 * have data for multiple entities and we want to return only that data for which user has the rights
	 */
	private Object					objectGroup;

	/**
	 * 
	 * @param dataProviderJndiName JNDI name of data provider in case of session bean as per current assumptions
	 * @param homeClass Bean home class name
	 * @param defaultPageSize default page size for the pagination data
	 * @param dataType type of the data object
	 * @param dataQualifier qualifier for data to be used in data provider
	 * @param doMetaData DOMetadata for the requests
	 * @param objectGroup Object group for current requests
	 * @throws ComponentException if there is any problem
	 */
	PaginationUser( String dataProviderJndiName, Class homeClass, int defaultPageSize, Class dataType,
			String dataQualifier, DOMetaData doMetaData, Object objectGroup ) throws ComponentException
	{

		Object dataProviderObject = JNDILookupManager.lookupRemoteEJB( dataProviderJndiName, homeClass );
		if( !( dataProviderObject instanceof IPaginationDataProvider ) )
		{
			ComponentException ce = new ComponentException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"DataProvider remote interface should implement IPaginationDataProvider interface. However the retrieved data provider is not of this type. dataProviderObject["
							+ dataProviderObject + "] implementedInterfaces["
							+ ( StringUtils.arrayToString( dataProviderObject.getClass().getInterfaces() ) ) + "]" );
			LOGGER.error( ce );
			throw ce;
		}
		this.dataProvider = (IPaginationDataProvider) dataProviderObject;

		this.defaultPageSize = defaultPageSize;
		this.dataType = dataType;
		this.dataQualifier = dataQualifier;
		this.currentPageIndex = 1;
		this.doMetaData = doMetaData;
		this.objectGroup = objectGroup;
		this.lastAccess = System.currentTimeMillis();
	}

	/**
	 * Same as above. The only difference is that we are taking simple class name of data provider here instead of a
	 * session bean. It assumes that data provider will reside on same server where pagination manager is working.
	 * Hence we shall just instantiate the instance of data provider and will use it
	 */
	PaginationUser( String dataProviderName, int defaultPageSize, Class dataType, String dataQualifier,
			DOMetaData doMetaData, Object objectGroup ) throws ComponentException
	{
		try
		{
			// using thread context class loader so that at server, it can load the classes in context of server current
			// thread
			this.dataProvider = (IPaginationDataProvider) Class
					.forName( dataProviderName, true, Thread.currentThread().getContextClassLoader() ).newInstance();
		}
		catch( InstantiationException e )
		{
			LOGGER.error( e.getMessage() );
			throw new ComponentException( 17, "ComponentExcetion", e );
		}
		catch( IllegalAccessException e )
		{
			LOGGER.error( e.getMessage() );
			throw new ComponentException( 15, "ComponentExcetion", e );
		}
		catch( ClassNotFoundException e )
		{
			LOGGER.error( e.getMessage() );
			throw new ComponentException( 18, "ComponentExcetion", e );
		}

		this.defaultPageSize = defaultPageSize;
		this.dataType = dataType;
		this.dataQualifier = dataQualifier;
		this.currentPageIndex = 1;
		this.doMetaData = doMetaData;
		this.objectGroup = objectGroup;
		this.lastAccess = System.currentTimeMillis();
	}

	void updateCache() throws ComponentException
	{
		searchClause = addObjectGroupClause( objectGroup, searchClause, doMetaData );
		searchClause = addDOMetadataQuery( searchClause, doMetaData );
		updateTotalRecordCount();
	}

	Collection getPaginationData() throws ComponentException
	{
		try
		{
			return dataProvider.getPaginatedData( currentPageIndex, defaultPageSize, dataQualifier, searchClause,
					orderByClause, doMetaData, completeHql );
		}
		catch( ApplicationException e )
		{
			ComponentException ce = new ComponentException( e.getErrorCode(),
					"Problem while getting data from dataprovider", e );
			LOGGER.error( ce );
			throw ce;

		}
	}

	private void updateTotalRecordCount() throws ComponentException
	{
		try
		{
			// dometadata, completehql
			int totalRecords = dataProvider.getTotalNumberOfRecords( dataType, dataQualifier, searchClause, objectGroup,
					doMetaData, completeHql );
			LOGGER.debug( "totalRecords[" + totalRecords + "]" );
			setTotalRecords( totalRecords );
		}
		catch( ApplicationException e )
		{
			ComponentException ce = new ComponentException( e.getErrorCode(),
					"Problem while getting total number of records from dataprovider", e );
			LOGGER.error( ce );
			throw ce;
		}
	}

	private String addObjectGroupClause( Object objectGroup, String searchClause, DOMetaData doMetaData )
	{
		if( objectGroup == null )
		{
			return searchClause;
		}
		// special case handling to manage the users listing. Should be removed
		if( doMetaData.getClassName() != null && doMetaData.getClassName().equals( "Users" ) )
		{
			return searchClause;
		}
		if( searchClause != null && searchClause.trim().length() > 0 )
		{
			searchClause = ( new StringBuilder() ).append( searchClause ).append( " and a.objectGroup='" )
					.append( objectGroup.toString() ).append( "'" ).toString();
		}
		else
		{
			searchClause = ( new StringBuilder() ).append( " a.objectGroup='" ).append( objectGroup.toString() )
					.append( "'" ).toString();
		}
		LOGGER.debug( "searchClause-after-adding-objectGroup[ " + searchClause + " ]" );
		return searchClause;
	}

	private String addDOMetadataQuery( String searchClause, DOMetaData doMetaData )
	{
		// if any explicit query part is specified with DOMetadata for listing, add this to search clause
		LOGGER.debug( "adding DOMetadata query to search clause. current-search-clause[" + searchClause + "]" );
		if( doMetaData != null )
		{
			String queryString = doMetaData.getQueryForList();
			if( StringUtils.isQualifiedString( queryString ) )
			{
				queryString = " ( " + queryString + " ) ";
				if( StringUtils.isQualifiedString( searchClause ) )
				{
					searchClause = searchClause + " and  " + queryString;
				}
				else
				{
					searchClause = queryString;
				}
			}
		}
		LOGGER.debug( "updated-search-clause[" + searchClause + "]" );
		return searchClause;
	}

	public String getCompleteHql()
	{
		return completeHql;
	}

	public int getCurrentPageIndex()
	{
		return currentPageIndex;
	}

	public IPaginationDataProvider getDataProvider()
	{
		return dataProvider;
	}

	public String getDataQualifier()
	{
		return dataQualifier;
	}

	public Class getDataType()
	{
		return dataType;
	}

	public int getDefaultPageSize()
	{
		return defaultPageSize;
	}

	public DOMetaData getDoMetaData()
	{
		return doMetaData;
	}

	public long getLastAccess()
	{
		return lastAccess;
	}

	public Object getObjectGroup()
	{
		return objectGroup;
	}

	public String getOrderByClause()
	{
		return orderByClause;
	}

	public String getSearchClause()
	{
		return searchClause;
	}

	void setCompleteHql( String completeHql )
	{
		this.completeHql = completeHql;
	}

	void setCurrentPageIndex( int start )
	{
		this.currentPageIndex = start;
	}

	void setDataProvider( IPaginationDataProvider dataProvider )
	{
		this.dataProvider = dataProvider;
	}

	void setDataQualifier( String dataQualifier )
	{
		this.dataQualifier = dataQualifier;
	}

	void setDataType( Class dataType )
	{
		this.dataType = dataType;
	}

	void setDefaultPageSize( int defaultPageSize )
	{
		this.defaultPageSize = defaultPageSize;
	}

	void setDoMetaData( DOMetaData doMetaData )
	{
		this.doMetaData = doMetaData;
	}

	void setLastAccess( long lastAccess )
	{
		this.lastAccess = lastAccess;
	}

	void setObjectGroup( Object objectGroup )
	{
		this.objectGroup = objectGroup;
	}

	void setOrderByClause( String orderByClause )
	{
		this.orderByClause = orderByClause;
	}

	void setSearchClause( String searchClause )
	{
		this.searchClause = searchClause;
	}

	void setTotalRecords( int totalRecords )
	{
		this.totalRecords = totalRecords;
	}

	public int getTotalRecords()
	{
		return totalRecords;
	}

}
