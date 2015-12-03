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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Vector;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import javax.json.Json;
import javax.json.JsonObject;



/**
 *
 * @author nirmit2810
 */

@ServerEndpoint(value="/whiteboardendpoint", encoders = {FigureEncoder.class}, decoders = {FigureDecoder.class})

public class MyWhiteboard {
    
     Node root = new Node();
    private static LinkedHashMap<Area, Double> map = new LinkedHashMap<Area, Double>();
    public static class Node {
        Object v;
        protected HPoint k;
        private double point[];
        private Node left;
        private Node right;
        public Node() {
            point = new double[2];
            left = null;
            right = null;
        }
        public Node(double[] point, Node left, Node right) {
            this.point = point;
            this.left = left;
            this.right = right;
        }
    }
    
    public static class HPoint {

        protected double[] coord;

        protected HPoint(int n) {
            coord = new double[n];
        }

        public HPoint(double[] x) {

            coord = new double[2];
            for (int i = 0; i < 2; ++i)
                coord[i] = x[i];
        }
    }

    public static class KeySizeException extends Exception {

        protected KeySizeException() {
            super("Key size mismatch");
        }

        // arbitrary; every serializable class has to have one of these
        public static final long serialVersionUID = 2L;

    }
    ///////***********////////////////

    static Node newNode(double arr[]) {
        Node temp = new Node();
        temp.point[0] = arr[0];
        temp.point[1] = arr[1];
        return temp;
    }

    static Node insertrec(Node root, double point[], int depth) {
        //int cd = depth % 2;
        if (root == null) {
            return newNode(point);
        }
        if (depth == 0) {
            if (point[0] < root.point[0]) {
                root.left = insertrec(root.left, point, 1 - depth);
            } else if (point[0] > root.point[0]) {
                root.right = insertrec(root.right, point, 1 - depth);
            } else {
                return root;
            }
        } else { // y level
            if (point[1] < root.point[1]) {
                root.left = insertrec(root.left, point, 1 - depth);
            } else if (point[1] > root.point[1]) {
                root.right = insertrec(root.right, point, 1 - depth);
            } else {
                return root;
            }
        }
        return root;
    }

    static Node insert(Node root, double point[]) {
        return insertrec(root, point, 0);
    }

    static boolean arePointsSame(double point1[], double point2[]) {
        // Compare individual pointinate values
        for (int i = 0; i < 2; ++i)
            if (point1[i] != point2[i])
                return false;
        return true;
    }

    static boolean Searchrec(Node root, double point[], int depth) {
        if (root == null)
            return false;
        if (arePointsSame(root.point, point))
            return true;
        if (depth == 0) {
            if (point[0] < root.point[0]) {
                return Searchrec(root.left, point, 1 - depth);
            } else if (point[0] > root.point[0]) {
                return Searchrec(root.right, point, 1 - depth);
            }
        } else { // y level
            if (point[1] < root.point[1]) {
                return Searchrec(root.left, point, 1 - depth);
            } else if (point[1] > root.point[1]) {
                return Searchrec(root.right, point, 1 - depth);
            }
        }
        return true;
    }

    static boolean search(Node root, double point[]) {
        return Searchrec(root, point, 0);
    }

    static double Distance(double[] point1, double[] point2) {
        double x1 = Math.toRadians(point1[0]);
        double x2 = Math.toRadians(point2[0]);
        double y1 = Math.toRadians(point1[1]);
        double y2 = Math.toRadians(point2[1]);
        double x = (y2 - y1) * Math.cos((x1 + x2) / 2);
        double y = x2 - x1;
        double D = Math.sqrt((x * x) + (y * y)) * 6371000; // meters
        return D;
    }

    static void searchKDSubtree(PriorityQueue < Double > pq, HashMap < Double, double[] > hm, Node root, double[] Qpoint, int k, int depth) {
        Node child = null;
        int dim = depth;
        double dist = Distance(Qpoint, root.point);

        if (pq.size() < k) {
            pq.add(dist);
            hm.put(dist, root.point);
        } else if (dist < pq.peek()) {
            pq.poll();
            pq.add(dist);
            hm.put(dist, root.point);
        }
        if (Qpoint[dim] < root.point[dim]) {
            if (root.left != null) {
                searchKDSubtree(pq, hm, root.left, Qpoint, k, (depth + 1) % 2);
                child = root.right;
            }
        } else {
            if (root.right != null) {
                searchKDSubtree(pq, hm, root.right, Qpoint, k, (depth + 1) % 2);
                child = root.left;
            }
        }
        if ((pq.size() < k || (Qpoint[dim] - root.point[dim]) < pq.peek()) && child != null) {
            searchKDSubtree(pq, hm, child, Qpoint, k, (depth + 1) % 2);
        }
    }

