package com.vedantatree.comps.dof.enterprise.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.db.orm.HibernateUtil;
import org.vedantatree.utils.exceptions.ComponentException;
import org.vedantatree.utils.exceptions.db.DAOException;

import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.web.vdo.Column;


/**
 * This class is providing the facility to get the object schema information for a given metadata based on the
 * properties set in database.
 * 
 * <p>
 * User provides the UI metadata in XML form as of now, however, further this class enhance that metadata by adding
 * other information like added columns, column types etc from database.
 * 
 * <p>
 * It uses hibernate schema API to get all of this information.
 * 
 * @author Administrator
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 *
 */
public class HibernateSchemaManager
{

	/** For logging purpose */
	private static final Log				LOGGER			= LogFactory.getLog( HibernateSchemaManager.class );
	private static final String				NOT_NULLABLE	= "REQUIRED";

	public static HibernateSchemaManager	hSchema			= new HibernateSchemaManager();

	private HibernateSchemaManager()
	{

	}

	public static HibernateSchemaManager getSharedInstance()
	{
		if( hSchema == null )
		{
			hSchema = new HibernateSchemaManager();
		}
		return hSchema;
	}

	/**
	 * The getColumn method returns a column whose name is passes as parameter , after getting it from the column list
	 * provided
	 * 
	 * @param colList
	 * @param colName
	 * @return
	 */
	private Column getColumn( List<Column> colList, String colName )
	{
		LOGGER.debug( "ColName is [" + colName + "]" );
		Column column = null;
		for( int i = 0; i < colList.size(); i++ )
		{
			if( colList.get( i ).getDbName().trim().equalsIgnoreCase( colName.trim() ) )
			{
				column = colList.get( i );
			}
		}
		LOGGER.debug( "Returned column is [" + column + "]" );
		return column;
	}

	public DOMetaData mergeDOMetadataWithORMSchema( DOMetaData doMetaData ) throws ComponentException
	{
		// TODO: Nested properties handling
		/*
		 * psuedo code
		 * 
		 * get column metadata from hibernate utils
		 * 
		 * iterate over column metadata Column = get equaivalentColumn from dometadata if not exist && enforce is false
		 * create a new column set the properties name, type, validation add to dometadata at same index otherwise if
		 * column is not null synchronize the column properties with hibernate class metadata properties to sync: type,
		 * validation - null
		 * 
		 * return DOMetadata
		 * 
		 * -----------------
		 * 
		 * Get class meta data list from hibernate utils Iterate over meta data list for columns get the equivalent
		 * column from dometadata if column found null and enforce is not true call method to create a column from
		 * hibernate metadata and add it to dometadata else if column is not null update the column with hibernate
		 * metadata
		 */

		String tableName = doMetaData.getClassName();
		LOGGER.debug( "dometadata class name[" + tableName + "]" );

		List<HashMap<String, String>> columnORMMetadataList = null;
		try
		{
			columnORMMetadataList = HibernateUtil.getClassMetadataInformation( tableName );
		}
		catch( DAOException e )
		{
			throw new ComponentException( e.getErrorCode(), e.getMessage() );
		}
		LOGGER.debug( "orm-column-metadata[" + columnORMMetadataList + "]" );

		List<Column> columnDOMetadataList = doMetaData.getColumns();

		for( int counter = 0; counter < columnORMMetadataList.size(); counter++ )
		{

			HashMap<String, String> singleColumnMetaData = columnORMMetadataList.get( counter );

			String colName = singleColumnMetaData.get( HibernateUtil.PROPERTY_NAME );
			String colType = singleColumnMetaData.get( HibernateUtil.PROPERTY_TYPE );
			String nullAllowed = singleColumnMetaData.get( HibernateUtil.PROPERTY_IS_NULL_ALLOWED );

			String validationRule = null;
			if( nullAllowed.trim().equalsIgnoreCase( "false" ) )
			{
				validationRule = NOT_NULLABLE;
			}
			LOGGER.debug(
					"db: colName[" + colName + "] colType[" + colType + "] validationRule[" + validationRule + "]" );

			/*
			 * Get column from xml dometadata for equi column
			 */
			Column doColumn = getColumn( columnDOMetadataList, colName );
			LOGGER.debug( "doColumn[" + doColumn + "]" );

			if( doColumn == null && !doMetaData.shouldEnforce() )
			{

				// Setting the values in column object
				Column newORMColumn = new Column();
				newORMColumn.setDbName( colName );
				newORMColumn.setDataType( colType );
				newORMColumn.setDisplayName( colName );
				newORMColumn.setValidationRules( validationRule );
				newORMColumn.setEditable( true );
				newORMColumn.setListPageColumn( true );
				newORMColumn.setSearchable( true );
				newORMColumn.setViewLink( false );
				columnDOMetadataList.add( newORMColumn );

				LOGGER.debug( "new-orm-column-added[" + newORMColumn + "]" );
			}
			else if( doColumn != null )
			{
				doColumn.setDataType( colType );
				if( validationRule != null && ( doColumn.getValidationRules() == null
						|| !doColumn.getValidationRules().contains( validationRule ) ) )
				{
					doColumn.setValidationRules( validationRule );
				}
			}
		}

		return doMetaData;
	}

}

