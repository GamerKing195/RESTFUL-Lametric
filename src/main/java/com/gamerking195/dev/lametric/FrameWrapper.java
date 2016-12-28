package com.gamerking195.dev.lametric;

import java.util.ArrayList;

public class FrameWrapper
{
	public ArrayList<Frame> frames;
	
	public FrameWrapper(ArrayList<Frame> frames)
	{
		this.frames = frames;
	}
	
	public void addFrame(Frame frame)
	{
		frames.add(frame);
	}
	
	public ArrayList<Frame> getList()
	{
		return frames;
	}
}
