package com.vedantatree.comps.dof.web.jsptags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;


public class TableTag2 extends BodyTagSupport
{

	/**
	 * 
	 */
	private int rowCount;

	public int getRowCount()
	{
		return rowCount;
	}

	public void setRowCount( int rowCount )
	{
		this.rowCount = rowCount;
	}

	private static final long serialVersionUID = 1L;

	public int doStartTag() throws JspException
	{

		try
		{
			pageContext.getOut().print( "<table>" );
		}
		catch( IOException e )
		{
			throw new JspException( "problem occur" + e.getMessage() );
		}
		return ( EVAL_BODY_INCLUDE );
	}

	public int doAfterBody() throws JspTagException
	{
		try
		{
			pageContext.getOut().print( "</table>" );
		}
		catch( IOException e )
		{
			System.out.print( "Error occur" );
		}
		return EVAL_PAGE;
	}

}
