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
        FullPointData fullData = new FullPointData();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
        String Line;
        
        while ((Line = reader.readLine()) != null) {
            if (Line.startsWith("//")) {
                //何もしない
            } else {
                //データサンプル(タブ区切り)                
                //10	2016-12-19	15:38:09	35.43132	136.62603	51.800	33.0	false
                Matcher mc = DATA_PATTARN.matcher(Line);
                if (mc.matches()) {
                    int ID = Integer.parseInt(mc.group(1));
                    String day = mc.group(2);
                    String time = mc.group(3);
                    double lat = Double.parseDouble(mc.group(4));
                    double lng = Double.parseDouble(mc.group(5));
                    double speed = Double.parseDouble(mc.group(6));
                    double hight = Double.parseDouble(mc.group(7));
                    double[] posdouble = {lat, lng, hight};
                    //位置クラス（１つの点を表す）
                    GPSPosition pos = GPSPosition.parseFromDouble(posdouble);
                    
                    SinglePointData data = new SinglePointData(ID,day,time,pos,speed);
                    fullData.addSinglePointData(data);
                }
            }
        }

        reader.close();

        //プログラムの出力
        fullData.calculateAllDifferenceValue();
        fullData.cheakTurning();
        
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
        fullData.writeOutAll(outputFile);
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
