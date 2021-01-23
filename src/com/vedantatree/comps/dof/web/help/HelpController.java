package com.vedantatree.comps.dof.web.help;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.config.ConfigurationManager;
import org.vedantatree.utils.exceptions.ApplicationException;
import org.vedantatree.utils.exceptions.IErrorCodes;


/**
 * This controller is used to generate the help pages for application modules.
 * 
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class HelpController extends HttpServlet
{

	private Log							LOGGER					= LogFactory.getLog( HelpController.class );

	private static final String			MODULE_NAME				= "module";
	private static final String			HELP_PAGE_ID			= "id";
	private static final String			HELP_RENDERING_PAGE		= "help.jsp";
	private static final String			ATTRIBUTE_HELP_OUTPUT	= "helpPageOutput";
	private static final String			ATTRIBUTE_HELP_PAGE_ID	= "helpPageId";

	/**
	 * Map for holding the parsed help pages
	 */
	private HashMap<String, HelpPage>	moduleRootHelpPages		= new HashMap<>();

	/**
	 * Help Configuration XML Parser
	 */
	private HelpXMLParser				helpXMLParser			= new HelpXMLParser();

	/**
	 * Servlet method to handle the request for help page
	 * 
	 * It get the help page id and module name from request. Then it looks for the corresponding module help
	 * configuration file from class path and load it as help page. This help page provides us the html/js for web page.
	 * The output is set to request and request is forwarded to help.jsp page to render it.
	 */
	@Override
	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException
	{
		java.util.Locale locale = request.getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle( "ApplicationResources", locale );

		String moduleName = request.getParameter( MODULE_NAME );
		String id = request.getParameter( HELP_PAGE_ID );

		LOGGER.debug( "id[ " + id + " ] module[" + moduleName + "]" );

		HelpPage helpPage;
		try
		{
			helpPage = getModuleRootHelpPage( moduleName, bundle );
		}
		catch( ApplicationException e )
		{
			LOGGER.error( e );
			throw new ServletException( e );
		}

		request.setAttribute( ATTRIBUTE_HELP_OUTPUT, helpPage.getGeneratedJavaScript() );
		request.setAttribute( ATTRIBUTE_HELP_PAGE_ID, id );

		RequestDispatcher view = request.getRequestDispatcher( HELP_RENDERING_PAGE );
		view.forward( request, response );
	}

	public HelpPage getModuleRootHelpPage( String moduleName, ResourceBundle bundle ) throws ApplicationException
	{
		HelpPage helpPage = moduleRootHelpPages.get( moduleName );
		if( helpPage == null )
		{
			InputStream helpXMLInputStream = ConfigurationManager.loadConfigurationFile( moduleName + "_help.xml" );
			if( helpXMLInputStream == null )
			{
				ApplicationException ae = new ApplicationException( IErrorCodes.RESOURCE_NOT_FOUND,
						"Help Configuration file not found for module[" + moduleName + "]" );
				LOGGER.error( ae );
				throw ae;
			}
			helpPage = helpXMLParser.parseHelpXML( helpXMLInputStream, bundle );
			moduleRootHelpPages.put( moduleName, helpPage );
		}
		return helpPage;
	}

}
