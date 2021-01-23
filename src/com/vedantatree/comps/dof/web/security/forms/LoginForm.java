package com.vedantatree.comps.dof.web.security.forms;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;


public class LoginForm extends ActionForm
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1442708375071619702L;

	private String				username;

	private String				password;

	private String				userLang;

	private String				confirmPassword;

	private String				currentPassword;

	private String				userEmailId;

	public String getCurrentPassword()
	{
		return currentPassword;
	}

	public void setCurrentPassword( String currentPassword )
	{
		this.currentPassword = currentPassword;
	}

	/**
	 * reset() to reset the values of the fields
	 */
	public void reset( ActionMapping arg0, HttpServletRequest arg1 )
	{
		this.userLang = null;
		this.username = null;
		this.password = null;
		this.confirmPassword = null;

	}

	public String getConfirmPassword()
	{
		return confirmPassword;
	}

	public void setConfirmPassword( String confirmPassword )
	{
		this.confirmPassword = confirmPassword;
	}

	public void setUsername( String username )
	{
		this.username = username;
	}

	public String getUsername()
	{
		return this.username;
	}

	public String getPassword()
	{
		return this.password;
	}

	public void setPassword( String password )
	{
		this.password = password;
	}

	// public ActionErrors validate( ActionMapping mapping, HttpServletRequest
	// request ) {
	// ActionErrors errors = new ActionErrors();
	// if( ( username == null ) || ( username.length() < 1 ) )
	// errors.add( "username", new ActionMessage( "error.username.required" ) );
	// if( ( password == null ) || ( password.length() < 1 ) )
	// errors.add( "password", new ActionMessage( "error.password.required" ) );
	// if( errors.isEmpty() )
	// return null;
	// else
	// return errors;
	// }

	public String getUserLang()
	{
		return userLang;
	}

	public void setUserLang( String userLang )
	{
		this.userLang = userLang;
	}

	public String getUserEmailId()
	{
		return userEmailId;
	}

	public void setUserEmailId( String userEmailId )
	{
		this.userEmailId = userEmailId;
	}

}
