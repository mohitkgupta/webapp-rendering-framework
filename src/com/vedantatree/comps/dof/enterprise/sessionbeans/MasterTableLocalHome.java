package com.vedantatree.comps.dof.enterprise.sessionbeans;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

import com.vedantatree.comps.dof.enterprise.clients.MasterTable;


public interface MasterTableLocalHome extends EJBLocalHome
{

	public MasterTable create() throws CreateException, RemoteException;

}
