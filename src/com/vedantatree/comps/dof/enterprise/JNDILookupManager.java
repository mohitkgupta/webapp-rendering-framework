package com.vedantatree.comps.dof.enterprise;

import java.util.Arrays;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.BeanUtils;
import org.vedantatree.utils.config.ConfigurationManager;
import org.vedantatree.utils.exceptions.ApplicationException;
import org.vedantatree.utils.exceptions.ComponentException;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;


/**
 * This is a JNDI based lookup manager, which search for the remote and local EJB components using JNDI based on given
 * information.
 * 
 * <p>
 * This is designed to support searching of both remote and local EJB components. Separate methods have been provided
 * for these.
 * 
 * <p>
 * It picks the properties for lookup from 'jndi.properties' file. It utilize configuration manager to lookup the
 * property file. In this property file, one JNDI configuration can be given as default information for current
 * application. However, if user needs to specify the lookup information for other applications also, then information
 * property names can be prefixed with app name identifier like <app name prefix>.<property name>. At runtime, user can
 * specify the app name identifier while asking for any remote or local reference. This component will automatically
 * picks the right configuration for this application and will find the reference.
 * 
 * <p>
 * TODO:- modify it to work as service locator with cache for ejb homes and context. It will help in performance.
 * Context should be cached for applications.
 * 
 * @author
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */

public class JNDILookupManager
{

	/**
	 * declred for taking the log prints.
	 */
	private static Log					LOGGER					= LogFactory.getLog( JNDILookupManager.class );

	/**
	 * name of the method in the home interface to called through reflection.
	 */
	private final static String			METHODNAME				= "create";

	/**
	 * Currently we are putting the checks for default application using null, it should be replaced by this default
	 * string
	 */
	// private final static String DEFAULT_APPLICATION = "default";

	/**
	 * keys name defined in jndi.properties file to load the values from the file.
	 */
	private final static String			INITIAL_CONTEXT_FACTORY	= "initial.factory";
	private final static String			PROVIDER_URL			= "ormi.url";
	private final static String			SECURITY_PRINCIPAL		= "security.principal";
	private final static String			SECURITY_CREDENTIAL		= "security.credentials";

	/**
	 * Initial Context for default application
	 */
	private final static InitialContext	INITIAL_CONTEXT;

	// private final HashMap<String, InitialContext> initialContextCache;
	// private final HashMap<String, EJBHome> ejbHomeCache;

	static
	{
		if( !ConfigurationManager.getSharedInstance().containsProperty( INITIAL_CONTEXT_FACTORY ) )
		{
			try
			{
				ConfigurationManager.ensurePropertiesLoaded( "jndi.properties" );
			}
			catch( ApplicationException e )
			{
				LOGGER.fatal( "Problem while initializing JNDILookupManager", e );
				throw new SystemException( IErrorCodes.COMPONENT_INITIALIZATION_ERROR,
						"Problem while initializing JNDILookupManager", e );
			}
		}
		// building initial context for default application
		INITIAL_CONTEXT = buildInitialContext( null );
	}

	/**
	 * This method lookup any type of resource listed with JNDI for given name. It may be other than EJB Home or EJB
	 * object.
	 * 
	 * @param objectJNDIName Name of the object listed in JNDI directory
	 * @param applicationName Name of application, which is looking for this object. Component will load the JNDI server
	 *        information from properties based on specified application name
	 * @return Object for given name from JNDI directory, null otherwise
	 * @throws ComponentException If there is any problem
	 */
	public static Object lookup( String objectJNDIName, String applicationName ) throws ComponentException
	{
		LOGGER.debug(
				"entered::lookup. objectJNDIName[ " + objectJNDIName + "] applicationName[" + applicationName + "]" );
		try
		{
			InitialContext initialContext = applicationName == null ? INITIAL_CONTEXT
					: buildInitialContext( applicationName );
			LOGGER.debug( "jndi-context-env[" + initialContext.getEnvironment() + "]" );

			Object jndiObject = initialContext.lookup( objectJNDIName );

			LOGGER.debug( "jndiObject[" + jndiObject + "]" );
			if( LOGGER.isDebugEnabled() && jndiObject != null )
			{
				LOGGER.debug( "jndiObject-Class[" + jndiObject.getClass().getName() + "]" );
				LOGGER.debug(
						"jndiObject-Interfaces[" + Arrays.toString( jndiObject.getClass().getInterfaces() ) + "]" );
			}

			return jndiObject;
		}
		catch( NamingException e )
		{
			ComponentException ce = new ComponentException( IErrorCodes.JAVA_NAMING_ERROR,
					"Naming Exception Occured while getting remote JNDI object. JNDIName[" + objectJNDIName + "]", e );
			LOGGER.error( ce );
			throw ce;
		}
	}

