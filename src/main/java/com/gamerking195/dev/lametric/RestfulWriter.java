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

	private void createFiles()
	{
        System.out.println("");
        System.out.println("=================BEGIN DEBUG=================");
        System.out.println("");
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
            System.out.println("MINESWINE: ");
			System.out.println(getMineswineApp());
			writer.write(getMineswineApp());
			writer.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

        System.out.println("");

		File mcFile = new File(filePath+"/mojangapp.json");

		if (mcFile.exists())
			mcFile.delete();

		try
		{
			mcFile.createNewFile();

			mcFile.setWritable(true);
			mcFile.setReadable(true);

			FileWriter writer = new FileWriter(mcFile);
            System.out.println("MOJANG: ");
			System.out.println(getMojangApp());
			writer.write(getMojangApp());
			writer.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

        System.out.println("");
        System.out.println("=================END DEBUG=================");
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
				for (Map.Entry<String, JsonElement> entry : entries)
				{
                    System.out.println("SERVICE "+entry.getKey()+" IS "+entry.getValue().getAsString().replace("\"", ""));

					switch(entry.getValue().getAsString().replace("\"", ""))
					{
					case "green": greenServices.add(entry.getKey()); break;
					case "yellow": yellowServices.add(entry.getKey()); break;
					case "red": redServices.add(entry.getKey()); break;
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

				System.out.println("RED SERVICES SIZE = " + redServices.size());
                System.out.println("YELLOW SERVICES SIZE = " + yellowServices.size());
                System.out.println("GREEN SERVICES SIZE = " + greenServices.size());

				for (String string : redServices)
				{
					frames.addFrame(new Frame("SERVICE "+string+" UNAVAILABLE", redIcon));
				}

				for (String string : yellowServices)
				{
					frames.addFrame(new Frame("SERVICE "+string+" UNSTABLE", yellowIcon));
				}

				if (greenServices.size() > 0)
				    frames.addFrame(new Frame("ALL OTHER SERVICES AVAILABLE", greenIcon));

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
