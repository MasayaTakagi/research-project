/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import data.GPSPosition;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ktajima
 */
public class CSVtoHTML implements LogParser{

    public static final Pattern DATA_PATTARN = Pattern.compile("([0-9:]+)\t([0-9\\.]+)\t([0-9\\.]+)\t([0-9]+)\t([0-9]+)\t([0-9\\.]+)");
    
    @Override
    public void parseLog(File inputFile, File outputFile) throws IOException {
        //実際に変換するメソッド
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8"));
        
        String Line;
        //header.txtを読み込んで出力
        BufferedReader headerreader = new BufferedReader(new InputStreamReader(new FileInputStream("haeder.txt"),"UTF-8"));
        while((Line = headerreader.readLine()) != null){
            writer.println(Line);
        }
        headerreader.close();

        //データ部分を読み込み
        ArrayList<ArrayList<GPSPosition>> dataList = new ArrayList<ArrayList<GPSPosition>>();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"UTF-8"));
        ArrayList<GPSPosition> currentList = new ArrayList<GPSPosition>();
        ArrayList<Integer> levelList = new ArrayList<Integer>();
        int currentlevel = 0;
        while((Line = reader.readLine()) != null){
            if(Line.startsWith("//")){
                //何もしない
            } else { 
                //データサンプル(タブ区切り)                
                //15:25:54:993	35.44704	136.67436	0	1396	0
                Matcher mc = DATA_PATTARN.matcher(Line);
                if(mc.matches()){
                    //String time = mc.group(1); // 使わない
                    double lat = Double.parseDouble(mc.group(2));
                    double lng = Double.parseDouble(mc.group(3));
                    double[] posdouble = {lat,lng,0};
                    //位置クラス（１つの点を表す）
                    GPSPosition pos = GPSPosition.parseFromDouble(posdouble);
                    
                    double speed = Double.parseDouble(mc.group(4));
                    int level = getSpeedLevel(speed);
                    if(currentlevel ==  level){
                        currentList.add(pos);
                    } else {
                        dataList.add(currentList);
                        levelList.add(currentlevel);
                        GPSPosition lastPos = null;
                        if(currentList.size() > 0){
                            lastPos = currentList.get(currentList.size()-1);
                        }
                        currentList = new ArrayList<GPSPosition>();
                        currentlevel = level;
                        if(lastPos != null){
                            currentList.add(lastPos);
                        }
                        currentList.add(pos);
                    }
                }
            }
        }
        //最後の要素を追加
        dataList.add(currentList);
        levelList.add(currentlevel);
        reader.close();       
        
        //最後の点を取得
        GPSPosition lastPos = null;
        if(currentList.size() > 0){
            lastPos = currentList.get(currentList.size()-1);
        }      
        
        //プログラムの出力
        writer.println("      var mapOptions = {");
        writer.println("          zoom: 15,");
        if(lastPos != null){
            double[] posdouble = lastPos.getPositonByDoubleDegreeValue();
            writer.println("          center: new google.maps.LatLng(" + posdouble[0] + "," + posdouble[1] + "),");
        } else {
            writer.println("          center: new google.maps.LatLng(0, -180),");
        }
        writer.println("          mapTypeId: google.maps.MapTypeId.ROADMAP");
        writer.println("        };");
        writer.println("      map = new google.maps.Map(document.getElementById('map_canvas'),mapOptions);");

        //dataListを変換
        for(int i=0;i<dataList.size();i++){
            ArrayList<GPSPosition> currentlist = dataList.get(i);
            int level = levelList.get(i);
            if(currentlist.size() > 1) {
                writer.println("        var line"+i+" = [");
                for(GPSPosition pos:currentlist){
                   double[] posdouble = pos.getPositonByDoubleDegreeValue();
                   writer.println("            new google.maps.LatLng(" + posdouble[0] + "," + posdouble[1] + "),");
     
                }
                writer.println("        ];");
                writer.println("         var line"+i+"Path = new google.maps.Polyline({");
                writer.println("          path: line"+i+",");
                switch(level){
                    case 0:
                        writer.println("          strokeColor: '#000000',");
                        break;
                    case 1:
                        writer.println("          strokeColor: '#0000FF',");
                        break;
                    case 2:
                        writer.println("          strokeColor: '#00ffff',");
                        break;
                    case 3:
                        writer.println("          strokeColor: '#7cfc00',");
                        break;
                    case 4:
                        writer.println("          strokeColor: '#ffa500',");
                        break;
                    case 5:
                        writer.println("          strokeColor: '#FF0000',");
                        break;
                    case 6:
                        writer.println("          strokeColor: '#FF0000',");
                        break;
                }
                writer.println("          strokeOpacity: 1.0,");
                writer.println("          strokeWeight: 5");
                writer.println("        });");
                writer.println("        line"+i+"Path.setMap(map);");
            }
        }
        
        //fooder.txtを読み込んで出力
        BufferedReader fooderreader = new BufferedReader(new InputStreamReader(new FileInputStream("fooder.txt"),"UTF-8"));
        while((Line = fooderreader.readLine()) != null){
            writer.println(Line);
        }
        fooderreader.close();

        writer.close();
    }

    //Throttleのレベル設定
    private int getSpeedLevel(double speed){
        if(speed < 0){
            return 0;
        } else if(speed < 10) {
            return 1;
        } else if(speed < 20) {
            return 2;
        } else if(speed < 30) {
            return 3;
        } else if(speed < 40) {
            return 4;
         } else if(speed < 50) {
            return 5;
        } else {
            return 6;
        }
    }
    
    @Override
    public String getParserName() {
        return "OBDParser";
    }

    @Override
    public void setTimeZone(int plusGMT) {
        return;
    }
    
}
