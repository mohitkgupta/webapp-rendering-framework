package com.vedantatree.comps.dof.pagination.server;

import java.util.Collection;

import org.vedantatree.utils.exceptions.ApplicationException;
import org.vedantatree.utils.exceptions.ComponentException;

import com.vedantatree.comps.dof.DOMetaData;


/**
 * It provides the paginated data to Pagination Manager as per request.
 * 
 * This class can put all customzied business logic as permodule requirements. Further it can use Hiberante Utils method
 * to get the data.
 * 
 * It can put all the logic to returned only desired data based on data qualifier
 * 
 * 
 * @author Arvind Pal
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public interface IPaginationDataProvider
{

	/**
	 * Get collection of objects needed to list on list page
	 * 
	 * @param currentPageIndex
	 * @param pageSize
	 * @param dataQualifier
	 * @param searchClause
	 * @param orderByClause
	 * @param completeQuery
	 * @return Collention of objects needed to show on list page
	 * @throws ComponentException-
	 */
	Collection getPaginatedData( int currentPageIndex, int pageSize, String dataQualifier, String searchClause,
			String orderByClause, DOMetaData doMetaData, String completeQuery ) throws ApplicationException;

	/**
	 * Get total number of records present in database for given class and whereClause
	 * 
	 * TODO Should pass completeQuery also
	 * 
	 * @return Total number of records present in database.
	 * @throws ComponentException
	 * @deprecated Use {@link #getTotalNumberOfRecords(Class,String,String,Object,DOMetaData,String)} instead
	 */
	@Deprecated
	int getTotalNumberOfRecords( String dataQualifier, String searchClause ) throws ApplicationException;

	/**
	 * Get the total number of records for current pagination client
	 * 
	 * @param dataType Type of the data. It will be the class of data object which this pagination data provider is
	 *        dealing with
	 * @param dataQualifier Any qualifier which may help developer to implement specified business logic in data
	 *        provider
	 * @param searchClause search clause. It alraedy contains the extra clauses of object group and DoMetadata query
	 * @param objectGroup Object group which can tell us the object group of data. It is particularly used when we have
	 *        data for multiple entities, and we want to return data only for entity for which user has the right or
	 *        current logged in
	 * @param doMetaData DOMetadata for the object/UI
	 * @param completeQuery Complete SQL query, (HQL as of now), if we want to override everything and want to pass our
	 *        own query
	 * @return Total number of records present as per above filter criteria
	 * @throws ApplicationException If there is any problem
	 */
	int getTotalNumberOfRecords( Class dataType, String dataQualifier, String searchClause, Object objectGroup,
			DOMetaData doMetaData, String completeQuery ) throws ApplicationException;

}
