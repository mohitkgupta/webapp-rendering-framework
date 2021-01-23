package com.vedantatree.comps.dof.pagination.server;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.vedantatree.utils.exceptions.ComponentException;

import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.pagination.PaginationData;


/**
 * EJB implementation for Pagination Manager.
 * 
 * For documentation, refer to RemotePaginationManager and Pagination Manager classes. This class is just a facade
 * class.
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 * 
 */
public class PaginationManagerBean implements SessionBean
{

	private PaginationManager paginationManagerInstance = PaginationManager.getSharedInstance();;

	public void setSessionContext( SessionContext arg0 ) throws EJBException, RemoteException
	{
	}

	public void ejbCreate() throws EJBException, RemoteException
	{
		paginationManagerInstance = PaginationManager.getSharedInstance();
	}

	public void ejbActivate() throws EJBException, RemoteException
	{
		paginationManagerInstance = PaginationManager.getSharedInstance();
	}

	public void ejbPassivate() throws EJBException, RemoteException
	{
		paginationManagerInstance = null;
	}

	public void ejbRemove() throws EJBException, RemoteException
	{
		paginationManagerInstance = null;
	}

	public PaginationData getPaginationData( int paginationUserId, String pageAction )
			throws ComponentException, RemoteException
	{
		return paginationManagerInstance.getPaginationData( paginationUserId, pageAction );
	}

	public int registerPaginationUser( String paginationDataProviderJndiName, Class homeClass, Class dataType,
			String dataQualifier, int defaultPageSize, DOMetaData doMetaData, Object objectGroup )
			throws ComponentException, RemoteException
	{
		return paginationManagerInstance.registerPaginationUser( paginationDataProviderJndiName, homeClass, dataType,
				dataQualifier, defaultPageSize, doMetaData, objectGroup );
	}

	public int registerPaginationUser( String paginationDataProviderName, Class dataType, String dataQualifier,
			int defaultPageSize, DOMetaData doMetaData, Object objectGroup ) throws ComponentException, RemoteException
	{
		return paginationManagerInstance.registerPaginationUser( paginationDataProviderName, dataType, dataQualifier,
				defaultPageSize, doMetaData, objectGroup );
	}

	public void unregisterPaginationUser( int paginationUserId ) throws ComponentException, RemoteException
	{
		paginationManagerInstance.unregisterPaginationUser( paginationUserId );
	}

	public void updateRegisteredUser( int paginationUserId, int currentPageIndex, String searchClause,
			String orderByClause, DOMetaData doMetaData, String completeQuery, Object objectGroup )
			throws RemoteException, ComponentException
	{
		paginationManagerInstance.updateRegisteredUser( paginationUserId, currentPageIndex, searchClause, orderByClause,
				doMetaData, completeQuery, objectGroup );
	}

}
