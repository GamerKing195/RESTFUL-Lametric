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
import org.apache.commons.collections4.queue.CircularFifoQueue;

/*
 * Created by GamerKing195 on
 */
public class RestfulWriter
{
    private String filePath = System.getProperty("user.dir")+"/resources";

    private String mineswineIp = "play.mineswine.com";

    private String greenIcon = "a3307";
    private String yellowIcon = "a3273";
    private String redIcon = "a3305";
    private String pigIcon = "i2767";
    private String mojangIcon = "i5848";

    //Create a FIFO queue with a maximum of 7 entries.
    private CircularFifoQueue<Integer> queue = new CircularFifoQueue<>(7);

    private static boolean debug = false;

    private Frame mineswineLaunch = new Frame("MINESWINE INFORMATION", pigIcon);
    private Frame mojangLaunch = new Frame("MOJANG STATUS", mojangIcon);

    private int mineswineOfflineDuration = 0;

    public static void main(String[] args)
    {
        RestfulWriter restful = new RestfulWriter();

        new Timer().scheduleAtFixedRate(restful.new RefreshTask(), 0L, debug ? 30 * 1000L : 60 * 1000L);
    }

    private void createFiles()
    {
        if (debug) {
            System.out.println("");
            System.out.println("=================BEGIN DEBUG=================");
            System.out.println("");
        }
        File subDirectories = new File(filePath);
        subDirectories.mkdirs();

        //MINESWINE APP
        File msFile = new File(filePath+"/mineswineapp.json");

        if (msFile.exists())
            msFile.delete();

        try
        {
            msFile.createNewFile();

            msFile.setWritable(true);
            msFile.setReadable(true);
            FileWriter writer = new FileWriter(msFile);

            String mineswineStatus = getMineswineApp();

            if (debug) {
                System.out.println("MINESWINE: ");
                System.out.println(mineswineStatus);
            }

            writer.write(mineswineStatus);
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (debug) {
            System.out.println("");
        }

        //MOJANG APP
        File mcFile = new File(filePath+"/mojangapp.json");

        if (mcFile.exists())
            mcFile.delete();

        try
        {
            mcFile.createNewFile();

            mcFile.setWritable(true);
            mcFile.setReadable(true);

            FileWriter writer = new FileWriter(mcFile);

            if (debug)
                System.out.println("SERVICES: ");

            String mojangStatus = getMojangApp();

            if (debug) {
                System.out.println("MOJANG: ");
                System.out.println(mojangStatus);
            }

            writer.write(mojangStatus);
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        if (debug) {
            System.out.println("");
            System.out.println("=================END DEBUG=================");
        }
    }

    private String getMineswineApp()
    {
        Gson gson = new Gson();

        String serverInfo = "NULL";

        try {
            serverInfo = readFrom("https://us.mc-api.net/v3/server/ping/" + mineswineIp + "/csv");
        }
        catch (IOException e) {
            try {
                serverInfo = readFrom("https://eu.mc-api.net/v3/server/ping/" + mineswineIp + "/csv");
            }
            catch(IOException ex) {
                serverInfo = "ping-failed";
            }
        }

        String[] split = serverInfo.split(",");

        if (split.length < 1) {
            split = serverInfo.split(",");
            if (split.length < 1 || serverInfo.equals("NULL")) {
                FrameWrapper frames = new FrameWrapper(new ArrayList<Frame>());
                frames.addFrame(mineswineLaunch);
                frames.addFrame(new Frame("STATUS: Offline", redIcon));
                frames.addFrame(new Frame("ERROR: MC-API Issues/Text parsing failed.", redIcon));

                return gson.toJson(frames);
            }
        }

        boolean online = Boolean.valueOf(split[0]);

        if (!online)
        {
            mineswineOfflineDuration++;
            FrameWrapper frames = new FrameWrapper(new ArrayList<Frame>());
            frames.addFrame(mineswineLaunch);
            frames.addFrame(new Frame("STATUS: Offline", redIcon));
            if (mineswineOfflineDuration == 1)
                frames.addFrame(new Frame("OFFLINE FOR "+mineswineOfflineDuration+" MINUTE.", redIcon));
            else
                frames.addFrame(new Frame("OFFLINE FOR "+mineswineOfflineDuration+" MINUTES.", redIcon));

            return gson.toJson(frames);
        }

        Integer maxCount = Integer.valueOf(split[2]);
        Integer playerCount = Integer.valueOf(split[1]);

        //if the latest entry is the current player count don't log it, only log differences.
        if (queue.size() == 0 || !queue.get(queue.size()-1).equals(playerCount))
            queue.add(playerCount);

        FrameWrapper frames = new FrameWrapper(new ArrayList<Frame>());
        frames.addFrame(mineswineLaunch);
        frames.addFrame(new Frame("STATUS: Online", greenIcon));
        frames.addFrame(new Frame("PLAYERS: "+playerCount+"/"+maxCount, pigIcon));
        frames.addFrame(new Frame(frames.getList().size(), queue.toArray(new Integer[queue.size()])));

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

                    if (debug)
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

                if (debug) {
                    System.out.println("RED SERVICES SIZE = " + redServices.size());
                    System.out.println("YELLOW SERVICES SIZE = " + yellowServices.size());
                    System.out.println("GREEN SERVICES SIZE = " + greenServices.size());
                }

                for (String string : redServices)
                {
                    frames.addFrame(new Frame("SERVICE "+string.toUpperCase()+" UNAVAILABLE", redIcon));
                }

                for (String string : yellowServices)
                {
                    frames.addFrame(new Frame("SERVICE "+string.toUpperCase()+" UNSTABLE", yellowIcon));
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

    private String readFrom(String url) throws IOException
    {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }
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
