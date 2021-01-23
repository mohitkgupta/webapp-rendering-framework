package com.vedantatree.comps.dof.enterprise.clients;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBObject;

import org.vedantatree.utils.exceptions.ComponentException;
import org.vedantatree.utils.exceptions.db.RelationExistException;
import org.vedantatree.utils.exceptions.server.ServerBusinessException;

import com.vedantatree.comps.dof.DOMetaData;


/**
 * Master Table Interface provides the definition of the methods for MasterTables which are defined in the settings of
 * the projects.
 * 
 * Objects of the master tables are saveNUpdate,delete and fetching of the objects of the class according to the name
 * provided in the DOMetaData.xml file is fetched and the operations are performed on these objects.
 * 
 * these methods are generalized for every object which is the part of the Master table Framework.
 * 
 * @author
 * 
 */
public interface MasterTable extends EJBObject
{

	/**
	 * deleteData will delete the object from the database on the basis of the id and className provided.
	 * 
	 * @param Id id of object to be deleted from the database
	 * 
	 * @param className of the class whose object to be deleted
	 * 
	 * @throws RemoteException
	 */
	public void deleteData( String className, Long Id )
			throws RemoteException, ServerBusinessException, RelationExistException;

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

	public List getAllData( DOMetaData classObject, String sortColumn, String isDescending )
			throws RemoteException, ServerBusinessException;

	/**
	 * getDataById method will return the object on the basis of the Id and the classname from DOMetaData from the
	 * database.
	 * 
	 * @param Id id of the object to be fetch
	 * 
	 * @return will return the object from the database if exist else will return null.
	 * 
	 * @throws RemoteException
	 */

	public Object getDataById( DOMetaData classObject, Long Id ) throws RemoteException, ServerBusinessException;

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
	 */

	public List getSearchData( DOMetaData classObject, String searchColumnValue, String searchColumnName )
			throws RemoteException, ServerBusinessException;

	/**
	 * It is used to merge the DOMetadata produced from XML with ORM Schema
	 * 
	 * @param doMetadata name of the class whose metaData we want
	 * 
	 * @return Object of the DOMetaData of that class.
	 * @throws RemoteException
	 */

	public DOMetaData mergeDOMetadataWithORMSchema( DOMetaData doMetadata )
			throws RemoteException, ComponentException, ServerBusinessException;

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
	 */

	public Object saveOrUpdateData( DOMetaData classObject, List dataList, Long id, Map dataMap )
			throws RemoteException, ServerBusinessException;

}
