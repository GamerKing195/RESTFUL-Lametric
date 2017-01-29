package com.gamerking195.dev.lametric;

public class Frame 
{
	private String text;
	private String icon;
	private Integer index;
    private Integer[] chartData;

    Frame(Integer index, Integer[] chartData)
    {
        this.index = index;
        this.chartData = chartData;
    }

    Frame(String text, String icon)
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
