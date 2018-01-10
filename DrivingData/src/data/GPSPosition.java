package data;



import java.io.PrintStream;
import java.text.DecimalFormat;

public class GPSPosition {
	/** 更新日時 */
	public static final int version = 20061106;

	public static DecimalFormat secondFomrat = new DecimalFormat("00.000");
	public static DecimalFormat minFomrat = new DecimalFormat("00");
	public static DecimalFormat degFomrat = new DecimalFormat("#00");
	public static DecimalFormat heightFomrat = new DecimalFormat("##0.0");
	
	public static final int ELLIPSOIDAL_TYPE_TOKYO = 0;
	public static final int ELLIPSOIDAL_TYPE_WGS84 = 1;
	
	/** 法律的に定められた値 */
	public static final double[] LONG_RADIUS = {6377397.155,6378137};
	public static final double[] TIRE_PROFILE = {299.152813,298.257222101};
	/** 以下計算省略のために定数定義 */
	public static final double[] SHORT_RADIUS = {6356078.963,6356752.314};
	
	
	private String NSIndicator;
	private String EWIndicator;
	private int NSdegree;
	private int EWdegree;

	private int NSminutes;
	private int EWminutes;
	
	private double NSSeconds;
	private double EWSeconds;
	
	private double height;

	//デバック用モデル
	public static boolean debug = false;
	public static PrintStream Writer = null;
	//
	
	public GPSPosition(String ido,String keido){
		//旧バージョン互換用高さなしモデル
		this(ido,keido,0);
		//System.err.println("高さなしモデルのGPS座標を生成しました。");
	}
	public GPSPosition(String ido,String keido,double h){
		this.NSIndicator = ido.substring(0,1);
		this.EWIndicator = keido.substring(0,1);
		
		String[] numberIdo = (ido.substring(1)).split("\\.");
		this.NSdegree = Integer.parseInt(numberIdo[0]);
		this.NSminutes = Integer.parseInt(numberIdo[1]);
		this.NSSeconds = Double.parseDouble(numberIdo[2]+"."+numberIdo[3]);
		
		
		String[] numberKeido = keido.substring(1).split("\\.");
		this.EWdegree = Integer.parseInt(numberKeido[0]);
		this.EWminutes = Integer.parseInt(numberKeido[1]);
		this.EWSeconds = Double.parseDouble(numberKeido[2]+"."+numberKeido[3]);
		
		this.height = h;
	}
	
	/** 長さ２のdouble配列で緯度経度を取得する
	 * 単位は分で　０に（X軸用）　１に緯度（Y軸用）が入る */
	public double[] getPositonByDoubleMinuties(){
		double[] retarray = new double[2];
		
		double nsvalue = 0;
		nsvalue += this.NSdegree * 60;
		nsvalue += this.NSminutes;
		nsvalue += this.NSSeconds / 60.0;
		retarray[1] = nsvalue;
		
		double ewvalue = 0;
		ewvalue += this.EWdegree * 60;
		ewvalue += this.EWminutes;
		ewvalue += this.EWSeconds / 60.0;
		retarray[0] = ewvalue;
		
		return retarray;
	}
	
	/** 長さ３の配列として緯度経度高さを　度　で取り出す。
	 * 配列の順も緯度・軽度・高さのまま */
	public double[] getPositonByDoubleDegreeValue(){
		double[] retarray = new double[3];
		
		double nsvalue = 0;
		nsvalue += this.NSdegree;
		nsvalue += this.NSminutes / 60.0;
		nsvalue += this.NSSeconds / 3600.0;
		retarray[0] = nsvalue;
		
		double ewvalue = 0;
		ewvalue += this.EWdegree;
		ewvalue += this.EWminutes / 60.0;
		ewvalue += this.EWSeconds / 3600.0;
		retarray[1] = ewvalue;
		
		retarray[2] = this.height;
		
		return retarray;
	}
	
	public String[] getPositionByString(){
		String[] retArray = new String[2];
		
		retArray[1] = this.get緯度();
		
		retArray[0] = this.get経度();
		
		return retArray;
	}
	
	public String get緯度(){
		StringBuilder builder = new StringBuilder();
		builder.append(this.NSIndicator);
		builder.append(degFomrat.format(this.NSdegree));
		builder.append(".");
		builder.append(minFomrat.format(this.NSminutes));
		builder.append(".");
		builder.append(secondFomrat.format(this.NSSeconds));
		return builder.toString();
	}
	
	
	
