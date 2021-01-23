package com.vedantatree.comps.dof.common.bdo;

import java.io.Serializable;
import java.util.Map;


/**
 * This object faciliatates to contain the display data for an action. This is being maintained with Action Metadata
 * classes.
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 *
 */
public class DisplayData implements Serializable, Cloneable
{

	/**
	 * Name of the image to show on UI
	 */
	private String				imageName;

	/**
	 * Key of text message to show as tool tip or name. This is a key from resource file for the desired message.
	 */
	private String				textKey;

	/**
	 * Mapping of Text Name/Message with language
	 */
	private Map<String, String>	languageVsNamesMap;

	@Override
	public Object clone() throws CloneNotSupportedException
	{

		return super.clone();
	}

	public String getImageName()
	{
		return imageName;
	}

	public Map<String, String> getLanguageVsNamesMap()
	{
		return languageVsNamesMap;
	}

	public void setLanguageVsNamesMap( Map<String, String> languageVsNamesMap )
	{
		this.languageVsNamesMap = languageVsNamesMap;
	}

	public String getNameByLanguage( String languageKey )
	{
		return languageVsNamesMap == null ? null : languageVsNamesMap.get( languageKey );
	}

	public String getTextKey()
	{
		return textKey;
	}

	public void setImageName( String imageName )
	{
		this.imageName = imageName;
	}

	public void setText( String text )
	{
		this.textKey = text;
	}

	@Override
	public String toString()
	{
		return "imageName is [" + imageName + "] textKey[" + textKey + "]";
	}
}
