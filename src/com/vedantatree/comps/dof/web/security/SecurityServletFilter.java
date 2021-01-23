package com.vedantatree.comps.dof.web.security;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.comps.securitymanager.AppSecurityException;
import org.vedantatree.comps.securitymanager.AppSecurityManager;
import org.vedantatree.comps.securitymanager.model.Menu;
import org.vedantatree.comps.securitymanager.model.MenuItem;
import org.vedantatree.comps.securitymanager.model.User;
import org.vedantatree.utils.Utilities;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;

import com.vedantatree.comps.dof.DOFUtils;
import com.vedantatree.comps.dof.ViewMetaData;
import com.vedantatree.comps.dof.web.WebAppConstants;


/**
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public class SecurityServletFilter implements Filter, WebAppConstants
{

	private static Log								LOGGER		= LogFactory.getLog( SecurityServletFilter.class );

	private static InheritableThreadLocal<Object>	THREADLOCAL	= new InheritableThreadLocal<>();

	private static String							objectGroupPropertyName;

	@Override
	public void init( FilterConfig arg0 ) throws ServletException
	{
	}

	@Override
	public void destroy()
	{
	}

	private void setResponseHeaders( HttpServletResponse response ) throws Exception
	{
		// refer to http://dltyxh.com/resin-doc/doc/proxy-cache.xtp for header details
		// Refer to http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9 for HTTP Header detail. Very good
		// document
		// http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html - for caching related headers

		response.setHeader( "pragma", "no-cache" );

		// Setting the max-age header will cache the results for a specified time. For heavily loaded pages, even
		// setting short expires times can significantly improve performance.
		// Note, pages using sessions should not be cached, although more sophisticated headers like
		// "Cache-Control: private" can specify caching only for the session's browser.

		response.setHeader( "Cache-control", "no-cache, no-store, must-revalidate" ); // ,
		// max-age=60

		/*
		 * Normally, this behavior is not what you want. Instead, you may want the browser to cache the page, but not
		 * let other browsers see the same page. To do that, you'll set the "Cache-Control: private" header. You'll need
		 * to use addHeader, not setHeader, so the browser will get both "Cache-Control" directives. // An application
		 * can also set the Expires header to enable caching, when the expiration date is a specific
		 */
		response.addHeader( "Cache-Control", "private" );

		// time instead of an interval. For heavily loaded pages, even setting short expires times can significantly
		// improve performance. Sessions should be disabled for caching.

		// ( (HttpServletResponse) response ).setHeader( "Expires", "01 Apr 1995 01:10:10 GMT");
		response.setDateHeader( "Expires", 0 ); // -1 // System.currentmilliseconds() +
		// 15000;
	}

	@Override
	public final void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
			throws IOException, ServletException
	{
		LOGGER.trace( "doFilter: request[" + request + "]" );

		try
		{
			// commented as it is creating problem.
			// at one page, while adding prepayments to OP from popup, browser is asking to resend the information.
			// We are using ajax servlets here to refresh the page to reflect newly added prepayments
			// so when user is saying yes, browser is sending information again and those records are being added again
			// and application is crashing
			// need to check it
			// Need to verify it with Vikas for this case
			// Again opening it on 26-09-11, as we found that above problem was due to some other reason

			setResponseHeaders( (HttpServletResponse) response );

			printRequestInformation( request );

			boolean authorized = isAuthroizedRequest( request );
			LOGGER.debug( "authorized-for-request[" + authorized + "]" );

			if( authorized )
			{
				setLanguageAttributes( request );

				Object objectGroup = ( (HttpServletRequest) request ).getSession().getAttribute( OBJECTGROUP );
				LOGGER.debug( "objectGroup[ " + objectGroup + " ]" );

				/*
				 * get objectgroup from session setObjectGroup with this objectgroup
				 * call doChain
				 * SetObjectGroup method with null
				 */
				setObjectGroup( objectGroup );
				// log the time if required sometime in future
				// long responseTime =
				try
				{
					chain.doFilter( request, response );
				}
				finally
				{
					setObjectGroup( null );
				}
			}
			else
			{
				LOGGER.debug( "Request is not validated due to Authentication failure." );

				( (HttpServletResponse) response ).sendError( ( (HttpServletResponse) response ).SC_UNAUTHORIZED,
						"User is not authorized to access the request URL["
								+ ( (HttpServletRequest) request ).getRequestURL() + "]" );
			}
		}
		catch( Throwable th )
		{
			// a servlet exception will automatically send the control to error page

			LOGGER.error( "Error while processing the request", th );
			if( request.getAttribute( WebAppConstants.EXCEPTION_OBJECT ) == null )
			{
				DOFUtils.setExceptionPageParameters( request, th, null, null );
			}
			// Servlet exception does not pass root cause to Exception's generic cause variable, but these are storing
			// it in local variable. So we are accessing that variable here.

			if( th instanceof ServletException )
			{
				LOGGER.error( "Nested Exception of ServletException", ( (ServletException) th ).getRootCause() );

				throw (ServletException) th;
			}
			throw new ServletException( th.getMessage(), th );
		}
	}

	private void setLanguageAttributes( ServletRequest request )
	{
		if( request instanceof HttpServletRequest )
		{

			Object localeSet = ( (HttpServletRequest) request ).getSession().getAttribute( LOCALE_STRING );
			LOGGER.debug( "locale-string-attribute[ " + localeSet + "]" );

			if( localeSet == null )
			{
				String language = request.getLocale().toString();
				LOGGER.debug( "request-locale[ " + language + " ]" );
				( (HttpServletRequest) request ).getSession().setAttribute( LOCALE_STRING, new Locale( language ) );
			}

		}
		else
		{
			LOGGER.info( "Unable to set language in filter as we dont have HTTP Request." );
		}

	}

	protected String getIndexPage()
	{
		return "index.jsp";
	}

	protected String getErrorPage()
	{
		return "/jsp/comps/error.jsp";
	}

	protected boolean isAuthroizedRequest( ServletRequest request )
	{
		LOGGER.trace( "isAuthroizedRequest: request[" + request + "]" );

		Utilities.assertNotNullArgument( request );
		if( !( request instanceof HttpServletRequest ) )
		{
			SystemException se = new SystemException( IErrorCodes.ILLEGAL_ARGUMENT_ERROR,
					"PublicBooks assumes HTTPServletRequest, however the request is of different type. Request["
							+ request + "]" );
			LOGGER.error( se );
			throw se;
		}

		HttpServletRequest httpRequest = ( (HttpServletRequest) request );
		User user = (User) httpRequest.getSession().getAttribute( USER );

		String processedRequestURI = getProcessedRequestURI( httpRequest );
		LOGGER.debug( "user[" + user + "] updated-requestURI[ " + processedRequestURI + " ] original-requestURI["
				+ httpRequest.getRequestURI() + "]" );

		/*
		 * If user is not set, and request does not need authorization, let us allow it
		 * 
		 * Have commented user == null check, because sometime user directly put the home page url or so and in that
		 * case, user is still set in session. Need to find a good solution, like if anyone is accessing home or login
		 * page url, all data should be cleaned as we are doing in logout
		 */
		// if( /* user == null || */processedRequestURI == null )
		// {
		if( !doesRequestNeedAuthroization( httpRequest.getRequestURI() ) )
		{
			LOGGER.info( "Request Authorized: Request does not need authorization" );
			return true;
		}
		else if( processedRequestURI == null )
		{
			LOGGER.info(
					"Request not Authorized: as request is not authroized for system and processRequest URI is also null so we can not check the validity" );
			return false;
		}

		/*
		 * If request is for system call and hence does not need authorization
		 */
		if( isAuthroizedSystemRequest( user, processedRequestURI ) )
		{
			LOGGER.info( "Request Authorized: Authroized as system request" );
			return true;
		}

		/*
		 * Check with security manager, if user has right on the request URL
		 */
		else
		{
			if( user == null )
			{
				LOGGER.debug( "returning false, as not user is logged in" );
				return false;
			}

			ViewMetaData viewMetaData = (ViewMetaData) httpRequest.getSession().getAttribute( VIEW_METADATA );

			if( viewMetaData == null )
			{
				SystemException systemException = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
						"Viewmetadata must not be null here, if user is logged in. It seems to be some development bug" );
				LOGGER.error( systemException );
				throw systemException;
			}

			Menu currentMenu = null;

			/*
			 * Let us try to find a menu for given request
			 * if we find it, good
			 * if we don't find it, check if any menu is already selected.
			 * Then probably this request can be for menu item
			 */
			try
			{
				currentMenu = AppSecurityManager.getSharedInstance().validateRequestForMenu( user,
						processedRequestURI );
				LOGGER.debug( "validated-menu[ " + currentMenu + " ]" );
				viewMetaData.setCurrentMenu( currentMenu );

				LOGGER.info( "Request Authorized: menu found for request URL[" + processedRequestURI + "]" );
				return true;
			}
			catch( AppSecurityException secEx )
			{
				if( viewMetaData.getCurrentMenu() == null )
				{
					LOGGER.info( "Request Not Authorized: No menu found for request URL[" + processedRequestURI + "]" );
					return false;
				}
				currentMenu = viewMetaData.getCurrentMenu();
			}
			LOGGER.debug( "currentMenu[" + currentMenu + "]" );

			/*
			 * Let us try to find a menu item for given request
			 * if it is not found, it means that user is not authorized for this request
			 */
			try
			{
				MenuItem currentMenuItem = AppSecurityManager.getSharedInstance().validateRequestForMenuItem( user,
						currentMenu, processedRequestURI );
				LOGGER.debug( "setting current menu item id [" + currentMenuItem.getId() + "] and url ["
						+ currentMenuItem.getUrl() + "]" );
				viewMetaData.setCurrentMenuItem( currentMenuItem );
			}
			catch( AppSecurityException secEx )
			{
				LOGGER.info( "Request Not Authorized: User and requested Menu Item not found. processedURI["
						+ processedRequestURI + "]" );
				return false;
			}

			LOGGER.info( "Request Authorized: User and requested Menu/Menu Item found" );
			return Boolean.TRUE;
		}
	}

	/**
	 * It checks whether the request is for any system call, which may not need authorization.
	 * 
	 * @param user Logged in User
	 * @param requestURI Request URI
	 * @return true if request is a system call and does not need authorization, false otherwise
	 */
	protected boolean isAuthroizedSystemRequest( User user, String requestURI )
	{
		LOGGER.info( "Request not Authorized: No System request defined with default SecurityServletFilter" );
		return false;
	}

	/**
	 * It checks whether the current request need authorization or not. There may be many url like information, login
	 * etc which may not need authorization.
	 * 
	 * @param requestURI Request URI
	 * @return True if request does not need authorization, false otherwise
	 */
	protected boolean doesRequestNeedAuthroization( String requestURI )
	{
		LOGGER.debug( "Request need authroization. No criteria mentioned with default servlet filter" );
		return true;
	}

	/**
	 * It return the processed request URI, specific to application. Application may use different standards of URI to
	 * check the validations.
	 * 
	 * Security Manager is invoked for getting the application specific format of URI to validate. If anyone wants to
	 * override the default implementation, she can provide the custom implementation of Security Manager.
	 * 
	 * @param httpRequest Request
	 * @return process Request URI
	 */
	protected String getProcessedRequestURI( HttpServletRequest httpRequest )
	{
		return AppSecurityManager.getSharedInstance().getProcessedRequestURI( httpRequest );
	}

	public static Object getObjectGroup()
	{
		return THREADLOCAL.get();
	}

	public static String getObjectGroupoPropertyName()
	{
		return objectGroupPropertyName;
	}

	public static void setObjectGroup( Object objectGroup )
	{
		THREADLOCAL.set( objectGroup );
		LOGGER.debug( "setting the object group in SecurityServletFilter. objectGroup[ " + objectGroup + " ]" );
	}

	public static void setObjectGroupoPropertyName( String objectGroupProperty )
	{
		objectGroupPropertyName = objectGroupProperty;
		LOGGER.debug( "setting object group property name. prpName[ " + objectGroupProperty + " ]" );
	}

	private void printRequestInformation( ServletRequest request )
	{
		if( !LOGGER.isDebugEnabled() )
		{
			return;
		}
		StringBuffer sb = new StringBuffer();
		sb.append( "\n----------------------------------------------------------\n" );
		sb.append( "\nServlet Request Info >>>>> " );
		sb.append( "\nRemoteAddress:	" + request.getRemoteAddr() );
		sb.append( "\nRemote Host:		" + request.getRemoteHost() );
		sb.append( "\nPort:				" + request.getServerPort() );
		sb.append( "\nProtocol:			" + request.getProtocol() );
		sb.append( "\nLocale:			" + request.getLocale() );
		sb.append( "\nContent Type:		" + request.getContentType() );

		if( request instanceof HttpServletRequest )
		{
			sb.append( "\nRequest URI:		" + ( ( (HttpServletRequest) request ).getRequestURI() ) );
			sb.append( "\nRequest URL:		" + ( ( (HttpServletRequest) request ).getRequestURL() ) );
			sb.append( "\nContext Path:		" + ( (HttpServletRequest) request ).getContextPath() );
			sb.append( "\nPath Info:		" + ( (HttpServletRequest) request ).getPathInfo() );
			sb.append( "\nQuery String:		" + ( (HttpServletRequest) request ).getQueryString() );
			sb.append( "\nMethod:			" + ( (HttpServletRequest) request ).getMethod() );
			sb.append( "\nSession Id:		" + ( (HttpServletRequest) request ).getRequestedSessionId() );
			sb.append( "\nContent Type:		" + ( (HttpServletRequest) request ).getContentType() );
			sb.append( "\nstruts-Locale:	"
					+ ( (HttpServletRequest) request ).getSession().getAttribute( LOCALE_STRING ) );

			HttpSession session = ( (HttpServletRequest) request ).getSession();
			sb.append( "\n\nPrinting Session Attributes Information:" );
			sb.append( "\nCreation Time:		" + session.getCreationTime() );
			sb.append( "\nLast Access Time:		" + session.getLastAccessedTime() );
			sb.append( "\nMax Inactive Interval:" + session.getMaxInactiveInterval() );

			sb.append( "\n\nPRINTING SESSION ATTRIBUTES - Check for any item which can be removed:" );
			sb.append( "\nPRINTING SESSION ATTRIBUTES - Check for any item which can be removed:" );
			Enumeration<String> attributeNames = session.getAttributeNames();
			String attributeName;
			Object attributeValue;
			while( attributeNames.hasMoreElements() )
			{
				attributeName = attributeNames.nextElement();
				attributeValue = session.getAttribute( attributeName );

				sb.append( "\n" );
				sb.append( "name[" + attributeName + "] value[" + attributeValue + "] value-type["
						+ ( attributeValue != null ? attributeValue.getClass() : null + "]" ) );
				if( attributeValue != null )
				{
					if( attributeValue instanceof Collection )
					{
						sb.append( " size[" + ( (Collection) attributeValue ).size() + "]" );
					}
					else if( attributeValue.getClass().isArray() )
					{
						sb.append( " size[" + ( (Object[]) attributeValue ).length + "]" );
					}
				}
			}
			sb.append( "\n\n" );
		}
		sb.append( "\n----------------------------------------------------------\n" );

		LOGGER.debug( sb );
	}
}
