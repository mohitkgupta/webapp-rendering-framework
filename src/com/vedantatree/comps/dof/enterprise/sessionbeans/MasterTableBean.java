package com.vedantatree.comps.dof.enterprise.sessionbeans;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.exceptions.ComponentException;
import org.vedantatree.utils.exceptions.ExceptionUtils;
import org.vedantatree.utils.exceptions.ObjectNotFoundException;
import org.vedantatree.utils.exceptions.db.DAOException;
import org.vedantatree.utils.exceptions.db.RelationExistException;
import org.vedantatree.utils.exceptions.server.ServerBusinessException;
import org.vedantatree.utils.exceptions.server.ServerSystemException;

import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.enterprise.dao.HibernateSchemaManager;
import com.vedantatree.comps.dof.enterprise.dao.MasterTableDAO;


/**
 * MasterTable Bean class will give the actual implementation of the methods defined in MasterTable interface and some
 * methods of the SessionBean interface are implemented
 * 
 * @author
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */

public class MasterTableBean implements SessionBean
{

	/**
	 * reference of the SessionContext object
	 */
	public SessionContext	ctx;
	/**
	 * Log object to get Log prints of the class
	 */
	public static final Log	LOGGER	= LogFactory.getLog( MasterTableBean.class );
	/**
	 * Singleton Object of the MasterTableDAO class used as the wrapper between HibernateUtil and bean class
	 */
	private MasterTableDAO	dao		= MasterTableDAO.getSharedInstance();

	/**
	 * deleteData will delete the object from the database on the basis of the id and className provided.
	 * 
	 * @param Id id of object to be deleted from the database
	 * 
	 * @param className of the class whose object to be deleted
	 * 
	 * @throws RemoteException
	 * @throws ServerBusinessException
	 */
	public void deleteData( String className, Long Id )
			throws RemoteException, ServerBusinessException, RelationExistException
	{

		LOGGER.trace( "In Master Table bean deleteData()" );

		/*
		 * will call the method defined in the wrapper class MasterTableDAO deleteData() using the className and the id
		 * for the object to be deleted from database and will return the Object received from that class to the caller.
		 */

		try
		{
			MasterTableDAO.deleteData( className, Id );
			LOGGER.debug( "Object deleted" );
		}
		catch( DAOException e )
		{
			LOGGER.error( "Exception Occured while deleting the object for class[ " + className + " ]and Id[ " + Id
					+ " ]" + e );
			throw new ServerBusinessException( e.getErrorCode(),
					"Exception Occured while deleting the object for class[ " + className + " ]and Id[ " + Id + " ]",
					e );
		}

	}

	public void ejbActivate()
	{
		// when bean activated
	}

	public void ejbCreate() throws CreateException, RemoteException
	{
		// stateless bean has create() with no args which
		// causes one bean instance to which multiple employees cling to.
	}

	public void ejbPassivate()
	{
		// when bean deactivated
	}

	public void ejbRemove()
	{
		// when bean removed

	}

	/**
	 * getAllData will fetch all the Objects from the database and return the list of the Objects of the class.
	 * 
	 * will fetch all the information from the DOMetaData.
	 * 
	 * @param classObject Object of DOMetadata class which holds all the info about the object
	 * 
	 * @param sortColumn on which column you want to sort the list
	 * 
	 * @param isDescending whether the list should be in ascending order or in desc order by default it will be in asc
	 *        order
	 * 
	 * @return will return the list of the Objects
	 * 
	 * @throws RemoteException
	 */
	public List getAllData( DOMetaData classObject, String sortColumn, String isDescending ) throws RemoteException
	{

		LOGGER.trace( "In Master Table bean getData()" );

		/**
		 * will call the method defined in the wrapper class MasterTableDAO getDataByClassName() and will return the
		 * datalist received from that class
		 */
		try
		{
			return MasterTableDAO.getDataByClassName( classObject, sortColumn, isDescending );
		}
		catch( DAOException e )
		{
			LOGGER.error( "Problem while interacting with database", e );
			throw new ServerSystemException( e.getErrorCode(), "Problem while interacting with database", e );
		}

	}