	public String get経度(){
		StringBuilder builder = new StringBuilder();
		builder.append(this.EWIndicator);
		builder.append(degFomrat.format(this.EWdegree));
		builder.append(".");
		builder.append(minFomrat.format(this.EWminutes));
		builder.append(".");
		builder.append(secondFomrat.format(this.EWSeconds));
		return builder.toString();
	}
	
	public String get高度(){
		return heightFomrat.format(this.height);
	}
	
	
	public static final int NETYPE_NS = 0;
	public static final int NETYPE_EW = 1;

	public double getValue(int VALUETYPE,int NETYPE){
		switch(NETYPE){
		case NETYPE_NS:
			switch(VALUETYPE){
			case VALUETYPE_SECOND:
				return this.NSSeconds;
			case VALUETYPE_MINUTES:
				return this.NSminutes;
			case VALUETYPE_DEGREE:
				return this.NSdegree;
			}
			break;
		case NETYPE_EW:
			switch(VALUETYPE){
			case VALUETYPE_SECOND:
				return this.EWSeconds;
			case VALUETYPE_MINUTES:
				return this.EWminutes;
			case VALUETYPE_DEGREE:
				return this.EWdegree;
			}
			break;
		}
		return -1;
	}
	public String getEWIndicator() {
		return EWIndicator;
	}

	public String getNSIndicator() {
		return NSIndicator;
	}
	
	public static final int VALUETYPE_SECOND = 0;
	public static final int VALUETYPE_MINUTES = 1;
	public static final int VALUETYPE_DEGREE = 2;
	
	public static final int DIRECTIONTYPE_NORTH = 0;
	public static final int DIRECTIONTYPE_SOUTH = 1;
	public static final int DIRECTIONTYPE_EAST = 2;
	public static final int DIRECTIONTYPE_WEST = 3;
	
	
	public GPSPosition shift(double value,int DIRECTIONTYPE,int VALUETYPE){
		switch(DIRECTIONTYPE){
		case DIRECTIONTYPE_NORTH:
			return shiftNS(value,VALUETYPE);
		case DIRECTIONTYPE_SOUTH:
			return shiftNS(value*-1,VALUETYPE);
		case DIRECTIONTYPE_EAST:
			return shiftEW(value,VALUETYPE);
		case DIRECTIONTYPE_WEST:
			return shiftEW(value*-1,VALUETYPE);
		}
		return null;
	}

	public GPSPosition shiftNS(double value,int VALUETYPE){
		//こちらは変更しない
		String EWValue = this.get経度();//相違部分
		//変更用の値取得
		double Deg = this.getValue(VALUETYPE_DEGREE,GPSPosition.NETYPE_NS);//相違部分
		double Min = this.getValue(VALUETYPE_MINUTES,GPSPosition.NETYPE_NS);//相違部分
		double Sec = this.getValue(VALUETYPE_SECOND,GPSPosition.NETYPE_NS);//相違部分
		
		double[] posvalue = {Deg,Min,Sec};
		double[] shiftValue = this.addValue(posvalue,value,VALUETYPE);
		StringBuilder buf = new StringBuilder();
		buf.append(this.getNSIndicator());//相違部分
		buf.append(degFomrat.format(shiftValue[0]));
		buf.append(".");
		buf.append(minFomrat.format(shiftValue[1]));
		buf.append(".");
		buf.append(secondFomrat.format(shiftValue[2]));

		
		return new GPSPosition(buf.toString(),EWValue);
	}
	
	public GPSPosition shiftEW(double value,int VALUETYPE){
		//こちらは変更しない
		String NSValue = this.get緯度();//相違部分
		//変更用の値取得
		double Deg = this.getValue(VALUETYPE_DEGREE,GPSPosition.NETYPE_EW);//相違部分
		double Min = this.getValue(VALUETYPE_MINUTES,GPSPosition.NETYPE_EW);//相違部分
		double Sec = this.getValue(VALUETYPE_SECOND,GPSPosition.NETYPE_EW);//相違部分
		
		double[] posvalue = {Deg,Min,Sec};
		double[] shiftValue = this.addValue(posvalue,value,VALUETYPE);
		StringBuilder buf = new StringBuilder();
		buf.append(this.getEWIndicator());//相違部分
		buf.append(degFomrat.format(shiftValue[0]));
		buf.append(".");
		buf.append(minFomrat.format(shiftValue[1]));
		buf.append(".");
		buf.append(secondFomrat.format(shiftValue[2]));
		
		return new GPSPosition(NSValue,buf.toString());
	}
	
