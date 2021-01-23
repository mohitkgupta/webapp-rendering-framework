package com.vedantatree.comps.dof.web.vdo;

import org.vedantatree.utils.StringUtils;


/**
 * Object of this type represent the metadata of action which should be shown on an edit page mostly, like cancel/save
 * action on top of edit page.
 * 
 * <p>
 * These are parsed from the Dynamic Object form metadata configuration to set with DOMetadata. Later DOMetadata is used
 * with ViewMetadata for enabling rendering operation on UI.
 * 
 * <pre>
 * <actions>
 * 		<!-- Edit Actions that will be displayed on add-edit page -->
 * 		<edit-actions>
 * 			<!--
 * 				Edit Action >> Information for creating action
 * 				addaction - whether this action will be displayed on add page or on edit page. Default value : true
 * 				popup - To ensure that edit-action will be opened in poup or on same page
 * 				selection - In case of popup listing what will be selection type
 * 				class-name - class name for current object under action
 * 				jsfunction - JavaScript function name that will be invoked on current action
 * 				jsinfo - JavaScript function parameter information
 * 				jsreturn - JavaScript function returns or not
 * 				actiontype - Type of action (submit | cancel | button - simple button)
 * 				accesskey - To define a shortcut key for this action
 * 				styleid - To set id or name field for this action
 * 				tabindex - To set tab position for this action
 * 				title - setting tool-tip for current action
 * 				style - To set extra properties for this action for look & feel
 * 				style-class - Set style-sheet class name for this action
 * 				
 * 		Note : URL should be relative to base
 * 
 * 		-->
 * 			<edit-action addaction="true" popup="true" selection="single|multiple"
 * 				class-name="beta" jsfunction="onSubmit" jsinfo="validate" jsreturn="true|false"
 * 				action-type="submit|button|cancel" access-key="key" disabled="true|false"
 * 				style-id="id" tab-index="1" title="clickme" style="" style-class="main">
 * 				<url>ManageTender.do?actiion=edit</url>
 * 				<roles>user,admin</roles>
 * 				<display-name img="view.gif">common.edit</display-name>
 * 			</edit-action>
 * </actions>
 * </pre>
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */

public class EditAction extends PageAction
{

	/**
	 * It indicates whether current action is for add page or for edit page. Default value is true, i.e. for add page.
	 */
	private boolean	addAction;

	/**
	 * Name of the java script function, which will be invoked on this action
	 */
	private String	jsFunction;

	/**
	 * Any extra information parameters which need to be set to JS function while invoking it
	 */
	private String	jsInfo;

	/**
	 * It indicates whether JS method used in this action returns something or not
	 */
	private boolean	jsReturn;

	/**
	 * Type of the action.
	 * 
	 * <p>
	 * Possible types are
	 * <ul>
	 * <li>submit - for submit type of button
	 * <li>cancel - for cancel button
	 * <li>button - for simple type of button
	 * </ul>
	 */
	private String	actionType;

	/**
	 * Access key to access this action using key board
	 */
	private String	accessKey;

	/**
	 * 
	 */
	private boolean	disabled;

	/**
	 * CSS Style id, which we want to assign to this action button
	 */
	private String	styleId;

	/**
	 * Tab index to be set to action button for keyboard navigation
	 */
	private String	tabIndex;

	/**
	 * Name of the action to be shown
	 * 
	 * <p>
	 * TODO - need to analyze it in link of display name
	 */
	private String	title;

	/**
	 * Extra css style information to set to this action on UI
	 */
	private String	style;

	/**
	 * CSS class name to be applied to this action
	 */
	private String	styleClass;

	/**
	 * Name of the action which should be set on UI.
	 * 
	 * <p>
	 * TODO - Need to analyze it, do we still need it
	 */
	private String	name;

	public String getAccessKey()
	{
		return accessKey;
	}

	public String getActionType()
	{
		return actionType;
	}

	public String getJsFunction()
	{
		return jsFunction;
	}

	public String getJsInfo()
	{
		return jsInfo;
	}

	public String getName()
	{
		return name;
	}

	public String getStyle()
	{
		return style;
	}

	public String getStyleClass()
	{
		return styleClass;
	}

	public String getStyleId()
	{
		return styleId;
	}

	public String getTabIndex()
	{
		return tabIndex;
	}

	public String getTitle()
	{
		return title;
	}

	public boolean isAddAction()
	{
		return addAction;
	}

	public boolean isDisabled()
	{
		return disabled;
	}

	public boolean isJsReturn()
	{
		return jsReturn;
	}

	public void setAccessKey( String accessKey )
	{
		this.accessKey = accessKey;
	}

	public void setActionType( String actionType )
	{
		this.actionType = actionType;
	}

	public void setAddAction( boolean addAction )
	{
		this.addAction = addAction;
	}

	public void setDisabled( boolean disabled )
	{
		this.disabled = disabled;
	}

	public void setJsFunction( String jsFunction )
	{
		this.jsFunction = jsFunction;
	}

	public void setJsInfo( String jsInfo )
	{
		this.jsInfo = jsInfo;
	}

	public void setJsReturn( boolean jsReturn )
	{
		this.jsReturn = jsReturn;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public void setStyle( String style )
	{
		this.style = style;
	}

	public void setStyleClass( String styleClass )
	{
		this.styleClass = styleClass;
	}

	public void setStyleId( String styleId )
	{
		this.styleId = styleId;
	}

	public void setTabIndex( String tabIndex )
	{
		this.tabIndex = tabIndex;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	@Override
	public String toString()
	{
		return StringUtils.toGenericString( this ) + " url[" + getUrl() + "] displayData[" + getDisplayData()
				+ "] className[" + getClassName() + "] selection[" + getSelection() + "] popup[" + getPopup() + "]";
	}
}
