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
import java.nio.file.FileSystems;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.queue.CircularFifoQueue;

/*
 * RESTful-Lametric
 *
 * Author: GamerKing195
 *
 * License: GNU General Public License
 *
 * Tl;Dr for GNU License,
 *
 * Take the code do what you want but you have to credit me and use the same license.
 *
 */
public class RestfulWriter
{
    private String filePath = System.getProperty("user.home") + FileSystems.getDefault().getSeparator() + "resources";

    private String greenIcon = "a3307";
    private String redIcon = "a3305";
    private String pigIcon = "i2767";
    private String mojangIcon = "i5848";

    //Create a FIFO queue with a maximum of 7 entries.
    private CircularFifoQueue<Integer> queue = new CircularFifoQueue<>(7);

    private static boolean debug = true;

    private Frame mineswineLaunch = new Frame("MINESWINE INFORMATION", pigIcon);
    private Frame mojangLaunch = new Frame("MOJANG STATUS", mojangIcon);

    private int mineswineOfflineDuration = 0;

    private HashMap<String, Integer> serviceDowntimeMap = new HashMap<>();

    public static void main(String[] args)
    {
        RestfulWriter restful = new RestfulWriter();

        new Timer().scheduleAtFixedRate(restful.new RefreshTask(), 0L, debug ? 30 * 1000L : 60 * 1000L);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
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

        String serverInfo;

        //If these arent working try checking http vs https.
        String mineswineIp = "play.mineswine.com";
        try {
            serverInfo = readFrom("https://us.mc-api.net/v3/server/ping/" + mineswineIp + "/csv");
        }
        catch (IOException e) {
            e.printStackTrace();

            try {
                serverInfo = readFrom("https://eu.mc-api.net/v3/server/ping/" + mineswineIp + "/csv");
            }
            catch(IOException ex) {
                ex.printStackTrace();

                serverInfo = "ping-failed";
            }
        }

        String[] split = serverInfo.split(",");

        if (debug) {
            System.out.println("INFO = " + serverInfo);
            System.out.println("SPLIT-SIZE = " + split.length);
        }

        if (serverInfo.equalsIgnoreCase("null")) {
            FrameWrapper frames = new FrameWrapper(new ArrayList<>());
            frames.addFrame(mineswineLaunch);
            frames.addFrame(new Frame("STATUS: Offline", redIcon));
            frames.addFrame(new Frame("ERROR: MC-API Issues/Text parsing failed ERROR #1.", redIcon));

            return gson.toJson(frames);
        } else if (serverInfo.equalsIgnoreCase("ping-failed")) {
            FrameWrapper frames = new FrameWrapper(new ArrayList<>());
            frames.addFrame(mineswineLaunch);
            frames.addFrame(new Frame("STATUS: Offline", redIcon));
            frames.addFrame(new Frame("ERROR: MC-API Issues/Text parsing failed ERROR #2.", redIcon));

            return gson.toJson(frames);
        } else if (serverInfo.length() == 0) {
            FrameWrapper frames = new FrameWrapper(new ArrayList<>());
            frames.addFrame(mineswineLaunch);
            frames.addFrame(new Frame("STATUS: Offline", redIcon));
            frames.addFrame(new Frame("ERROR: MC-API Issues/Text parsing failed ERROR #3.", redIcon));

            return gson.toJson(frames);
        } else if (split.length < 4) {
            FrameWrapper frames = new FrameWrapper(new ArrayList<>());
            frames.addFrame(mineswineLaunch);
            frames.addFrame(new Frame("STATUS: Offline", redIcon));
            frames.addFrame(new Frame("ERROR: MC-API Issues/Text parsing failed ERROR #4.", redIcon));

            return gson.toJson(frames);
        }

        boolean online = Boolean.valueOf(split[0]);

        if (!online)
        {
            mineswineOfflineDuration++;
            FrameWrapper frames = new FrameWrapper(new ArrayList<>());
            frames.addFrame(mineswineLaunch);
            if (mineswineOfflineDuration == 1)
                frames.addFrame(new Frame("STATUS: OFFLINE FOR "+getTimeFancy(mineswineOfflineDuration), redIcon));
            else
                frames.addFrame(new Frame("STATUS: OFFLINE FOR "+getTimeFancy(mineswineOfflineDuration), redIcon));

            return gson.toJson(frames);
        }

        Integer maxCount = Integer.valueOf(split[2]);
        Integer playerCount = Integer.valueOf(split[1]);

        //if the latest entry is the current player count don't log it, only log differences.
        if (queue.size() == 0 || !queue.get(queue.size()-1).equals(playerCount))
            queue.add(playerCount);

        FrameWrapper frames = new FrameWrapper(new ArrayList<>());
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
            String statusCheck;

            if (debug)
                statusCheck = "[{\"minecraft.net\":\"green\"},{\"session.minecraft.net\":\"green\"},{\"account.mojang.com\":\"green\"},{\"auth.mojang.com\":\"green\"},{\"skins.minecraft.net\":\"green\"},{\"authserver.mojang.com\":\"yellow\"},{\"sessionserver.mojang.com\":\"red\"},{\"api.mojang.com\":\"green\"},{\"textures.minecraft.net\":\"green\"},{\"mojang.com\":\"green\"}]";
            else
                statusCheck = readFrom("https://status.mojang.com/check");


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
                FrameWrapper frames = new FrameWrapper(new ArrayList<>());
                frames.addFrame(mojangLaunch);
                frames.addFrame(new Frame("ALL SERVICES AVAILABLE", greenIcon));

                serviceDowntimeMap.clear();

                return gson.toJson(frames);
            }
            else
            {
                FrameWrapper frames = new FrameWrapper(new ArrayList<>());
                frames.addFrame(mojangLaunch);

                if (debug) {
                    System.out.println("RED SERVICES SIZE = " + redServices.size());
                    System.out.println("YELLOW SERVICES SIZE = " + yellowServices.size());
                    System.out.println("GREEN SERVICES SIZE = " + greenServices.size());
                }

                for (String string : redServices)
                {
                    if (serviceDowntimeMap.containsKey(string))
                        serviceDowntimeMap.put(string, serviceDowntimeMap.get(string)+1);
                    else
                        serviceDowntimeMap.put(string, 1);


                    frames.addFrame(new Frame("SERVICE "+string.toUpperCase()+" UNAVAILABLE FOR "+getTimeFancy(serviceDowntimeMap.get(string)), redIcon));
                }

                for (String string : yellowServices)
                {
                    if (serviceDowntimeMap.containsKey(string))
                        serviceDowntimeMap.put(string, serviceDowntimeMap.get(string)+1);
                    else
                        serviceDowntimeMap.put(string, 1);

                    String yellowIcon = "a3273";
                    frames.addFrame(new Frame("SERVICE "+string.toUpperCase()+" UNSTABLE FOR "+getTimeFancy(serviceDowntimeMap.get(string)), yellowIcon));
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

        FrameWrapper frames = new FrameWrapper(new ArrayList<>());
        frames.addFrame(mojangLaunch);
        frames.addFrame(new Frame("ERROR: Failed to parse json.", redIcon));

        return gson.toJson(frames);
    }

    private static String readFrom(String url) throws IOException {
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

    private String getTimeFancy(int minutes) {
        if (minutes == 0)
            return "NULL";

        int days = minutes / (60*24);

        minutes %= (60*24);

        if (debug)
            System.out.println("MINUTES = "+minutes);

        int hours = minutes / 60;

        minutes %= 60;

        if (debug)
            System.out.println("MINUTES 2 = "+minutes);

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append(days > 1 ? " DAYS" : " DAY");

            if (hours == 0 && minutes == 0)
                sb.append(".");
            else
                sb.append(", ");
        }

        if (hours > 0) {
            sb.append(hours).append(hours > 1 ? " HOURS" : " HOUR");

            if (minutes == 0)
                sb.append(".");
            else
                sb.append(", AND ");
        }

        if (minutes > 0) {
            sb.append(minutes).append(minutes > 1 ? " MINUTES." : " MINUTE.");
        }

        return sb.toString();
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
