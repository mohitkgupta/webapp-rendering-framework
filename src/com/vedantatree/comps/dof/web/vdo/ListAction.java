package com.vedantatree.comps.dof.web.vdo;

import org.vedantatree.utils.StringUtils;


/**
 * Object of this type represent the metadata of action which should be shown on a list page in every record row.
 * 
 * <p>
 * These are parsed from the Dynamic Object form metadata configuration to set with DOMetadata. Later DOMetadata is used
 * with ViewMetadata for enabling rendering operation on UI.
 * 
 * <pre>
 * <actions>
 * 	<!-- Form actions that will be displayed on list page -->
 * 		<list-actions>
 * 			<!--
 * 				List action information for creating list action on list page
 * 				popup - To ensure that list-action will be opened in poup or on same page
 * 				selection - In case of popup listing what will be selection type
 * 				class-name - class name for current action
 * 				ajax - Action will be performed through AJAX call or simple
 * 				url - url for current action
 * 				roles - valid roles for current action << Not in use now, as we are getting roles from security service
 * 				display-name - Display name for current action
 * 				img - Image path for displaying on current action
 * 				confirmation-message - Message that will be required to show before actually calling the action as confirmation
 * 				error-page - error page path in case of any problem - -Not working
 * 
 * 				** Note - URL should be relative to base URL
 * 			-->
 * 			<list-action popup="true" selection="single|multiple"
 * 				class-name="alpha" ajax="true">
 * 				<url>ManageTender.do?action=edit</url>
 * 				<roles>storemanager,manager</roles>
 * 				<display-name img="view.gif">common.edit</display-name>
 * 				<confirmation-message>conf.msg</confirmation-message>
 * 				<error-page>/tendererror.jsp</error-page>
 * 			</list-action>
 * </actions>
 * </pre>
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class ListAction extends PageAction
{

	public static String	LIST_ACTION_ORIENTATION_ANY		= "any";
	public static String	LIST_ACTION_ORIENTATION_LEFT	= "left";
	public static String	LIST_ACTION_ORIENTATION_RIGHT	= "right";

	/**
	 * It indicates whether the mechanism to send the request on this action should be using Ajax or using convention
	 * request/response approach.
	 */
	private boolean			ajaxEnabled;

	/**
	 * A message which should be shown as confirmation message to user on action, before actually sending the request to
	 * server. This will be a key to actual message from resource file.
	 */
	private String			confirmationMessage;

	/**
	 * Address of the error page, which should be shown in case of any error occured during this operation
	 */
	private String			errorPage;

	/**
	 * Orientation of the list action, which tells us where this action should be rendered on screen i.e. on left side
	 * of data columns or on right side. This was a new requirement we get from users where they want some of the
	 * crucial actions on right side at the end, so that user won't user these by mistake and should be used conciously
	 * 
	 * Attribute added to list action metadata is 'orientation="right"'
	 * 
	 * By default, every list action is left oriented.
	 */

	private String			orientation						= LIST_ACTION_ORIENTATION_LEFT;

	public String getOrientation()
	{
		return orientation;
	}

	public void setOrientation( String orientation )
	{
		this.orientation = orientation;
	}

	public boolean isLeftOriented()
	{
		return LIST_ACTION_ORIENTATION_LEFT.equalsIgnoreCase( orientation );
	}

	public boolean isRightOriented()
	{
		return LIST_ACTION_ORIENTATION_RIGHT.equalsIgnoreCase( orientation );
	}

	public String getConfirmationMessage()
	{
		return confirmationMessage;
	}

	public String getErrorPage()
	{
		return errorPage;
	}

	public boolean isAjaxEnabled()
	{
		return ajaxEnabled;
	}

	public void setAjaxEnabled( boolean ajaxEnabled )
	{
		this.ajaxEnabled = ajaxEnabled;
	}

	public void setConfirmationMessage( String confirmationMessage )
	{
		this.confirmationMessage = confirmationMessage;
	}

	public void setErrorPage( String errorPage )
	{
		this.errorPage = errorPage;
	}

	@Override
	public String toString()
	{
		return StringUtils.toGenericString( this ) + " url[" + getUrl() + "] displayData[" + getDisplayData()
				+ "] className[" + getClassName() + "] selection[" + getSelection() + "] popup[" + getPopup()
				+ "] orientation[" + orientation + "]";
	}

}
