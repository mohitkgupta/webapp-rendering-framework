package com.vedantatree.comps.dof;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.comps.securitymanager.model.Menu;
import org.vedantatree.comps.securitymanager.model.MenuItem;
import org.vedantatree.comps.securitymanager.model.User;
import org.vedantatree.utils.BeanUtils;
import org.vedantatree.utils.StringUtils;
import org.vedantatree.utils.config.ConfigurationManager;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;

import com.vedantatree.comps.dof.web.WebAppConstants;


/**
 * This object represent the metadata for whole view for application.
 * 
 * <p>
 * As Dynamic UI framework is controlling the rendering of many portions of the UI, this metadata object will contain
 * many important information required by framework to render the UI like Global Menus to show at top of the UI, Top
 * navigation Menus, and left Navigation Menus for a selected menu. Other framework components like LeftNavigation, and
 * Top Navigation generator tags take this information from ViewMetadata to render the UI.
 * 
 * <p>
 * This class can be override to provide the custom filtering of various menus shown at UI. Suppose any application
 * module wants to filter some menu or menu items based on some application specific property or operation mode, that
 * can be achieved by overriding the methods given in this class. These methods are mostly like a hook to specify the
 * valid menus for current application situation.
 * 
 * <p>
 * It internally uses the current logged in user to get the available menus and menu items. As User is the source of all
 * menus, sub-menus, menu items and their associated roles, and rights as defined with Security Service Provider.
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class ViewMetaData
{

	private static final Log		LOGGER					= LogFactory.getLog( ViewMetaData.class );
	public static byte				TOP_NAVIGATION_MENU		= 0;
	public static byte				LEFT_NAVIGATION_MENU	= 1;
	public static byte				GLOBAL_NAVIGATION_MENU	= 2;

	/**
	 * Current HTTP session where associated view is rendered
	 * 
	 * <p>
	 * TODO efforts can be made further to remove this property to make it generic
	 */
	private HttpSession				session;

	/**
	 * Menu current selected in application
	 * 
	 * <p>
	 * Current Selected menu is mostly set either from Security Filter who keep an eye on every request coming to
	 * application and hence knows when any request is coming to change the selected menu. Or it is set from Login
	 * action to set the default menu at the time of login.
	 */
	private Menu					currentMenu;

	/**
	 * Current left menu item selected in application
	 * 
	 * <p>
	 * Current menu items selected from left navigation is set from Security filter. Security filter keeps on checking
	 * every request to the system, and it fetches the left menu item for every new request and set it as current left
	 * menu item. Or it is set from Login action to set the default left navigation at the time of login.
	 */
	private MenuItem				currentLeftMenuItem;

	/**
	 * Menu item current selected in application, not the left menu item
	 * 
	 * <p>
	 * Current menu items selected in the application. is set from Security filter. Security filter keeps on checking
	 * every request to the system, and it fetches the menu item for every new request and set it as current menu item.
	 * Or it is set from Login action to set the default menu item selected at the time of login.
	 */
	private MenuItem				currentMenuItem;

	/**
	 * Current logged in user
	 * 
	 * <p>
	 * Current user is set from Login action at the time of login. Viewmetadata is also initialized from Login action on
	 * login, and hence all required properties like session and user etc is set from there.
	 */
	private User					currentUser;

	/**
	 * Collection of valid left navigatio menu items
	 * 
	 * <p>
	 * User already have filtered left navigation menu items for a specific menu based on Security Service roles and
	 * rights. However, there are functional scenairos when application wants to filter these menu items further based
	 * on their own logic or preferences, like based on some operation mode. Following collection contains that filtered
	 * list. Original collection of menu items from user is filtered whenever any menu is set as current menu. It is
	 * being done to avoid filtering again and again on every UI refresh, and hence done once whenever current menu
	 * changes.
	 */
	private Collection<MenuItem>	validLeftNavigationItems;

	/**
	 * Collection of valid left navigatio menu items
	 * 
	 * <p>
	 * User already have filtered left navigation menu items for a specific menu based on Security Service roles and
	 * rights. However, there are functional scenairos when application wants to filter these menu items further based
	 * on their own logic or preferences, like based on some operation mode. Following collection contains that filtered
	 * list. Original collection of menu items from user is filtered whenever any menu is set as current menu. It is
	 * being done to avoid filtering again and again on every UI refresh, and hence done once whenever current menu
	 * changes.
	 */
	private Collection<Menu>		validTopNavigationItems;
	private Collection<Menu>		validGlobalNavigationItems;

	public static final String		VIEWMETADATA_CLASSNAME	= "ViewMetadata_Class";

	/**
	 * Protected constructor for View Metadata
	 * 
	 * <p>
	 * It is made protected as View Metadata instance should always be created from createInstance method, which is
	 * placed in this class only. Protected is required so that developer can extend this class to provide custom class
	 * name in configuration.
	 */
	protected ViewMetaData()
	{
	}

	/**
	 * This method will return the new instance of View Metadata.
	 * 
	 * <p>
	 * It will try to read the class name from configuration, if developers has specified any custom class name. If a
	 * custom class is found, which should be of course of ViewMetadata type, then it will use that class to create new
	 * instance. Otherwise, it will use this default class to create new View Metadata.
	 * 
	 * @return new instance of ViewMetadata
	 */
	public static ViewMetaData createInstance()
	{
		String viewMetadataClassName = ConfigurationManager.getSharedInstance()
				.getPropertyValue( VIEWMETADATA_CLASSNAME, false );

		LOGGER.debug( "createInstance: viewMetadata-class-name[" + viewMetadataClassName + "]" );

		Object newInstance;

		if( !StringUtils.isQualifiedString( viewMetadataClassName )
				|| viewMetadataClassName.trim().equals( ViewMetaData.class.getName() ) )
		{
			newInstance = new ViewMetaData();
		}
		else
		{
			try
			{
				newInstance = BeanUtils.newInstance( viewMetadataClassName.trim(), null, null );
			}
			catch( Exception e )
			{
				SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
						"Problem while creating instance of ViewMetadata. wrapped-error-message[" + e.getMessage()
								+ "]",
						e );
				LOGGER.error( se );
				throw se;
			}
			if( !( newInstance instanceof ViewMetaData ) )
			{
				SystemException se = new SystemException( IErrorCodes.ILLEGAL_ARGUMENT_ERROR,
						"Wrong ViewMetadata class name has been specified. It is not of ViewMetadata type. specifiedClassName["
								+ viewMetadataClassName + "]" );
				LOGGER.error( se );
				throw se;
			}
		}
		LOGGER.debug( "returning viewmetadata[" + newInstance + "]" );
		return (ViewMetaData) newInstance;
	}

	public final HttpSession getSession()
	{
		return session;
	}

	public final Menu getCurrentMenu()
	{
		return currentMenu;
	}

	public final MenuItem getCurrentMenuItem()
	{
		return currentMenuItem;
	}

	public final User getCurrentUser()
	{
		return currentUser;
	}

	public final boolean isAuthroizedAction( String URL )
	{
		return currentMenu.containsMenuItemForURL( URL );
	}

	/**
	 * It returns the encrypted web page url for the given URL.
	 * 
	 * <p>
	 * This method is generally called from View Helper while rendering the actions on UI. UI action's URL come from XML
	 * metadata in its original form, but we want to render only encrypted URL. So this method helps to convert those
	 * real URL to corresponding encrypted URL.
	 * 
	 * @param actionURL actual URl from metadata
	 * @return encrypted URL
	 */
	public final String getWebPageURL( String actionURL )
	{
		LOGGER.debug( "actionURL[" + actionURL + "]" );

		// special case handling, as in xml metadata, we specify 'default' as URL if we just want button to work as
		// simple submit button to the default action of current page form
		if( "default".equalsIgnoreCase( actionURL ) )
		{
			return actionURL;
		}

		int firstAmpIndex = actionURL.indexOf( "&" );
		String paramsOtherThanFirst = firstAmpIndex > 0 ? actionURL.substring( firstAmpIndex ) : null;
		LOGGER.debug( "paramsOtherThanFirst[" + paramsOtherThanFirst + "]" );

		actionURL = org.vedantatree.comps.securitymanager.AppSecurityManager.getSharedInstance()
				.getProcessedRequestURI( actionURL );
		MenuItem menuItem = currentMenu.getMenuItemForURL( actionURL );
		if( menuItem == null )
		{
			SystemException se = new SystemException( IErrorCodes.AUTHENTICATION_FAILURE,
					"Current user is not authorized for specified menu URL. url[" + actionURL
							+ "]. Control should not reach here as View Helper already show only those actions for which user is authorized. It seems to be a development bug or some methods have been overriden in wrong way." );
			LOGGER.error( se );
			throw se;
		}
		String menuURL = menuItem.getWebPageURL();
		LOGGER.debug( "menuURL[" + menuURL + "]" );

		if( firstAmpIndex > 0 )
		{
			menuURL += paramsOtherThanFirst;
		}

		LOGGER.debug( "finalURL[" + menuURL + "]" );
		return menuURL;
	}

	public final void setCurrentMenu( Menu currentMenu )
	{
		LOGGER.debug( "setCurrentMenu: newMenu[" + currentMenu + "] earlier-menu[" + this.currentMenu + "]" );
		this.currentMenu = currentMenu;
		if( this.currentMenu == null )
		{
			// flush the cache if user is set as null
			validLeftNavigationItems = null;
		}
		else
		{
			// initialize the cache of left navigation menu items to avoid filtering again and again on every
			// refresh, as left navigation is dependent on selected menu
			validLeftNavigationItems = collectValidTopLevelLeftNavigationItems(
					currentUser.getLeftNavigationMenuItems( currentMenu ), null, LEFT_NAVIGATION_MENU );
		}
	}

	public final void setCurrentMenuItem( MenuItem currentMenuItem )
	{
		this.currentMenuItem = currentMenuItem;

		if( currentMenuItem != null && currentMenuItem.getActionType() == WebAppConstants.LEFT_NAV_ACTION )
		{
			setCurrentLeftMenuItem( currentMenuItem );
		}
	}

	public final void setCurrentUser( User currentUser )
	{
		LOGGER.debug( "setCurrentUser[" + currentUser + "] earlierUser[" + this.currentUser + "]" );
		this.currentUser = currentUser;
		if( this.currentUser == null )
		{
			// flush the cache if user is set as null
			validTopNavigationItems = null;
			validGlobalNavigationItems = null;
		}
		else
		{
			// initialize top and global menus to avoid filtering based on application logic again and again on every
			// refresh
			validTopNavigationItems = collectValidNavigationMenus( currentUser.getTopNavigationMenus(), null,
					TOP_NAVIGATION_MENU );
			validGlobalNavigationItems = collectValidNavigationMenus( currentUser.getGlobalMenus(), null,
					GLOBAL_NAVIGATION_MENU );

		}
	}

	public final void setSession( HttpSession session )
	{
		this.session = session;
	}

	public final MenuItem getCurrentLeftMenuItem()
	{
		return currentLeftMenuItem;
	}

	public final void setCurrentLeftMenuItem( MenuItem currentLeftMenuItem )
	{
		this.currentLeftMenuItem = currentLeftMenuItem;
	}

	public final Collection<MenuItem> getLeftNavigation()
	{
		assertUserSet();
		assertCurrentMenuSet();

		// return currentUser.getLeftNavigationMenuItems( currentMenu );
		return validLeftNavigationItems;
	}

	public final Collection<Menu> getTopNavigation()
	{
		assertUserSet();

		// return currentUser.getTopNavigationMenus();
		return validTopNavigationItems;
	}

	public final Collection<Menu> getGlobalMenus()
	{
		assertUserSet();

		// return currentUser.getGlobalMenus();
		return validGlobalNavigationItems;
	}

	private int recursionCount = 0;

	private Collection<MenuItem> collectValidTopLevelLeftNavigationItems( Collection<MenuItem> navigationItems,
			Collection<MenuItem> validNavigationItems, byte navigationType )
	{
		LOGGER.debug( "collectValidNavigationItems: navigationType[" + navigationType + "]" );
		if( navigationItems == null || navigationItems.size() == 0 )
		{
			return validNavigationItems;
		}
		if( validNavigationItems == null )
		{
			validNavigationItems = new ArrayList<>();
		}

		for( MenuItem navigationItem : navigationItems )
		{
			LOGGER.debug( "navigationItem[" + navigationItem + "]" );

			if( isValidNavigationMenuItem( navigationItem, navigationType ) )
			{
				// remove this code after few days of testing
				if( LOGGER.isDebugEnabled() )
				{
					if( ++recursionCount > 200 )
					{
						LOGGER.debug( "breaking as recursion is having problem, going above 100 cycles" );
						LOGGER.debug( "breaking as recursion is having problem, going above 100 cycles" );
						LOGGER.debug( "breaking as recursion is having problem, going above 100 cycles" );
						LOGGER.debug( "breaking as recursion is having problem, going above 100 cycles" );
						LOGGER.debug( "breaking as recursion is having problem, going above 100 cycles" );
						break;
					}
				}
				LOGGER.debug( "qualified as valid navigation item" );
				validNavigationItems.add( navigationItem );

				// no need to collect sub-items of top left navigation items here. This method collect the valid left
				// navigation items only for top level
				// sub left navigation items will be collected in separate method called from Left Navigation Tag

				// collectValidTopLevelLeftNavigationItems( navigationItem.getSubMenuItems(), validNavigationItems,
				// navigationType
				// );
			}
		}
		return validNavigationItems;
	}

	public Collection<MenuItem> getValidSubMenuItems( MenuItem parentMenuItem )
	{
		LOGGER.debug( "getValidSubMenuItems: parentMenuItem[" + parentMenuItem + "]" );

		Collection<MenuItem> subMenuItems = parentMenuItem.getSubMenuItems();
		if( subMenuItems == null || subMenuItems.size() == 0 )
		{
			LOGGER.debug( "return null as no sub menus found." );
			return null;
		}
		Collection<MenuItem> validatedSubMenuItems = new ArrayList<>();
		for( MenuItem menuItem2 : subMenuItems )
		{
			MenuItem menuItem = menuItem2;
			if( isValidNavigationMenuItem( menuItem, LEFT_NAVIGATION_MENU ) )
			{
				LOGGER.debug( "found valid sub menu[" + menuItem + "]" );
				validatedSubMenuItems.add( menuItem );
			}
		}

		return validatedSubMenuItems;
	}

	protected boolean isValidNavigationMenuItem( MenuItem menuItem, byte navigationType )
	{
		return true;
	}

	private Collection<Menu> collectValidNavigationMenus( Collection<Menu> navigationMenus,
			Collection<Menu> validNavigationMenus, byte navigationType )
	{
		LOGGER.debug( "collectValidNavigationMenus: navigationType[" + navigationType + "]" );
		if( navigationMenus == null || navigationMenus.size() == 0 )
		{
			return validNavigationMenus;
		}
		if( validNavigationMenus == null )
		{
			validNavigationMenus = new ArrayList<>();
		}

		for( Menu navigationMenu : navigationMenus )
		{
			LOGGER.debug( "navigationMenu[" + navigationMenu + "]" );

			if( isValidNavigationMenu( navigationMenu, navigationType ) )
			{
				LOGGER.debug( "qualified as valid menu" );
				validNavigationMenus.add( navigationMenu );
			}
		}
		return validNavigationMenus;
	}

	protected boolean isValidNavigationMenu( Menu navigationMenu, byte navigationType )
	{
		return true;
	}

	private void assertUserSet()
	{
		if( currentUser == null )
		{
			SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"User is not logged in, so navigation menus can not be returned. It seems like some development bug. Please report to development team." );
			LOGGER.fatal( se );
			throw se;
		}
	}

	private void assertCurrentMenuSet()
	{
		if( currentMenu == null )
		{
			SystemException se = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"No menu is selected, so navigation menu items can not be returned. It seems like some development bug. Please report to development team." );
			LOGGER.fatal( se );
			throw se;
		}

	}

}