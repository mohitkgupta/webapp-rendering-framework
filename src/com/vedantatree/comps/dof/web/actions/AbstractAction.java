package com.vedantatree.comps.dof.web.actions;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.vedantatree.utils.BeanUtils;
import org.vedantatree.utils.StringUtils;
import org.vedantatree.utils.exceptions.ApplicationException;
import org.vedantatree.utils.exceptions.ComponentException;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;
import org.vedantatree.utils.exceptions.db.RelationExistException;
import org.vedantatree.utils.exceptions.server.ServerBusinessException;

import com.vedantatree.comps.dof.DOFUtils;
import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.DOSchemaManager;
import com.vedantatree.comps.dof.ViewHelper;
import com.vedantatree.comps.dof.common.bdo.DisplayData;
import com.vedantatree.comps.dof.enterprise.JNDILookupManager;
import com.vedantatree.comps.dof.pagination.client.PaginationManagerDelegate;
import com.vedantatree.comps.dof.web.WebAppConstants;
import com.vedantatree.comps.dof.web.security.SecurityServletFilter;
import com.vedantatree.comps.dof.web.servlets.DynaListItemSelectionServlet;
import com.vedantatree.comps.dof.web.vdo.PageAction;


/**
 * It is the parent abstract class for all PublicBooks actions which provides facilities for dynamic page generation
 * using Dynamic Object Framework
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public abstract class AbstractAction extends Action implements WebAppConstants
{

	private static final Log							LOGGER					= LogFactory
			.getLog( AbstractAction.class );

	/**
	 * Local cache to maintain the request object for a operation. We populate this data for a request in the beginning
	 * of execute meethod and clean it while exiting from this method.
	 */
	private InheritableThreadLocal<HttpServletRequest>	operationRequest		= new InheritableThreadLocal<>();

	/**
	 * Local cache to maintain the response object for a operation. We populate this data for a request in the beginning
	 * of execute method and clean it while exiting from this method.
	 */
	private InheritableThreadLocal<HttpServletResponse>	operationResponse		= new InheritableThreadLocal<>();

	/**
	 * Local cache to maintain the Action Form object for a operation. We populate this data for a request in the
	 * beginning
	 * of execute method and clean it while exiting from this method.
	 */
	private InheritableThreadLocal<ActionForm>			operationFormBean		= new InheritableThreadLocal<>();

	/**
	 * Local cache to maintain the action mapping object for a operation. We populate this data for a request in the
	 * beginning
	 * of execute method and clean it while exiting from this method.
	 */
	private InheritableThreadLocal<ActionMapping>		operationActionMapping	= new InheritableThreadLocal<>();;

	/**
	 * It is the entrance of action and is called for every request which is directed to any struts action in framework
	 * implementing the AbstractAction. It initialize the required cache and the delegate the call to appropriate method
	 * as per specific form action. Finally it flush the cache.
	 */
	@Override
	public ActionForward execute( ActionMapping mapping, ActionForm formBean, HttpServletRequest request,
			HttpServletResponse response ) throws IOException, ServletException
	{
		LOGGER.debug( "execute: form[" + formBean + "]" );

		DOMetaData doMetadata = null;
		String formAction = null;
		ActionForward actionForward = null;

		try
		{
			formAction = request.getParameter( FORM_ACTION );
			LOGGER.debug( "formAction[ " + formAction + " ]" );

			setOperationRequest( request );
			setOperationResponse( response );
			setOperationActionMapping( mapping );
			setOperationFormBean( formBean );

			// get DOMetadata for current action and request
			doMetadata = getDOMetadata( request );
			LOGGER.trace( "execute: doMetadata[" + doMetadata + "]" );

			// Set all the required attributes to request like form/list actions, title etc
			// ViewHelper will be set here, if it has not been set yet
			DOFUtils.initializeRequestCache( request, doMetadata );

			// get business logic bean for server and data operations
			Object businessLogicBean = getBusinessLogicBean( request );

			// Requirement is to give user a chance to verify the validity of data.
			// If data is not valid, developer can set the error messages and can ask to forward the control to
			// desired page. So this method has been introduced which give a chance to user to verify the data, and if
			// it is not valid, return the action forward where control should be redirected to that page instead of
			// processing the request further

			actionForward = verifyInputs( mapping, formAction, request, formBean, businessLogicBean, doMetadata );

			// call the appropriate method for getting the action forward mapping based upon the form action set in
			// request. So depending upon the type of action, we ask the class to return the next link to forward the
			// request after setting the appropriate data in request

			// actionForward is null, it means that there is no problem in data verification and we can move on with
			// processing otherwise we shall not process it further, but will redirect the control to actionForward
			// returned by application

			if( actionForward == null )
			{
				if( ACTION_ADD.equals( formAction ) )
				{
					actionForward = getAddPage( request, response, mapping, formBean, doMetadata, businessLogicBean );
				}
				else if( ACTION_EDIT.equals( formAction ) )
				{
					actionForward = getEditPage( request, response, mapping, formBean, doMetadata, businessLogicBean );
				}
				else if( ACTION_VIEW.equals( formAction ) )
				{
					actionForward = getViewPage( request, response, mapping, formBean, doMetadata, businessLogicBean );
				}
				else if( ACTION_SAVE.equals( formAction ) )
				{
					actionForward = getSavePage( request, response, mapping, formBean, doMetadata, businessLogicBean );
				}
				else if( ACTION_DELETE.equals( formAction ) )
				{
					actionForward = getDeletePage( request, response, mapping, formBean, doMetadata,
							businessLogicBean );
				}
				else if( ACTION_LIST.equals( formAction ) )
				{
					actionForward = getListPage( request, response, mapping, formBean, doMetadata, businessLogicBean );
				}
				else
				{
					// if no generic action is matched, it means that the request is for some custom operation other
					// than
					// generic operations. Custom actions can be like : Manage banks of Supplier
					actionForward = getCustomizedPage( request, response, mapping, formBean, doMetadata,
							businessLogicBean );
				}
			}
			else
			{
				LOGGER.debug( "Implementation has returned actionForward after verification of data. "
						+ "It means that some verification has been failed and developer wants to direct the control "
						+ "to some specific page without saving the object. actionForward[" + actionForward + "]" );
			}

			LOGGER.debug( "returning-actionforward[" + actionForward + "]" );
			if( actionForward == null )
			{
				throw new ServletException(
						"No action forward has been found for given action. It seems like some implementation bug. form-action["
								+ formAction + "]" );
			}

			// This method can be called to give user a chance to customize request or session info
			// However currently we are commenting out this call, as it gives a big control to user to modify any
			// information which may break the system
			// Moreover there is no known case as of now.
			// We may open it once we get the usecases to use it
			// customizeRequestImpl( request );

			return actionForward;
		}
		catch( Throwable th )
		{
			LOGGER.error( "Error in Action Execution", th );

			try
			{
				actionForward = handleError( th, formAction );
				if( actionForward != null )
				{
					LOGGER.debug(
							"Returing actionforward provided by error handler. actionForward[" + actionForward + "]" );
					return actionForward;
				}
			}
			catch( Throwable e )
			{
				LOGGER.error( "Action Error Handler throw the error - fowarding to error page", e );
				th = e;
			}

			// handle exception. passing default value means, don't set this value, leave it
			DOFUtils.setExceptionPageParameters( request, th, WebAppConstants.DEFAULT_VALUE,
					WebAppConstants.DEFAULT_VALUE, WebAppConstants.DEFAULT_VALUE );
			if( th instanceof ServletException )
			{
				throw (ServletException) th;
			}
			throw new ServletException(
					"Error in execution of (abstract) action. Please refer to other details given on error page for information",
					th );
		}
		finally
		{
			setOperationRequest( null );
			setOperationResponse( null );
			setOperationActionMapping( null );
			setOperationFormBean( null );

			// cleaning the cache which we have populated initially
			// if dometadata is null, it means that we were unable to initialize the dometadata and also the request
			// cache

			// commenting it as this code is being executed before complete processing of request in forward path, and
			// then this method deletes the cache from request. So it is not a right approach. we need something which
			// can clean the cache once complete request is processed, that can be done in postprocessor of struts if
			// this facility is there
			if( doMetadata != null )
			{
				// DOFUtils.flushRequestCache( request, doMetadata );
			}
		}
	}

	/**
	 * Method to handle the error raised during Action execution. Developers can override this method to implement the
	 * error handling mechanism for various errors raised during various phases of execution. This will be a convenient
	 * place to handle all kind of execution errors and write the respective logic to manage these errors.
	 * 
	 * If error is handled properly and developer wants to forward the control to another page with some error messages
	 * or similar, developer can return a ActionForward object. Framework will forward the request to this returned
	 * address.
	 * 
	 * If there is some error occurred while handling the error, the framework will handle it like a normal error and
	 * will forward the control to error page.
	 * 
	 * Request, Response and Mapping can be retrieved using corresponding getter methods.
	 * 
	 * @param th error occurred during action execution
	 * @param fromAction Action for which we are processing the current request
	 * @return ActionForward - address of target web resource
	 * @throws ApplicationException If there is any error
	 */
	protected ActionForward handleError( Throwable th, String fromAction ) throws ApplicationException
	{
		return null;
	}

	/**
	 * It returns the name of action mapping where we should forward the request for adding any object.
	 * 
	 * Before returning the web link, this method should ensure all required properties set in request or session. For
	 * example, a new object, which may be required on add page to show the default values of various fields
	 */
	protected ActionForward getAddPage( HttpServletRequest request, HttpServletResponse response, ActionMapping mapping,
			ActionForm formBean, DOMetaData doMetadata, Object businessLogicBean )
			throws RemoteException, ServerBusinessException, ComponentException
	{
		LOGGER.trace( "getAddPage: formBean[" + formBean + "]" );

		Object newObject = getNewObject( businessLogicBean );
		request.getSession().setAttribute( getEditObjectKey(), newObject );

		// Tiles def will be added from dofutils with other parameters
		populateData( ACTION_ADD, request, formBean, newObject, businessLogicBean, doMetadata );
		return mapping.findForward( getWebPageMappingName( ACTION_ADD ) );
	}

	/**
	 * It returns the name of action mapping where we should forward the request for editing any existing object.
	 * 
	 * Before returning the web link, this method should ensure all required properties set in request or session. For
	 * example, it should load the existing object from persistence service and should set it so that it can be used at
	 * web page to fill the data. It retrieves the object based on object id passed in request. So default
	 * implementation throw exception if no object id is set in request.
	 */
	protected ActionForward getEditPage( HttpServletRequest request, HttpServletResponse response,
			ActionMapping mapping, ActionForm formBean, DOMetaData doMetadata, Object businessLogicBean )
			throws RemoteException, ComponentException, ServerBusinessException
	{

		LOGGER.debug( "getEditPage: formBean[" + formBean + "]" );

		Object objectId = request.getParameter( EDIT_OBJECT_ID );
		LOGGER.debug( "edit-object-Id[ " + objectId + " ]" );

		if( objectId == null )
		{
			IllegalStateException ise = new IllegalStateException( "Id found null for object to edit in Request." );
			LOGGER.error( ise );
			throw ise;
		}

		Object editObject = getObjectById( businessLogicBean, objectId );
		LOGGER.debug( "edit-Object [ " + editObject + " ]" );

		request.getSession().setAttribute( getEditObjectKey(), editObject );
		// Tiles def will be added from dofutils with other parameters
		populateData( ACTION_EDIT, request, formBean, editObject, businessLogicBean, doMetadata );
		return mapping.findForward( getWebPageMappingName( ACTION_EDIT ) );
	}

	/**
	 * It returns the name of action mapping where we should forward the request for viewing any object details in read
	 * only mode.
	 * 
	 * It is same as getEditPage. The only difference is that it sets a property, WebAppConstants.Editable_view to
	 * indicate that view should be read only. Edit page implementation should respect this property and show render the
	 * page in read only mode, if this is set.
	 */
	protected ActionForward getViewPage( HttpServletRequest request, HttpServletResponse response,
			ActionMapping mapping, ActionForm formBean, DOMetaData doMetadata, Object businessLogicBean )
			throws RemoteException, ServerBusinessException, ComponentException
	{
		request.setAttribute( EDITABLE_VIEW, Boolean.FALSE );
		return getEditPage( request, response, mapping, formBean, doMetadata, businessLogicBean );
	}

	/**
	 * It returns the name of action mapping where we should forward the request after saving an object. Generally it
	 * used to be list page, however it may be different based on application flow requirement. So first we give the
	 * chance to user to implement a custom page after saving the object. For this, this method calls the
	 * getCustomizedPage to check if any page is returned after saving. If it is not, by default, it redirects it to
	 * list page.
	 * 
	 * Default implementation uses business logic bean to save the object. It also set the object group property to
	 * object, if it is applicable.
	 * 
	 * @throws ComponentException TODO
	 */
	protected ActionForward getSavePage( HttpServletRequest request, HttpServletResponse response,
			ActionMapping mapping, ActionForm formBean, DOMetaData doMetadata, Object businessLogicBean )
			throws RemoteException, ServerBusinessException, ComponentException
	{

		Object dataObject = request.getSession().getAttribute( getEditObjectKey() );

		LOGGER.debug( "getEditObjectKey[ " + getEditObjectKey() + " ] dataObject[ " + dataObject + " ]" );

		if( dataObject == null )
		{
			throw new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"Save action called without having any data state in session." );
		}

		// TODO - analyze if this can be moved other appropriate place
		Object objectGroup = SecurityServletFilter.getObjectGroup();
		LOGGER.debug( "obejcetGroup [ " + objectGroup + " ]" );
		if( objectGroup != null )
		{

			String objectPropertyName = SecurityServletFilter.getObjectGroupoPropertyName();
			LOGGER.debug( "objectGroupPropertyName[ " + objectPropertyName + " ]" );
			try
			{
				if( dataObject != null && objectPropertyName != null )
				{
					BeanUtils.invokeMethod( dataObject, objectPropertyName, new Object[]
					{ objectGroup } );
				}
				else
				{
					throw new SystemException( IErrorCodes.ILLEGAL_ARGUMENT_ERROR,
							"DataObject or objectGroupPropertyName is null" );
				}
			}
			catch( ApplicationException e )
			{
				throw new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
						"Problem while getting values from BeanUtils", e );
			}

		}

		// ask applications to populate the data object with data filled by user
		populateData( ACTION_SAVE, request, formBean, dataObject, businessLogicBean, doMetadata );

		// save the data object
		dataObject = saveObject( businessLogicBean, dataObject );

		// setting saved object to session
		request.getSession().setAttribute( getEditObjectKey(), dataObject );

		// give a chance to user if she wants to show any custom page, other than list page, after saving an object
		ActionForward customSaveActionFoward = getCustomizedPage( request, response, mapping, formBean, doMetadata,
				businessLogicBean );
		LOGGER.trace( " CustomSaveActionFoward in Abstract Action = [" + customSaveActionFoward + "]" );

		// if we get any custom page, forward to it. Otherwise, forward to list page
		return customSaveActionFoward == null
				? getListPage( request, response, mapping, formBean, doMetadata, businessLogicBean )
				: customSaveActionFoward;
	}

	/**
	 * It returns the name of action mapping where we should forward the request after deleting any object. Default
	 * implementation also deletes the object using business logic bean from persist services.
	 * 
	 * It deletes the object based on object id passed in request. So default implementation throw exception if no
	 * object id is set in request.
	 * 
	 * @throws ComponentException TODO
	 */
	protected ActionForward getDeletePage( HttpServletRequest request, HttpServletResponse response,
			ActionMapping mapping, ActionForm formBean, DOMetaData doMetadata, Object businessLogicBean )
			throws RemoteException, ServerBusinessException, ComponentException
	{

		Object id = request.getParameter( EDIT_OBJECT_ID );
		if( id == null )
		{
			IllegalStateException ise = new IllegalStateException( "Id in Request found null for object to delete." );
			LOGGER.error( ise );
			throw ise;
		}
		try
		{
			deleteObjectById( request, businessLogicBean, id );
		}
		catch( RelationExistException e )
		{
			LOGGER.debug( "Problem while deleting the object having alive relations. id[" + id + "]", e );

			// TODO G change the name of the key like live.relation.detected
			request.setAttribute( LIVE_RELATIONS_DETECTED, "delete.exception.message" );
			//
			// ServerBusinessException sbe = new ServerBusinessException( IErrorCodes.CHILD_RECORD_FOUND,
			// "chile record found" );
			// LOGGER.error( sbe );
			// throw sbe;
		}

		return getListPage( request, response, mapping, formBean, doMetadata, businessLogicBean );
	}

	/**
	 * It returns the name of action mapping where we should forward the request for listing the records. Default
	 * implementation returns the link of DynamicListPage.
	 * 
	 * Before return, it needs to following things
	 * 
	 * Create search clause based on parameters set in request Create sort clause based on parameters set in reuest
	 * Create URL for various actions, like sort/searh/pagination, on list page Manage handling of custom search in case
	 * user has provided some custom search
	 * 
	 * @throws ComponentException TODO
	 */
	protected ActionForward getListPage( HttpServletRequest request, HttpServletResponse response,
			ActionMapping mapping, ActionForm formBean, DOMetaData doMetadata, Object businessLogicBean )
			throws RemoteException, ServerBusinessException, ComponentException
	{

		// creating base URL using web UI base path and action name
		// This URL will further be used to create the URL for searching, sorting and pagination and then these URLs
		// will be set to corresponding action UI element, so that whenever user clicks on these, control goes to right
		// URL. Page default action used to be like supplier.do

		// if baseURL is already set by called, then let us use that and skip this step

		if( request.getAttribute( BASE_URL ) == null )
		{
			// see if user want to provide their own base url
			StringBuffer baseURL = generateBaseURLImpl( request, doMetadata );

			// if nothing is found, let us built default base url
			if( baseURL == null )
			{
				baseURL = new StringBuffer();
				baseURL.append( doMetadata.getBaseURL() );
				baseURL.append( "?action=" );
				baseURL.append( ACTION_LIST );
			}
			request.setAttribute( BASE_URL, baseURL.toString() );
		}

		// TODO: It can be configurable from metadata
		request.setAttribute( SHOW_PAGINATION_TAG, true );

		// check if anyone has already set the data list before calling this action. It can be set, suppose any UI is
		// opening a child objects window and already having the list of objects with it
		// if anyone does not set the data list already, then we need to get it from PaginationManager

		if( request.getAttribute( DATALIST ) == null )
		{

			LOGGER.debug( "initializing data list from pagination manager" );

			int paginationUserId = Integer.MIN_VALUE;
			PaginationManagerDelegate paginationManager = PaginationManagerDelegate.getSharedInstance();

			/*
			 * get the pagination user id if user id is null register the user with all the required parameters create
			 * customize queries in case of customized search call update PM for updating with required parameters
			 * create the pagination url accordingly Get the list set as datalist Forward the request to list page
			 */

			// pagination is set from UI which actually telling the operation code for pagination i.e. next, previous,
			// fist, last or page index
			String pagination = request.getParameter( PAGINATION );
			String customizeSearch = request.getParameter( CUSTOMIZED_SEARCH );
			String paginationUserKey = getPaginationUserKey( request );
			Object paginationUserIdObj = request.getSession().getAttribute( paginationUserKey );
			LOGGER.debug( "session-paginationUserId[" + paginationUserIdObj + "] pagination[" + pagination
					+ "] customizedSearch[" + customizeSearch + "]" );

			// remove the previous pagination id if a new request has come to page
			// like if visited any other page and come back to this

			// TODO: Need to check this case more. We shall not remove the pagination user form session till any list
			// page is active. However we may need to re-initialize this in some case, when we need to find and jot down
			// here

			if( paginationUserIdObj != null && ( pagination == null && customizeSearch == null ) )
			{
				paginationUserId = ( (Integer) paginationUserIdObj ).intValue();
				paginationManager.unregisterPaginationUser( paginationUserId );
				// removing the selected ids data from session for current pagination user so that cache can be
				// reinitialized if required
				request.getSession().removeAttribute( getSelectedItemsKey( request ) );
				request.getSession().removeAttribute( paginationUserKey );
				paginationUserIdObj = null;
			}

			/*
			 * TODO: Correct me sometime
			 * 
			 * This newPaginationUser variable is a temporary solution. Problem is, we want to call the
			 * updatePaginationCache at server for pagination user after updatePaginationUser method is called,
			 * considering that it will update any search, sort or query clause etc. And this method update the total
			 * record counts also.
			 * 
			 * However here if we dont have any search, sort clause etc, then update method won't be called, and so the
			 * total records. If total records are not updated, UI will always show 1 page only.
			 * 
			 * Better solution is to pass all clauses while registration itself, later, if we are updating any existing
			 * user then these can be passed again. However for now, we are just keeping a flag, that this request is
			 * for new user, hence we need to call update anyhow whether there is any search,sort clause or not.
			 */
			boolean newPaginationUser = paginationUserIdObj == null;
			if( paginationUserIdObj == null )
			{
				paginationUserId = registerPaginationUser( request, paginationManager, doMetadata );
				request.getSession().setAttribute( paginationUserKey, new Integer( paginationUserId ) );
				// setting the set for caching selected ids data in session for current pagination user
				// it will be accessed from DynaList page when user will select or unselect any data
				request.getSession().setAttribute(
						DynaListItemSelectionServlet.LIST_ITEM_SELECTION_SESSION_ITEM_IDS + "_" + paginationUserId,
						new HashSet<String>() );
				LOGGER.debug( "register-paginationUserId[" + paginationUserId + "]" );
			}
			else
			{
				paginationUserId = ( (Integer) paginationUserIdObj ).intValue();
			}

			updatePaginationManager( paginationManager, paginationUserId, doMetadata, request, mapping,
					newPaginationUser );
			Collection dataCollection = paginationManager.getPaginationData( paginationUserId, pagination );
			request.setAttribute( LIST_PAGE_DATA, dataCollection );

			LOGGER.debug( "datalist[" + dataCollection.size() + "]" );
		}

		// POPUP handling - Based on this attribute we set the tiles definition on list page
		String popup = request.getParameter( POPUP );
		LOGGER.debug( "popup-req-param[" + popup + "]" );
		if( popup != null && popup.equalsIgnoreCase( "true" ) )
		{
			request.setAttribute( POPUP, true );
		}

		// set help page id, null check is applied because data can be set by caller action
		if( request.getAttribute( HELP ) == null )
		{
			request.setAttribute( HELP, doMetadata.getListPageHelpKey() );
		}

		return mapping.findForward( getWebPageMappingName( ACTION_LIST ) );
	}

	/**
	 * It returns the name of action mapping where we should forward the request in case of a action which is not
	 * generic. This situation comes when user is handling any case other than generic CRUD operations.
	 * 
	 * Here default implementation returns null. Implementing classes can override it to provide any implementation.
	 */
	protected ActionForward getCustomizedPage( HttpServletRequest request, HttpServletResponse response,
			ActionMapping mapping, ActionForm formBean, DOMetaData doMetadata, Object businessLogicBean )
			throws RemoteException, ServerBusinessException, ComponentException
	{
		return null;
	}

	/**
	 * This method returns the Set of Item ids selected on UI
	 * 
	 * @param request Current Server request
	 * @return Returns the Set of selected item ids
	 */
	protected final Set<String> getSelectedItemIds( HttpServletRequest request )
	{
		return (Set<String>) request.getSession().getAttribute( getSelectedItemsKey( request ) );
	}

	private final String getSelectedItemsKey( HttpServletRequest request )
	{
		return DynaListItemSelectionServlet.LIST_ITEM_SELECTION_SESSION_ITEM_IDS + "_"
				+ getPaginationUserKey( request );
	}

	/**
	 * It is a hook for user to provide the custom search url part for list page url. It needs to be overridden to
	 * create search URL in case of customize search. User need to add all the parameters to the specified pageActionURL
	 * string buffer so that this updated search url can be used to set to all the listing links on list page like
	 * search/sort/pagination etc.
	 * 
	 * It is called from getListPage in case if getListPage method find that custom search is being used
	 * 
	 * @param request HTTP Request
	 * @param pageActionURL String Buffer where user need to add the customized search clause
	 * @deprecated This method is no more required to be overriden. Instead user should use
	 *             'getURLSearchClauseForListImpl' method to override the default search URL part created by framework.
	 *             It is called every time before updating going to render the list page for data parameters
	 */
	@Deprecated
	protected void configurePageActionURLForCustomizeSearch( HttpServletRequest request, StringBuffer pageActionURL )
	{
		// nothing as default implementation
	}

	protected abstract void deleteObjectById( HttpServletRequest request, Object businessLogicBean, Object id )
			throws RelationExistException, ServerBusinessException, RemoteException;

	/**
	 * This method can be override when we need to customize the base url for list page. Default base url is page action
	 * from dometadata and 'list' as action name
	 * 
	 * @param request request from server
	 * @param doMetaData DOMetadata object
	 * 
	 * @return Base URL String
	 */
	protected StringBuffer generateBaseURLImpl( HttpServletRequest request, DOMetaData doMetaData )
	{
		return null;
	}

	protected Object getBusinessLogicBean( HttpServletRequest request ) throws ComponentException
	{
		return JNDILookupManager.lookupRemoteEJB( getBusinessLogicBeanName( request ),
				getBusinessLogicBeanHomeClass( request ) );
	}

	/**
	 * @return Class for session bean home
	 */
	protected abstract Class getBusinessLogicBeanHomeClass( HttpServletRequest request );

	/**
	 * @return JNDI name of the session bean
	 */
	protected abstract String getBusinessLogicBeanName( HttpServletRequest request );

	/**
	 * It is a hook using which user can provide any customized search clause. However it is called only if 'searchAct'
	 * attribute is set to something in request parameter.
	 * 
	 * @param request Server request
	 * @param doMetadata DOMetadata object
	 * @return Custom search clause
	 * @deprecated This method is no more required to be overriden. Instead user should use
	 *             'getQuerySearchClauseForListImpl' method to override the default search clause by framework. It is
	 *             called everytime before updating the pagination manager for data parameters
	 */
	@Deprecated
	protected String getCustomizeSearchClause( HttpServletRequest request, DOMetaData doMetadata )
	{
		return null;
	}

	/**
	 * It returns the DOMetadata for current action. DOMetadata is being loaded from DOSchemaManager by passing the key
	 * for Metadata. The action itself decide the key of metadata which it wants to load. Generally the key should match
	 * the metadata file name and the 'name' property of root 'object' element.
	 * 
	 * @param request HTTP Request
	 * @return Metadata for this action
	 * @throws ComponentException If there is any problem
	 */
	protected final DOMetaData getDOMetadata( HttpServletRequest request ) throws ComponentException
	{
		// We can simply ask DOSchemaManager everytime for the DOMetadata, as DOSchemaManager is maintaining the cache.
		// However object can be removed from cache if there are many concurrent request, as doschemamanager has the
		// maximum cache limit. So to ensure object at least for the life of request, we are putting it in session.
		// Otherwise, DOSchemaManager may need to create a new object again.

		// TODO: It can be put in request also. Please see if that is right.

		String metadataName = getUIMetadataName( request );
		LOGGER.debug( "meta data name [" + metadataName + "]" );
		DOMetaData doMetadata = (DOMetaData) request.getSession().getAttribute( metadataName );
		if( doMetadata == null )
		{
			doMetadata = DOSchemaManager.getDOMetadata( metadataName );
			request.getSession().setAttribute( metadataName, doMetadata );
		}
		return doMetadata;
	}

	/**
	 * It returns the ViewHelper for current action based on current DOMetadata
	 * 
	 * ViewHelper can not be stored in DOMetadata becuase same DOMetadata may be used for different UIs and ViewHelper
	 * stores view specific runtime properties
	 * 
	 * ViewHelper can not be stored in Action instance itself, as who knows, implementor of Abstract Action can return
	 * different DOMetadata for different request
	 * 
	 * @param request HTTP Request Object
	 * @return View Helper for this action
	 * @throws ComponentException If any exception arise while asking for dometadata
	 */
	protected ViewHelper getViewHelper( HttpServletRequest request ) throws ComponentException
	{
		DOMetaData metaData = getDOMetadata( request );
		return (ViewHelper) request.getSession().getAttribute( metaData.getViewHelperKey() );
	}

	protected String getEditObjectKey()
	{
		return StringUtils.getSimpleClassName( getClass().getName() ) + EDIT_OBJECT;
	}

	/**
	 * @return The log object for logging purpose
	 * 
	 * @deprecated No need to override this method any more. We shall use class specific logger itself, so
	 *             AbstractAction will use its own logger.
	 */
	@Deprecated
	protected Log getLogger()
	{
		return LOGGER;
	}

	/**
	 * Return new Business Data Object
	 * 
	 * @param businessLogicBean Session bean for now
	 * @return New BDO
	 * @throws ServerBusinessException
	 * @throws RemoteException
	 */
	protected abstract Object getNewObject( Object businessLogicBean ) throws ServerBusinessException, RemoteException;

	/**
	 * Return the Business Data Object for specified object id
	 * 
	 * @param businessLogicBean Session Bean for now
	 * @param id Id of the object
	 * @return
	 * @throws ServerBusinessException
	 * @throws RemoteException
	 */
	protected abstract Object getObjectById( Object businessLogicBean, Object id )
			throws ServerBusinessException, RemoteException;

	public Object getObjectGroup( HttpServletRequest request )
	{
		/*
		 * Fetch the Object Group from session
		 */
		return request.getSession().getAttribute( OBJECTGROUP );
	}

	/**
	 * It is used to make the customized action url in case of customized search. The action URL is required to set it
	 * to sorting / pagination / search actions on list page. In case of customize search, only concrete action can
	 * decide the required attributes of URL.
	 * 
	 * A base url will be passed as "pageActionURL" string buffer. User should add the rest part of URL to this string
	 * buffer.
	 * 
	 * TODO: The id should be changed to some unique id. The current id is not safe as it can be easily duplicated if
	 * same struts action is being used twice for two different UI
	 * 
	 * @param request HTTP Request Object
	 * @param pageActionURL StringBuffer containing base URL
	 */
	protected final String getPaginationUserKey( HttpServletRequest request )
	{
		return getClass().getName() + "." + PAGINATION_USER_ID_KEY;
	}

	private int getPaginationUserId( HttpServletRequest request, DOMetaData doMetadata )
	{
		Object paginationUserIdObj = request.getSession().getAttribute( getPaginationUserKey( request ) );
		if( paginationUserIdObj == null )
		{
			return Integer.MIN_VALUE;
		}
		return ( (Integer) paginationUserIdObj ).intValue();
	}

	/**
	 * It returns DOMetadata type for the action for which we will find the DOMetadata object from XML. Like in case of
	 * SupplierAction, it would be "Supplier".
	 * 
	 * The metadata will be searched as following:
	 * 
	 * If given name is a fully qualified object name having '.', the last simple name will be picked Try to find the
	 * file named as simplified name prefix with '.xml' If this file found, its child nodes will be scanned for the node
	 * named as 'object' and attribute named as 'name' having value equal to simplified name if file not found, search
	 * will be done in common metadata file for matching name node
	 * 
	 * @param request HTTP request object, can be used to find any request parameter
	 * @return DOMetadata Type for the action
	 */
	protected abstract String getUIMetadataName( HttpServletRequest request );

	protected abstract String getWebPageMappingName( String action );

	/**
	 * This method will give the user a chance to verify the data filled by user on UI. If data is verified, framework
	 * will send the request to populate the data in data object and then further to save it to persistent services
	 * using session beans etc.
	 * 
	 * This method is called before saving the data filled by user on form to database. This method should be
	 * implemented by extended classes to verify the data filled by user on form. If any data attribute is not correct,
	 * developer can use action errors to show the messages on screen or they can use any other preferred medium. If
	 * validation fails, developers can return ActionForward for input page. Framework will forward the request to that
	 * page instead of calling populate and saving functions further. Hence any kind of validations on data can be put
	 * here.
	 * 
	 * @param mapping Action Mapping give by Struts as per struts Config
	 * @param action Action for which current request is received
	 * @param request Server Request Object
	 * @param formBean Form Bean from Struts
	 * @param businessLogicBean Business Logic Service reference to use if any data needs to be fetched from server,
	 *        however it should be avoided as it will add performance burden. Right approach is to set the identifier in
	 *        data object here and get their actual value on server and set these to data object before saving in
	 *        session bean itself. If any validations need to be done from server, and that is better approach to show a
	 *        interactive message to user, that can be done here using business logic bean.
	 * @param doMetadata DOMetadata object for this UI request
	 * @return Null if there is no validation failure, or Instance of ActionForward otherwise if there are validation
	 *         failure and you want to redirect the control to any other page instead of calling the populate and save
	 *         methods. Generally the custom page will be input page itself in case of validation failure as developers
	 *         would want to redirect the control to input page for corrections.
	 * @throws RemoteException If there is any problem while accessing the Remote Services, control will be forwarded to
	 *         error page
	 * @throws ServerBusinessException If there is any exception from server business logic services, control will be
	 *         forwarded to error page
	 */
	protected ActionForward verifyInputs( ActionMapping mapping, String action, HttpServletRequest request,
			ActionForm formBean, Object businessLogicBean, DOMetaData doMetadata )
			throws RemoteException, ServerBusinessException
	{
		return null;
	}

	/**
	 * This method should be implemented by applications to populate the data objects and form bean depending upon the
	 * current action. Like in case of saving, data will be filled from form bean to data objects. However, in case of
	 * adding a new object or editing any existing object, it will be from data object to form mean.
	 * 
	 * @param action
	 * @param request
	 * @param formBean
	 * @param businessObject
	 * @param businessLogicBean
	 * @param doMetadata
	 * @throws RemoteException
	 * @throws ServerBusinessException
	 */
	protected abstract void populateData( String action, HttpServletRequest request, ActionForm formBean,
			Object businessObject, Object businessLogicBean, DOMetaData doMetadata )
			throws RemoteException, ServerBusinessException, ComponentException;

	protected abstract Object saveObject( Object businessLogicBean, Object dataObject )
			throws ServerBusinessException, RemoteException;

	/**
	 * It is used to register the pagination user with pagination manager. It is called by execute method whenever it is
	 * required to register the pagination user and is left abstract so that concrete implementation can specify the
	 * data provider name, page size etc
	 * 
	 * @param request HTTP Request Object
	 * @param paginationManager PaginationManagerDelegate instance
	 * @param doMetadata DOMetadata object for this action
	 * @return registered user id
	 */
	protected abstract int registerPaginationUser( HttpServletRequest request,
			PaginationManagerDelegate paginationManager, DOMetaData doMetadata )
			throws RemoteException, ServerBusinessException, ComponentException;

	private void updatePaginationManager( PaginationManagerDelegate paginationManager, int paginationUserId,
			DOMetaData doMetadata, HttpServletRequest request, ActionMapping mapping, boolean newPaginationUser )
			throws RemoteException, ServerBusinessException, ComponentException
	{
		// handle pagination page index
		String paginationAction = request.getParameter( PAGINATION );
		LOGGER.debug( "paginationAction[" + paginationAction + "]" );
		// no change to current page index if pagination action is set, otherwise set it to zero as we are getting the
		// first page. set first page as required page if pagination action is null i.e. first time call
		int pageIndex = paginationAction == null ? 0 : Integer.MAX_VALUE;

		// handle searching database clause and url formation, it provides hook also for user
		String searchClause = configureSearchClauseForList( request, doMetadata );

		// handle sorting database clause and url formation, it provides hooks also for user
		String orderByClause = configureSortClausesForList( request, doMetadata );

		// hook to get complete query for pagination from user
		String completePaginationQuery = getCompletePaginationQueryImpl( request, doMetadata );

		LOGGER.debug( "orderByClause[" + orderByClause + "] searchClause[" + searchClause + "] completePaginationQuery["
				+ completePaginationQuery + "]" );

		// if any of the criteria got changed like search clause or sorting order of column or complete query, update
		// pagination manager
		if( searchClause != null || orderByClause != null || completePaginationQuery != null || newPaginationUser )
		{
			LOGGER.debug( "updatingPM-framework-values: paginationUser[ " + paginationUserId + " ] pageIndex["
					+ pageIndex + "] searchClause[" + searchClause + "] orderByClause[" + orderByClause + "]" );

			updatePaginationManagerImpl( paginationManager, paginationUserId, pageIndex, searchClause, orderByClause,
					completePaginationQuery, doMetadata, request );
		}
	}

	/**
	 * It work on request parameters and try to derive the search clause for database and search url for list page. It
	 * also provides hooks to user to override the default implementation of both database clause and url for list
	 * 
	 * @param request Server Request
	 * @param doMetadata DOMetadata object
	 * @return Search Clause for database, formed by framework, possibly modified by users through the hooks provided
	 */
	private String configureSearchClauseForList( HttpServletRequest request, DOMetaData doMetadata )
			throws ComponentException
	{

		String baseURL = (String) request.getAttribute( "baseURL" );

		// name of search column and its value. It is set by default search UI elements provided by framework
		String searchColumnName = request.getParameter( SEARCH_COLUMN );
		String searchColumnValue = request.getParameter( SEARCH_COLUMN_VALUE );

		// name of column on which we need to apply the sorting. It is set by sorting UI element from list page, default
		// functionality by framework
		String sortColumnName = request.getParameter( SORT_COLUMN );

		// pagination action is set from UI if user has click on pagination command. It can be like
		// next/previous/first/last or page number
		String paginationAction = request.getParameter( PAGINATION );

		// it should be not null if user want to provide a custom search instead of using the default search provided by
		// framework
		String customizedSearch = request.getParameter( CUSTOMIZED_SEARCH );

		// search clause from request, if user has set a customized search clause
		String searchClause = (String) request.getAttribute( SEARCH_CLAUSE );
		String searchURLPartByUser = (String) request.getAttribute( SEARCH_URL_PART );

		LOGGER.debug( "baseURL[" + baseURL + "]" );
		LOGGER.debug( "paginationAction[" + paginationAction + "]" );
		LOGGER.debug( "searchColumnName[" + searchColumnName + "]" );
		LOGGER.debug( "searchColumnValue[" + searchColumnValue + "]" );
		LOGGER.debug( "sortColumnName[" + sortColumnName + "]" );
		LOGGER.debug( "customizedSearch[" + customizedSearch + "]" );
		LOGGER.debug( "searchClause-from-request[" + searchClause + "]" );
		LOGGER.debug( "searchURL-from-request[" + searchURLPartByUser + "]" );

		// if custom search is set, add the parameter for custom search for next link
		// actually it is a deprecated method, so as soon as we shall remove the deprected method to return the custom
		// data, we shall remove this block also
		if( customizedSearch != null )
		{
			LOGGER.debug( "customizedSearch[" + customizedSearch + "]" );
			baseURL = baseURL + "&searchAct=list";
			request.setAttribute( "baseURL", baseURL );
		}

		// Prepare search clause for list page
		StringBuffer searchClauseURL = new StringBuffer();

		// search tag is shown if customzied search is not enabled.
		// TODO It might be configured in metadata xml
		request.setAttribute( SHOW_SEARCH_TAG, true );

		// if search clause has not been set by anybody else - third party using this action, then let us create a
		// default search clause by framework process
		if( searchClause == null )
		{
			LOGGER.debug( "In IF When searchClause is NULL" );
			// if call is from customize search page OR any user wants to provider customize search clause values
			if( customizedSearch != null )
			{
				LOGGER.debug( "In IF When customizedSearch is not NULL" );

				// hide the search tag if we are using the customized search
				request.setAttribute( SHOW_SEARCH_TAG, false );

				// if pagination action is null, means first time we are here so need to make a search clause
				// if sort column is not null, means sorting has been changed, so need to update search clause
				if( paginationAction == null || sortColumnName != null )
				{
					LOGGER.debug( "In IF When paginationAction is NULL or sortColumnName is not NULL" );
					searchClause = getCustomizeSearchClause( request, doMetadata );
					LOGGER.debug( "searchClause-formed[" + searchClause + "]" );
				}

				// need to configure the pageActionURL for pagination/search/sort links as per current criteria
				configurePageActionURLForCustomizeSearch( request, searchClauseURL );

				// TODO search title
			}
			else if( StringUtils.isQualifiedString( searchColumnName ) )
			{
				validateUserInputForSearch( request, searchColumnValue );

				searchColumnValue = getCustomizedSearchColumnValue( searchColumnName, searchColumnValue );

				// set search column value as null if it is empty string
				if( !StringUtils.isQualifiedString( searchColumnValue ) )
				{
					searchColumnValue = null;
				}

				searchClause = DOFUtils.getPaginationSearchString( doMetadata, searchColumnValue, searchColumnName );
				searchClauseURL.append( "&searchColumn=" + searchColumnName );
				searchClauseURL.append( "&searchValue=" + searchColumnValue );

				request.setAttribute( TITLE, doMetadata.getListPageTitleKey() );

				// TODO: why are we setting this custom back action. Search page also should show all original actions
				// so that user can use these on search page also
				ArrayList<PageAction> formActions = new ArrayList<>();

				PageAction backAction = new PageAction();
				backAction.setUrl( doMetadata.getBaseURL() + "?action=" + ACTION_LIST );

				DisplayData backTextData = new DisplayData();
				backTextData.setText( "back" );

				backAction.setDisplayData( backTextData );
				// String faction2[] =
				// { doMetadata.getPageAction() + "?action=" + ACTION_LIST, "back" };

				formActions.add( backAction );
				request.setAttribute( DOMetaData.FORM_ACTIONS, formActions );
			}
			// If nothing is matched, let us see if user want to return some custom data from hooks below
		}
		// else get the pageActionURL from attribute. It should be set by callee who is setting the search clause
		else
		{
			StringUtils.assertQualifiedArgument( searchURLPartByUser );
			searchClauseURL.append( searchURLPartByUser );
		}

		LOGGER.debug( "searchClause[" + searchClause + "]" );
		LOGGER.debug( "searchClauseURL[" + searchClauseURL + "]" );

		// hooks for users to modify the attributes if they want to do that
		searchClause = getQuerySearchClauseForListImpl( searchClause, request, doMetadata );
		searchClauseURL = getURLSearchClauseForListImpl( searchClauseURL, request, doMetadata );
		String searchClauseURLString = searchClauseURL.toString();

		LOGGER.debug( "searchClause-after-user-hook[" + searchClause + "]" );
		LOGGER.debug( "searchClauseURL-after-user-hook[" + searchClauseURL + "]" );

		// Set searchClause for list page
		// TODO: Check scenario if user want to override any existing one
		if( StringUtils.isQualifiedString( searchClauseURLString ) )
		{
			request.setAttribute( SEARCH_CLAUSE_URL, searchClauseURLString );
		}

		// return final search clause
		return searchClause;

	}

	/**
	 * Developers can override this method to return the customized data for search value input by user on UI.
	 * For example: User is searching on status of any item. Status are shown as 'New', 'Approved', and 'Canceled'. But
	 * this status are managed in database as numeric values 1, 2, 3 respectively. In that case, user can return the
	 * right values for status which should be appended in query to search the data.
	 * 
	 * @param searchColumnValue search value input by user
	 * @return Customized search value
	 * @throws ComponentException If there is any problem
	 */
	protected String getCustomizedSearchColumnValue( String searchColumnName, String searchColumnValueByUser )
			throws ComponentException
	{
		return searchColumnValueByUser;
	}

	/**
	 * It give user a chance to return the customized search clause for database query. Framework is creating the search
	 * clause and calling this method as a hook for user. User can work upon already created search clause and then can
	 * return the custom clause
	 * 
	 * @param searchClause Search clause created by framework
	 * @param request Server Request
	 * @param doMetadata DOMetadata object
	 * @return Customized or same search clause. Default implementation returns same search clause
	 */
	protected String getQuerySearchClauseForListImpl( String searchClause, HttpServletRequest request,
			DOMetaData doMetadata )
	{
		return searchClause;
	}

	/**
	 * It give user a chance to return the customized search clause URL for list page. Framework is creating the search
	 * clause URL and calling this method as a hook for user. User can work upon already created URL and then can return
	 * the custom URL
	 * 
	 * User need to add all the parameters to the specified pageActionURL string buffer so that this updated search url
	 * can be used to set to all the listing links on list page like search/sort/pagination etc.
	 * 
	 * 
	 * @param searchClauseURL URl for search clause created by framework
	 * @param request Server Request
	 * @param doMetadata DOMetadata object
	 * @return Customized or same search clause URL. Default implementation returns same URL
	 */
	protected StringBuffer getURLSearchClauseForListImpl( StringBuffer searchClauseURL, HttpServletRequest request,
			DOMetaData doMetadata )
	{
		return searchClauseURL;
	}

	/**
	 * It work on request parameters and try to derive the sort clause for database and sort url for list page. It also
	 * provides hooks to user to override the default implementation of both database clause and url for list
	 * 
	 * @param request Server Request
	 * @param doMetadata DOMetadata object
	 * @return OrderBy Clause for database, formed by framework, possibly modified by users through the hooks provided
	 */
	private String configureSortClausesForList( HttpServletRequest request, DOMetaData doMetadata )
	{
		String sortColumnName = request.getParameter( SORT_COLUMN );
		String sortOrderDescending = request.getParameter( IS_DESCENDING );
		LOGGER.debug( "sortColumnName[" + sortColumnName + "] descending[" + sortOrderDescending + "]" );
		String orderBy = null;

		// if sort column name is not null, let us make order by clause
		if( StringUtils.isQualifiedString( sortColumnName ) )
		{

			/*
			 * Earlier list page is sending the display name key, however new dynamicList page is sending the DBName of
			 * the column. However old code is here to support the list.jsp.
			 */
			orderBy = DOFUtils.getColumnDBName( doMetadata, sortColumnName );
			if( orderBy == null )
			{
				LOGGER.debug( "setting sortColumn name as order by clause, it seems like new dynamicList page case" );
				orderBy = sortColumnName;
			}
		}

		// give user a chance to override the order by clause
		orderBy = getQuerySortClauseForListImpl( orderBy, request, doMetadata );

		// it seems redundant, but the difference is that here we are setting the attribute, and earlier it is set as
		// parameter. set sort column name to request to render it properly on UI
		request.setAttribute( SORT_COLUMN, sortColumnName );

		if( !StringUtils.isQualifiedString( orderBy ) )
		{
			LOGGER.debug(
					"No 'order by' clause has been defined, neither from UI nor by User custom implementation. Hence returning null" );
			return null;
		}

		// now let us create sort clause url so that it can be set to all links on list page
		StringBuffer sortClauseURL = new StringBuffer();

		// if user has already set the sort clause url, use it.
		// TODO: we should remove this approach later, because we are already giving the method in abstract action to
		// override it
		if( request.getAttribute( SORT_CLAUSE_URL ) != null )
		{
			sortClauseURL.append( request.getAttribute( SORT_CLAUSE_URL ) );
		}
		else
		{
			LOGGER.debug( "creating the sort clause" );

			// handle if ascending order is selected
			if( sortOrderDescending != null && "true".equals( sortOrderDescending ) )
			{
				orderBy += " asc";
				request.setAttribute( IS_DESCENDING, "true" );
				sortClauseURL.append( "&isDescending=true" );
			}
			// handle if descending order is selected
			else if( orderBy != null )
			{
				orderBy += " desc";
				request.setAttribute( IS_DESCENDING, "false" );
				sortClauseURL.append( "&isDescending=false" );
			}

			sortClauseURL.append( "&sortColumn=" + sortColumnName );
		}
		LOGGER.debug( "sortClause-URL[" + sortClauseURL + "]" );

		String sortClauseURLString = getURLSortClauseForListImpl( sortClauseURL.toString(), request, doMetadata );
		LOGGER.debug( "sortClause-URL-updateByUser[" + sortClauseURLString + "]" );

		// TODO: is this right.. what if user want to reset anything. Check this scenario
		if( StringUtils.isQualifiedString( sortClauseURLString ) )
		{
			request.setAttribute( SORT_CLAUSE_URL, sortClauseURLString );
		}

		return orderBy;
	}

	/**
	 * It give user a chance to return the customized order by clause. Framework is creating the order by clause and
	 * calling this method as a hook for user. User can work upon already created order by clause and then can return
	 * the custom clause
	 * 
	 * @param orderBy Order By clause created by framework
	 * @param request Server Request
	 * @param doMetadata DOMetadata object
	 * @return Customized or same order by clause. Default implementation returns same order by clause
	 */
	protected String getQuerySortClauseForListImpl( String orderBy, HttpServletRequest request, DOMetaData doMetadata )
	{
		return orderBy;
	}

	/**
	 * It give user a chance to return the customized sort/orderby clause URL for list page. Framework is creating the
	 * sort clause URL and calling this method as a hook for user. User can work upon already created URL and then can
	 * return the custom URL
	 * 
	 * @param sortClauseURL URl for sort clause created by framework
	 * @param request Server Request
	 * @param doMetadata DOMetadata object
	 * @return Customized or same Sort clause URL. Default implementation returns same URL
	 */
	protected String getURLSortClauseForListImpl( String sortClauseURL, HttpServletRequest request,
			DOMetaData doMetadata )
	{
		return sortClauseURL;
	}

	/**
	 * This method is updating the pagination manager with new updated information for data. User may override this
	 * method to provide their custom implementation for any of the parameter
	 * 
	 * @param paginationManager Instance of Pagination Manager Delegate
	 * @param paginationUserId Id of the pagination user
	 * @param pageIndex Current page index on UI
	 * @param searchClause Search clause to filter the data
	 * @param orderBy Order by clause to sort the data
	 * @param completeQuery Complete query, if anyone want to use it
	 * @param doMetadata DOMetadata object
	 * @param request Server request object
	 * @throws ServerBusinessException If there is any problem
	 */
	protected void updatePaginationManagerImpl( PaginationManagerDelegate paginationManager, int paginationUserId,
			int pageIndex, String searchClause, String orderBy, String completeQuery, DOMetaData doMetadata,
			HttpServletRequest request ) throws ServerBusinessException, RemoteException, ComponentException
	{
		LOGGER.debug( "updatingPMImpl: paginationUser[ " + paginationUserId + " ] pageIndex[" + pageIndex
				+ "] searchClause[" + searchClause + "] orderBy[" + orderBy + "]" );

		paginationManager.updateRegisteredUser( paginationUserId, pageIndex, searchClause, orderBy, doMetadata,
				completeQuery );
	}

	/**
	 * This method is being called by framework abstract action before updating the pagination manager, to give user a
	 * chance to provide the complete query for getting the pagination data. User may override this method to provide
	 * the query which pagination manager should use to fetch the data from database
	 * 
	 * @param request Server Request
	 * @param doMetadata DoMetadata object
	 * @return Complete query for pagination
	 */
	protected String getCompletePaginationQueryImpl( HttpServletRequest request, DOMetaData doMetadata )
	{
		return null;
	}

	/**
	 * This method will provide base to customize requests and sessions before sending to list page
	 * 
	 * This method is not being called as of now as it is not in use anywhere. However later if we found any usecase
	 * supporting this call, we shall open it
	 * 
	 * @param request - HttpServerRequest
	 */
	private void customizeRequestImpl( HttpServletRequest request )
	{
		/*
		 * Add your code to modify Request or Session handling for listing
		 */
	}

	// TODO - integrate with struts validation framework, put default validations
	private void validateUserInputForSearch( HttpServletRequest request, String userInput )
	{
		/*
		 * Code for checking the invalid character in search column value. This is to avoid the invalid/errornous inputs
		 * given by user in search field
		 */

		// boolean isValidChrs = false;
		//
		// if(searchColumnValue!=null){
		// LOGGER.debug( "in if when searchColumnValue is not null" );
		// searchColumnValue=searchColumnValue.trim();
		// char[] chars = searchColumnValue.toCharArray();
		// for (int x = 0; x < chars.length; x++) {
		// final char c = chars[x];
		// LOGGER.debug("c = " + c);
		// LOGGER.debug("((int)c) = " + ((int) c));
		// if(c==39)
		// {
		// LOGGER.debug("in if");
		// isValidChrs = true;
		// break;
		// }
		// }
		// }
		//
		// if(isValidChrs){
		// LOGGER.debug( "in if when isValidChrs is true '" );
		// searchColumnValue="";
		// LOGGER.debug( "Setting Attribute in Request" );
		// request.setAttribute(WRONGINPUT_FROMLISTPAGE, "wrongInput");
		// searchColumnValue="";
		// }

		// if(searchColumnValue!=null){
		// LOGGER.debug( "in if when searchColumnValue is not null" );
		// searchColumnValue=searchColumnValue.trim();
		// if(searchColumnValue.equals("'")){
		// LOGGER.debug( "in if when searchColumnValue is '" );
		// searchColumnValue="";
		// LOGGER.debug( "Setting Attribute in Request" );
		// request.setAttribute(WRONGINPUT_FROMLISTPAGE, "wrongInput");
		// searchColumnValue="";
		// }
		// }

		// protected abstract String getPageHelpId();

	}

	/**
	 * It returns the struts action mapping for give request
	 * 
	 * @param request Server request
	 * @return Struts Action Mapping
	 */
	protected ActionMapping getActionMapping( HttpServletRequest request )
	{
		return (ActionMapping) request.getAttribute( Globals.MAPPING_KEY );
	}

	protected HttpServletRequest getOperationRequest()
	{
		return operationRequest.get();
	}

	private void setOperationRequest( HttpServletRequest httpRequest )
	{
		this.operationRequest.set( httpRequest );
	}

	protected HttpServletResponse getOperationResponse()
	{
		return operationResponse.get();
	}

	private void setOperationResponse( HttpServletResponse httpResponse )
	{
		this.operationResponse.set( httpResponse );
	}

	protected ActionMapping getOperationActionMapping()
	{
		return operationActionMapping.get();
	}

	private void setOperationActionMapping( ActionMapping actionMapping )
	{
		this.operationActionMapping.set( actionMapping );
	}

	protected ActionForm getOperationFormBean()
	{
		return operationFormBean.get();
	}

	private void setOperationFormBean( ActionForm formBean )
	{
		this.operationFormBean.set( formBean );
	}

	protected DOMetaData getOperationDOMetadata() throws ComponentException
	{
		return getDOMetadata( getOperationRequest() );
	}

}
