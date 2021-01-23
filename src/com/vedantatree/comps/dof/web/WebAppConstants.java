package com.vedantatree.comps.dof.web;

import org.apache.struts.Globals;
import org.vedantatree.utils.config.ConfigurationManager;


/**
 * Constant File having generic constants for all web components, including for DOF framework also
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public interface WebAppConstants
{

	public static final String	SELECTEDMENU							= "selectedMenu";
	public static final String	SELECTEDMENU_ITEM						= "selectedMenuItem";

	// ====== Action Types
	public static final byte	LEFT_NAV_ACTION							= 1;
	public static final byte	RIGHT_NAV_ACTION						= 5;
	public static final byte	PAGE_ACTION								= 2;
	public static final byte	LIST_ACTION								= 3;
	public static final byte	LIST_HEADER_ACTION						= 4;

	// ===== constants for locale information, used for locale settings
	static final String			LANGUAGE								= "language";
	static final String			MODULE									= "module";

	static final String			COUNTRY_ISO_CODE_FRANCE					= "fr";
	static final String			COUNTRY_ISO_CODE_RWANDA					= "rw";
	static final String			COUNTRY_ISO_CODE_UNITEDKINGDOM			= "gb";										// "uk";

	// fra can also be used for french
	static final String			LANGUAGE_ISO_CODE_FRENCH				= "fre";
	static final String			LANGUAGE_ISO_CODE_KINIYARWANDA			= "kin";
	static final String			LANGUAGE_ISO_CODE_SWAHILI				= "swa";
	static final String			LANGUAGE_ISO_CODE_ENGLISH				= "en";										// "eng";

	// ====== constants for login/logout
	public static final String	LOGIN_EXTENSION_FILE					= "login.extension.file";

	public final static String	HOME									= "home";
	public final static String	LOGOUT									= "logout";
	public final static String	FORGOT_PASSWORD							= "forgotpwd";
	public final static String	UNLOCK_ACCOUNT_REQ						= "unlockAccount";
	public final static String	EMAIL_SUBMIT							= "emailSubmit";
	public final static String	VERIFY_TEMP_PWD							= "verifyPwdToken";
	public final static String	AUTHENTICATION_FAILED					= "authenticationFailed";

	public final static String	HOMEPAGE								= "homepage";

	public final static String	AUTHENTICATE							= "authenticate";

	public final static String	TRUE									= "true";
	public final static String	FALSE									= "false";

	// ====== constants for view part
	static String				MODULES_DEFAULT_TOP_MENUS				= "modules-default-top-menus";
	static String				CALL_FROM_MENUITEM_SWITCHING			= "fromMenuItemSwitchingServlet";
	static String				CURRENT_LEFT_MENUITEM					= "currentLeftNavItem";
	static String				VIEW_METADATA							= "viewmetadata";
	static String				VIEW_HELPER								= "viewHelper";
	static String				FORM_BDO								= "form.businessDataObject";
	static String				PAGINATION_USER_ID_KEY					= "paginationUserId";
	static String				CUSTOMIZED_SEARCH						= "searchAct";
	static String				FORM_ACTION								= "action";
	static String				ACTION_HOME								= "home";
	static String				ACTION_ADD								= "add";
	static String				ACTION_EDIT								= "edit";
	static String				ACTION_VIEW								= "viewPage";
	static String				ACTION_LIST								= "list";
	static String				ACTION_DELETE							= "delete";
	static String				ACTION_SAVE								= "save";
	static String				SINGLE_ADD								= "singleAdd";
	static String				ACTION_BACK								= "back";
	static String				ACTION_CANCEL							= "cancel";
	static String				URL										= "url";
	static String				SORT_COLUMN								= "sortColumn";
	static String				HELP									= "Help";
	static String				SHOW_ADD_BUTTON							= "showAddButton";
	static String				LIST_PAGE_DATA							= "dataList";
	static String				EDIT_OBJECT_ID							= "id";
	static String				EDIT_OBJECT								= "edit_object";
	static String				EDITABLE_VIEW							= "editable_view";
	static String				WRONGINPUT_FROMLISTPAGE					= "wrongInputSearchFromList";
	static String				MODULENAME								= "moduleName";

	public static String		CONSUMABLE_INVENTORY_MODULE				= "ConsumableInventory";

	public final static String	PR_STATUS_VALIDATED						= "1";
	public static final String	OBJECTGROUP								= "ObjectGroup";

	/**
	 * Locale String Constant for setting the Locale
	 */
	static String				LOCALE_STRING							= Globals.LOCALE_KEY;

	/**
	 * back url for tracking the back page
	 */
	String						BACK_URL								= "backUrl";

	/**
	 * for providing the blank tiles definition.
	 */
	String						BLANK_TILES_DEF							= "def";

	/**
	 * attribute will be set if RelationExistException Occured while deleting any object.
	 */
	String						LIVE_RELATIONS_DETECTED					= "deleteConfirm";

	/**
	 * Represents of name of request paramater referring the selected ids.
	 */
	public static final String	SELECTED_IDs							= "ids";

	/**
	 * Constants for list page
	 */

	/**
	 * key of title of the list page
	 */
	final String				TITLE									= "title";

	final String				SUBTITLE								= "subTitle";

	final String				FROMEDIT								= "FromEdit";

	/**
	 * Boolean set false when you don't want to show search tag on list page
	 */
	final String				SHOW_SEARCH_TAG							= "showSearchTag";

	/**
	 * Boolean set false when you don't want to show pagination tag on list page
	 */
	final String				SHOW_PAGINATION_TAG						= "showPaginationTag";

	/**
	 * specify the path where we need to send information when we click on search button for eg.
	 * purchaseRequisition.do?action=list
	 */
	final String				SEARCH_PATH								= "searchPath";

	/**
	 * when list page is opened as a popup then set this attribute to true then this will be opened in popup tiles
	 * layout
	 */
	final String				POPUP									= "popup";

	final String				DATALIST								= "dataList";

	/**
	 * Selection field (Not currnetly used)
	 */
	final String				SELECTION_FIELD							= "selectionField";

	/**
	 * Selection Mode need to set "single" or "multiple"
	 */
	final String				SELECTION_MODE							= "selection";

	/**
	 * name of javascript function where call will go It will be on paren list page
	 */
	final String				JAVASCRIPT_FUNCTION						= "javascriptFunction";

	/**
	 * Pagination Link When user will click on pagination tabs then page will redirect to given link
	 */
	final String				PAGINATION_LINK							= "paginationLink";

	/**
	 * For search clause for list
	 */
	public static final String	SEARCH_CLAUSE							= "searchClause";

	public static final String	SEARCH_URL_PART							= "searchURLPart";

	/**
	 * Common constants for the Actions
	 */
	public final static String	SEARCH_COLUMN							= "searchColumn";

	public final static String	SEARCH_COLUMN_VALUE						= "searchValue";

	public final static String	PARENT_ID								= "parentId";

	public final static String	ROOT									= "root";

	public final static String	IS_DESCENDING							= "isDescending";

	public final static String	SEARCH									= "search";

	public final static String	PAGINATION								= "pagination";

	/**
	 * Refers thatSelection mode is single.
	 */
	public final static String	SINGLE									= "single";

	/**
	 * Refers thatSelection mode is multiple.
	 */
	public final static String	MULTIPLE								= "multiple";

	/**
	 * Refers the request attribute named subTitle.
	 */
	public final static String	SUB_TITLE								= "subTitle";

	/**
	 * attributes defined for message page which will be desplayed after deletion of any object from the page.
	 * 
	 * 
	 */

	/**
	 * URL which will be append on the ok button on the message page to go set by the action
	 */
	String						MESSAGEURL								= "messageUrl";

	/**
	 * message which will be shown on the message page after deletion of object
	 */
	String						MESSAGE									= "message";

	/**
	 * Header description of the page
	 */
	String						HEADING									= "heading";

	/**
	 * variable defined for showing the message for date compatibility
	 */
	String						DATE_NOT_COMPATIBLE						= "dateCompatible";

	String						UNIQUE_CONSTRAINT_FAILED				= "uniqueValues";

	String						NOT_VALIDATED							= "notValidate";

	/**
	 * used for setting the exception object in the request and same can be fetched on error page
	 */
	String						EXCEPTION_PREV_PAGE_URL					= "exp.previousPageURL";
	String						EXCEPTION_OBJECT						= "exceptionObject";
	String						EXCEPTION_TILES_DEFINITION				= "exp.tilesDefinition";
	String						ERRORMESSAGE							= "errorMessage";

	String						DEFAULT_VALUE							= "app.defaultValue";

	public final static String	FA_CATEGORY								= "category";

	public final static String	USER									= "user";

	public final static String	PAGINATION_DEFAULT_PAGE_SIZE_PROPERTY	= "DEFAULT_PAGE_SIZE";

	/*
	 * Set number of records showing on single list page
	 */
	public final static int		PAGE_SIZE								= ConfigurationManager.getSharedInstance()
			.getPropertyValueAsInteger( PAGINATION_DEFAULT_PAGE_SIZE_PROPERTY );

	public final static String	SELECTED_ID								= "id";

	public final static String	DOMETADATA_OBJECT						= "classObject";

	/**
	 * Used for Setting the PageAction in Request for Button Tag
	 */
	String						PAGEACTION								= "pageAction";

	public final static String	BASE_URL								= "baseURL";

	public final static String	SEARCH_CLAUSE_URL						= "searchClauseURL";

	public final static String	SORT_CLAUSE_URL							= "sortClauseURL";

	/**
	 * Variables defined for Login
	 */

	public static final String	BLANKDEFCLOSE							= "blankDefClose";

	public static final String	BLANKDEFLOGIN							= "blankDefLogin";

}
