package com.vedantatree.comps.dof.enterprise.clients;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;


public interface MasterTableHome extends EJBHome
{

	public MasterTable create() throws CreateException, RemoteException;

}
