package com.vedantatree.comps.dof.web.help;

import java.io.InputStream;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.exceptions.ApplicationException;
import org.vedantatree.utils.exceptions.ExceptionUtils;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * Documentation
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 * 
 *         Class to Parse the XML file and Generate Parent-Child relationship.
 */

public class HelpXMLParser
{

	private Log					LOGGER		= LogFactory.getLog( HelpXMLParser.class );

	private static final String	PAGEID		= "id";
	private static final String	HELPPAGE	= "helppage";
	private static final String	TITLE		= "title";
	private static final String	URL			= "url";
	private static final String	MAIN_MODULE	= "mainmodule";
	private static final String	MODULE_NAME	= "moduleName";

	/*
	 * TODO: This approach should be changed. We should find some other better way.
	 */
	private int					objectId	= 1;

	/**
	 * It is used to parse the help XML file for creating the help pages.
	 * 
	 * @param xmlFileStream File Stream for XML help file
	 * @return Generated HelpPage object, with child help pages also, if these exist
	 * @throws ApplicationException if there is any error
	 */
	public HelpPage parseHelpXML( InputStream xmlFileStream, ResourceBundle resourceBundle ) throws ApplicationException
	{
		LOGGER.trace( "In parseHelpXML Method" );

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse( xmlFileStream );
			Node rootNode = document.getDocumentElement();
			LOGGER.debug( "rootNode[" + rootNode + "]" );

			HelpPage rootHelpPage = createHelpPage( rootNode, null, true );
			StringBuffer generatedJavaScript = new StringBuffer( 2048 );
			generatedJavaScript.append( ( new StringBuilder() ).append( "var e1=createElement(1,\"" )
					.append( resourceBundle.getString( rootHelpPage.getTitle() ) ).append( "\", \"" )
					.append( resourceBundle.getString( rootHelpPage.getURL() ) ).append( "\",\"" )
					.append( rootHelpPage.getId() ).append( "\");" ).toString() );
			rootHelpPage = parseChildNode( rootHelpPage, rootNode, rootHelpPage.getModuleName(), generatedJavaScript,
					resourceBundle );
			rootHelpPage.setGeneratedJavaScript( generatedJavaScript );
			return rootHelpPage;
		}
		catch( Exception e )
		{

			ExceptionUtils.logException( LOGGER, e.getMessage(), e );

			if( e instanceof ApplicationException )
			{
				throw (ApplicationException) e;
			}
			throw new ApplicationException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"Error while creating help pages by parsing the Help XML file", e );
		}
	}

	/**
	 * This method is used to parse the child nodes recursively and create help pages for these. This method called by
	 * parseXml method for parsing the children nodes of help pages.
	 * 
	 * @param parentHelpPage: Help Page for parent node in XML
	 * @param parentNode Parent node in XML
	 * @param generatedJavaScript StringBuffer for storing the generatedJavaScript
	 */

	private HelpPage parseChildNode( HelpPage parentHelpPage, Node parentNode, String moduleName,
			StringBuffer generatedJavaScript, ResourceBundle resourceBundle ) throws ApplicationException
	{
		LOGGER.trace( "parseChildNode: rootHelpPage[" + parentHelpPage + "] parentNode[" + parentNode + "]" );

		Set childrenPages = parentHelpPage.getChildrenHelpPages();
		int currentObjectId = objectId;
		Node childNode = parentNode.getFirstChild();

		LOGGER.debug( "currentObjectId[" + currentObjectId + "]" );
		LOGGER.debug( "childNode[" + childNode + "]" );

		while( childNode != null )
		{
			String nodeName = childNode.getNodeName();
			LOGGER.debug( "nodeName[" + nodeName + "]" );

			if( nodeName != null && nodeName.equals( HELPPAGE ) )
			{
				objectId++;
				HelpPage childPage = createHelpPage( childNode, moduleName, false );

				generatedJavaScript
						.append( ( new StringBuilder() ).append( "var e" ).append( objectId ).append( "=" )
								.append( "createElement(" ).append( objectId ).append( "," )
								.append( new StringBuilder().append( "\"" )
										.append( resourceBundle.getString( childPage.getTitle() ) ).append( "\"" ) )
								.append( "," )
								.append( new StringBuilder().append( "\"" )
										.append( resourceBundle.getString( childPage.getURL() ) ).append( "\"" ) )
								.append( "," )
								.append( childPage.getId() != null
										? ( new StringBuilder() ).append( "\"" ).append( childPage.getId() )
												.append( "\"" ).toString()
										: "null" )
								.append( ");" ).toString() );

				generatedJavaScript.append( ( new StringBuilder() ).append( "append(e" ).append( currentObjectId )
						.append( "," ).append( "e" ).append( objectId ).append( ");" ).toString() );

				childrenPages.add( childPage );
				if( childNode.hasChildNodes() )
				{
					parseChildNode( childPage, childNode, moduleName, generatedJavaScript, resourceBundle );
				}
			}
			childNode = childNode.getNextSibling();
		}
		return parentHelpPage;
	}

	private HelpPage createHelpPage( Node childNode, String moduleName, boolean root ) throws ApplicationException
	{
		LOGGER.debug( "nodeName[" + childNode.getNodeName() + "]" );

		HelpPage childPage = new HelpPage();

		NamedNodeMap nnm = childNode.getAttributes();

		if( nnm != null )
		{
			if( root )
			{
				String nodeName = childNode.getNodeName();
				if( !MAIN_MODULE.equalsIgnoreCase( nodeName ) )
				{
					ApplicationException ae = new ApplicationException( IErrorCodes.ILLEGAL_STATE_ERROR,
							"Help Page XML file MUST have 'mainmodule' node at root with moduleName attribute. helpPage["
									+ childPage + "]" );
					LOGGER.debug( ae );
					throw ae;
				}
				Node module = nnm.getNamedItem( MODULE_NAME );
				if( module == null )
				{
					ApplicationException ae = new ApplicationException( IErrorCodes.ILLEGAL_STATE_ERROR,
							"Help Page XML file root node MUST have 'moduleName' attribute. helpPage[" + childPage
									+ "]" );
					LOGGER.debug( ae );
					throw ae;
				}
				moduleName = module.getNodeValue();
			}
			childPage.setModuleName( moduleName );

			Node id = nnm.getNamedItem( PAGEID );
			Node title = nnm.getNamedItem( TITLE );
			Node url = nnm.getNamedItem( URL );
			if( id == null || title == null || url == null )
			{
				ApplicationException ae = new ApplicationException( IErrorCodes.ILLEGAL_STATE_ERROR,
						"Any one or more attributes from id, title and url are not found with Help Page XML 'helpPage' node. id["
								+ id + "] title[" + title + "] url[" + url + "]" );
				LOGGER.debug( ae );
				throw ae;
			}

			LOGGER.debug( "node-url[" + url + "]" );
			LOGGER.debug( "node-title[" + title + "]" );
			LOGGER.debug( "node-Id[" + id + "]" );

			childPage.setId( id.getNodeValue() );
			childPage.setTitle( title.getNodeValue() );
			childPage.setURL( url.getNodeValue() );

			List errors = childPage.validate();
			if( errors != null )
			{
				ApplicationException ae = new ApplicationException( IErrorCodes.ILLEGAL_STATE_ERROR,
						"Help Page node properties are not defined properly. helpPage[" + childPage + "] errors["
								+ errors + "]" );
				LOGGER.debug( ae );
				throw ae;
			}
		}
		else
		{
			ApplicationException ae = new ApplicationException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"No attributes are defined with Help Page node. helpPage[" + childPage + "]" );
			LOGGER.debug( ae );
			throw ae;
		}

		return childPage;
	}

}
