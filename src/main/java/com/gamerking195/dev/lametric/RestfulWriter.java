package com.gamerking195.dev.lametric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class RestfulWriter 
{
	private String filePath = System.getProperty("user.dir")+"/resources";

	private String mineswineIp = "play.mineswine.com";

	private String greenIcon = "a3307";
	private String yellowIcon = "a3273";
	private String redIcon = "a3305";
	private String pigIcon = "i2767";
	private String mojangIcon = "i5848";

	private Frame mineswineLaunch = new Frame("MINESWINE INFORMATION", pigIcon);
	private Frame mojangLaunch = new Frame("MOJANG STATUS", mojangIcon);

	public static void main(String[] args)
	{
		RestfulWriter restful = new RestfulWriter();

		new Timer().scheduleAtFixedRate(restful.new RefreshTask(), 0L, 60 * 1000L);
	}

	public void createFiles()
	{
		File subDirectories = new File(filePath);
		subDirectories.mkdirs();

		File msFile = new File(filePath+"/mineswineapp.json");

		if (msFile.exists())
			msFile.delete();

		try
		{
			msFile.createNewFile();

			msFile.setWritable(true);
			msFile.setReadable(true);
			FileWriter writer = new FileWriter(msFile);
			writer.write(getMineswineApp());
			writer.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		File mcFile = new File(filePath+"/mojangapp.json");

		if (mcFile.exists())
			mcFile.delete();

		try
		{
			mcFile.createNewFile();

			mcFile.setWritable(true);
			mcFile.setReadable(true);

			FileWriter writer = new FileWriter(mcFile);
			writer.write(getMojangApp());
			writer.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	private String readFrom(String url) throws IOException 
	{
		InputStream is = new URL(url).openStream();
		try 
		{
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

			StringBuilder sb = new StringBuilder();
			int cp;
			while ((cp = rd.read()) != -1) {
				sb.append((char) cp);
			}
			return sb.toString();
		}
		finally
		{
			is.close();
		}
	}

	private String getMineswineApp()
	{
		Gson gson = new Gson();
		try 
		{
			String serverInfo = readFrom("https://us.mc-api.net/v3/server/info/"+mineswineIp+"/csv");
			String[] split = serverInfo.split(",");
			boolean online = Boolean.valueOf(split[0]);
			
			if (split[1].equalsIgnoreCase("ping-failed"))
			{
				FrameWrapper frames = new FrameWrapper(new ArrayList<Frame>());
				frames.addFrame(mineswineLaunch);
				frames.addFrame(new Frame("STATUS: Offline", redIcon));
				frames.addFrame(new Frame("ERROR: Server connection failed", redIcon));

				return gson.toJson(frames);
			}
			
			Integer maxCount = Integer.valueOf(split[2]);
			Integer playerCount = Integer.valueOf(split[1]);

			FrameWrapper frames = new FrameWrapper(new ArrayList<Frame>());
			frames.addFrame(mineswineLaunch);
			if (online)
				frames.addFrame(new Frame("STATUS: Online", greenIcon));
			else
				frames.addFrame(new Frame("STATUS: Offline", redIcon));
			frames.addFrame(new Frame("PLAYERS: "+playerCount+"/"+maxCount, pigIcon));

			return gson.toJson(frames);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		FrameWrapper frames = new FrameWrapper(new ArrayList<Frame>());
		frames.addFrame(mineswineLaunch);
		frames.addFrame(new Frame("STATUS: Offline", redIcon));
		frames.addFrame(new Frame("ERROR: JSON Parsing failed.", redIcon));

		return gson.toJson(frames);
	}

	private String getMojangApp()
	{
		Gson gson = new Gson();

		try
		{
			String statusCheck = readFrom("https://status.mojang.com/check");

			Type type = new TypeToken<ArrayList<JsonObject>>(){}.getType();
			ArrayList<JsonObject> statuses = gson.fromJson(statusCheck, type);

			ArrayList<String> greenServices = new ArrayList<>();
			ArrayList<String> yellowServices = new ArrayList<>();
			ArrayList<String> redServices = new ArrayList<>();

			for (JsonObject object : statuses)
			{
				Set<Map.Entry<String, JsonElement>> entries = object.entrySet();
				for (Map.Entry<String, JsonElement> entry: entries) 
				{
					switch(entry.getValue().getAsString())
					{
					case "green": greenServices.add(entry.getKey());
					case "yelow": yellowServices.add(entry.getKey());
					case "red": redServices.add(entry.getKey());
					}
				}
			}

			if (greenServices.size() == 10)
			{
				FrameWrapper frames = new FrameWrapper(new ArrayList<Frame>());
				frames.addFrame(mojangLaunch);
				frames.addFrame(new Frame("ALL SERVICES AVAILABLE", greenIcon));

				return gson.toJson(frames);
			}
			else
			{
				FrameWrapper frames = new FrameWrapper(new ArrayList<Frame>());
				frames.addFrame(mojangLaunch);

				for (String string : redServices)
				{
					frames.addFrame(new Frame("SERVICE "+string+" UNAVAILABLE", redIcon));
				}

				for (String string : yellowServices)
				{
					frames.addFrame(new Frame("SERVICE "+string+" UNSTABLE", yellowIcon));
				}

				for (String string : greenServices)
				{
					frames.addFrame(new Frame("SERVICE "+string+" AVAILABLE", greenIcon));
				}

				return gson.toJson(frames);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		FrameWrapper frames = new FrameWrapper(new ArrayList<Frame>());
		frames.addFrame(mojangLaunch);
		frames.addFrame(new Frame("ERROR: Failed to parse json.", redIcon));

		return gson.toJson(frames);
	}

	public class RefreshTask
	extends TimerTask
	{
		@Override
		public void run() 
		{
			createFiles();
		}	
	}
}
