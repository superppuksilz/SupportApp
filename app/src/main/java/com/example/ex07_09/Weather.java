package com.example.ex07_09;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by RENESYS on 2016-05-13.
 */
public class Weather {

    String line = "a";
    double lon;
    double lat;
    private Location loca;

    public Weather(){

    }

    public void setURL() {
        try {
            
            URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?lon=127&lat=37&APPID=b98538e3e4195e5390a18fbb31a2d57e");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            InputStream in = new BufferedInputStream(conn.getInputStream());
            JSONObject json = new JSONObject(getStringFromInputStream(in));

            JSONObject j = getForecast(json);
            line = j.getJSONArray("weather").getJSONObject(0).getString("main");
            System.out.println(line);

        } catch (MalformedURLException e) {
            System.out.println("URL ERROR");
            e.printStackTrace();
        }catch(IOException e){
            System.out.println("IO ERROR");
            e.printStackTrace();
        }catch(Exception e){
            System.out.println("ERROR");
            e.printStackTrace();
        }
    }

    public String getLine(){
        return line;
    }

    //get longitude & latitude from network base
    private void getCoordinate(double lon, double lat, LocationManager lm){
        try {
            loca = lm.getLastKnownLocation(lm.NETWORK_PROVIDER);
            lon = loca.getLongitude();
            lat = loca.getLatitude();
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    //return the correct JSONObject based on current time
    private JSONObject getForecast(JSONObject json) {
        JSONObject j = new JSONObject();
        String currentTime = getTime();
        try {
                for(int i = 0; ; i++){
                    j = json.getJSONArray("list").getJSONObject(i);
                    String time = j.getString("dt_txt");
                    if(timeProfit(currentTime, time))
                        break;
                }


        }catch(Exception e){

        }
        return j;
    }

    //make current time string
    private String getTime(){
        String format = new String("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.KOREA);
        return sdf.format(new Date());
    }

    //determine whether time is correct
    private boolean timeProfit(String origin, String compare){
        String[] tOrigin = origin.split("[- :]");
        String[] tCompare = compare.split("[- :]");
        int lOrigin = Integer.parseInt(tOrigin[1])*31*24 + Integer.parseInt(tOrigin[2])*24
                + Integer.parseInt(tOrigin[3]);
        int lCompare = Integer.parseInt(tCompare[1])*31*24 + Integer.parseInt(tCompare[2])*24
                + Integer.parseInt(tCompare[3]);
        return lOrigin < lCompare && lOrigin + 3 >= lCompare;
    }

    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
