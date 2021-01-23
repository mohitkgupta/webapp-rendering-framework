package com.vedantatree.comps.dof.web.jsptags;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;


public class ExpandTag extends BodyTagSupport
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	private String				lable				= null;

	private String				id					= null;

	private ResourceBundle		bundle;

	public String getLable()
	{
		return lable;
	}

	public void setLable( String lable )
	{
		this.lable = lable;
	}

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public ResourceBundle getBundle()
	{
		return bundle;
	}

	public void setBundle( ResourceBundle bundle )
	{
		this.bundle = bundle;
	}

	public int doAfterBody() throws JspException
	{
		try
		{
			BodyContent bc = getBodyContent();
			String body = bc.getString();
			JspWriter out = bc.getEnclosingWriter();
			out.print( "<table width='100%' cellspacing='0' cellpadding='0' border='0'>"
					+ "<tbody><tr><td align='center' class='plus'><a href='javascript:void(0);' onclick='javascript:changeImage("
					+ '"' + getId() + '"' + "); hideLayer(" + '"' + getLable() + '"' + ");'>" + "<img id='" + getId()
					+ "'src='style/images/plus.png' border='0' class='plus_img_button' /></a></td>"
					+ "<td class='main_supplierlinks'>" + bundle.getString( getLable() )
					+ "</td></tr></tbody></table>" );
			out.print( "<div id='" + getLable() + "'class='inner_div' style='display:none'>" );
			out.print( body );
			out.print( "</div>" );

		}
		catch( IOException e )
		{
			throw new JspException( "problem occur" + e.getMessage() );
		}
		return EVAL_PAGE;
	}

}