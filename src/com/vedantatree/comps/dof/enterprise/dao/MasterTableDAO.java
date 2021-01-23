package com.vedantatree.comps.dof.enterprise.dao;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.BeanUtils;
import org.vedantatree.utils.db.orm.HibernateUtil;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.ObjectNotFoundException;
import org.vedantatree.utils.exceptions.db.DAOException;
import org.vedantatree.utils.exceptions.db.RelationExistException;

import com.vedantatree.comps.dof.DOFUtils;
import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.web.vdo.Column;


/**
 * wrapper class to MasterTable bean for communicating with the hibenateUtil class and for doing all the opearion in the
 * database
 * 
 * @author
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */

public class MasterTableDAO
{

	/**
	 * Log object to get Log prints in the class
	 */
	private static final Log			LOGGER			= LogFactory.getLog( MasterTableDAO.class );

	/**
	 * singleton instance of the class
	 */
	private static final MasterTableDAO	sharedInstance	= new MasterTableDAO();

	/**
	 * deleteData will delete the object from the database on the basis of the id and classname provided.
	 * 
	 * @param Id id of object to be deleted from the database
	 * @param className name of the class whose object to be deleted
	 * @throws RelationExistException
	 */
	public static void deleteData( String className, Long Id ) throws DAOException, RelationExistException
	{
		LOGGER.trace( "In Master Table DAO deleteData()" );

		HibernateUtil.deleteObject( className, Id );

		LOGGER.debug( "Object deleted" );
	}

	/**
	 * getDataByClassName will fetch all the object from the database and return the list of the object as per
	 * objectName provided in arguments. will fetch all the information from the DOMetaData
	 * 
	 * @param objectName name of class
	 * 
	 * @param sortColumn on which column you want to sort the list
	 * 
	 * @param isDescending whether the list should be in ascending order or in desc order by default it will be in asc
	 *        order
	 * 
	 * @return will return the list of the Object
	 * @throws DAOException
	 */
	public static List getDataByClassName( DOMetaData classObject, String sortColumn, String isDescending )
			throws DAOException
	{
		LOGGER.trace( "In getDataByclassName() of MasterTable DAO" );

		List tableData = HibernateUtil.getAllObjectsByClassName( classObject.getClassName(), sortColumn, isDescending,
				null );

		LOGGER.debug( "Total Object received in getDataByclassName[ " + tableData.size() + " ]" );

		return tableData;
	}

	/**
	 * getDataById method will return the object on the basis of the Id and the DOMeatadata from the database.
	 * 
	 * @param Id id of the object to be fetch
	 * 
	 * @return will return the object from the database if exist else will return null.
	 * @throws ObjectNotFoundException
	 */
	public static Object getDataById( DOMetaData classObject, Long Id ) throws DAOException, ObjectNotFoundException
	{
		LOGGER.trace( "In Master Table DAO getColumnData()" );

		Object columnData = HibernateUtil.getObjectById( Id, classObject.getClassName() );

		LOGGER.debug( "object-to-return[ " + columnData + " ]" );
		return columnData;
	}

