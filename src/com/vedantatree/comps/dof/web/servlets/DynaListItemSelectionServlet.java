package com.vedantatree.comps.dof.web.servlets;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.StringUtils;

import com.vedantatree.comps.dof.web.tag.DynaListDataTableTag;


/**
 * This servlet will help the Dynamic List page to maintain the selection of row items across the pagination pages from
 * UI. Whenever any selection will be changed on UI, UI will call this servlet to update the selection cache in session.
 * This session cache will be used by applications while processing the UI or business logic at UI tier, whenever
 * required.
 * 
 * Currently we are using Web UI pagination user id as unique key to store the selections in Session. Later it can be
 * changed, if we find any better option.
 * 
 * Currently servlet is handling three operations, add / remove / clear all. For add and remove operations, a string of
 * comma separated ids is expected. For clear all, nothing is expected as then we just need to clear the cache from
 * session
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public class DynaListItemSelectionServlet extends HttpServlet
{

	private static Log		LOGGER										= LogFactory
			.getLog( DynaListDataTableTag.class );

	public static String	LIST_ITEM_SELECTION_OPERATION_MODE			= "list.item.selection.op.code";
	public static String	LIST_ITEM_SELECTION_OPERATION_MODE_ADD		= "list.item.selection.op.code.add";
	public static String	LIST_ITEM_SELECTION_OPERATION_MODE_REMOVE	= "list.item.selection.op.code.remove";
	public static String	LIST_ITEM_SELECTION_OPERATION_MODE_CLEAR	= "list.item.selection.op.code.clear";
	public static String	LIST_ITEM_SELECTION_UI_ID					= "list.item.selection.ui.id";
	public static String	LIST_ITEM_SELECTION_ITEM_IDS				= "list.item.selection.item.ids";
	public static String	LIST_ITEM_SELECTION_SESSION_ITEM_IDS		= "list.item.selection.session.item.ids";

	@Override
	protected void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException
	{
		LOGGER.trace( "DynaListItemSelectionServlet: doGet" );

		/*
		 * get operation mode, whether to add or remove or clear
		 * 
		 * get UI id from request
		 * get ids to update from request
		 * get saved ids list from session
		 * 
		 * if add: add new ids to persisted list
		 * if remove: remove new ids from persisted list
		 * if clear: clear the persisted list from session
		 */

		Object requestAttributeTemp = request.getAttribute( LIST_ITEM_SELECTION_UI_ID );
		String uiId = requestAttributeTemp == null ? null : requestAttributeTemp.toString();
		LOGGER.debug( "uiId[" + uiId + "]" );

		if( !StringUtils.isQualifiedString( uiId ) )
		{
			ServletException se = new ServletException(
					"UI ID must not be null while operating the item selection for dynamic list. Please set it in request before sending the request" );
			LOGGER.error( se );
			throw se;
		}

		requestAttributeTemp = request.getAttribute( LIST_ITEM_SELECTION_OPERATION_MODE );
		String operationCode = requestAttributeTemp == null ? null : requestAttributeTemp.toString();
		LOGGER.debug( "operationCode[" + operationCode + "]" );

		if( !StringUtils.isQualifiedString( operationCode ) )
		{
			ServletException se = new ServletException(
					"Operation Code must not be null while operating the item selection for dynamic list. Please set it in request before sending the request" );
			LOGGER.error( se );
			throw se;
		}

		requestAttributeTemp = request.getAttribute( LIST_ITEM_SELECTION_ITEM_IDS );
		String selectionItems = requestAttributeTemp == null ? null : requestAttributeTemp.toString();
		LOGGER.debug( "selectionItems[" + selectionItems + "]" );

		if( !operationCode.equalsIgnoreCase( LIST_ITEM_SELECTION_OPERATION_MODE_CLEAR )
				&& !StringUtils.isQualifiedString( selectionItems ) )
		{
			ServletException se = new ServletException(
					"List of items to update for selection must not be null while operating the item selection for dynamic list. Please set it in request before sending the request" );
			LOGGER.error( se );
			throw se;
		}
		List<String> selectionItemsList = StringUtils.getTokenizedString( selectionItems, "," );

		requestAttributeTemp = request.getSession().getAttribute( LIST_ITEM_SELECTION_SESSION_ITEM_IDS + "_" + uiId );
		Set<String> sessionSelectedItems = (Set) requestAttributeTemp;
		LOGGER.debug( "sessionSelectedItems[" + sessionSelectedItems + "]" );

		if( sessionSelectedItems == null )
		{
			sessionSelectedItems = new HashSet<>();
			request.getSession().setAttribute( LIST_ITEM_SELECTION_SESSION_ITEM_IDS + "_" + uiId,
					sessionSelectedItems );
		}

		if( LIST_ITEM_SELECTION_OPERATION_MODE_CLEAR.equalsIgnoreCase( operationCode ) )
		{
			sessionSelectedItems.clear();
		}
		else if( LIST_ITEM_SELECTION_OPERATION_MODE_ADD.equalsIgnoreCase( operationCode ) )
		{
			sessionSelectedItems.addAll( selectionItemsList );
		}
		else if( LIST_ITEM_SELECTION_OPERATION_MODE_REMOVE.equalsIgnoreCase( operationCode ) )
		{
			sessionSelectedItems.removeAll( selectionItemsList );
		}
		else
		{
			ServletException se = new ServletException(
					"Specified operation code is not recognized. Please specify correct operation code.current-operationCode["
							+ operationCode + "]" );
			LOGGER.error( se );
			throw se;
		}
		LOGGER.debug( "updated-sessionSelectedItems[" + sessionSelectedItems + "]" );
	}

}
