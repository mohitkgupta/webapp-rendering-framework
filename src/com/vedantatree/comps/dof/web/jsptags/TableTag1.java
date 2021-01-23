package com.vedantatree.comps.dof.web.jsptags;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;


public class TableTag1 extends BodyTagSupport
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private String				lable;
	private List				listOfObject;
	private String[]			headerList;
	String						className;

	public String getClassName()
	{
		return className;
	}

	public void setClassName( String className )
	{
		this.className = className;
	}

	public String getLable()
	{
		return lable;
	}

	public void setLable( String lable )
	{
		this.lable = lable;
	}

	public List getListOfObject()
	{
		return listOfObject;
	}

	public void setListOfObject( List listOfObject )
	{
		this.listOfObject = listOfObject;
	}

	public String[] getHeaderList()
	{
		return headerList;
	}

	public void setHeaderList( String[] headerList )
	{
		this.headerList = headerList;
	}

	public int doStartTag() throws JspException
	{
		String name = getLable();
		try
		{
			pageContext.getOut().print( "<b><h1>" + name + "</h1></b></br>" );
			pageContext.getOut().print(
					"<table border='2' col='" + ( headerList.length ) + "' row='" + ( listOfObject.size() ) + "'>" );
			setClassName( "table2" );
			pageContext.getOut().print( "<tr>" );
			for( int i = 0; i < headerList.length; i++ )
			{

				pageContext.getOut().print( "<th>" + headerList[i] + "</th>" );

			}
			pageContext.getOut().print( "<tr>" );

			for( int j = 0; j < listOfObject.size(); j++ )
			{
				String[] colValue = (String[]) listOfObject.get( j );
				pageContext.getOut().print( "<tr class=" + className + ">" );
				for( int k = 0; k < headerList.length; k++ )
				{

					if( ( j % 2 ) == 0 )
					{
						setClassName( "table1" );
					}
					else
					{
						setClassName( "table2" );
					}

					pageContext.getOut().print( "<td>" );
					pageContext.getOut().print( colValue[k].toString() );
					pageContext.getOut().print( "</td>" );

				}
				pageContext.getOut().print( "</tr>" );

			}
			pageContext.getOut().print( "</table>" );
		}
		catch( IOException e )
		{
			throw new JspException( "problem occur" + e.getMessage() );
		}
		return SKIP_BODY;
	}

	public int doEndTag() throws JspException
	{
		return EVAL_PAGE;
	}

}
