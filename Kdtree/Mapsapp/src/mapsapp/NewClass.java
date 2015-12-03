/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapsapp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;

/**
 *
 * @author mettu
 */

public class Mapsapp {

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
    //////////// for rsearch //////////////
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

    static void findKNN(double[] Qpoint, Node root, int k) {
        PriorityQueue < Double > pq = new PriorityQueue < Double > (10, Collections.reverseOrder());
        HashMap < Double, double[] > hm = new HashMap();
        searchKDSubtree(pq, hm, root, Qpoint, k, 0);
        System.out.println(pq.size());
        while (pq.size() != 0) {
            double[] ans = hm.get(pq.poll());
            System.out.println(ans[0] + " " + ans[1]);
            System.out.println(pq.size());
        }
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
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Node root = new Node();
        root = null;
        try {
            FileInputStream in = new FileInputStream("C:\\Users\\mettu\\Documents\\NetBeansProjects\\Mapsapp\\location.txt");
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

//        Vector v = new Vector();
     //   double point1[] = {
       //     45.6681598, -121.8964710
        //};
        //do/uble point2[] = {
          //  31.9451492, -86.7060887
        //};
    //    try {
      //      v = range(point2, point1, root);
        //    System.out.println(v.size());
    //    } catch (Exception e) {
      //      System.out.println(e);//
       // }
        //findKNN(point1,root,10);

      //  findKNN(point2, root, 10);

        //if (search(root, point1))
          //  System.out.println("Found\n");
    //    else
      //      System.out.println("Not Found\n");

        //double point3[] = {
          //  12, 19
       // };
        //if (search(root, point2))
          //  System.out.println("Found\n");
 //       else
   //         System.out.println("Not Found\n");

    }
}