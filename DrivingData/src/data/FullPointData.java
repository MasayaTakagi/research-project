/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author takagi masaya
 */
public class FullPointData {

    public ArrayList<SinglePointData> dataList = new ArrayList<SinglePointData>();

    private final double TURNING_SPEED = 30; //右左折中の上限速度
    private final double STOP_SPEED = 3; //停止と判断する速度
    private final double STOP_DISTANCE = 1; //停止と判断する移動距離
    private final double TOTAL_DISTANCE = 30; //右左折中の上限移動距離
    private final double ANGLE_VARIATION = 60;  //右左折中の最小変化角
    private final double CHEAK_PROPORTION = 0.4;  //右左折と判断する割合
    private final int LOG_ID_START = 961;
    private final int LOG_ID_FINISH = 970;

    public FullPointData() {

    }

    public FullPointData(ArrayList dataList) {
        this.dataList = new ArrayList<>(dataList);
    }

    public void addSinglePointData(SinglePointData data) {
        this.dataList.add(data);
    }

    public SinglePointData getSinglePointData(int ID) {
        return this.dataList.get(ID - 1);
    }

    public GPSPosition getLastPosition() {
        return this.dataList.get(dataList.size() - 1).getPosition();
    }

    public int getDataSize() {
        return this.dataList.size();
    }

    public ArrayList<SinglePointData> getList() {
        return this.dataList;
    }

    public SinglePointData getNextPointData(SinglePointData posData) {
        int ID = posData.getID();
        if (ID < this.dataList.size()) {
            return this.dataList.get(ID);
        } else {
            System.out.println("IDが範囲外です");
            return this.dataList.get(ID - 1);
        }
    }

    public ArrayList<ArrayList<SinglePointData>> makeDataList() {
        ArrayList<ArrayList<SinglePointData>> dataList = new ArrayList<ArrayList<SinglePointData>>();
        ArrayList<SinglePointData> currentList = new ArrayList<SinglePointData>();
        int currentStatus = 0;
        for (SinglePointData posData : this.dataList) {
            currentList.add(posData);
            if (currentStatus == 0 && posData.getTurnSta() != 0) {
                dataList.add(currentList);
                currentList = new ArrayList<SinglePointData>();
                currentList.add(posData);
            } else if (1 <= currentStatus && currentStatus <= 3) {
                if (posData.getTurnSta() < 1) {
                    dataList.add(currentList);
                    currentList.add(posData);
                    currentList = new ArrayList<SinglePointData>();
                }
            } else if (-3 <= currentStatus && currentStatus <= -1) {
                if (posData.getTurnSta() > -1) {
                    dataList.add(currentList);
                    currentList = new ArrayList<SinglePointData>();
                    currentList.add(posData);
                }
            }
            currentStatus = posData.getTurnSta();
        }
        if (currentList.size() > 0) {
            dataList.add(currentList);
        }
        dataList.add(currentList);
        return dataList;
    }

    public void setTurnSta(int startID, int endID, int turnSta) {
        int setSta[] = new int[3];
        if (turnSta == 1) {
            setSta[0] = 1;
            setSta[1] = 2;
            setSta[2] = 3;
        } else if (turnSta == 2) {
            setSta[0] = -1;
            setSta[1] = -2;
            setSta[2] = -3;
        }
        this.getSinglePointData(startID).setTurnSta(setSta[0]);
        for (int currentID = startID + 1; currentID < endID; currentID++) {
            this.getSinglePointData(currentID).setTurnSta(setSta[1]);
        }
        this.getSinglePointData(endID).setTurnSta(setSta[2]);
    }

    public static void calculateDifferenceValue(SinglePointData data1, SinglePointData data2) {
        double[] llh1 = data1.getPosition().getPositonByDoubleDegreeValue();
        double[] xyz1 = GPSPosition.llh2xyz(llh1, 1);

        double[] llh2 = data2.getPosition().getPositonByDoubleDegreeValue();
        double[] xyz2 = GPSPosition.llh2xyz(llh2, 1);

        double distance = Math.sqrt((xyz2[0] - xyz1[0]) * (xyz2[0] - xyz1[0]) + (xyz2[1] - xyz1[1]) * (xyz2[1] - xyz1[1]));
        double radian = Math.atan2(xyz2[1] - xyz1[1], xyz2[0] - xyz1[0]);
        double degree = radian * 180 / Math.PI;

        data1.setDifferenceValue(degree, distance);

        /**
         * System.out.println(Arrays.toString(xyz1));
         * System.out.println(Arrays.toString(xyz2));
         * System.out.println(distance); System.out.println(degree);
         */
    }

    public void calculateAllDifferenceValue() {
        int i = 0;
        for (SinglePointData posData : this.dataList) {
            SinglePointData nextPosData = this.getNextPointData(posData);
            FullPointData.calculateDifferenceValue(posData, nextPosData);
            i++;
            if (i >= this.dataList.size() - 1) {
                posData.setDifferenceValue(0, 0);
                break;
            }
        }
    }

