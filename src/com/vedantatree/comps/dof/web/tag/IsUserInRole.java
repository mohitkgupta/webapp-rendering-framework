package com.vedantatree.comps.dof.web.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.vedantatree.comps.securitymanager.model.User;
import org.vedantatree.comps.securitymanager.model.UserRole;


/**
 * Tag to check if current user is in specified role or not
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 *
 */

public class IsUserInRole extends BodyTagSupport
{

	private static final long	serialVersionUID	= 1L;

	private String				sessionRole;

	private User				user;

	private int					flag				= 0;

	public String getsessionRole()
	{
		return sessionRole;
	}

	public void setsessionRole( String lable )
	{
		this.sessionRole = lable;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser( User users )
	{
		this.user = users;
	}

	@Override
	public int doAfterBody() throws JspException
	{
		try
		{
			BodyContent bc = getBodyContent();
			String body = bc.getString();
			JspWriter out = bc.getEnclosingWriter();

			UserRole[] role = (UserRole[]) user.getRolesForCurrentDomain().toArray();

			/*
			 * @TODO user.getRole will return a list. traverse the list and get the role matching the role defined.
			 */
			for( UserRole element : role )
			{

				if( element.getName().equals( sessionRole ) )
				{
					return EVAL_BODY_INCLUDE;
				}
				else
				{
					return SKIP_BODY;
				}
			}
		}
		catch( Exception e )
		{
			throw new JspException( "problem occur" + e.getMessage() );
		}
		return EVAL_PAGE;
	}

}
