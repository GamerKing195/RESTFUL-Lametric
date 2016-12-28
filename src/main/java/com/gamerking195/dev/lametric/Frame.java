package com.gamerking195.dev.lametric;

public class Frame 
{
	public String text;
	public String icon;
	
	public Frame(String text, String icon)
	{
		this.text = text;
		this.icon = icon;
	}

	public String getText() 
	{
		return text;
	}

	public void setText(String text) 
	{
		this.text = text;
	}

	public String getIcon() 
	{
		return icon;
	}

	public void setIcon(String icon) 
	{
		this.icon = icon;
	}
}