    public void cheakTurning() {
        double angleVariation;
        double speed;
        double distance1 = 0;
        double distance2 = 0;
        int turnStartID = 0;
        int turnEndID;
        int turnDirection = 0;//1:左折, 2:右折
        int turnFlag = 1;//1:直進時, 2:右左折時
        int turnCheakCount[] = {0, 0}; //左折カウント,右折カウント
        ArrayList<Double> angleVariationList = new ArrayList<Double>();
        ArrayList<Integer> cheakTurnList = new ArrayList<Integer>();

        double angle1;//基準となる移動方向
        double angle2;//移動した後の移動方向

        for (int dataID = 1; dataID <= this.dataList.size(); dataID++) {
            if (turnFlag == 1) {
                //通常時 : 右左折開始を見つける
                //速度が遅い:停止していないデータを抽出
                speed = this.dataList.get(dataID - 1).getSpeed();
                if (speed < TURNING_SPEED && speed > STOP_SPEED) {
                    //移動距離から停止していないデータを抽出
                    if (this.dataList.get(dataID - 1).getDifferenceValue()[1] > STOP_DISTANCE) {
                        distance1 = 0;
                        angle1 = this.dataList.get(dataID - 1).getDifferenceValue()[0];
                        //一定距離走行するまでループ
                        angleVariationList = new ArrayList<Double>();
                        for (int i = 1; distance1 < this.TOTAL_DISTANCE; i++) {
                            //dataListが無くなったら終了
                            if (dataID + i >= this.dataList.size()) {
                                return;
                            }
                            angle2 = this.dataList.get(dataID - 1 + i).getDifferenceValue()[0];
                            distance1 = distance1 + this.dataList.get(dataID - 1 + i).getDifferenceValue()[1];
                            if (distance1 < this.TOTAL_DISTANCE && this.dataList.get(dataID - 1 + i).getSpeed() > STOP_SPEED) {
                                //変化角の計算
                                if (angle1 <= 0) {
                                    if (angle1 <= angle2 && angle2 <= angle1 + 180) {
                                        //angleが増えた場合(左折)
                                        if (angle2 < 0) {
                                            angleVariation = Math.abs(angle2 - angle1);
                                        } else {
                                            angleVariation = Math.abs(angle2 + angle1);
                                        }
                                    } else {
                                        //angleが減った場合(右折)
                                        if (angle2 < angle1) {
                                            angleVariation = -Math.abs(angle2 - angle1);
                                        } else {
                                            angleVariation = -(360 - Math.abs(angle2 - angle1));
                                        }
                                    }
                                } else {
                                    if (angle1 >= angle2 && angle2 >= angle1 - 180) {
                                        if (angle2 > 0) {
                                            angleVariation = -Math.abs(angle2 - angle1);
                                        } else {
                                            angleVariation = -Math.abs(angle2 + angle1);
                                        }
                                    } else {
                                        if (angle2 > angle1) {
                                            angleVariation = Math.abs(angle2 - angle1);
                                        } else {
                                            angleVariation = (360 - Math.abs(angle2 - angle1));
                                        }
                                    }
                                }
                                angleVariationList.add(angleVariation);
                            }
                        }
                        if (dataID >=LOG_ID_START && dataID <= LOG_ID_FINISH) {
                            System.out.println("----" + dataID + "----");
                            System.out.println(angleVariationList);
                        }
                        for (double angle : angleVariationList) {
                            if (angle > this.ANGLE_VARIATION) {
                                turnCheakCount[0]++;
                            } else if (angle < -this.ANGLE_VARIATION) {
                                turnCheakCount[1]++;
                            }
                        }
                        if (turnCheakCount[0] > angleVariationList.size() * CHEAK_PROPORTION) {
                            turnStartID = dataID;
                            turnFlag = 2;
                            turnDirection = 1;
                        } else if (turnCheakCount[1] > angleVariationList.size() * CHEAK_PROPORTION) {
                            turnStartID = dataID;
                            turnFlag = 2;
                            turnDirection = 2;
                        }
                        turnCheakCount[0] = 0;
                        turnCheakCount[1] = 0;
                    }
                }
            }
            if (turnFlag == 2) {
                //右左折時 : 右左折終了を見つける
                speed = this.dataList.get(dataID - 1).getSpeed();
                distance2 = distance2 + this.dataList.get(dataID - 1).getDifferenceValue()[1];
                if (speed > this.TURNING_SPEED || distance2 > this.TOTAL_DISTANCE) {
                    turnEndID = dataID;
                    turnFlag = 1;
                    distance2 = 0;
                    this.setTurnSta(turnStartID, turnEndID, turnDirection);
                }
            }
        }
    }

    public void cheakLaneChanging() {

    }
    
    public ArrayList<SinglePointData> makeSideCheakList(ArrayList<LocalTime> timeList){
        ArrayList<SinglePointData> sideCheakList = new ArrayList<SinglePointData>();
        for(LocalTime time1:timeList){
            for(int dataID = 1; dataID <= this.dataList.size(); dataID++){
                LocalTime time2 = this.dataList.get(dataID).getTime();
                if(time2.compareTo(time1) == 0){
                    sideCheakList.add(this.dataList.get(dataID));
                    break;
                }
            }
        }
        return sideCheakList;
    }

    public void writeOutAll(File outputFile) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
        writer.println("ID\t日付\t時間\t緯度\t経度\t速度(km/hour)\t高度(m)\t角度(°)\t距離(m)");

        for (SinglePointData posData : this.dataList) {
            writer.println(posData.writeOut());
        }
        writer.close();
    }

}