	/**
	 * gets the pojo object against DOMetadata and id. If id is null, then it will return a new Object for the class
	 * 
	 * @param className String
	 * @param listOfObjects List<HashMap<String, String>>
	 * @throws ObjectNotFoundException
	 * @throws DAOException
	 */
	public static Object getObjectByDOMetaData( DOMetaData classObject, List listOfValues, Long Id, Map dataMap )
			throws DAOException, ObjectNotFoundException
	{
		BeanUtils.LOGGER.trace(
				"In getObjectByDOMetaData: classObject[" + classObject + "] listOfValues[" + listOfValues + "]" );

		Class cls;
		Object pojoObject = null;

		try
		{
			cls = HibernateUtil.getClassByClassName( classObject.getClassName() );
			if( Id != null )
			{
				pojoObject = HibernateUtil.getObjectById( Id, classObject.getClassName() );
			}
			else
			{
				pojoObject = cls.newInstance();
			}

			List<Column> columns = classObject.getColumns();
			int i = 0;

			for( Column column : columns )
			{
				if( column.isEditable() )
				{

					String columnDataType = columns.get( i ).getDataType();
					Object obj = dataMap.get( columns.get( i ).getDbName().trim() );
					LOGGER.debug( "columnDataType[" + columnDataType + "] columnValue[" + obj + "]" );

					Object columnFormattedValue = null;
					if( columnDataType.contains( "ManyToOne" ) )
					{
						columnFormattedValue = obj;
					}
					else if( obj instanceof String )
					{
						String strValue = (String) obj;
						if( columnDataType.contains( "Long" ) )
						{
							columnFormattedValue = Long.valueOf( strValue );
						}
						else if( columnDataType.contains( "Boolean" ) )
						{
							columnFormattedValue = Boolean.valueOf( strValue );
						}
						else if( columnDataType.contains( "Double" ) )
						{
							columnFormattedValue = Double.valueOf( strValue );
						}
						else if( columnDataType.contains( "Integer" ) )
						{
							columnFormattedValue = Integer.valueOf( strValue );
						}
						else
						{
							columnFormattedValue = strValue;
						}
					}
					else
					{
						DAOException dae = new DAOException( IErrorCodes.ILLEGAL_STATE_ERROR,
								"Unsupport data type of column. dataType[" + columnDataType + "]" );
						LOGGER.error( dae );
						throw dae;
					}

					LOGGER.debug( "final val value is [" + columnFormattedValue + "]" );

					BeanUtils.LOGGER.debug( "value-retrieved[ " + columnFormattedValue + " ] for column[ "
							+ columns.get( i ).getDbName().trim() + " ]" );

					String seterMethodName = "set" + columns.get( i ).getDbName().trim().substring( 0, 1 ).toUpperCase()
							+ columns.get( i ).getDbName().trim().substring( 1 );

					BeanUtils.LOGGER.debug( "Class retrived from ClassObject[ " + cls + " ]" );
					Method method = BeanUtils.getMethod( cls, seterMethodName );

					if( method == null )
					{
						DAOException dae = new DAOException( IErrorCodes.ILLEGAL_STATE_ERROR,
								"Unsupported operation. Method not found in object for property. methodName["
										+ seterMethodName + "] column[" + columns.get( i ).getDbName() + "]" );
						LOGGER.error( dae );
						throw dae;
					}
					method.invoke( pojoObject, new Object[]
					{ columnFormattedValue } );
					i++;
				}
			}

		}
		catch( Exception e )
		{
			DAOException dae = new DAOException( IErrorCodes.ILLEGAL_STATE_ERROR, "Error during operation", e );
			LOGGER.error( dae );
			throw dae;
		}

		return pojoObject;
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
	 */
	public static List getSearchData( DOMetaData classObject, String searchColumnValue, String searchColumnName )
			throws DAOException
	{
		LOGGER.trace( "In getSearchData() of MasterTable DAO" );

		String searchQyeryString = DOFUtils.getSearchString( classObject, searchColumnValue, searchColumnName );
		List tableData = HibernateUtil.getAllObjectsByClassName( classObject.getClassName(), searchQyeryString );

		LOGGER.debug( "Total Object received in getSearchData[ " + tableData.size() + " ]" );
		return tableData;
	}

	/**
	 * method for getting the shared instance of the class
	 * 
	 * @return
	 */

	public static MasterTableDAO getSharedInstance()
	{
		return sharedInstance;
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
	 * @throws ObjectNotFoundException
	 * 
	 * @throws RemoteException
	 * 
	 *         TODO remove listOfValues, it is not required
	 */
	public static Object saveOrUpdateData( DOMetaData classObject, List<Object> listOfValues, Long Id, Map dataMap )
			throws DAOException, ObjectNotFoundException
	{
		LOGGER.trace( "In Master Table DAO addData()" );

		Object pojoObject = getObjectByDOMetaData( classObject, listOfValues, Id, dataMap );

		LOGGER.debug( "Successfully add the Object in the database in MasterTableDAO " );

		try
		{
			pojoObject = HibernateUtil.saveOrUpdateObject( pojoObject );
		}
		catch( DAOException e )
		{
			LOGGER.error( "Problem while saving/updating the object. error-message[" + e.getMessage() + "]", e );
			throw e;
		}
		return pojoObject;
	}

}
