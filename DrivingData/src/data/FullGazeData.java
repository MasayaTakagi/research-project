/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.time.LocalTime;

/**
 *
 * @author takagi masaya
 */
public class FullGazeData {

    public ArrayList<SingleGazeData> dataList = new ArrayList<SingleGazeData>();

    private final int LEFT_LIMIT = 300; //側方確認と判断するしきい値
    private final int CHEAK_DATA_FRE = 4;

    public FullGazeData() {

    }

    public FullGazeData(ArrayList dataList) {
        this.dataList = new ArrayList<>(dataList);
    }

    public void addSingleGazeData(SingleGazeData data) {
        this.dataList.add(data);
    }

    public SingleGazeData getSingleGazeData(int ID) {
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
        int startID = 0;
        int finishID = 0;

        for (int dataID = 1; dataID <= this.dataList.size(); dataID++) {
            int[] currentData = this.getSingleGazeData(dataID).getMatrix();
            if (currentData[0] == 0 && currentData[1] == 0) {
                for (int index1 = 1; index1 < 3 && dataID - index1 > 0; index1++) {
                    if (this.getSingleGazeData(dataID - index1).getMatrix()[0] != 0 && this.getSingleGazeData(dataID - index1).getMatrix()[1] != 0) {
                        beforeData = this.getSingleGazeData(dataID - index1).getMatrix()[0];
                        break;
                    }
                }
                for (int index2 = 1; index2 < this.dataList.size() - dataID && index2 < 3; index2++) {
                    if (this.getSingleGazeData(dataID + index2).getMatrix()[0] != 0 && this.getSingleGazeData(dataID + index2).getMatrix()[1] != 0) {
                        afterData = this.getSingleGazeData(dataID + index2).getMatrix()[0];
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
                        break;
                    case 1:
                        if (currentData[0] >= this.LEFT_LIMIT) {
                            count--;
                        } else if (count < this.CHEAK_DATA_FRE) {
                            count++;
                        }
                        break;
                }
                
                switch (status) {
                    case 0:
                        if (count >= this.CHEAK_DATA_FRE) {
                            sideCheakList.add(this.getSingleGazeData(dataID).getDate());
                            startID = dataID;
                            status = 1;
                        }
                        break;
                    case 1:
                         if (count <= 0) {
                            finishID = dataID;
                            this.setSta(startID, finishID);
                            status = 0;
                        }
                         break;
                }
            }
            //System.out.println(i + "   " +currentData[0] + "    " + count);
        }
        return sideCheakList;
    }

    public void setSta(int startID, int endID) {
        this.getSingleGazeData(startID).setSta(1);
        for (int currentID = startID + 1; currentID < endID; currentID++) {
            this.getSingleGazeData(currentID).setSta(2);
        }
        this.getSingleGazeData(endID).setSta(3);
    }

    public void writeOutAll(File outputFile) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
        writer.println("ID\t時間\tx\ty\tstatus");

        for (SingleGazeData gazeData : this.dataList) {
            writer.println(gazeData.writeOut());
        }
        writer.close();
    }

}
