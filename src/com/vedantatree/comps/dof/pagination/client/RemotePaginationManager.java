package com.vedantatree.comps.dof.pagination.client;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import org.vedantatree.utils.exceptions.ComponentException;
import org.vedantatree.utils.exceptions.server.ServerBusinessException;

import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.pagination.PaginationData;


/**
 * This is the remote interface of Pagination Manager Service running on Server. It is used by UI clients to get the
 * paginated data to display in list pages.
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public interface RemotePaginationManager extends EJBObject
{

	/**
	 * It returns the data for pagination request. Before any user make this request, it should register itself with
	 * Pagination Manager service using registerPaginationUser method. Registration will provide user a unique id, which
	 * should be passed to pagination manager in all future interactions.
	 * 
	 * @param paginationUserId Id of the Pagination User
	 * @param pageAction UI action which should indicate any one action from Next, Previous, First, and Last. If page
	 *        action is null, we shall return the first page data. If page action is the page number at UI, we shall
	 *        return the data of specified page number. If pageAction is not valid either with any of above options, we
	 *        shall return first page data again.
	 * @return Data for current request
	 * @throws RemoteException if there is any problem
	 * @throws ServerBusinessException if there is any problem
	 */
	PaginationData getPaginationData( int paginationUserId, String pageAction )
			throws RemoteException, ComponentException;

	/**
	 * This method is used to register a Pagination Client with Pagination Manager. Pagination Manager will return a
	 * unique id for the newly register client, which should be used in all futuer interactions.
	 * 
	 * @param paginationDataProviderJndiName JNDI name of the pagination data provider. It is assumed as session bean as
	 *        of now, and should be implemented by user
	 * @param homeClass Class of the Session Bean home for data provider
	 * @param dataType Type of data object
	 * @param dataQualifier Any qualifier which may help the user to implement business logic in data provider
	 *        implementation
	 * @param defaultPageSize Default page size
	 * @param doMetaData DOMetadata for current client UI
	 * @param objectGroup Object Group, which works as filter for data and helps to return only that data which is
	 *        available to current user based on set Object group. Assume a case when we are working in SAAS
	 *        environment, then there we might be having data of multiple entities or Organizations. This object group
	 *        represents those entities, and hence will be used to filer the data based on current entity.
	 * @return Unique ID for newly registered user
	 * @throws RemoteException If there is any problem
	 * @throws ServerBusinessException If there is any problem
	 * @throws ComponentException TODO
	 */
	public int registerPaginationUser( String paginationDataProviderJndiName, Class homeClass, Class dataType,
			String dataQualifier, int defaultPageSize, DOMetaData doMetaData, Object objectGroup )
			throws RemoteException, ComponentException;

	/**
	 * This method is same as above. The only difference is that here we are expecting a simple class name as data
	 * provider rather than a session bean. It also follows that the class should reside on same server and class loader
	 * space where Pagination Manager is working.
	 */
	public int registerPaginationUser( String paginationDataProviderName, Class dataType, String dataQualifier,
			int defaultPageSize, DOMetaData doMetaData, Object objectGroup ) throws RemoteException, ComponentException;

	/**
	 * This method is used to un-register a pagination user from Pagination Manager. Every registered user should be
	 * un-register by using this method when it is not longer required. However, for safety purpose, we have implemented
	 * a cleaner which will clean the users itself if these are not active for a specific duration. This duration can be
	 * specified in property file.
	 * 
	 * @param paginationUserId Unique id of the pagination user to unregister
	 * @throws RemoteException If there is any problem
	 * @throws ServerBusinessException If there is any problem
	 * @throws ComponentException TODO
	 */
	public void unregisterPaginationUser( int paginationUserId ) throws RemoteException, ComponentException;

	/**
	 * This method is used to update the information of registered pagination user. This can be used by UI client
	 * whenever there is any change in pagination requirements for any registered user.
	 * 
	 * @param paginationUserId Unique id of the pagination user to uniquely identifying it
	 * @param currentPageIndex Index of current page which we want to show on UI
	 * @param searchClause Search clause for data
	 * @param orderByClause Order by clause for data
	 * @param doMetaData DOMetadata for UI and UI objects
	 * @param completeQuery Complete Query in case we don't want to use Search Clause, Sort Clause and DOMetadata and
	 *        want to run our own custom query
	 * @param objectGroup Object group as defined with above methods
	 * @throws RemoteException if there is any problem
	 * @throws ServerBusinessException if there is any problem
	 */
	void updateRegisteredUser( int paginationUserId, int currentPageIndex, String searchClause, String orderByClause,
			DOMetaData doMetaData, String completeQuery, Object objectGroup )
			throws RemoteException, ComponentException;
}
