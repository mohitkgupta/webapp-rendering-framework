package com.vedantatree.comps.dof.web.security.actions;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.MultipartRequestWrapper;
import org.vedantatree.comps.securitymanager.AppSecurityException;
import org.vedantatree.comps.securitymanager.AppSecurityManager;
import org.vedantatree.comps.securitymanager.model.Menu;
import org.vedantatree.comps.securitymanager.model.MenuItem;
import org.vedantatree.comps.securitymanager.model.User;
import org.vedantatree.utils.StringUtils;
import org.vedantatree.utils.config.ConfigurationManager;
import org.vedantatree.utils.exceptions.ApplicationException;
import org.vedantatree.utils.exceptions.ExceptionUtils;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;

import com.vedantatree.comps.dof.DOFUtils;
import com.vedantatree.comps.dof.ViewMetaData;
import com.vedantatree.comps.dof.web.WebAppConstants;
import com.vedantatree.comps.dof.web.WebUIUtils;
import com.vedantatree.comps.dof.web.security.forms.LoginForm;


/**
 * Action class for Login management and to configure the application for settings like, view related information,
 * Locale-related information etc
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */

public class LoginAction extends Action implements WebAppConstants
{

	private static Log LOGGER = LogFactory.getLog( LoginAction.class );

	/**
	 * This method is used for login management.
	 */
	@Override
	public final ActionForward execute( ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, ApplicationException
	{
		LOGGER.trace( "LoginAction: execute" );

		HttpSession session = null;
		try
		{
			LoginForm loginForm = (LoginForm) form;
			session = request.getSession();
			// coming from login.jsp - entered by user
			String userName = loginForm.getUsername();
			String userPassword = loginForm.getPassword();
			// set from login.jsp
			String module = (String) session.getAttribute( MODULE );
			// coming from login.jsp
			String formAction = request.getParameter( FORM_ACTION );
			Locale currentLocale = (Locale) session.getAttribute( LOCALE_STRING );

			LOGGER.debug( "userName[" + userName + "] form-action[" + formAction + "] module[" + module + "]" );

			/*
			 * Clean user login information in any case, login - logout - others
			 */
			cleanApplicationInformation( session );

			/*
			 * forward request to home page or logout page, if action recommends for it
			 * Form action is coming as Logout from logout link in GlobalSettingHandler
			 * coming as null from login.jsp
			 * 
			 * Authentication failure is managed in login.jsp. Struts config will pass the parameter of error message
			 * key and login.jsp will show it
			 */
			session = request.getSession( true );
			session.setAttribute( MODULE, module );
			if( formAction != null )
			{
				if( formAction.equalsIgnoreCase( HOME ) )
				{
					LOGGER.debug( "Forwarding to Home Page as user requested for home page." );
					return mapping.findForward( HOMEPAGE );
				}

				else if( formAction.equalsIgnoreCase( LOGOUT ) )
				{
					LOGGER.debug( "Logout. Forwarding to Home Page or to Login page." );
					String loginforward = module == null ? HOMEPAGE : "login" + module;
					return mapping.findForward( loginforward );
				}
				else if( formAction.equalsIgnoreCase( FORGOT_PASSWORD )
						|| formAction.equalsIgnoreCase( UNLOCK_ACCOUNT_REQ ) )
				{
					LOGGER.debug( "Request start for password reset." );
					return mapping.findForward( "recoverpwdinput" );
				}
				else if( formAction.equalsIgnoreCase( EMAIL_SUBMIT ) )
				{
					LOGGER.debug( "User had submit the mail id for temporary password." );
					String login = loginForm.getUsername();
					String emailId = loginForm.getUserEmailId();

					boolean mailStatus = AppSecurityManager.getSharedInstance().startPwdRecoverRequest( login,
							emailId );
					if( !mailStatus )
					{
						LOGGER.debug( "Mail sending to the mail id [ " + emailId + "] had been failed for user id ["
								+ login + "]" );
						SystemException se = new SystemException( User.USER_ACCLOCKED, "Mail sending to the mail id [ "
								+ emailId + "] had been failed for user id [" + login + "]" );
						LOGGER.error( se );
						return mapping.findForward( "mailSentFailed" );
					}
					return mapping.findForward( "pwdTokenSentInfo" );
				}
				else if( formAction.equalsIgnoreCase( VERIFY_TEMP_PWD ) )
				{
					LOGGER.debug( "To login using the temporary password." );
					String newUrl = StringUtils.isQualifiedString( module )
							? ( "jsp/comps/login.jsp" + "?" + WebAppConstants.MODULE + "=" + module )
							: "jsp/comps/login.jsp";
					LOGGER.debug( "forwarding-to-after-setting-password[" + newUrl + "]" );
					try
					{
						response.sendRedirect( newUrl );
						return null;
					}
					catch( IOException e )
					{
						LOGGER.info( "Problem in redirection after temporary password sent", e );
						DOFUtils.setExceptionPageParameters( request, e, null, null );
						throw new IOException( "Problem in redirection after password change" );
					}
				}
				else
				{
					SystemException se = new SystemException( IErrorCodes.ILLEGAL_ARGUMENT_ERROR,
							"Specified form action value is not handled. It seems like a development bug. Wrong FormAction has been set. formAction["
									+ formAction + "]" );
					DOFUtils.setExceptionPageParameters( request, se, null, null );
					LOGGER.error( se );
					throw se;
				}
			}

			// user name comes from login page
			// module name set from login page, comes from index page
			if( !StringUtils.isQualifiedString( userName ) || !StringUtils.isQualifiedString( module ) )
			{
				SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
						"User Name, or User Password, or Module name is found null at the time of login. userName["
								+ userName + "] moduleName[" + module + "]" );
				DOFUtils.setExceptionPageParameters( request, se );
				LOGGER.error( se );
				throw se;
			}

			/*
			 * Proceed to Login operation
			 * Validate user information
			 * If validated, set the required application information
			 */
			User applicationUser = null;

			try
			{
				applicationUser = AppSecurityManager.getSharedInstance().authenticate( userName, userPassword, module );
			}
			catch( ApplicationException e )
			{
				if( e.getErrorCode() == User.USER_ACCLOCKED )
				{
					LOGGER.info( "The User account had been locked!!!", e );
					session.setAttribute( UNLOCK_ACCOUNT_REQ, "yes" );
					String newUrl = StringUtils.isQualifiedString( module )
							? ( "jsp/comps/login.jsp" + "?" + WebAppConstants.MODULE + "=" + module )
							: "jsp/comps/login.jsp";
					LOGGER.debug( "forwarding-to-after-account-locked[" + newUrl + "]" );
					// TODO change to login.jsp .
					response.sendRedirect( newUrl );
					return null;
				}
				else if( e.getErrorCode() == User.USER_INVALID_PWDTKN )
				{
					LOGGER.info( "The User need to use correct temppray password token !!!", e );
					return mapping.findForward( "wrongTempToken" );
				}
				else if( e.getErrorCode() == User.USER_INACTIVE )
				{
					LOGGER.info( "The User is not active. Please contact to admin !!!", e );
					return mapping.findForward( "unactiveUser" );
				}
				else if( e instanceof AppSecurityException )
				{

					LOGGER.info( "Security exception in login process", e );

					DOFUtils.setExceptionPageParameters( request, e, null, null );
					return mapping.findForward(
							e.getErrorCode() == IErrorCodes.AUTHENTICATION_FAILURE ? "authenticationFailed"
									: "loginFailed" );
				}

				LOGGER.info( "Problem in login process", e );
				DOFUtils.setExceptionPageParameters( request, e, null, null );
				return mapping.findForward( "loginFailed" );
			}
			LOGGER.debug( "User logged in. authenticated-user[" + applicationUser + "]" );

			// creating new session after invalidating the session on login/logout. It ensure to have a new session
			session = request.getSession( true );

			/*
			 * set language to english by default, if language is not set
			 */
			configureLocaleInformation( session, currentLocale );

			// configure application for logged in user
			// TODO: Mohit, change it to ApplicationContext
			ViewMetaData viewMetaData = null;

			if( applicationUser != null )
			{
				viewMetaData = configureApplicationInformation( applicationUser, module, loginForm, request, session );

				LOGGER.info( "User authenticated. user[ " + applicationUser.getUserId() + "] viewmetadata["
						+ viewMetaData + "]" );

				if( applicationUser.isPasswordExpired() )
				{
					LOGGER.info( "Password expired for user. Forwarding the request to Change Password Page!!!" );
					return mapping.findForward( "changePassword" );
				}

				if( viewMetaData == null )
				{
					SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
							"ViewMetadata MUST not be null after user logged in. It seems to be some development bug. Please forward it to System Developers." );
					DOFUtils.setExceptionPageParameters( request, se, null, null );
					LOGGER.error( se );
					throw se;
				}
			}
			else
			{
				LOGGER.info( "Didn't get any user from Security Manager for specified credential!!!" );
				return mapping.findForward( "loginFailed" );
			}

			if( module == null )
			{
				SystemException se = new SystemException( IErrorCodes.ILLEGAL_ARGUMENT_ERROR,
						"No module found with Request. It seems like a development bug. Some module name must be set during login" );
				DOFUtils.setExceptionPageParameters( request, se, null, null );
				LOGGER.error( se );
				throw se;
			}

			LOGGER.debug( "forwarding-to[" + module + "]" );

			/*
			 * Forwarding the control to next page
			 * Workflow goes according to two condition
			 * 
			 * One - if skip modules are defined in properties, and current module matches there, then the control
			 * should be forwarded to first menu of current module
			 * 
			 * Two - If skip modules are not defined, then next place path should be defined in properties.
			 * Control will be forward to this path.
			 */

			return forwardToApplicationPage( module, viewMetaData, request, response, mapping );
		}
		catch( Throwable e )
		{
			cleanApplicationInformation( session );
			WebUIUtils.setExceptionPageParameters( request, e, null, "popup" );
			if( e instanceof ApplicationException )
			{
				throw (ApplicationException) e;
			}
			else if( e instanceof SystemException )
			{
				throw (SystemException) e;
			}
			else
			{
				ApplicationException ae = new ApplicationException( IErrorCodes.ILLEGAL_STATE_ERROR,
						"Error during login/logout action. message[" + e.getMessage() + "]", e );
				ExceptionUtils.logException( LOGGER, ae.getMessage(), ae );
				throw ae;
			}
		}
	}

	/**
	 * This method will forward the control after successful login to first application page. Default behavior is to
	 * forward the control to next path defined with properties as 'login.forward.path', or to first menu of module,
	 * if module name is mentioned with 'login.forward.skip' property. However it can be overridden to change this
	 * behavior.
	 * 
	 * @param moduleName Name of current module
	 * @param viewMetaData ViewMetadata object
	 * @param request HTTP Request
	 * @param response HTTP Response
	 * @param mapping Struts Action Mapping object
	 * @return Next action
	 * @throws Exception if there is any error
	 */
	protected ActionForward forwardToApplicationPage( String moduleName, ViewMetaData viewMetaData,
			HttpServletRequest request, HttpServletResponse response, ActionMapping mapping ) throws Exception
	{
		String forwardToMenuModules = ConfigurationManager.getSharedInstance().getPropertyValue( "login.forward.menu",
				false );
		LOGGER.debug( "login.forward.menu-modules-to-forward-to-firstMenu[" + forwardToMenuModules + "]" );

		List moduleNamesForMenuForwarding = null;
		if( StringUtils.isQualifiedString( forwardToMenuModules ) )
		{
			moduleNamesForMenuForwarding = StringUtils.getTokenizedString( forwardToMenuModules, "," );
		}
		LOGGER.debug( "moduleNamesForMenuForwarding[" + moduleNamesForMenuForwarding + "]" );

		if( moduleNamesForMenuForwarding != null && moduleNamesForMenuForwarding.contains( moduleName ) )
		{
			/*
			 * Struts use multipart Request wrapper, however apache server using simple request wrapper.
			 * Hence we are extracting the request from request wrapper, to avoid, class cast exception
			 */
			if( request instanceof MultipartRequestWrapper )
			{
				request = ( (MultipartRequestWrapper) request ).getRequest();
				LOGGER.debug( "unwrap multipart request wrapper. unwrappedRequest[" + request + "]" );
			}
			/*
			 * For removing the EntityClass selection page from Budget Master Module for
			 * other modules it will work as it is.
			 */
			LOGGER.debug( "forwarding-to-currentMenu[" + viewMetaData.getCurrentMenu().getUrl() + "]" );
			request.getRequestDispatcher( viewMetaData.getCurrentMenu().getUrl() ).forward( request, response );

			/*
			 * TODO : We are forwarding to home page, after forwarding to first menu. Is that correct?
			 */
			return mapping.findForward( HOMEPAGE );
		}
		else
		{
			String pathToForward = ConfigurationManager.getSharedInstance().getPropertyValue( "login.forward.path" );
			LOGGER.debug( "login.forward.path-forwardingToPath[" + pathToForward + "]" );

			if( !StringUtils.isQualifiedString( pathToForward ) )
			{
				SystemException se = new SystemException( IErrorCodes.RESOURCE_NOT_FOUND,
						"Forward path not defined after login action. Please specify the path with application properties as 'login.forward.path'. " );
				DOFUtils.setExceptionPageParameters( request, se, null, null );
				LOGGER.error( se );
				throw se;
			}
			return mapping.findForward( pathToForward );
		}
	}

	/**
	 * This method is used to clean the session from application information. This is called on login to clear the
	 * previous information.
	 * 
	 * @param session HTTP Session
	 */
	protected void cleanApplicationInformation( HttpSession session )
	{
		session.setAttribute( "budgetEntity", null );
		session.setAttribute( VIEW_METADATA, null );
		session.removeAttribute( USER );
		session.removeAttribute( UNLOCK_ACCOUNT_REQ );
		// removing session attributes other than Locale
		Enumeration attributeNames = session.getAttributeNames();
		String attributeName;
		while( attributeNames.hasMoreElements() )
		{
			attributeName = (String) attributeNames.nextElement();
			// module is required while authenticating user fromk AppSecurityManager > SupervisionDelegate, as menus are
			// set based on Module
			if( attributeName.equalsIgnoreCase( LOCALE_STRING ) || attributeName.equalsIgnoreCase( MODULE ) )
			{
				LOGGER.debug( "Skipping cleaning of LocaleString or Module in Login Action. attributeName["
						+ attributeName + "] value[" + session.getAttribute( attributeName ) + "]" );
				continue;
			}
			LOGGER.debug( "removing-attribute-from-session[ " + attributeName + " ]" );
			session.removeAttribute( attributeName );
		}

		session.setAttribute( WebAppConstants.SELECTEDMENU, null );
		session.setAttribute( WebAppConstants.SELECTEDMENU_ITEM, null );

		session.invalidate();
	}

	/**
	 * This method is used to configure the session and application context for newly logged in user.
	 * 
	 * @param user Newly logged in user
	 * @param module Name of the current module
	 * @param request HTTP Request
	 * @param session HTTP Session
	 * @return ViewMetadata object having information for currently logged in user
	 */
	protected ViewMetaData configureApplicationInformation( User user, String module, LoginForm loginForm,
			HttpServletRequest request, HttpSession session ) throws Exception
	{

		user.initialize( module );
		session.setAttribute( USER, user );
		session.setAttribute( MODULE, module );

		ViewMetaData viewMetaData = ViewMetaData.createInstance();
		viewMetaData.setSession( session );
		viewMetaData.setCurrentUser( user );

		LOGGER.debug( "topNavigation[" + viewMetaData.getTopNavigation() + "]" );
		LOGGER.debug( "topNavigation-Size["
				+ ( viewMetaData.getTopNavigation() != null ? viewMetaData.getTopNavigation().size() : 0 ) + "]" );

		if( !user.isPasswordExpired() )
		{
			if( viewMetaData.getTopNavigation() != null && viewMetaData.getTopNavigation().size() > 0 )
			{
				String modulesDefaultTopMenus = ConfigurationManager.getSharedInstance()
						.getPropertyValue( MODULES_DEFAULT_TOP_MENUS );

				if( !StringUtils.isQualifiedString( modulesDefaultTopMenus ) )
				{
					SystemException se = new SystemException( IErrorCodes.RESOURCE_NOT_FOUND,
							"Default top menus for modules are not defined. These should be defined in public-books.properties for the property name as{modules-default-top-menus}. " );
					DOFUtils.setExceptionPageParameters( request, se, null, null );
					LOGGER.error( se );
					throw se;
				}
				List<String> defaultTopMenus = StringUtils.getTokenizedString( modulesDefaultTopMenus, "," );
				LOGGER.debug( "defaultTopMenus-from-config[" + defaultTopMenus + "]" );

				Iterator menuIter = viewMetaData.getTopNavigation().iterator();

				// if no default top menu is defined with prop file, we should use first menu
				Menu firstMenu = null;
				while( menuIter.hasNext() )
				{
					Menu headerMenu = (Menu) menuIter.next();
					if( firstMenu == null )
					{
						firstMenu = headerMenu;
					}
					LOGGER.debug( "headerMenu[" + headerMenu + "]" );

					MenuItem leftMenuItem = null;

					if( defaultTopMenus.contains( headerMenu.getDisplayName() ) )
					{
						setCurrentMenu( headerMenu, viewMetaData, session );
						break;
					}
				}
				if( viewMetaData.getCurrentMenu() == null && firstMenu != null )
				{
					setCurrentMenu( firstMenu, viewMetaData, session );
				}
				if( viewMetaData.getCurrentMenu() == null )
				{
					SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
							"No current menu found for current user to login. It seems like, either menus are not defined properly with system or there is some development bug" );
					DOFUtils.setExceptionPageParameters( request, se, null, null );
					LOGGER.error( se );
					throw se;
				}
			}
			else
			{
				LOGGER.debug(
						"No header Menu found for current User. Probably no menu item is defined for current user or application OR user is not authorized for any of the menu. User["
								+ user.getDisplayName() + "]" );
				// Menu headerMenu = new Menu();
				// headerMenu.setDisplayName( "Logout" );
				// headerMenu.setUrl( "index.jsp" );
				// headerMenu.setIndex( 0 );
				// viewMetaData.setCurrentMenu( headerMenu );
				ApplicationException se = new ApplicationException( IErrorCodes.RESOURCE_NOT_FOUND,
						"No header Menu found for current User. Probably no menu item is defined for current user or application OR user is not authorized for any of the menu. User["
								+ user + "]. Please define the menus first in supervision." );
				DOFUtils.setExceptionPageParameters( request, se, null, null );
				LOGGER.error( se );
				throw se;
			}
		}

		session.setAttribute( VIEW_METADATA, viewMetaData );

		return viewMetaData;
	}

	private void setCurrentMenu( Menu headerMenu, ViewMetaData viewMetaData, HttpSession session )
	{
		LOGGER.debug( "login-setting-current-menu[" + headerMenu.getDisplayName() + "]and id [" + headerMenu.getId() );
		viewMetaData.setCurrentMenu( headerMenu );

		MenuItem leftMenuItem = null;
		if( headerMenu.getLeftNavigationMenuItems() != null && headerMenu.getLeftNavigationMenuItems().size() > 0 )
		{
			leftMenuItem = headerMenu.getLeftNavigationMenuItems().iterator().next();
			LOGGER.debug( "login-setting-current-menu-item[" + leftMenuItem + "] id [" + leftMenuItem.getId() + "]" );
			viewMetaData.setCurrentMenuItem( leftMenuItem );
			session.setAttribute( CURRENT_LEFT_MENUITEM, leftMenuItem );
			session.setAttribute( CALL_FROM_MENUITEM_SWITCHING, "no" );
		}
	}

	/**
	 * It is used to configure the session for locale related information.
	 * 
	 * @param session HTTP Session
	 */
	protected void configureLocaleInformation( HttpSession session, Locale currentLocale ) throws Exception
	{
		LOGGER.debug( "current-locale[" + currentLocale + "]" );

		if( currentLocale == null )
		{
			LOGGER.debug( "noLanguageSet-SettingDefault[" + currentLocale.getLanguage() + "]" );
			currentLocale = new Locale( WebAppConstants.LANGUAGE_ISO_CODE_ENGLISH );
		}
		// This check has been put to manage the case when Browser set en_US as language. As our resource files are
		// currently written only for _en, so we are changing the locale language to 'en'.
		if( currentLocale.getLanguage().contains( "en" ) )
		{
			LOGGER.debug( "in matching of en actual is " + currentLocale.getLanguage() + "] going to set is "
					+ WebAppConstants.LANGUAGE_ISO_CODE_ENGLISH + "]" );
			currentLocale = new Locale( WebAppConstants.LANGUAGE_ISO_CODE_ENGLISH );
		}
		session.setAttribute( LOCALE_STRING, currentLocale );
	}
}