	/**
	 * getDataById method will return the object on the basis of the Id and the classname from DOMetaData from the
	 * database.
	 * 
	 * @param Id id of the object to be fetch
	 * 
	 * @return will return the object from the database if exist else will return null.
	 * 
	 * @throws RemoteException
	 * @throws ServerBusinessException
	 */
	public Object getDataById( DOMetaData classObject, Long Id ) throws RemoteException, ServerBusinessException
	{
		LOGGER.trace( "In Master Table bean getColumnData()" );

		/**
		 * will call the method defined in the wrapper class MasterTableDAO getDataById() using the Dometadata object
		 * and the id for the object to be fetched from databaseand will return the Object received from that class to
		 * the caller.
		 */
		Object columnData;
		try
		{
			columnData = MasterTableDAO.getDataById( classObject, Id );

			LOGGER.debug( "Total Object received in getColumnData[ " + columnData + " ]" );

			/**
			 * will return the object fetched from the database to the caller
			 */
			return columnData;
		}
		catch( DAOException e )
		{
			LOGGER.error( "Exception Occured while accessing the database" + e );
			throw new ServerBusinessException( e.getErrorCode(), "Exception Occured while accessing the database", e );
		}
		catch( ObjectNotFoundException e )
		{
			LOGGER.error( "Object not found in the database for class[ " + classObject.getClassName() + " ]with Id[ "
					+ Id + " ]" + e );
			throw new ServerBusinessException( e.getErrorCode(), "Object not found in the database for class[ "
					+ classObject.getClassName() + " ]with Id[ " + Id + " ]", e );
		}
	}

	/**
	 * getSearchData will fetch all the Objects from the database and return the list of the Objects of the class.on the
	 * basis of the SearchColumn Name given in the arguments and searchColumnValues.
	 * 
	 * will fetch all the information from the DOMetaData.
	 * 
	 * @param classObject Object of DOMetadata class which holds all the info about the object
	 * 
	 * @param searchColumnName on which column you want to search in the list
	 * 
	 * @param searchColumnValue which value you want to search in the list
	 * 
	 * @return will return the list of the Objects
	 * 
	 * @throws RemoteException
	 * @throws ServerBusinessException
	 */
	public List getSearchData( DOMetaData classObject, String searchColumnValue, String searchColumnName )
			throws RemoteException, ServerBusinessException
	{

		LOGGER.trace( "In Master Table bean getSearchData()" );

		/**
		 * will call the method defined in the wrapper class MasterTableDAO getSearchData() and will return the datalist
		 * received from that class
		 */

		try
		{
			return MasterTableDAO.getSearchData( classObject, searchColumnValue, searchColumnName );
		}
		catch( DAOException e )
		{
			LOGGER.error( "Exception Occured while accessing the database" + e );
			throw new ServerBusinessException( e.getErrorCode(), "Exception Occured while accessing the database", e );
		}

	}

	/**
	 * getDOMetaData() will fetch the DOMetaData object from the DOMetaData.xml file if exist otherwise from database it
	 * will fetch the object and will return it to the caller of the method.
	 * 
	 * @param doMetadata name of the class whose metaData we want
	 * 
	 * @return Object of the DOMetaData of that class.
	 * @throws RemoteException,ComponentException
	 * @throws RemoteException
	 */
	public DOMetaData mergeDOMetadataWithORMSchema( DOMetaData doMetadata ) throws RemoteException, ComponentException
	{
		LOGGER.trace( "In getDOMetaData() for ClassName[ " + doMetadata + " ]" );

		/**
		 * will return the object of DOMetaData class.
		 */
		return HibernateSchemaManager.getSharedInstance().mergeDOMetadataWithORMSchema( doMetadata );
	}

	/**
	 * saveOrUpdateData will save the Object if it is newly created object else it will only update the object in the
	 * data base and same object will be returned
	 * 
	 * @param classObject Object of DOMetadata class which holds all the info about the object
	 * 
	 * @param dataList list of values for the object
	 * 
	 * @param id id of the object if already saved otherwise null
	 * 
	 * @param dataMap which will contain the information about the datatype of the values of object.
	 * 
	 * @return will return the persisted object in the data base
	 * 
	 * @throws RemoteException
	 * @throws ServerBusinessException
	 */
	public Object saveOrUpdateData( DOMetaData classObject, List listOfValues, Long Id, Map dataMap )
			throws RemoteException, ServerBusinessException
	{

		LOGGER.trace( "In Master Table bean addData()" );

		LOGGER.debug( "Successfully add the Object in the database in addData()" );

		/**
		 * will call the method defined in the wrapper class MasterTableDAO saveOrUpdateData() and will return the
		 * Object received from that class to the caller.
		 */

		try
		{
			return MasterTableDAO.saveOrUpdateData( classObject, listOfValues, Id, dataMap );
		}
		catch( DAOException e )
		{

			ExceptionUtils.logException( LOGGER, e.getMessage(), e );
			throw new ServerBusinessException( e.getErrorCode(), "Exception Occured while saving the object for class[ "
					+ classObject.getClassName() + " ]and Id[ " + Id + " ]", e );
		}
		catch( ObjectNotFoundException e )
		{
			ExceptionUtils.logException( LOGGER, e.getMessage(), e );
			throw new ServerBusinessException( e.getErrorCode(),
					"Object not found for class[ " + classObject.getClassName() + " ]and Id[ " + Id + " ]", e );
		}

	}

	public void setSessionContext( SessionContext ctx )
	{
		this.ctx = ctx;
	}

	public void unsetSessionContext()
	{
		this.ctx = null;
	}
}
