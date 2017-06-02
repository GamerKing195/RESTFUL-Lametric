package com.gamerking195.dev.lametric;

import java.util.ArrayList;

class FrameWrapper
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
	
	ArrayList<Frame> getList()
	{
		return frames;
	}
}
