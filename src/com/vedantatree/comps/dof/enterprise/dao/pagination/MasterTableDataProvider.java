package com.vedantatree.comps.dof.enterprise.dao.pagination;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.db.orm.HibernateUtil;
import org.vedantatree.utils.exceptions.ApplicationException;

import com.vedantatree.comps.dof.DOFUtils;
import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.pagination.server.IPaginationDataProvider;


/**
 * Paginatino Data provider for Master Data UI rendering using Dynamic Object Framework
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class MasterTableDataProvider implements IPaginationDataProvider

{

	private static Log LOGGER = LogFactory.getLog( MasterTableDataProvider.class );

	public Collection getPaginatedData( int currentPageIndex, int pageSize, String dataQualifier, String searchClause,
			String orderByClause, DOMetaData doMetaData, String completeQuery ) throws ApplicationException
	{

		LOGGER.debug( "getPaginatedData: doMetadata[ " + doMetaData + " ] searchClause[" + searchClause + "]" );

		String[] selectString = DOFUtils.getSelectStringArray( doMetaData );
		int totalRecords = 0;
		if( currentPageIndex == Integer.MAX_VALUE )
		{
			totalRecords = getTotalNumberOfRecords( dataQualifier, searchClause );
		}

		return HibernateUtil.getPaginatedData( dataQualifier, selectString, searchClause, currentPageIndex, pageSize,
				orderByClause, totalRecords );

	}

	public int getTotalNumberOfRecords( String dataQualifier, String searchClause ) throws ApplicationException
	{
		return HibernateUtil.getTotalNumberOfRecords( dataQualifier, searchClause );
	}

	public int getTotalNumberOfRecords( Class dataType, String dataQualifier, String searchClause, Object objectGroup,
			DOMetaData doMetaData, String completeQuery ) throws ApplicationException
	{
		return HibernateUtil.getTotalNumberOfRecords( dataQualifier, searchClause );
	}

}
