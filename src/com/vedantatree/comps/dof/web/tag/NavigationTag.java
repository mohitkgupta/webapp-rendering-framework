package com.vedantatree.comps.dof.web.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.BeanUtils;

import com.vedantatree.comps.dof.DOMetaData;
import com.vedantatree.comps.dof.web.WebAppConstants;


/**
 * This tag generates the navigation links in case when we have object with parent child relations. for example,
 * department has sub-departments and so on. In that case, it will generate the navigation links for all parents.
 * 
 * TODO change the name to HierarchyNavigationTag
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public class NavigationTag extends BodyTagSupport
{

	private final static Log	LOGGER				= LogFactory.getLog( NavigationTag.class );
	private final static String	CURRENT_NAV_OBJECT	= "currentNavigationObject";

	private static String		id;

	@Override
	public int doEndTag() throws JspException
	{

		return EVAL_PAGE;
	}

	@Override
	public int doStartTag() throws JspException
	{

		String parentId = pageContext.getRequest().getParameter( "parentId" );
		if( parentId != null && !parentId.equalsIgnoreCase( "" ) && !parentId.equalsIgnoreCase( "root" ) )
		{

			HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
			DOMetaData doMetaData = (DOMetaData) req.getAttribute( DOMetaData.DOMEATADATA_KEY );
			String url = (String) req.getAttribute( WebAppConstants.URL );
			Boolean popup = (Boolean) req.getAttribute( WebAppConstants.POPUP );
			String selection = (String) req.getAttribute( WebAppConstants.SELECTION_MODE );

			Object parentObject = null;
			Object parentObjectInfo = null;
			String navigationContent = "";

			Object currentNavigationObject = req.getAttribute( CURRENT_NAV_OBJECT );
			LOGGER.debug( "Got Current Object [" + currentNavigationObject + "]" );

			List<String> parentList = null;
			if( currentNavigationObject != null )
			{
				Object childObject = currentNavigationObject;
				try
				{
					do
					{
						LOGGER.debug( "childObject[" + childObject + "]" );

						parentObjectInfo = null;
						parentObject = BeanUtils.getPropertyValue( childObject, doMetaData.getParentColumn() );
						LOGGER.debug( "parentObject[" + parentObject + "]" );

						if( parentList == null )
						{
							parentList = new ArrayList<>();
						}

						if( parentObject != null )
						{
							LOGGER.debug( "idColumn[" + doMetaData.getIdColumn().getDbName() );
							parentObjectInfo = BeanUtils.getPropertyValue( parentObject,
									doMetaData.getIdColumn().getDbName() );

							LOGGER.debug( "parentObjectId[" + parentObjectInfo + "]" );
							parentList.add( parentObjectInfo.toString() );
						}
						else
						{
							parentList.add( null );
							break;
						}

						childObject = parentObject;
					} while( parentObject != null );
				}
				catch( Exception exc )
				{
					LOGGER.error( "Exception while creating Navigation Tag [ " + exc.getMessage() + " ]", exc );
					throw new JspException( "Exception while creating Navigation Tag [ " + exc.getMessage() + " ]" );
				}
			}

			if( parentList != null )
			{

				int count = 1;
				int size = parentList.size();
				int index = size - 1;
				for( Iterator iter = parentList.iterator(); iter.hasNext(); count++, index-- )
				{

					String content = "";

					String element = (String) iter.next();
					content += "<a href='" + url + "?action=list&parentId=";

					if( element != null )
					{
						content += element + "&className=" + doMetaData.getClassName();
					}
					else
					{
						content += "root&className=" + doMetaData.getClassName();
					}
					if( popup.booleanValue() )
					{
						content += "&popup=true&selection=" + selection + "'";
					}
					content += " border=0 />";
					if( element != null )
					{
						if( index == size - 1 )
						{
							content += "Level " + count + " </a>";
						}
						else
						{
							content += "Level " + count + " </a> >> ";
						}
					}
					else
					{
						if( index == size - 1 )
						{
							content += "Root</a>";
						}
						else
						{
							content += "Root</a> >> ";
						}
					}
					LOGGER.debug( "Got Content for Navigation Tag[" + content + "]" );
					navigationContent = content + navigationContent;
				}
			}

			try
			{
				pageContext.getOut().println( "<div class='navigation' align='left'>" + navigationContent + "</div>" );
			}
			catch( IOException exc )
			{
				LOGGER.error( "NavigationTag : " + exc.getMessage(), exc );
				throw new JspException( "NavigationTag : " + exc.getMessage() );
			}
		}
		return SKIP_BODY;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public void setId( String id )
	{
		NavigationTag.id = id;
	}
}
