/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.Arrays;

/**
 *
 * @author takagi masaya
 */
public class NewClass {

    public static void main(String args[]) {

        GPSPosition pos1;
        double lat1 = 35.43132;
        double lng1 = 136.62603;
        double hight1 = 51.800;

        double[] posdouble1 = {lat1, lng1, hight1};
        //位置クラス（１つの点を表す）
        pos1 = GPSPosition.parseFromDouble(posdouble1);
        double[] posxyz1 = new double[3];
        posxyz1 = GPSPosition.llh2xyz(posdouble1, 1);
        
        GPSPosition pos2;
        double lat2 = 35.43131;
        double lng2 = 136.62587;
        double hight2 = 52.300;

        double[] posdouble2 = {lat2, lng2, hight2};
        //位置クラス（１つの点を表す）
        pos2 = GPSPosition.parseFromDouble(posdouble2);
        double[] posxyz2 = new double[3];
        posxyz2 = GPSPosition.llh2xyz(posdouble2, 1);
        
        double distance = Math.sqrt((posxyz2[0]-posxyz1[0])*(posxyz2[0]-posxyz1[0])+(posxyz2[1]-posxyz1[1])*(posxyz2[1]-posxyz1[1]));
        double radian = Math.atan2(posxyz2[1]-posxyz1[1],posxyz2[0]-posxyz1[0]);
        double degree = radian * 180 / Math.PI;
        
        System.out.println(Arrays.toString(posxyz1));
        System.out.println(Arrays.toString(posxyz2));
        System.out.println(distance);
        System.out.println(degree);
        
    }
}
