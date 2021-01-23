package com.vedantatree.comps.dof.web;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.vedantatree.utils.config.ConfigurationManager;


public class ApplicationInitServlet extends HttpServlet
{

	protected final String	CONFIG_DIRECTORY_PATH	= File.separator + "conf" + File.separator;
	protected final String	COMMA					= ",";

	/**
	 * The base method of Servlet which will be called whenever the Servlet Engine will be initialized.
	 * It will call the methods to load the application standard and application specific properties.
	 * Developers can override the 'loadApplicationSpecificProperties' method to load Application Specific
	 * properties.
	 */
	@Override
	public final void init( ServletConfig servletConfig ) throws ServletException
	{
		System.out.println( "ApplicationInitServlet: initializing Application" );

		super.init( servletConfig );

		System.out.println( "ApplicationInitServlet: Initializing Application Standard Properties" );
		loadStandardAppProperties( servletConfig );
		System.out.println( "ApplicationInitServlet: Application Standard Properties Initialized" );

		System.out.println( "ApplicationInitServlet: Initializing Languages" );
		loadLanguages( servletConfig );
		System.out.println( "ApplicationInitServlet: Languages Initialized" );

		System.out.println( "ApplicationInitServlet: Initializing Application Specific Properties" );
		loadApplicationSpecificProperties( servletConfig );
		System.out.println( "ApplicationInitServlet: Application Specific Properties Initialized" );

		System.out.println( "ApplicationInitServlet: Application Initialized" );
	}

	private void loadStandardAppProperties( ServletConfig servletConfig ) throws ServletException
	{

		StringBuffer configFilesPath = new StringBuffer();

		configFilesPath.append( CONFIG_DIRECTORY_PATH );
		configFilesPath.append( "app.properties" );
		configFilesPath.append( COMMA );

		configFilesPath.append( CONFIG_DIRECTORY_PATH );
		configFilesPath.append( "pagination.properties" );
		configFilesPath.append( COMMA );

		configFilesPath.append( CONFIG_DIRECTORY_PATH );
		configFilesPath.append( "dof.properties" );
		configFilesPath.append( COMMA );

		configFilesPath.append( CONFIG_DIRECTORY_PATH );
		configFilesPath.append( "jndi.properties" );

		String log4jFileName = CONFIG_DIRECTORY_PATH + "log4j.xml";

		ConfigurationManager.initializeWebApplication( servletConfig.getServletContext(), log4jFileName,
				configFilesPath.toString() );
	}

	/**
	 * Applications can override this servlet and this method to load their specific properties. Default properties will
	 * be loaded by default implementation.
	 */
	protected void loadApplicationSpecificProperties( ServletConfig servletConfig ) throws ServletException
	{
	}

	protected void loadLanguages( ServletConfig servletConfig ) throws ServletException
	{
		// Collection languageIds = new ArrayList();
		// Collection languages = new ArrayList();
		// Map languageNames = new HashMap();
		//
		// Language language = new Language();
		// language.setId( "en" );
		// language.setName( "language.english" );
		// language.setImage( "/images/uk_flag.gif" );
		//
		// languages.add( language );
		// languageIds.add( language.getId() );
		// languageNames.put( language.getId(), language.getName() );
		//
		// servletConfig.getServletContext().setAttribute( "languages", languages );
		// servletConfig.getServletContext().setAttribute( "languageNames", languageNames );
		// servletConfig.getServletContext().setAttribute( "languageIds", languageIds );
		//
		// System.out.println( "Languages are loaded[" + languages + "]" );
	}

}