    static JSONObject findKNN(double[] Qpoint, Node root, int k) {
        JSONObject coordinates = new JSONObject();
        JSONArray lat_json = new JSONArray();
        JSONArray long_json = new JSONArray();
        PriorityQueue < Double > pq = new PriorityQueue < Double > (10, Collections.reverseOrder());
        HashMap < Double, double[] > hm = new HashMap();
        searchKDSubtree(pq, hm, root, Qpoint, k, 0);
        System.out.println(pq.size());
        while (pq.size() != 0) {
            double[] ans = hm.get(pq.poll());
            System.out.println(ans[0] + " " + ans[1]);
            System.out.println(pq.size());
            lat_json.add(ans[0]);
            long_json.add(ans[1]);                
            
        }
        coordinates.put("latitude", lat_json);
        coordinates.put("longitude", long_json);
        return coordinates;
        
    }

    ////////////////// ************ Rectangular Search ************ //////////////////
    static void rsearch(double[] lowk, double[] uppk, Node t, Vector < Node > v) {

        if (t == null)
            return;
        if (t.point[0] >= lowk[0] && t.point[0] <= uppk[0] && t.point[1] >= uppk[1] && t.point[1] <= lowk[1]) {
            v.addElement(t);
        }
        rsearch(lowk, uppk, t.left, v);
        rsearch(lowk, uppk, t.right, v);
    }

    static Vector < Node > range(double[] lowk, double[] uppk, Node root) throws KeySizeException {

        if (lowk.length != uppk.length) {
            throw new KeySizeException();
        } else if (lowk.length != 2) {
            throw new KeySizeException();
        } else {
            Vector < Node > v = new Vector();
            rsearch(lowk, uppk, root, v);
            return v;
        }
    }

    @OnMessage
    public void broadcastFigure(Figure figure, Session session) throws IOException, EncodeException {
        System.out.println("broadcastFigure: " + figure);
        JSONObject coordinates = new JSONObject();
        JSONArray lat_json = new JSONArray();
        JSONArray long_json = new JSONArray();
       try {
         /*   FileInputStream in = new FileInputStream("C:\\Users\\Nirmit Shah\\Desktop\\mapsapp_ec504\\location.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));*/
            //sample values
            try
            {
                double latitude =  figure.getJson().getJsonObject("coords").getJsonNumber("Latitude").doubleValue();
                double longitude = figure.getJson().getJsonObject("coords").getJsonNumber("Longitude").doubleValue();
                double[] coord = {latitude, longitude};
                coordinates = findKNN(coord,root,1000);
            }
            catch (NullPointerException e)
            { 
                double max_lat = figure.getJson().getJsonObject("coords").getJsonObject("North_east").getJsonNumber("lat").doubleValue();
                double max_lng = figure.getJson().getJsonObject("coords").getJsonObject("North_east").getJsonNumber("lng").doubleValue();
                double min_lat = figure.getJson().getJsonObject("coords").getJsonObject("South_west").getJsonNumber("lat").doubleValue();
                double min_lng = figure.getJson().getJsonObject("coords").getJsonObject("South_west").getJsonNumber("lng").doubleValue();
                double[] max = {max_lat, min_lng};
                double[] min = {min_lat, max_lng}; 
                System.out.println(max_lat + " " +min_lng);
                Vector<Node> v = new Vector();
                rsearch(min, max, root, v);
                for(int i=0; i < v.size(); i++)
                {
                    Node x = v.get(i);
                   // System.out.println(x.point[0] + " "+ x.point[1]);
                    lat_json.add(x.point[0]);
                    long_json.add(x.point[1]);                
                }
                coordinates.put("latitude", lat_json);
                coordinates.put("longitude", long_json);
            }
         /*   String strLine;
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
                String[] latlong = pair.getKey().toString().split(":") ;
                String lat1 = latlong[1];
                String long1 = latlong[3];
                lat_json.add(lat1);
                long_json.add(long1);                
            }
            coordinates.put("latitude", lat_json);
            coordinates.put("longitude", long_json);*/
            figure.setJson(Json.createReader(new StringReader(coordinates.toString())).readObject());
            RemoteEndpoint.Basic other = session.getBasicRemote();
            other.sendObject(figure);
            System.out.println("sent");
         } catch (Exception e) {
            System.out.println("here");
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
       
        root = null;
        try {
            FileInputStream in = new FileInputStream("C:\\Users\\Nirmit Shah\\Desktop\\mapsapp_ec504\\location.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader( in ));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] line = strLine.split("\t");
                double lat = Double.valueOf(line[2]);
                double lan = Double.valueOf(line[3]);
                double[] point = {
                    lat, lan
                };
                //System.out.println(point[1]);
                root = insert(root, point);
            } in .close();
        } catch (Exception e) {
            e.printStackTrace();
        }
             
    }

    @OnClose
    public void onClose (Session peer) {
 
    }
}
