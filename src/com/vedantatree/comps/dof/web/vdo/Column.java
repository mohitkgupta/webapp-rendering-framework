package com.vedantatree.comps.dof.web.vdo;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The Column class represent the metadata of a single column in form/list ui.
 * 
 * <p>
 * This is being used by XMLSchemaManager while generating the DOMetadata from XML or may also be created by
 * MTSchemaManager after reading information from database. In case of Master Table Framework, once MTSchemaManager
 * read the database and return the list of columns, then accordingly DO Framwork render the UI further.
 * 
 * <properties>
 * <!--
 * searchable - To set that this field is searchable or not. Default value : false
 * sortable - To set that this field is sortable or not. Default value : true
 * dynamic - Value should be set to true if field value is calculated at runtime and is not coming from database
 * Default Value: false
 * -->
 * <property edit="true" view="true" view-link="true" sortable="false" searchable="true">
 * <display-name>tm.tenderCode</display-name>
 * <db-name>tenderCode</db-name>
 * <validation-rule>notnull:charonly</validation-rule>
 * </property>
 * </properties>
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class Column implements Serializable
{

	private static Log	LOGGER	= LogFactory.getLog( Column.class );

	/**
	 * It represents the database name of column by which DAO layer like Hibernate recognizes the column
	 */

	private String		dbName;

	/**
	 * The displayName is the string that is displayed to the user on the UI. It is the key of resource contents, so
	 * that a locale specific message can be show
	 */

	private String		displayName;

	/**
	 * The dataType tell the dataType of the colunmn.On the basis of this dataType , the input component (textBox,
	 * Calander etc.) is choosen on UI. This property can also be used for formatting the field value.
	 * 
	 * <p>
	 * Wherever it is required, it is set from HibernateSchemaManager on server, when we send the DOMetadata to
	 * server for merging the information from Hibernate Schema. It can be null for columns which are dynamic i.e. not
	 * present with Database
	 */
	private String		dataType;

	/**
	 * Represent if this field is editable or not
	 */
	private boolean		editable;

	/**
	 * Indicates whether this column is for list page, or not
	 */
	private boolean		listPageColumn;

	/**
	 * Indicates, in case of list page column, whether a view link should be shown for this column or not. This page
	 * will open the details of current record to view.
	 */
	private boolean		viewLink;

	/**
	 * Indicates whether the column is search-able or not on list page
	 */
	private boolean		searchable;

	/**
	 * Indicates whether the column is sort-able or not on list page
	 */
	private boolean		sortable;

	/**
	 * Represent whether it is a dynamic column or not.
	 * 
	 * <p>
	 * If not, it means that corresponding property must be present with database. If yes, then value of this column
	 * is calculated at runtime by the application
	 */
	private boolean		dynamic;

	/**
	 * Validation rules to validate the value of this column
	 */
	private String		validationRules;

	@Override
	public boolean equals( Object obj )
	{
		Column column = (Column) obj;
		LOGGER.debug( "current-column[" + this + "] other-column[" + obj + "]" );
		return dbName.trim().equals( column.getDbName().trim() )
				&& displayName.trim().equals( column.getDisplayName().trim() );
	}

	public String getDataType()
	{
		return dataType;
	}

	public String getDbName()
	{
		return dbName;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getValidationRules()
	{
		return validationRules;
	}

	@Override
	public int hashCode()
	{
		return ( dbName + displayName + dataType ).length();
	}

	public boolean isEditable()
	{
		return editable;
	}

	public boolean isListPageColumn()
	{
		return listPageColumn;
	}

	public boolean isSearchable()
	{
		return searchable;
	}

	public boolean isSortable()
	{
		return sortable;
	}

	public boolean isDynamic()
	{
		return dynamic;
	}

	public boolean isViewLink()
	{
		return viewLink;
	}

	public void setDataType( String dataType )
	{
		this.dataType = dataType;
	}

	public void setDbName( String name )
	{
		this.dbName = name;
	}

	public void setDisplayName( String displayName )
	{
		this.displayName = displayName;
	}

	public void setEditable( boolean editable )
	{
		this.editable = editable;
	}

	public void setListPageColumn( boolean listPageColumn )
	{
		this.listPageColumn = listPageColumn;
	}

	public void setSearchable( boolean searchable )
	{
		this.searchable = searchable;
	}

	public void setSortable( boolean sortable )
	{
		this.sortable = sortable;
	}

	public void setDynamic( boolean dynamic )
	{
		this.dynamic = dynamic;
	}

	public void setValidationRules( String validationRules )
	{
		this.validationRules = validationRules;
	}

	public void setViewLink( boolean viewLink )
	{
		this.viewLink = viewLink;
	}

	/*
	 * This is the overriden method of object class . Here it provides the string representation of a column.
	 */
	@Override
	public String toString()
	{
		return "DBname [" + dbName + "]  displayName [" + displayName + "]  type [" + dataType + "]  validationRules ["
				+ validationRules + "]  editable  [" + editable + "]  listPageColumn [" + listPageColumn
				+ "] searchable [" + searchable + "] dynamic[" + dynamic + "] view-link [" + viewLink + "] sortable["
				+ sortable + "]";
	}

	public static void main( String[] args )
	{
		System.out.println( "name: " + Date.class.getCanonicalName() );
	}

}
