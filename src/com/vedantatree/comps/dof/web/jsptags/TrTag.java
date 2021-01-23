package com.vedantatree.comps.dof.web.jsptags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;


public class TrTag extends BodyTagSupport
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	String						className;

	public String getClassName()
	{
		return className;
	}

	public void setClassName( String className )
	{
		this.className = className;
	}

	public int doStartTag() throws JspException
	{
		TableTag2 table = (TableTag2) findAncestorWithClass( this, TableTag2.class );
		int row = table.getRowCount();
		for( int k = 0; k < row; k++ )
		{
			if( ( k % 2 ) == 0 )
			{

				setClassName( "table1" );
			}
			else
			{
				setClassName( "table2" );
			}
		}

		try
		{

			pageContext.getOut().print( "<tr class=" + className + ">" );

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
			TableTag2 table = (TableTag2) findAncestorWithClass( this, TableTag2.class );
			int row = table.getRowCount();
			if( ( row % 2 ) == 0 )
			{
				row--;
				table.setRowCount( row );
			}
			else
			{
				row--;
				table.setRowCount( row );
			}
			pageContext.getOut().print( "</tr>" );

		}
		catch( IOException e )
		{
			System.out.print( "Error occur" );
		}
		return EVAL_PAGE;
	}

}