// ---------------------------------------------------------------------------------------

// private List getClassMetaData1( String className ) {
// LOGGER.debug( "className in HibernateSchemaManager is [" + className + "]" );
// List<HashMap<String, String>> columnsMetaDataList = HibernateUtil.getColumnMetadata( className );
// String IDFieldName = HibernateUtil.getIdentityName( className );
// String idDataType = HibernateUtil.getIdentityType( className );
// List columnList = new ArrayList();
// Column idColumn = new Column();
// idColumn.setDBname( IDFieldName );
// idColumn.setDisplayName( IDFieldName );
// idColumn.setDataType( idDataType );
// for( int counter = 0; counter < columnsMetaDataList.size(); counter++ ) {
// Column column = new Column();
// HashMap<String, String> singleColumnMetaData = columnsMetaDataList.get( counter );
// String colName = singleColumnMetaData.get( HibernateUtil.PROPERTY_NAME );
// String colType = singleColumnMetaData.get( HibernateUtil.PROPERTY_TYPE );
// String nullAllowed = singleColumnMetaData.get( HibernateUtil.PROPERTY_IS_NULL_ALLOWED );
//
// String validationRule = "";
// if( nullAllowed.trim().equalsIgnoreCase( "false" ) ) {
// validationRule = validationRule + NOT_NULLABLE;
// }
//
// // Setting the values in column object
// column.setDBname( colName );
// LOGGER.debug( "COLUMN DATA TYPE " + colType );
// column.setDataType( colType );
// column.setDisplayName( colName );
// column.setValidationRules( validationRule );
// column.setEditable( true );
// column.setListPageColumn( true );
// column.setSearchable( true );
// column.setViewLink( false );
//
// LOGGER.debug( column );
// columnList.add( column );
// }
//
// return columnList;
// }
//
// /**
// * Returns the metadata of all the columns .It finds the metadata using
// * Hibernate.
// *
// * @param tableName
// * name of the pojo class whose column's meta data is to be
// * found.
// * @return a list of columns containing oblects of class
// * com.vedantatree.publicbooks.enterprise.bl.masterTableFramework.
// */
// private DOMetaData getClassMetaDataOLD( String className ) {
// DOMetaData classMetaData = new DOMetaData();
// LOGGER.debug( "className in HibernateSchemaManager is [" + className + "]" );
// List<Column> columnList = new ArrayList<Column>();
// List<HashMap<String, String>> columnsMetaDataList = HibernateUtil.getColumnMetadata( className );
// String IDFieldName = HibernateUtil.getIdentityName( className );
// String idDataType = HibernateUtil.getIdentityType( className );
//
// Column idColumn = new Column();
// idColumn.setDBname( IDFieldName );
// idColumn.setDisplayName( IDFieldName );
// idColumn.setDataType( idDataType );
// //
// // columnList.add(idColumn);
// // String IDType = HibernateUtil.getIdentityType(className);
// for( int counter = 0; counter < columnsMetaDataList.size(); counter++ ) {
// Column column = new Column();
// HashMap<String, String> singleColumnMetaData = columnsMetaDataList.get( counter );
// String colName = singleColumnMetaData.get( HibernateUtil.PROPERTY_NAME );
// String colType = singleColumnMetaData.get( HibernateUtil.PROPERTY_TYPE );
// String nullAllowed = singleColumnMetaData.get( HibernateUtil.PROPERTY_IS_NULL_ALLOWED );
//
// String validationRule = "";
// if( nullAllowed.trim().equalsIgnoreCase( "false" ) ) {
// validationRule = validationRule + NOT_NULLABLE;
// }
//
// // Setting the values in column object
// column.setDBname( colName );
// LOGGER.debug( "COLUMN DATA TYPE " + colType );
// column.setDataType( colType );
// column.setDisplayName( colName );
// column.setValidationRules( validationRule );
// column.setEditable( true );
// column.setListPageColumn( true );
// column.setSearchable( true );
// column.setViewLink( false );
//
// LOGGER.debug( column );
// columnList.add( column );
// }
// classMetaData.setColumns( columnList );
// classMetaData.setClassName( className );
// classMetaData.setIdColumn( idColumn );
// return classMetaData;
// }
//
