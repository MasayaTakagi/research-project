/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import data.FullPointData;
import data.GPSPosition;
import data.SinglePointData;
import data.SingleGazeData;
import data.FullGazeData;
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
public class MakeHTMLParser implements LogParser {

    @Override
    public void parseLog(File inputFile, File outputFile) throws IOException {
        //実際に変換するメソッド
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
        String Line;
        
        //header.txtを読み込んで出力
        BufferedReader headerreader = new BufferedReader(new InputStreamReader(new FileInputStream("haeder.txt"), "UTF-8"));
        while ((Line = headerreader.readLine()) != null) {
            writer.println(Line);
        }
        headerreader.close();

        //データ部分を読み込み
        FullPointData fullGPSData = new FullPointData();
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
        int data1_id = 1;

        while ((Line = reader1.readLine()) != null) {
            if (Line.startsWith("//")) {
                //何もしない
            } else {
                //データサンプル(カンマ区切り)                
                //11,2018-01-23,15:27:41,35.40945,136.57719,0.300,26.0,false
                String[] data = Line.split(Pattern.quote(","));
                int ID = data1_id;
                String day = data[1];
                String time = data[2];
                double lat = Double.parseDouble(data[3]);
                double lng = Double.parseDouble(data[4]);
                double speed = Double.parseDouble(data[5]);
                double hight = Double.parseDouble(data[6]);
                double[] posdouble = {lat, lng, hight};
                //位置クラス（１つの点を表す）
                GPSPosition pos = GPSPosition.parseFromDouble(posdouble);

                SinglePointData posData = new SinglePointData(ID, day, time, pos, speed);
                fullGPSData.addSinglePointData(posData);
                data1_id++;
            }
        }
        reader1.close();
        

        //最後の点を取得
        GPSPosition lastPos = null;
        if (fullGPSData.getDataSize() > 0) {
            lastPos = fullGPSData.getLastPosition();
        }

        fullGPSData.calculateAllDifferenceValue();
        fullGPSData.cheakTurning();
        ArrayList<ArrayList<SinglePointData>> dataList = fullGPSData.makeDataList();

        //プログラムの出力
        writer.println("      var mapOptions = {");
        writer.println("          zoom: 15,");
        if (lastPos != null) {
            double[] posdouble = lastPos.getPositonByDoubleDegreeValue();
            writer.println("          center: new google.maps.LatLng(" + posdouble[0] + "," + posdouble[1] + "),");
        } else {
            writer.println("          center: new google.maps.LatLng(0, -180),");
        }
        writer.println("          mapTypeId: google.maps.MapTypeId.ROADMAP");
        writer.println("        };");
        writer.println("      map = new google.maps.Map(document.getElementById('map_canvas'),mapOptions);");

        if (dataList.size() > 0) {
            int i = 0;
            for (ArrayList<SinglePointData> currentList : dataList) {
                i++;
                if (currentList.size() > 0) {
                    writer.println("        var line" + i + " = [");
                    for (SinglePointData posData1 : currentList) {
                        double[] posdouble = posData1.getPosition().getPositonByDoubleDegreeValue();
                        writer.println("            new google.maps.LatLng(" + posdouble[0] + "," + posdouble[1] + "),");

                    }
                    writer.println("        ];");
                    writer.println("         var line" + i + "Path = new google.maps.Polyline({");
                    writer.println("          path: line" + i + ",");
                    if (currentList.get(0).getTurnSta() == 0) {
                        writer.println("          strokeColor: '#00b3fd',");
                    } else if (currentList.get(0).getTurnSta() > 0) {
                        writer.println("          strokeColor: '#ff9e00',");
                    } else if (currentList.get(0).getTurnSta() < 0) {
                        writer.println("          strokeColor: '#ff0000',");
                    }
                }
                writer.println("          strokeOpacity: 1.0,");
                writer.println("          strokeWeight: 5");
                writer.println("        });");
                writer.println("        line" + i + "Path.setMap(map);");
            }
        }
        

//fooder.txtを読み込んで出力
        BufferedReader fooderreader = new BufferedReader(new InputStreamReader(new FileInputStream("fooder.txt"), "UTF-8"));
        while ((Line = fooderreader.readLine()) != null) {
            writer.println(Line);
        }

        fooderreader.close();

        writer.close();
    }
    
        @Override
    public String getParserName() {
        return "TurnCheakCheak";
    }

    @Override
    public void setTimeZone(int plusGMT) {
        return;
    }

}
