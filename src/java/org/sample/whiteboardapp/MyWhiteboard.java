/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sample.whiteboardapp;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 *
 * @author nirmit2810
 */
@ServerEndpoint(value="/whiteboardendpoint", encoders = {FigureEncoder.class}, decoders = {FigureDecoder.class})
public class MyWhiteboard {
    
    private static LinkedHashMap<Area, Double> map = new LinkedHashMap<Area, Double>();

    @OnMessage
    public void broadcastFigure(Figure figure, Session session) throws IOException, EncodeException {
        System.out.println("broadcastFigure: " + figure);
       try {
            FileInputStream in = new FileInputStream("C:\\Users\\Nirmit Shah\\Desktop\\mapsapp_ec504\\location.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            //sample values
            double latitude =  Math.abs(figure.getJson().getJsonObject("coords").getJsonNumber("Latitude").doubleValue());
            double longitude = Math.abs(figure.getJson().getJsonObject("coords").getJsonNumber("Longitude").doubleValue());
            
            String strLine;
            LinkedHashMap<Area, Double> newmap = new LinkedHashMap<Area, Double>();
            while ((strLine = br.readLine()) != null) {
                String[] line = strLine.split("\t");
                double lat = Math.abs(Double.valueOf(line[2])) - latitude;
                double lon = Math.abs(Double.valueOf(line[3])) - longitude;
                double radius = Math.sqrt((Math.pow(lat, 2) + (Math.pow(lon, 2))));
                if (radius != 0) {
                    if (!map.isEmpty()) {
                        Iterator it = map.entrySet().iterator();
                        Boolean found = false;
                        newmap.clear();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            if ((Double) pair.getValue() >= radius && radius != 0) {
                                found = true;
                                newmap.put(createArea(line[0], line[1], Double.valueOf(line[2]), Double.valueOf(line[3])), radius);
                                if (newmap.size() < 10) {
                                    newmap.put((Area) pair.getKey(), (Double) pair.getValue());
                                    while (it.hasNext() && newmap.size() < 10) {
                                        pair = (Map.Entry) it.next();
                                        newmap.put((Area) pair.getKey(), (Double) pair.getValue());
                                    }
                                }
                                map = (LinkedHashMap<Area, Double>) newmap.clone();
                                break;
                            } else {
                                newmap.put((Area) pair.getKey(), (Double) pair.getValue());
                            }
                        }
                        if (!found && map.size() < 10) {
                            map.put(createArea(line[0], line[1], Double.valueOf(line[2]), Double.valueOf(line[3])), radius);
                        }
                    } else {
                        map.put(createArea(line[0], line[1], Double.valueOf(line[2]), Double.valueOf(line[3])), radius);
                    }
                }
            }
            in.close();
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
              
            }
            RemoteEndpoint.Basic other = session.getBasicRemote();
            other.sendObject(figure);
        } catch (Exception e) {
            e.printStackTrace();
        }
           

    }
    private static Area createArea(String code, String name, double lat, double lo) {
        Area obj = new Area();
        obj.setCode(code);
        obj.setName(name);
        obj.setLatitude(lat);
        obj.setLongitude(lo);
        return obj;
    }    
  
    @OnOpen
    public void onOpen (Session peer) {
    
    }

    @OnClose
    public void onClose (Session peer) {
 
    }
}
