package com.vedantatree.comps.dof.pagination;

import java.util.ArrayList;


/**
 * Data structure for returning the data to user for pagination request from Pagination Manager
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public class PaginationData extends ArrayList
{

	private static final long	serialVersionUID	= 2008041801L;

	/**
	 * Index of current page (current page show on UI)
	 */
	private int					currentPageIndex;

	/**
	 * Total number of records for a page
	 */
	private int					pageSize;

	/**
	 * Total number of records in the system for current UI
	 */
	private int					totalRecordsSize;

	/**
	 * ID of the pagination user for which this data is being loaded
	 */
	private long				paginationUserId;

	public int getCurrentPageIndex()
	{
		return currentPageIndex;
	}

	public int getPageSize()
	{
		return pageSize;
	}

	public int getTotalRecordsSize()
	{
		return totalRecordsSize;
	}

	public void setCurrentPageIndex( int currentPageIndex )
	{
		this.currentPageIndex = currentPageIndex;
	}

	public void setPageSize( int pageSize )
	{
		this.pageSize = pageSize;
	}

	public void setTotalRecordsSize( int totalRecordsSize )
	{
		this.totalRecordsSize = totalRecordsSize;
	}

	public long getPaginationUserId()
	{
		return paginationUserId;
	}

	public void setPaginationUserId( long paginationUserId )
	{
		this.paginationUserId = paginationUserId;
	}

}
