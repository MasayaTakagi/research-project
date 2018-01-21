/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.ArrayList;

/**
 *
 * @author takagi masaya
 */
public class FullGazeData {
    
    public ArrayList<SingleGazeData> dataList = new ArrayList<SingleGazeData>();
    
    private final int LEFT_LIMIT = 100; //側方確認と判断するしきい値
    
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
    
    public void cheakSide(){
        
    }
    
    
}
