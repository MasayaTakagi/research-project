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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ktajima
 */
public class GazeCheakParser implements LogParser {
        
    @Override
    public void parseLog(File inputFile, File outputFile) throws IOException {
        //実際に変換するメソッド
        String Line;       

        //データ部分を読み込み
        FullGazeData fullGazeData = new FullGazeData();

        BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
        
        int data2_id = 1;
        
        while ((Line = reader2.readLine()) != null) {
            if (Line.startsWith("//")) {
                //何もしない
            } else {
                //データサンプル(カンマ区切り)                
                //903,179,01:18:12
                String[] data =  Line.split(Pattern.quote(","));
                if (data.length == 3 ) {
                    int x = Integer.parseInt(data[0]);
                    int y = Integer.parseInt(data[1]);          
                    String time = data[2];
                    
                    //位置クラス（１つの点を表す）
                    SingleGazeData Data = new SingleGazeData(data2_id, x, y, time);
                    fullGazeData.addSingleGazeData(Data);
                    data2_id++;
                }
            }
        }
  
        reader2.close();
        
        ArrayList<LocalTime> gazeCheakList = fullGazeData.cheakSide();
        System.out.println(gazeCheakList);
        
    }
    
        @Override
    public String getParserName() {
        return "GazeCheakParser";
    }

    @Override
    public void setTimeZone(int plusGMT) {
        return;
    }

}
