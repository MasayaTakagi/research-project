/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.ArrayList;
import java.time.LocalTime;

/**
 *
 * @author takagi masaya
 */
public class FullGazeData {

    public ArrayList<SingleGazeData> dataList = new ArrayList<SingleGazeData>();

    private final int LEFT_LIMIT = 200; //側方確認と判断するしきい値
    private final int DATA_FRE = 10; //1秒あたりのデータ数
    private final double SIDE_CHEAK_TIME = 1; //側方確認と判断する秒数

    public FullGazeData() {

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

    public ArrayList<LocalTime> cheakSide() {
        ArrayList<LocalTime> sideCheakList = new ArrayList<LocalTime>();
        int status = 0;
        int count = 0;
        int beforeData = 0;
        int afterData = 0;

        for (int dataID = 1; dataID <= this.dataList.size(); dataID++) {
            int[] currentData = this.getGazePointData(dataID).getMatrix();
            if (currentData[0] == 0 && currentData[1] == 0) {
                for (int index1 = 1; index1 < 3 && dataID - index1 > 0; index1++) {
                    if (this.getGazePointData(dataID - index1).getMatrix()[0] != 0 && this.getGazePointData(dataID - index1).getMatrix()[1] != 0) {
                        beforeData = this.getGazePointData(dataID - index1).getMatrix()[0];
                        break;
                    }
                }
                for (int index2 = 1; index2 < this.dataList.size() - dataID && index2 < 3; index2++) {
                    if (this.getGazePointData(dataID + index2).getMatrix()[0] != 0 && this.getGazePointData(dataID + index2).getMatrix()[1] != 0) {
                        afterData = this.getGazePointData(dataID + index2).getMatrix()[0];
                        break;
                    }
                }
                if (beforeData != 0 && afterData != 0) {
                    currentData[0] = (beforeData + afterData) / 2;
                }
                beforeData = 0;
                afterData = 0;
            }
            if (currentData[0] != 0 || currentData[1] != 0) {
                switch (status) {
                    case 0:
                        if (currentData[0] < this.LEFT_LIMIT) {
                            count++;
                        } else if (count > 0) {
                            count--;
                        }
                        if (count > this.DATA_FRE * this.SIDE_CHEAK_TIME) {
                            sideCheakList.add(this.getGazePointData(dataID).getDate());
                            status = 1;
                        }
                        break;
                    case 1:
                        if (currentData[0] >= this.LEFT_LIMIT) {
                            count--;
                        } else if (count < this.DATA_FRE * this.SIDE_CHEAK_TIME) {
                            count++;
                        }
                        if (count <= 0) {
                            status = 0;
                        }
                        break;
                }
            }
            //System.out.println(i + "   " +currentData[0] + "    " + count);
        }
        return sideCheakList;
    }

}
