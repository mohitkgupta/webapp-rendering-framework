package com.vedantatree.comps.dof.pagination.client;

import java.rmi.RemoteException;

import org.vedantatree.utils.exceptions.ComponentException;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;
import org.vedantatree.utils.exceptions.server.ServerBusinessException;
import org.vedantatree.utils.exceptions.server.ServerSystemException;

import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.JNDILookupManager;
import com.vedantatree.comps.dof.pagination.PaginationData;
import com.vedantatree.comps.dof.web.security.SecurityServletFilter;


/**
 * Client side delegate for Pagination Manager Remote Bean
 * 
 * It is mostly used to set some client specific standard properties to Pagination Manager request like object group.
 * Going forward, we can also use this to set AdminContext, and can also pick the pagination manager JNDI name from
 * configuration file for web logic deployment.
 * 
 * For documentation of methods, refer to RemotePaginationManager interface.
 * 
 * @see RemotePaginationManager
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class PaginationManagerDelegate
{

	private static PaginationManagerDelegate	SHARED_INSTANCE	= new PaginationManagerDelegate();
	// private static Log LOGGER = LogFactory.getLog( PaginationManagerDelegate.class );

	private RemotePaginationManager				remotePaginationManager;

	private PaginationManagerDelegate()
	{
		try
		{

			remotePaginationManager = (RemotePaginationManager) JNDILookupManager.lookupRemoteEJB( "paginationManager",
					RemotePaginationManagerHome.class );
		}
		catch( ComponentException e )
		{
			// ExceptionUtils.logException( logger, message, e );

			throw new SystemException( IErrorCodes.COMPONENT_INITIALIZATION_ERROR,
					"Error while finding the remote instance of Pagination Manager remotePaginationManager[ "
							+ remotePaginationManager + " ]",
					e );
		}
	}

	public static PaginationManagerDelegate getSharedInstance()
	{
		return SHARED_INSTANCE;
	}

	private Object getObjectGroup() throws RemoteException, ServerBusinessException
	{
		return SecurityServletFilter.getObjectGroup();
	}

	public PaginationData getPaginationData( int paginationUserId, String pageAction )
			throws RemoteException, ServerBusinessException, ComponentException
	{
		return remotePaginationManager.getPaginationData( paginationUserId, pageAction );
	}

	public int registerPaginationUser( String paginationDataProviderJndiName, Class homeClass, Class dataType,
			String dataQualifier, int defaultPageSize, DOMetaData doMetaData )
			throws RemoteException, ServerBusinessException, ComponentException
	{
		getObjectGroup();
		return remotePaginationManager.registerPaginationUser( paginationDataProviderJndiName, homeClass, dataType,
				dataQualifier, defaultPageSize, doMetaData, getObjectGroup() );
	}

	public int registerPaginationUser( String paginationDataProviderJndiName, Class homeClass, Class dataType,
			String dataQualifier, int defaultPageSize, DOMetaData doMetaData, Object objectGroup )
			throws RemoteException, ServerBusinessException, ComponentException
	{
		return remotePaginationManager.registerPaginationUser( paginationDataProviderJndiName, homeClass, dataType,
				dataQualifier, defaultPageSize, doMetaData, objectGroup );
	}

	public int registerPaginationUser( String paginationDataProviderName, Class dataType, String dataQualifier,
			int defaultPageSize, DOMetaData doMetaData )
			throws RemoteException, ServerBusinessException, ComponentException
	{
		getObjectGroup();
		return remotePaginationManager.registerPaginationUser( paginationDataProviderName, dataType, dataQualifier,
				defaultPageSize, doMetaData, getObjectGroup() );
	}

	public int registerPaginationUser( String paginationDataProviderName, Class dataType, String dataQualifier,
			int defaultPageSize, DOMetaData doMetaData, Object objectGroup )
			throws RemoteException, ServerBusinessException, ComponentException
	{
		return remotePaginationManager.registerPaginationUser( paginationDataProviderName, dataType, dataQualifier,
				defaultPageSize, doMetaData, objectGroup );
	}

	public void unregisterPaginationUser( int paginationUserId )
			throws RemoteException, ServerBusinessException, ComponentException
	{
		remotePaginationManager.unregisterPaginationUser( paginationUserId );
	}

	public void updateRegisteredUser( int paginationUserId, int currentPageIndex, String searchClause,
			String orderByClause, DOMetaData doMetaData, String completeQuery )
			throws ServerBusinessException, RemoteException, ComponentException
	{

		try
		{
			remotePaginationManager.updateRegisteredUser( paginationUserId, currentPageIndex, searchClause,
					orderByClause, doMetaData, completeQuery, getObjectGroup() );
		}
		catch( RemoteException e )
		{
			throw new ServerSystemException( IErrorCodes.SERVER_SYSTEM_ERROR, e.getMessage(), e );
		}

	}

}
