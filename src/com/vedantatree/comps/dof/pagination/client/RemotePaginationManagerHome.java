package com.vedantatree.comps.dof.pagination.client;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;


/**
 * Home Interface for Pagination Manager Bean
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public interface RemotePaginationManagerHome extends EJBHome
{

	public RemotePaginationManager create() throws CreateException, RemoteException;

}
