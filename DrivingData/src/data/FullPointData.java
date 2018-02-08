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

    private final double TURNING_SPEED = 40; //右左折中の上限速度
    private final double STOP_SPEED = 3; //停止と判断する速度
    private final double STOP_DISTANCE = 1; //停止と判断する移動距離
    private final double TOTAL_DISTANCE = 30; //右左折中の上限移動距離
    private final double TURN_FINISH_DISTANCE = 30; //右左折終了と判断する距離
    private final double ANGLE_VARIATION = 60;  //右左折中の最小変化角
    private final double CHEAK_DISTANCE = 20;  //右左折と判断する割合
    private final int LOG_ID_START = 340;
    private final int LOG_ID_FINISH = 349;
    
    private static final double LONG_RADIUS = 6378137;
    private static final double SHORT_RADIUS = 6356752;
    private static final double TIRE_PROFILE = 298.257223563;
    private static final double E2 = 0.00669438;
    private static final double a = 6335439.327;
    

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
                    currentList.add(this.getNextPointData(posData));
                    dataList.add(currentList);
                    currentList = new ArrayList<SinglePointData>();
                }
            } else if (-3 <= currentStatus && currentStatus <= -1) {
                if (posData.getTurnSta() > -1) {
                    currentList.add(this.getNextPointData(posData));
                    dataList.add(currentList);
                    currentList = new ArrayList<SinglePointData>();
                }
            }
            currentStatus = posData.getTurnSta();
        }
        if (currentList.size() > 1) {
            dataList.add(currentList);
        }
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
        double[] llh2 = data2.getPosition().getPositonByDoubleDegreeValue();
        
        if(llh1[0] == llh2[0] && llh1[1] == llh2[1]){
           data1.setDifferenceValue(0, 0);
           return;
        }
        
        double x1 = Math.toRadians(llh1[0]);
        double y1 = Math.toRadians(llh1[1]);
        double x2 = Math.toRadians(llh2[0]);
        double y2 = Math.toRadians(llh2[1]);
        
        //方位の計算
        double Y = Math.cos(Math.toRadians(llh2[0])) * Math.sin(Math.toRadians(llh2[1] - llh1[1]));
        double X = Math.cos(Math.toRadians(llh1[0])) * Math.sin(Math.toRadians(llh2[0])) - Math.sin(Math.toRadians(llh1[0])) * Math.cos(Math.toRadians(llh2[0])) * Math.cos(Math.toRadians(llh2[1] - llh1[1]));

        double radian = Math.atan2(Y, X);
        //double radian = Math.atan2(Math.sin(y2 - y1), ((Math.cos(x1) * Math.tan(x2)) - (Math.sin(x1) * Math.cos(y2 - y1))));

        if (radian < 0) {
            radian = radian + 2 * Math.PI;
        }

        double degree = radian * 180 / Math.PI;
        
        if(degree == -0){
            degree = 0;
        }
        //距離の計算
        double dx = x1 - x2;
        double dy = y1 - y2;
        double W = Math.sqrt(1 - E2 * Math.sin((y1 + y2) / 2) * Math.sin((y1 + y2) / 2));
        double N = LONG_RADIUS / W;
        double M = LONG_RADIUS * (1 - E2) / (W * W * W);
        
        double distance = Math.sqrt((dy * M * dy * M) + (dx * N * Math.cos((y1 + y2) / 2) * dx * N * Math.cos((y1 + y2) / 2)));
        
        data1.setDifferenceValue(degree, distance);
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
        Double[] turnCheakDistance = {0d, 0d}; //左折カウント,右折カウント
        ArrayList<Double[]> angleVariationList = new ArrayList<Double[]>();
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
                        angleVariationList = new ArrayList<Double[]>();
                        for (int i = 1; distance1 < this.TOTAL_DISTANCE; i++) {
                            //dataListが無くなったら終了
                            if (dataID + i >= this.dataList.size()) {
                                return;
                            }
                            angle2 = this.dataList.get(dataID - 1 + i).getDifferenceValue()[0];
                            distance1 = distance1 + this.dataList.get(dataID - 1 + i).getDifferenceValue()[1];
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
                            Double[] angle_distance = {angleVariation, this.dataList.get(dataID - 1 + i).getDifferenceValue()[1]};
                            angleVariationList.add(angle_distance);
                        }
                        if (dataID >= LOG_ID_START && dataID <= LOG_ID_FINISH) {
                            System.out.println("----" + dataID + "----");
                            for (Double[] angle : angleVariationList) {
                                System.out.println(angle[0] + "   " + angle[1]);
                            }
                        }
                        for (Double[] angle : angleVariationList) {
                            if (angle[0] > this.ANGLE_VARIATION) {
                                turnCheakDistance[0] = turnCheakDistance[0] + angle[1];
                            } else if (angle[0] < -this.ANGLE_VARIATION) {
                                turnCheakDistance[1] = turnCheakDistance[1] + angle[1];
                            }
                        }
                        if (turnCheakDistance[0] > CHEAK_DISTANCE) {
                            turnStartID = dataID;
                            turnFlag = 2;
                            turnDirection = 1;
                            dataID++;
                        } else if (turnCheakDistance[1] > CHEAK_DISTANCE) {
                            turnStartID = dataID;
                            turnFlag = 2;
                            turnDirection = 2;
                            dataID++;
                        }
                        Arrays.fill(turnCheakDistance, 0d);
                    }
                }
            }
            if (turnFlag == 2) {
                //右左折時 : 右左折終了を見つける
                speed = this.dataList.get(dataID - 1).getSpeed();
                distance2 = distance2 + this.dataList.get(dataID - 1).getDifferenceValue()[1];
                if (speed > this.TURNING_SPEED || distance2 > this.TURN_FINISH_DISTANCE) {
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

    public ArrayList<SinglePointData> makeSideCheakList(ArrayList<LocalTime> timeList) {
        ArrayList<SinglePointData> sideCheakList = new ArrayList<SinglePointData>();
        for (LocalTime time1 : timeList) {
            for (int dataID = 1; dataID <= this.dataList.size(); dataID++) {
                LocalTime time2 = this.dataList.get(dataID - 1).getTime();
                if (time2.compareTo(time1) == 0) {
                    sideCheakList.add(this.dataList.get(dataID - 1));
                    this.getSinglePointData(dataID).setCheakSideSta(1);
                    System.out.println(dataID);
                    for (int i = 0; i < 4 && dataID + i < this.getDataSize(); i++) {
                        System.out.println(this.getSinglePointData(dataID + i).getTurnSta());
                        if (this.getSinglePointData(dataID + i).getTurnSta() > 0) {
                            this.getSinglePointData(dataID).setCheakSideSta(2);
                        }
                    }
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