	public double[] addValue(double[] base,double value,int VALUETYPE){

		double Deg = base[0];
		double Min = base[1];
		double Sec = base[2];
		
		
		switch(VALUETYPE){
		case VALUETYPE_DEGREE:
			Deg += value;
			Deg %= 180;
			break;
		case VALUETYPE_MINUTES:
			Min += value;
			//繰り上がり／下がりの処理
			if(Min < 0){
				int underflow = (int)(Min/60) - 1;
				Deg += underflow;
				Min += 60*(-1*underflow);
				Min %= 60;
			} else {
				Deg += (int)(Min/60);
				Min %= 60;
			}
			break;
		case VALUETYPE_SECOND:
			Sec += value;
			//繰り上がりの処理
			if(Sec < 0){
				int underflow = (int)(Sec/60) - 1;
				Min += underflow;
				Sec += 60*(-1*underflow);
				Sec %= 60;
			} else {
				Min += (int)(Sec/60);
				Sec %= 60;
			}
			
			//度まで影響が及ぶ場合
			if(Min < 0){
				int underflow = (int)(Min/60) - 1;
				Deg += underflow;
				Min += 60*(-1*underflow);
				Min %= 60;
			} else {
				Deg += (int)(Min/60);
				Min %= 60;
			}
			break;
		}
		
		double[] ret = {Deg,Min,Sec}; 
		return(ret);
	}
	
	public static GPSPosition parseFromDouble(double[] data){
		StringBuilder nsValue = new StringBuilder();
		if(data[0] >= 0){
			nsValue.append("N");
		} else {
			nsValue.append("S");
		}
		double nsMin = (data[0] - (int)data[0])*60;
		double nsSec = (nsMin - (int)nsMin)*60;
		nsValue.append(degFomrat.format((int)data[0]));
		nsValue.append(".");
		nsValue.append(minFomrat.format((int)nsMin));
		nsValue.append(".");
		nsValue.append(secondFomrat.format(nsSec));
		
		StringBuilder ewValue = new StringBuilder();
		if(data[1] >= 0){
			ewValue.append("E");
		} else {
			ewValue.append("W");
		}
		double ewMin = (data[1] - (int)data[1])*60;
		double ewSec = (ewMin - (int)ewMin)*60;
		ewValue.append(degFomrat.format((int)data[1]));
		ewValue.append(".");
		ewValue.append(minFomrat.format((int)ewMin));
		ewValue.append(".");
		ewValue.append(secondFomrat.format(ewSec));
		if(data.length > 2){
			return new GPSPosition(nsValue.toString(),ewValue.toString(),data[2]);
		} else {
			return new GPSPosition(nsValue.toString(),ewValue.toString());
		}
	}

	private static final double[] SHIFT_TYPE_TOKYO_TO_WGS84 = {-146.414,507.337,680.507};
	private static final double[] SHIFT_TYPE_WGS84_TO_TOKYO = {146.414,-507.337,-680.507};
	private static double[] shiftXYZValue(double[] basevalue,double[] shiftType){
		double[] retvalue = new double[3];
		for(int i=0;i<retvalue.length;i++){
			retvalue[i] = basevalue[i] + shiftType[i];
		}
		return retvalue;
	}
	
	/** 緯度経度高さから直交座標に変換する */
	public static double[] llh2xyz(double[] llh,int type){
		//必要な値の算出
		//	離心率の２乗
		//double e2_value = (LONG_RADIUS[type]*LONG_RADIUS[type] - SHORT_RADIUS[type]*SHORT_RADIUS[type] ) / LONG_RADIUS[type]*LONG_RADIUS[type];
		double e = Math.sqrt(LONG_RADIUS[type]*LONG_RADIUS[type] - SHORT_RADIUS[type]*SHORT_RADIUS[type]) / LONG_RADIUS[type];
		double e2_value = e*e;
		//　N値		
		double N_Valuye = LONG_RADIUS[type] / Math.sqrt(1.0 - e2_value*Math.pow(Math.sin(Math.toRadians(llh[0])),2));
		
		//値計算
		double[] retValue = new double[3];
		// 計算
		retValue[0] = (N_Valuye + llh[2]) * Math.cos(Math.toRadians(llh[0])) * Math.cos(Math.toRadians(llh[1])); //x = (N + h)cosφcosλ
		retValue[1] = (N_Valuye + llh[2]) * Math.cos(Math.toRadians(llh[0])) * Math.sin(Math.toRadians(llh[1])); //y = (N + h)cosφsinλ
		retValue[2] = (N_Valuye*(1-e2_value) + llh[2]) * Math.sin(Math.toRadians(llh[0])); //z = (N(1-e^2) + h)sinφ
		
		return retValue;
	}
	
