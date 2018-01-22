/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 *
 * @author takagi masaya
 */
public class FullGazeData {
    
    public ArrayList<SingleGazeData> dataList = new ArrayList<SingleGazeData>();
    
    private final int LEFT_LIMIT = 100; //側方確認と判断するしきい値
    private final int DATA_FRE = 10; //1秒あたりのデータ数
    private final int SIDE_CHEAK_TIME = 1; //側方確認と判断する秒数
    
    public FullGazeData(){
        
    }
    
    public FullGazeData(ArrayList dataList) {
        this.dataList = new ArrayList<>(dataList);
    }
    
    public void addSingleGazeData(SingleGazeData data) {
        this.dataList.add(data);
    }

    public SingleGazeData getGazePointData(int ID) {
        return this.dataList.get(ID - 1);
    }
    
     public int getDataSize() {
        return this.dataList.size();
    }

    public ArrayList<SingleGazeData> getList() {
        return this.dataList;
    }
    
    public ArrayList<LocalTime> cheakSide(){
        ArrayList<LocalTime> sideCheakList =  new ArrayList<LocalTime>();
        int status = 0;
        for(int i = 1; i <= this.dataList.size(); i++){
            int count = 0;
            int[] currentData = this.getGazePointData(i).getMatrix();
            if(currentData[0] < this.LEFT_LIMIT){
                count++;
            }else if(count > 0){
                count--;
            }
            if(count > this.LEFT_LIMIT * this.SIDE_CHEAK_TIME && status == 0){
                sideCheakList.add(this.getGazePointData(i).getTime());
                status = 1;
            }
            if(count <= 0&& status == 1){
                status = 0;
            }   
        }
        return sideCheakList;
    }
    
    
}