	/**
	 * lookupLocalEJB will load the session bean from the realam on the server and will return the object of the class
	 * whose home interface is passed to the method.
	 * 
	 * @param objectJNDIName name of the JNDI which we want to lookup.
	 * @param homeClassName name of the local home class whose session bean we want to load
	 * 
	 * @return object of the home class defined in the argument using reflection by calling the create method in that
	 *         interface.
	 * 
	 * @throws ComponentException if any things goes wrong then Component Exception will be thrown by the method.
	 */
	public static Object lookupLocalEJB( String objectJNDIName, Class homeClassName ) throws ComponentException
	{
		return lookupLocalEJB( objectJNDIName, homeClassName, null );
	}

	public static Object lookupLocalEJB( String objectJNDIName, Class homeClassName, String applicationName )
			throws ComponentException
	{
		LOGGER.trace( "Entered in lookupLocal() objectJNDIName[ " + objectJNDIName + " ] homeClassName[ "
				+ homeClassName + " ]" );
		try
		{

			LOGGER.debug( "Looking for the Bean objectJNDIName[ " + objectJNDIName + " ]" );

			/**
			 * will lookup the Session bean using the jndiname passed to the lookup method.and will return the object
			 */
			Object jndiObject = lookup( objectJNDIName, applicationName );

			LOGGER.debug( "homeClassName[ " + homeClassName + " ]" );

			/*
			 * now invokeMethod() defined in BeanUtils class will be called with home class reference and the
			 * methodname.that will return the reference to the bean object
			 */
			Object beanObject = BeanUtils.invokeMethod( jndiObject, METHODNAME, null );

			LOGGER.debug( "returning the beanObject[ " + beanObject + " ]" );

			/**
			 * that bean object will be returned to the caller of the method
			 */
			return beanObject;
		}
		catch( ApplicationException e )
		{
			LOGGER.error( "ApplicationException Occured while getting remote JNDI object.", e );

			/**
			 * will throw the ComponentException if any error occurs in calling the beans on the server.
			 */
			throw new ComponentException( IErrorCodes.JAVA_NAMING_ERROR,
					"Naming Exception Occured while getting local JNDI EJB object.", e );
		}
	}

	/**
	 * lookupRemoteEJB will load the session bean by narrowing the PortableRemoteObject and will return the object of
	 * the class whose home interface is passed to the method.
	 * 
	 * @param objectJNDIName name of the JNDI which we want to lookup.
	 * @param homeClassName name of the class whose session bean we want to load
	 * 
	 * @return object of the home class defined in the argument using reflection by calling the create method in that
	 *         interface.
	 * 
	 * @throws ComponentException if any things goes wrong then Component Exception will be thrown by the method.
	 */
	public static Object lookupRemoteEJB( String objectJNDIName, Class homeClassName ) throws ComponentException
	{
		return lookupRemoteEJB( objectJNDIName, homeClassName, null );
	}

