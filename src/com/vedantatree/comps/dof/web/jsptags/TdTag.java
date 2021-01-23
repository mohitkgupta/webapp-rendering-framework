package com.vedantatree.comps.dof.web.jsptags;

import java.io.IOException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;


public class TdTag extends BodyTagSupport
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int doAfterBody() throws JspTagException
	{
		TableTag2 table = (TableTag2) findAncestorWithClass( this, TableTag2.class );
		TrTag tr = (TrTag) findAncestorWithClass( this, TrTag.class );
		if( tr == null )
		{
			throw new JspTagException( "td not inside tr" );
		}
		if( table == null )
		{
			throw new JspTagException( "td not inside Tabletag2" );
		}
		try
		{

			BodyContent bc = getBodyContent();
			String body = bc.getString();
			JspWriter out = bc.getEnclosingWriter();
			out.print( "<td>" );
			out.print( body );
			out.print( "</td>" );

		}
		catch( IOException e )
		{
			System.out.print( "Error occur" );
		}
		return EVAL_PAGE;
	}

}
