package com.vedantatree.comps.dof.web;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.util.MessageResources;
import org.apache.struts.validator.DynaValidatorForm;
import org.vedantatree.utils.BeanUtils;
import org.vedantatree.utils.StringUtils;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;

import com.vedantatree.comps.dof.DOFUtils;


/**
 * UIUtils class is for providing some functionality with specified methods those methods are used to give the
 * functionality to other classes which helps in rendering the UI logics.
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class WebUIUtils implements WebAppConstants
{

	private static Log						LOGGER		= LogFactory.getLog( WebUIUtils.class );
	private static final MessageResources	messages	= MessageResources
			.getMessageResources( "ApplicationResources" );

	public static String getLocaleMessage( String messageKey )
	{
		StringUtils.assertQualifiedArgument( messageKey );
		return messages.getMessage( messageKey );
	}

	/**
	 * this method will be used by the action class to set the request attreibute for the Error page when any exception
	 * occured in the action classes and these ttribute will be fetched on error page and will display the message
	 * accordingly and will go back after hiting the back button
	 * 
	 * @param request request Object used in action class
	 * @param exceptionObject object of exception occured
	 * @param previousPageUrl url of page at which we will go after hitting OK button
	 * @param tilesDefinition of the action
	 */
	public static void setExceptionPageParameters( HttpServletRequest request, Object exceptionObject,
			String previousPageUrl, String tilesDefinition )
	{
		setExceptionPageParameters( request, exceptionObject, previousPageUrl, tilesDefinition, null );
	}

	/**
	 * this method will be used by the action class to set the request attreibute for the Error page when any exception
	 * occured in the action classes and these ttribute will be fetched on error page and will display the message
	 * accordingly and will go back after hiting the back button
	 * 
	 * @param request request Object used in action class
	 * @param exceptionObject object of exception occured
	 * @param previousPageUrl url of page at which we will go after hitting OK button
	 * @param tilesDefinition of the action
	 * @param message for servlets
	 */
	public static void setExceptionPageParameters( HttpServletRequest request, Object exceptionObject,
			String previousPageUrl, String tilesDefinition, String errorMessage )
	{
		LOGGER.trace( "setExceptionPageParameters: exceptionObject[ " + exceptionObject + " ]previousPageUrl[ "
				+ previousPageUrl + " ]tilesDefinition[ " + tilesDefinition + " ]request[ " + request
				+ " ]errorMessage[ " + errorMessage + " ]" );
		DOFUtils.setExceptionPageParameters( request, exceptionObject, previousPageUrl, tilesDefinition, errorMessage );
	}

	public static String getSearchPath( String searchColumn, String searchValue, String sortString, String isDescending,
			String searchAct )
	{
		String searchPath = "";

		if( searchColumn != null && !searchColumn.equalsIgnoreCase( "default" ) && searchValue != null )
		{
			searchPath = "&searchColumn=" + searchColumn + "&searchValue=" + searchValue;

		}
		else if( searchColumn == null && searchValue != null )
		{
			searchPath = "&searchColumn=" + searchColumn + "&searchValue=" + searchValue;

		}
		else if( searchColumn != null && !searchColumn.equalsIgnoreCase( "default" ) && searchValue == null )
		{
			searchPath = "&searchColumn=" + searchColumn;

		}
		if( sortString != null && !sortString.equalsIgnoreCase( "" ) )
		{
			searchPath += "&sortColumn=" + sortString + "&isDescending=" + isDescending;
		}
		if( searchAct != null && !searchAct.equalsIgnoreCase( "" ) )
		{
			searchPath += "&searchAct=" + searchAct;
		}
		LOGGER.debug( "Value of searchPath[ " + searchPath + " ]" );
		return searchPath;
	}

	public static String getInternationalisedValue( Locale locale, String key )
	{
		LOGGER.trace( "entering : getInternationalisedValue . locale[" + locale + "] key[" + key + "]" );

		ResourceBundle bundle = ResourceBundle.getBundle( "ApplicationResources", locale );
		String value = bundle.getString( key );

		LOGGER.trace( "exiting : getInternationalisedValue." );
		return value;
	}

	/*
	 * set in to the Map taken from Form bean
	 */

	public static void assemble( ActionForm form, String property, Map map, HttpServletRequest req )
	{
		ServletContext ctx = req.getSession().getServletContext();
		String[] arr = null;
		if( form instanceof DynaValidatorForm )
		{
			arr = (String[]) ( (DynaValidatorForm) form ).get( property );
		}
		else
		{
			try
			{
				arr = (String[]) BeanUtils.getPropertyValue( form, property );
			}
			catch( Exception e )
			{
				SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR, e.getMessage(), e );
				LOGGER.error( se );
				throw se;
			}
		}
		// Iterator it = ApplicationConfig.getConfig().getLanguages().getLanguageAsReference().iterator();
		Iterator it = ( (Collection) req.getSession().getServletContext().getAttribute( "languageIds" ) ).iterator();
		int i = 0;
		while( it.hasNext() )
		{
			// Language lang = (Language)it.next();
			// String langId = lang.getId();
			String langId = (String) it.next();
			if( arr[i] != null && arr[i].toString().length() > 0 )
			{
				map.put( langId, arr[i] );
			}
			i++;
		}
	}

	/*
	 * set in to the form Bean taken from Map
	 */

	public static void assemble( Map map, ActionForm form, String arrField, HttpServletRequest req )
	{
		ServletContext ctx = req.getSession().getServletContext();
		// Collection languages = (Collection)ctx.getAttribute("languages");
		Collection languages = (Collection) ctx.getAttribute( "languageIds" );
		String[] arr = new String[languages.size()];

		Iterator it = languages.iterator();
		int i = 0;
		while( it.hasNext() )
		{
			// String langId = (String)PropertyUtils.getProperty(it.next(), "id");

			String langId = (String) it.next();

			arr[i++] = (String) map.get( langId );

		}

		if( form instanceof DynaValidatorForm )

		{
			( (DynaValidatorForm) form ).set( arrField, arr );
		}
		else
		{
			try
			{
				BeanUtils.invokeMethod( form, "setNames", new Object[]
				{ arr } );
			}
			catch( Exception e )
			{
				SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR, e.getMessage(), e );
				LOGGER.error( se );
				throw se;
			}
		}

	}

}