	public static Object lookupRemoteEJB( String objectJNDIName, Class homeClassName, String applicationName )
			throws ComponentException
	{
		LOGGER.debug( "Entered in lookupRemote. objectJNDIName[ " + objectJNDIName + " ] homeClassName[ "
				+ homeClassName + " ]" );

		try
		{
			/*
			 * will lookup the Session bean using the jndi Name passed to the lookup method.and will return the object
			 */
			Object jndiObject = lookup( objectJNDIName, applicationName );

			/*
			 * now with the object recently fetched and the home classname we will call the narrow() of
			 * PortableRemoteObject class. which will give the reference to the home.
			 */
			Object home = null; // PortableRemoteObject.narrow( jndiObject, homeClassName );
			if( true )
			{
				throw new UnsupportedOperationException(
						"Need to refactor whole code for EJB lookup. PortableRemoteObject lib is required here for now." );
			}

			/*
			 * now invokeMethod() defined in BeanUtils class will be called with home class reference and the
			 * methodname.that will return the reference to the bean object
			 */
			Object beanObject = BeanUtils.invokeMethod( home, METHODNAME, null );

			/**
			 * that bean object will be returned to the caller of the method
			 */
			return beanObject;
		}
		catch( Exception e )
		{
			LOGGER.error( "Exception Occured while getting remote JNDI object.", e );

			/**
			 * will throw the ComponentException if any error occurs in calling the beans on the server.
			 */
			throw new ComponentException( IErrorCodes.JAVA_NAMING_ERROR,
					"Naming Exception Occured while getting remote JNDI object.", e );
		}
	}

	public static Object lookupTest( String objectJNDIName ) throws ComponentException
	{
		LOGGER.debug( "Entered in lookup. objectJNDIName[ " + objectJNDIName + "]" );

		try
		{

			/*
			 * will lookup the Session bean using the jndiname passed to the lookup method.and will return the object
			 */

			InitialContext in = new InitialContext();
			Object jndiObject = in.lookup( objectJNDIName );

			LOGGER.debug( "jndiObject[" + jndiObject + "]" );

			return jndiObject;
		}
		catch( NamingException e )
		{
			LOGGER.error( "Naming Exception Occured while getting remote JNDI object.", e );

			/**
			 * will throw the ComponentException if any error occurs in calling the beans on the server.
			 */
			throw new ComponentException( IErrorCodes.JAVA_NAMING_ERROR,
					"Naming Exception Occured while getting remote JNDI object.", e );
		}
	}

	private static InitialContext buildInitialContext( String applicationName )
	{
		/*
		 * instance of the Hashtable will be created to set the enviornment variable for invoking the beans in the
		 * container
		 */
		Hashtable<String, String> env = new Hashtable<>();

		/*
		 * will fetch the INITIAL_CONTEXT_FACTORY name from public-books.properties file and will load using
		 * ConfigurationManager class instance.and will put in hastable.
		 */
		env.put( Context.INITIAL_CONTEXT_FACTORY,
				ConfigurationManager.getSharedInstance().getPropertyValue( INITIAL_CONTEXT_FACTORY ) );

		/*
		 * will fetch the PROVIDER_URL(at which server the beans are deployed) name from public-books.properties
		 * file and will load using ConfigurationManager class instance.and will put in hastable.
		 */
		env.put( Context.PROVIDER_URL, ConfigurationManager.getSharedInstance()
				.getPropertyValue( applicationName != null ? applicationName + "." + PROVIDER_URL : PROVIDER_URL ) );

		/*
		 * will fetch the SECURITY_PRINCIPAL(for the server) from public-books.properties file and will load using
		 * ConfigurationManager class instance.and will put in hastable.
		 */
		env.put( Context.SECURITY_PRINCIPAL, ConfigurationManager.getSharedInstance().getPropertyValue(
				applicationName != null ? applicationName + "." + SECURITY_PRINCIPAL : SECURITY_PRINCIPAL ) );

		/*
		 * will fetch the SECURITY_CREDENTIAL(for the server) from public-books.properties file and will load using
		 * ConfigurationManager class instance.and will put in hastable.
		 */
		env.put( Context.SECURITY_CREDENTIALS, ConfigurationManager.getSharedInstance().getPropertyValue(
				applicationName != null ? applicationName + "." + SECURITY_CREDENTIAL : SECURITY_CREDENTIAL ) );

		/*
		 * create the instance of Initial Context by passing object of the hashtable created earlier with values set
		 * in.
		 */
		try
		{

			return new InitialContext( env );
		}
		catch( NamingException e )
		{
			LOGGER.fatal( "Error while initializing the JNDILookup Manager", e );
			throw new SystemException( IErrorCodes.COMPONENT_INITIALIZATION_ERROR,
					"Error while initializing InitialContext in JNDILookup Manager", e );
		}
	}

}