	/** 直交座標から緯度経度高さに変換する */
	public static double[] xyz2llh(double[] xyz,int type){
		//必要な値の算出
		double p_value = Math.sqrt(xyz[0]*xyz[0] + xyz[1]*xyz[1]);
		double th_value = Math.atan2(xyz[2]*LONG_RADIUS[type],p_value * SHORT_RADIUS[type]);
		//	離心率の２乗
		double e = Math.sqrt(LONG_RADIUS[type]*LONG_RADIUS[type] - SHORT_RADIUS[type]*SHORT_RADIUS[type]) / LONG_RADIUS[type];
		double e2_value = e*e;
		//	離心率の２乗(短半径基準)
		double e_dash = Math.sqrt(LONG_RADIUS[type]*LONG_RADIUS[type] - SHORT_RADIUS[type]*SHORT_RADIUS[type]) / SHORT_RADIUS[type];
		double e2_dash_value = e_dash*e_dash;


		//値計算
		double[] retValue = new double[3];
		// 計算
		retValue[0] = Math.atan2(xyz[2] + e2_dash_value*SHORT_RADIUS[type]*Math.pow(Math.sin(th_value),3),p_value - e2_value*LONG_RADIUS[type]*Math.pow(Math.cos(th_value),3));
		retValue[1] = Math.atan2(xyz[1],xyz[0]);//-PI　から　PI　の値が帰る

		//　N値	(φがでないと計算できない)	
		double N_Valuye = LONG_RADIUS[type] / Math.sqrt(1.0 - e2_value*Math.pow(Math.sin(retValue[0]),2));
		retValue[2] = (p_value/Math.cos(retValue[0])) - N_Valuye; 
		
		//度に戻す
		retValue[0] = Math.toDegrees(retValue[0]);
		retValue[1] = Math.toDegrees(retValue[1]);
		
		return retValue;
	}
	
	public GPSPosition Tyokyo2WGS84(GPSPosition basepos){
		//デバック用
		if(GPSPosition.debug){
		double[] base = basepos.getPositonByDoubleDegreeValue();
		double[] xyz = llh2xyz(base,ELLIPSOIDAL_TYPE_TOKYO);
		//System.out.printf("(x,y,z) = (%f,%f,%f)\n",xyz[0],xyz[1],xyz[2]);
		double[] repalce = xyz2llh(xyz,ELLIPSOIDAL_TYPE_TOKYO);
		
		GPSPosition baseGPS = GPSPosition.parseFromDouble(base);
		GPSPosition repalceGPS = GPSPosition.parseFromDouble(repalce);
		
		GPSPosition.Writer.printf("%s,%s,%s",baseGPS.get緯度(),baseGPS.get経度(),baseGPS.get高度());
		GPSPosition.Writer.printf(",%s,%s,%s",repalceGPS.get緯度(),repalceGPS.get経度(),repalceGPS.get高度());
		GPSPosition.Writer.printf(",%s,%s,%s\n",base[0]-repalce[0],base[1]-repalce[1],base[2]-repalce[2]);
		}
		//
		
		double[] Tokyo_xyz = llh2xyz(basepos.getPositonByDoubleDegreeValue(),ELLIPSOIDAL_TYPE_TOKYO);
		double[] WGS_llh = xyz2llh(shiftXYZValue(Tokyo_xyz,SHIFT_TYPE_TOKYO_TO_WGS84),ELLIPSOIDAL_TYPE_WGS84);
		
		return parseFromDouble(WGS_llh);
	}
	
	public GPSPosition WGS84_2_Tyokyo(GPSPosition basepos){
		double[] WGS_xyz = llh2xyz(basepos.getPositonByDoubleDegreeValue(),ELLIPSOIDAL_TYPE_WGS84);
		
		double[] Tokyo_llh = xyz2llh(shiftXYZValue(WGS_xyz,SHIFT_TYPE_WGS84_TO_TOKYO),ELLIPSOIDAL_TYPE_TOKYO);
		
		return parseFromDouble(Tokyo_llh);
	}

	@Override
	public String toString() {
		return this.get緯度()+this.get経度();
	}
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	

}
