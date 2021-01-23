package com.vedantatree.comps.dof.web.help;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vedantatree.utils.StringUtils;


/**
 * This is the data structure to contain the data for a Help Page
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public class HelpPage
{

	/**
	 * Id of the help page.
	 * 
	 * <p>
	 * This should be unique for help pages. Help Framework search for the help metadata node in configuration based on
	 * this id.
	 */
	private String			id;

	/**
	 * Title of help page which will be shown in web page
	 */
	private String			title;

	/**
	 * URL of the application web page for which this help page will work
	 */
	private String			url;

	/**
	 * Name of the application module with which this help page relates to
	 */
	private String			moduleName;

	/**
	 * Set of children help pages for current help page.
	 * 
	 * <p>
	 * Help pages structure is hierarchical, as one page can have many nested operation sub-pages. So this structure
	 * helps us to show the nested tree form of help navigation links.
	 */
	private Set				childrenHelpPages;

	/**
	 * Java script generated by Help XML parser while creating the help page. Only root node of Help will be having this
	 * java script. This java script will be used in web pages.
	 */
	private StringBuffer	generatedJavaScript;

	public HelpPage()
	{
		childrenHelpPages = new HashSet();
	}

	public String getId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}

	public String getURL()
	{
		return url;
	}

	public Set getChildrenHelpPages()
	{
		return childrenHelpPages;
	}

	public String getModuleName()
	{
		return moduleName;
	}

	public void setId( String helpPageId )
	{
		this.id = helpPageId;
	}

	public void setTitle( String helpPageTitle )
	{
		this.title = helpPageTitle;
	}

	public void setURL( String helpPageURL )
	{
		this.url = helpPageURL;
	}

	public void setChildrenHelpPages( Set childrenHelpPages )
	{
		this.childrenHelpPages = childrenHelpPages;
	}

	public void setModuleName( String moduleName )
	{
		this.moduleName = moduleName;
	}

	public StringBuffer getGeneratedJavaScript()
	{
		return generatedJavaScript;
	}

	public void setGeneratedJavaScript( StringBuffer generatedJavaScript )
	{
		this.generatedJavaScript = generatedJavaScript;
	}

	@Override
	public String toString()
	{
		return "HelpPage@" + hashCode() + ": id[" + id + "] moduleName[" + moduleName + "] title[" + title + "]";
	}

	public List<String> validate()
	{
		List<String> errors = new ArrayList<>();
		if( !StringUtils.isQualifiedString( id ) )
		{
			errors.add( "Id of help page is not set." );
		}
		if( !StringUtils.isQualifiedString( title ) )
		{
			errors.add( "Title of help page is not set for help page." );
		}
		if( !StringUtils.isQualifiedString( moduleName ) )
		{
			errors.add( "Module name is not set for help page." );
		}
		return errors.size() == 0 ? null : errors;
	}

}