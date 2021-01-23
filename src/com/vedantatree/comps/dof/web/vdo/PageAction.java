package com.vedantatree.comps.dof.web.vdo;

import java.io.Serializable;

import org.vedantatree.utils.StringUtils;

import com.vedantatree.comps.dof.common.bdo.DisplayData;


/**
 * Object of this type represent the metadata of action which can be shown on a page.
 * 
 * <p>
 * These are parsed from the "Dynamic Object form metadata configuration" and set with DOMetadata. Later DOMetadata is
 * used with ViewMetadata for enabling rendering operation on UI.
 * 
 * <pre>
 * <actions>
 * 	<!-- Form actions that will be displayed on list page -->
 * 	<form-actions>
 * 			<!--
 * 				Form Action information to create a form action on list page
 * 				popup - To ensure that form-action will be opened in poup or on same page
 * 				selection - In case of popup listing what will be selection type
 * 				class-name - class name for current action object	
 * 				url - url for current action
 * 				roles - valid roles for current action << No longer in use, as we are picking these from Security 
 * 						Services now
 * 				display-name - Display name for current action
 * 			-->
 * 			<form-action popup="true" selection="single|multiple"
 * 				class-name="alpha">
 * 				<url>ManageTender.do?actiion=edit</url>
 * 				<roles>user,admin</roles>
 * 				<display-name>common.edit</display-name>
 * 			</form-action>
 *   </form-actions>
 * </actions>
 * </pre>
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class PageAction implements Serializable, Cloneable
{

	private static final long	serialVersionUID	= 89381505473364727L;

	/**
	 * It indicates whether the requested page should be opened in same frame or in a popup.
	 * 
	 * <p>
	 * TODO - it can be boolean
	 */
	private String				popup;

	/**
	 * It indicates that if we are using the list page for selecting the records, then section type is single section or
	 * multiple selection.
	 */
	private String				selection;

	/**
	 * Class name for the object which is under operation. For example, if we are showing a list of employees and want
	 * to delete any employee; then we shall pass employee class name here with the id of employee. This may be used by
	 * server implementation to identify that which object class I should operate on for this action and for given id.
	 * 
	 * It should not be mandatory, and might be used only with metadata driven automatic generated pages, like in case
	 * of master data pages.
	 */
	private String				className;

	/**
	 * URL of the server component where the request should be forward
	 */
	private String				url;

	/**
	 * A data structure having the data for display purpose of this action, like display name, image name etc
	 */
	private DisplayData			displayData;

	/**
	 * It indicates whether this action is currently enabled for action or not. Generally this property is set based on
	 * the user implementation of IsValidActionImpl method in View Helper, which is called by Default implementation of
	 * ViewHelper to check whether the current action should be shown to user or not. Developer can return true or
	 * false, based on the applicable business logic. If we get false, we shall mark the action as disabled or otherwise
	 * it will be enabled.
	 * 
	 * <p>
	 * This property is being introduced to keep the alignment of actions on UI. Earlier, actions which were not
	 * required as per business logic, used to be removed from UI. However that spoil the alignment of the actions. To
	 * avoid that, now we shall just disable the actions based on this property. However keep in mind, that actions, for
	 * which user is not authorized will still be invisible to user.
	 * 
	 * Default value is set to true
	 */
	private boolean				enabled				= true;

	public String getClassName()
	{
		return className;
	}

	public DisplayData getDisplayData()
	{
		return displayData;
	}

	public String getPopup()
	{
		return popup;
	}

	public String getSelection()
	{
		return selection;
	}

	public String getUrl()
	{
		return url;
	}

	public void setClassName( String className )
	{
		this.className = className;
	}

	public void setDisplayData( DisplayData displayData )
	{
		this.displayData = displayData;
	}

	public void setPopup( String popup )
	{
		this.popup = popup;
	}

	public void setSelection( String selection )
	{
		this.selection = selection;
	}

	public void setUrl( String url )
	{
		this.url = url;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled( boolean enabled )
	{
		this.enabled = enabled;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{

		PageAction pageAction = (PageAction) super.clone();
		pageAction.setDisplayData( (DisplayData) getDisplayData().clone() );
		return pageAction;
	}

	@Override
	public String toString()
	{
		return StringUtils.toGenericString( this );

	}
}
