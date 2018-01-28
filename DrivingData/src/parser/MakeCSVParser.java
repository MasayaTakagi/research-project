/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import data.FullPointData;
import data.GPSPosition;
import data.SinglePointData;
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
public class MakeCSVParser implements LogParser {

    public static final Pattern DATA_PATTARN = Pattern.compile("([0-9]+)\t([0-9\\-]+)\t([0-9\\:]+)\t([0-9\\.]+)\t([0-9\\.]+)\t([0-9\\.]+)\t([0-9\\.]+)\t([a-z]+)");

    @Override
    public void parseLog(File inputFile, File outputFile) throws IOException {
        /**実際に変換するメソッド*/     
        //データ部分を読み込み
        FullPointData fullGPSData = new FullPointData();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
        int data1_id = 1;
        String Line;
        
        while ((Line = reader.readLine()) != null) {
            if (Line.startsWith("//")) {
                //何もしない
            } else {
                //データサンプル(タブ区切り)                
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

        reader.close();

        //プログラムの出力
        fullGPSData.calculateAllDifferenceValue();
        fullGPSData.cheakTurning();
        
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
        fullGPSData.writeOutAll(outputFile);
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
