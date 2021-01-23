package com.vedantatree.comps.dof.pagination.server;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.config.ConfigurationManager;
import org.vedantatree.utils.exceptions.ApplicationException;
import org.vedantatree.utils.exceptions.ComponentException;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;

import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.pagination.PaginationData;


/**
 * PaginationManager is responsible to manage the pagination requirement of various users. Design is based on Singleton
 * and Value List Handler Design Pattern.
 * 
 * <p>
 * Only one instance of Pagination Manager will exist in memory.
 * 
 * <p>
 * Anyone can register itself with PaginationManager as user of pagination specifying a customized data provider class
 * name (qualified class name or JNDI name for remote lookup) and some other required parameters. One unique id will be
 * assigned to user while registering and the same will be quoted by pagination user further while asking for any data.
 * 
 * <p>
 * Various methods like next, previous, first and last will provide the data to user. Pagination Manager will be
 * responsible to manage the current state, retrieve and return the required data to user.
 * 
 * <p>
 * So usage scenario would be -
 * <ul>
 * <li>Users will register itself with the Pagination Manager specifying all required parameters -
 * <li>A unique id will be assigned to user -
 * <li>Users will use the returned unique id to get data from pagination manager -
 * <li>After use, user will unregister itself -
 * <li>Data provider should be implemented by user in a customized way, hence can have all the business logic, even hard
 * coding as per business requirement data. Discuss if required.
 * </ul>
 * 
 * <p>
 * TODO: Can be a customized PaginationException - Need to decide
 * 
 * 
 * @author Arvind Pal
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public class PaginationManager
{

	private static Log							LOGGER				= LogFactory.getLog( PaginationManager.class );

	private static final String					PAGE_FIRST			= "first";
	private static final String					PAGE_PREVIOUS		= "previous";
	private static final String					PAGE_NEXT			= "next";
	private static final String					PAGE_LAST			= "last";

	/**
	 * Shared instance of Pagination Manager
	 */
	private static PaginationManager			paginationManager	= new PaginationManager();

	/**
	 * Cache for Pagination Users.
	 */
	private Hashtable<Integer, PaginationUser>	paginationUsers		= new Hashtable<>();

	/**
	 * Key for pagination users. It is gauranteed as unique for every user and maintained by Pagination Manager itself.
	 * Implementation wise, we are increase it by one on every new request.
	 */
	private int									paginationUserKey	= 0;

	private PaginationManager()
	{

		if( !ConfigurationManager.getSharedInstance().containsProperty( "CLEANUP_THREAD_SLEEP_TIME" ) )
		{
			try
			{
				ConfigurationManager.ensurePropertiesLoaded( "pagination.properties" );
			}
			catch( ApplicationException e )
			{
				LOGGER.fatal( "Problem while initializing PaginationManager", e );
				throw new SystemException( IErrorCodes.COMPONENT_INITIALIZATION_ERROR,
						"Problem while initializing PaginationManager", e );
			}
		}

		long idleUserTimeOut = 0;
		long idleUserCleanerThreadSleepTime = 0;
		if( ConfigurationManager.getSharedInstance().containsProperty( "USER_TIMEOUT" ) )
		{
			idleUserTimeOut = Integer
					.valueOf( ConfigurationManager.getSharedInstance().getPropertyValue( "USER_TIMEOUT" ) );
		}
		if( ConfigurationManager.getSharedInstance().containsProperty( "CLEANUP_THREAD_SLEEP_TIME" ) )
		{
			idleUserCleanerThreadSleepTime = Integer.valueOf(
					ConfigurationManager.getSharedInstance().getPropertyValue( "CLEANUP_THREAD_SLEEP_TIME" ) );
		}

		IdleUserCleaner cleaner = new IdleUserCleaner( idleUserTimeOut, idleUserCleanerThreadSleepTime );
		Thread cleanerThread = new Thread( cleaner );
		cleanerThread.setDaemon( true );
		cleanerThread.start();
	}

	public static PaginationManager getSharedInstance()
	{
		return paginationManager;
	}

	/**
	 * Register the pagination user with Pagination Manager
	 * 
	 * @param paginationDataProviderJndiName
	 * @param homeClass
	 * @param dataType Class of data to retrieve
	 * @param dataQualifier Any string representing data, like ApprovedPO. The qualifier will be passed to data
	 *        provider, based on which data provider can put checks and customized the data to return.
	 * @param defaultPageSize page size for pagination
	 * @param doMetaData TODO
	 * @return Unique id for registered user which will further be used while quoting for new data
	 */
	public int registerPaginationUser( String paginationDataProviderJndiName, Class homeClass, Class dataType,
			String dataQualifier, int defaultPageSize, DOMetaData doMetaData, Object objectGroup )
			throws ComponentException
	{
		LOGGER.debug( "Entering : registerPaginationUser" );

		PaginationUser paginationUser = new PaginationUser( paginationDataProviderJndiName, homeClass, defaultPageSize,
				dataType, dataQualifier, doMetaData, objectGroup );
		return registerPaginationUser( paginationUser );
	}

	/**
	 * Register the pagination user with Pagination Manager
	 * 
	 * @param paginationDataProviderName Customized data provider which will provide the paginated data on request from
	 *        pagination manager
	 * @param dataType Class of data to retrieve
	 * @param dataQualifier Any string representing data, like ApprovedPO. The qualifier will be passed to data
	 *        provider, based on which data provider can put checks and customized the data to return.
	 * @param defaultPageSize page size for pagination
	 * @param doMetaData TODO
	 * @param remote true if data provider should be accessed using JNDI, false if a new instance should be created by
	 *        Pagination Manager
	 * @return Unique id for registered user which will further be used while quoting for new data
	 */
	public int registerPaginationUser( String paginationDataProviderName, Class dataType, String dataQualifier,
			int defaultPageSize, DOMetaData doMetaData, Object objectGroup ) throws ComponentException
	{
		PaginationUser paginationUser = new PaginationUser( paginationDataProviderName, defaultPageSize, dataType,
				dataQualifier, doMetaData, objectGroup );
		return registerPaginationUser( paginationUser );
	}

	// TODO can be made public, if we decide to make pagination user data structure to end developers
	private int registerPaginationUser( PaginationUser paginationUser ) throws ComponentException
	{
		int userId = getUniquePaginationUserId();
		paginationUsers.put( userId, paginationUser );

		LOGGER.debug(
				"Users registered with Pagination Manager. userId[ " + userId + " ] user[ " + paginationUser + " ]" );

		// paginationUser.updateCache();
		return userId;
	}

	/**
	 * Unregister the already registerd user
	 * 
	 * @param paginationUserId The unique id which was returned to user while doing registration
	 * @return true if registered successfully
	 */
	public void unregisterPaginationUser( int paginationUserId ) throws ComponentException
	{
		if( paginationUsers.remove( paginationUserId ) == null )
		{
			ComponentException ce = new ComponentException( IErrorCodes.RESOURCE_NOT_FOUND,
					"Pagination Users not found for id: " + paginationUserId + ". It may be removed due to time out." );
			LOGGER.error( ce );
			throw ce;
		}
	}

	/**
	 * Funtion to update pagination user information
	 * 
	 * @param paginationUserId Users id by which user information will be updated
	 * @param currentPageIndex Set the current Page index if don't want to update then send Integer.MAX_VALUE
	 * @param searchClause searchClause that will be appended to query while fetching data from database.
	 * @param orderByClause
	 * @param doMetaData DOmeta data of class if any then data will be picked accordingly
	 * @param completeQuery If user need his own complex query for fethchig objects then user will provide complete
	 *        query if query!=null then records will be fetched according to this query and all other things will be
	 *        ingnored
	 */
	public void updateRegisteredUser( int paginationUserId, int currentPageIndex, String searchClause,
			String orderByClause, DOMetaData doMetaData, String completeQuery, Object objectGroup )
			throws ComponentException
	{

		PaginationUser paginationUser = paginationUsers.get( paginationUserId );
		LOGGER.debug( "paginationUser[ " + paginationUser + " ] userId[ " + paginationUserId + " ]" );
		if( paginationUser == null )
		{
			LOGGER.error( "Pagination Users not found for id: " + paginationUserId );
			ComponentException ce = new ComponentException( IErrorCodes.RESOURCE_NOT_FOUND,
					"Pagination Users not found for id: " + paginationUserId + ". It may be removed due to time out." );
			LOGGER.error( ce );
			throw ce;
		}

		// TODO document this case please
		if( currentPageIndex != Integer.MAX_VALUE )
		{
			paginationUser.setCurrentPageIndex( currentPageIndex );
		}
		paginationUser.setSearchClause( searchClause );
		paginationUser.setOrderByClause( orderByClause );
		paginationUser.setDoMetaData( doMetaData );
		paginationUser.setCompleteHql( completeQuery );
		paginationUser.setObjectGroup( objectGroup );

		paginationUser.updateCache();
	}

	/**
	 * Function will return all data needed to list on list page
	 * 
	 * @param paginationUserId -Key of hash map for pagination user
	 * @param pageAction - pageAction will decide that which function will called for paging
	 * @return Colletction of records
	 */
	public PaginationData getPaginationData( int paginationUserId, String pageAction ) throws ComponentException
	{

		LOGGER.trace( "getPaginationData: paginationUserId[" + paginationUserId + "] pageAction[" + pageAction + "]" );

		PaginationUser paginationUser = paginationUsers.get( paginationUserId );
		if( paginationUser == null )
		{
			ComponentException ce = new ComponentException( IErrorCodes.RESOURCE_NOT_FOUND,
					"Pagination Users not found for id: " + paginationUserId
							+ ". It may be removed due to time out. currentUsers[" + paginationUsers + "]" );
			LOGGER.error( ce );
			throw ce;
		}

		// update access time
		paginationUser.setLastAccess( System.currentTimeMillis() );

		Collection dataList = null;
		if( pageAction == null || PAGE_FIRST.equals( pageAction ) )
		{
			dataList = getFirstPageData( paginationUser );
		}
		else if( PAGE_NEXT.equals( pageAction ) )
		{
			dataList = getNextPageData( paginationUser );
		}
		else if( PAGE_PREVIOUS.equals( pageAction ) )
		{
			dataList = getPreviousPageData( paginationUser );
		}
		else if( PAGE_LAST.equals( pageAction ) )
		{
			dataList = getLastPageData( paginationUser );
		}
		else
		{
			// Probably case when user has passed the page number. Let us try for that.
			int userPageIndex = 1;
			try
			{
				userPageIndex = Integer.valueOf( pageAction );
			}
			catch( Exception e )
			{
				LOGGER.warn( "pagination option is not a page number either, hence returning the first page data" );
				dataList = getSelectedPageData( paginationUser, 1 );
			}

			dataList = getSelectedPageData( paginationUser, userPageIndex );
		}

		PaginationData paginationData = new PaginationData();
		if( dataList != null )
		{
			paginationData.addAll( dataList );
		}
		paginationData.setTotalRecordsSize( paginationUser.getTotalRecords() );
		paginationData.setCurrentPageIndex( paginationUser.getCurrentPageIndex() );
		paginationData.setPageSize( paginationUser.getDefaultPageSize() );
		paginationData.setPaginationUserId( paginationUserId );

		return paginationData;
	}

	/**
	 * Return data for first page
	 * 
	 * @param paginationUserId The unique id which was returned to user while doing registration
	 * @return Collection of data for first page
	 */
	private Collection getFirstPageData( PaginationUser paginationUser ) throws ComponentException
	{
		int currentPageIndex = 1;
		paginationUser.setCurrentPageIndex( currentPageIndex );
		return getPaginationDataInternal( paginationUser );
	}

	/**
	 * Return data for last page
	 * 
	 * @param paginationUserId The unique id which was returned to user while doing registration
	 * @return Collection of data for last page
	 */
	private Collection getLastPageData( PaginationUser paginationUser ) throws ComponentException
	{
		int pageSize = paginationUser.getDefaultPageSize();
		int totalRecords = paginationUser.getTotalRecords();

		// calculating the last page index
		int currentPageIndex = totalRecords / pageSize;
		if( totalRecords % pageSize > 0 )
		{
			currentPageIndex = currentPageIndex + 1;
		}
		LOGGER.debug( "lastPageIndex[" + currentPageIndex + "]" );

		paginationUser.setCurrentPageIndex( currentPageIndex );
		return getPaginationDataInternal( paginationUser );

	}

	/**
	 * Return data for next page, next from current pointer
	 * 
	 * @param paginationUserId The unique id which was returned to user while doing registration
	 * @return Collection of data for next page
	 */
	private Collection getNextPageData( PaginationUser paginationUser ) throws ComponentException
	{
		int currentPageIndex = paginationUser.getCurrentPageIndex();
		currentPageIndex++;
		paginationUser.setCurrentPageIndex( currentPageIndex );
		return getPaginationDataInternal( paginationUser );

	}

	/**
	 * Return data for previous page, previous from current pointer
	 * 
	 * @param paginationUserId The unique id which was returned to user while doing registration
	 * @return Collection of data for previous page
	 */
	private Collection getPreviousPageData( PaginationUser paginationUser ) throws ComponentException
	{
		int currentPageIndex = paginationUser.getCurrentPageIndex();
		currentPageIndex--;
		paginationUser.setCurrentPageIndex( currentPageIndex );
		return getPaginationDataInternal( paginationUser );
	}

	/**
	 * Return data for last page
	 * 
	 * @param paginationUserId The unique id which was returned to user while doing registration
	 * @param Selected page
	 * @return Collection of data for last page
	 */
	private Collection getSelectedPageData( PaginationUser paginationUser, int currentPageIndex )
			throws ComponentException
	{
		LOGGER.trace( "getSelectedPageData:  currentPageIndex[" + currentPageIndex + "]" );

		paginationUser.setCurrentPageIndex( currentPageIndex );
		return getPaginationDataInternal( paginationUser );
	}

	private Collection getPaginationDataInternal( PaginationUser paginationUser ) throws ComponentException
	{
		return paginationUser.getPaginationData();
	}

	private synchronized int getUniquePaginationUserId()
	{
		return ++paginationUserKey;
	}

	// ---------------------------------- Idle User Cleaner ------------------------------------------------

	/**
	 * This object is used to clean the idle users of pagination service which are not using the service since long. The
	 * defintion of 'long' is given with property file and system pick that value to decide it.
	 * 
	 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
	 */
	private class IdleUserCleaner implements Runnable
	{

		/**
		 * Time out after which any idle user will be wiped off the system
		 */
		long	idleUserTimeOut;

		/**
		 * Time after which the idle user cleaner will look for the idle users for cleanup process
		 */
		long	idleUserCleanerThreadSleepTime;

		public IdleUserCleaner( long idleUserTimeOut, long idleUserCleanerThreadSleepTime )
		{
			this.idleUserTimeOut = idleUserTimeOut;
			this.idleUserCleanerThreadSleepTime = idleUserCleanerThreadSleepTime;
		}

		@Override
		public void run()
		{

			/*
			 * Iterate over pagination users map get the values one by one i.e. users check current time - last access
			 * time for current user If gap is more than time out time (from config) remove it and log.info with
			 * complete information
			 */

			while( true )
			{
				// manually sync, as iterator is not safeguard for multiple threads even by Hashtable
				synchronized( paginationUsers )
				{
					PaginationUser paginationUser;
					for( Iterator iterator = paginationUsers.entrySet().iterator(); iterator.hasNext(); )
					{
						Map.Entry<Integer, PaginationUser> entry = (Map.Entry<Integer, PaginationUser>) iterator.next();
						paginationUser = entry.getValue();
						long lastAccess = paginationUser.getLastAccess();
						LOGGER.debug( "lastAccess[ " + lastAccess + " ] paginationUser[ " + paginationUser + " ]" );

						if( ( System.currentTimeMillis() - lastAccess ) > ( idleUserTimeOut ) )
						{
							iterator.remove();
							LOGGER.info( "Pagination user removed. user-key[ " + entry.getKey() + " ] lastAccess["
									+ lastAccess + "] timeElapsed[" + ( System.currentTimeMillis() - lastAccess )
									+ "] paginationUser[" + paginationUser + "]" );
						}
					}
					if( paginationUsers.size() > 500 )
					{
						for( int i = 0; i < 100; i++ )
						{
							LOGGER.info(
									"PERFORMANCE CHECK: Pagination Manager has accumulated more than 500 pagination users. Please check if that is the correct case. If correct, please increase the limit of check in Pagination Manager." );
						}
					}

					LOGGER.info( "PaginationUser Cleaner is going to sleep for [" + idleUserCleanerThreadSleepTime
							+ "] miliseconds. idleUserTimeOut[" + idleUserTimeOut + "] paginationUserSize["
							+ paginationUsers.size() + "]" );
				}

				try
				{

					Thread.sleep( idleUserCleanerThreadSleepTime );
				}
				catch( InterruptedException e )
				{
					LOGGER.error( "Idle Users clearner thread Interrupted", e );
					break;
				}
			}
		}

	}

}
