package com.gamerking195.dev.lametric;

import java.util.ArrayList;

public class FrameWrapper
{
	FrameWrapper(ArrayList<Frame> frames)
	{
		this.frames = frames;
	}

    void addFrame(Frame frame)
    {
        frames.add(frame);
    }

	private ArrayList<Frame> frames;
	
	public ArrayList<Frame> getList()
	{
		return frames;
	}
}
