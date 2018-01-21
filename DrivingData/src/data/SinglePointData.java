/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author takagi masaya
 */
public class SinglePointData {

    public int ID;
    public String day;//yyyy-mm-dd
    public String time;//hh:mm:ss
    public LocalDateTime date;
    public GPSPosition pos;
    public double speed;//km/s
    public double angle;//°
    public double distance;//m
    public int turnSta;//0:通常 1:左折開始 2:左折中 3:左折終了 -1:右折開始 -2:右折中 -3:右折終了

    public SinglePointData(int ID, String day, String time, GPSPosition pos, double speed) {
        this.ID = ID;
        this.day = day;
        this.time = time;
        this.pos = pos;
        this.speed = speed;
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        this.date = LocalDateTime.parse(day +" "+ time, dtf);

    }
    
    public SinglePointData(int ID, String day, String time, GPSPosition pos, double speed,double angle,double distance) {
        this.ID = ID;
        this.day = day;
        this.time = time;
        this.pos = pos;
        this.speed = speed;
        this.angle = angle;
        this.distance = distance;

    }
    
    public void setDifferenceValue(double angle,double distance){
        this.angle = angle;
        this.distance = distance;
    }
    
    public void setTurnSta(int turnSta){
        this.turnSta = turnSta;
    }

    public int getID() {
        return this.ID;
    }

    public String getDay() {
        return this.day;
    }

    public String getTime() {
        return this.time;
    }

    public GPSPosition getPosition() {
        return this.pos;
    }

    public double getSpeed() {
        return this.speed;
    }
    
    public double[] getDifferenceValue(){
        double[] differenceValue = {this.angle, this.distance};
        return differenceValue;
    }
    
    public int getTurnSta() {
        return this.turnSta;
    }
    
    public String writeOut(){
        String strID = String.valueOf(this.ID);
        String strDay = this.day;
        String strTime = this.time;
        String strLat = this.pos.get緯度();
        String strLng = this.pos.get経度();
        String strHeight = this.pos.get高度();
        String strSpeed = String.valueOf(this.speed);
        String strAngle = String.valueOf(this.angle);
        String strDistance = String.valueOf(this.distance);
        String strTurnSta = String.valueOf(this.turnSta);
        
        return strID+"\t"+strDay+"\t"+strTime+"\t"+strLat+"\t"+strLng+"\t"+strHeight+"\t"+strSpeed+"\t"+strAngle+"\t"+strDistance+"\t"+strTurnSta;
    }

}
