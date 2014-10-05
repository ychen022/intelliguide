package srb.intelligent.intelliguide;

import java.text.DecimalFormat;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;

public class GuidePortal {
	private LatLng cord;
	private double cordX;
	private double cordY;
	private String name;
	private String guidePath;
	private boolean played = false; 
	
	public GuidePortal(LatLng cord, String name, String path) {
		this.cord = cord;
		this.cordX = cord.longitude;
		this.cordY = cord.latitude;
		this.name = name;
		this.guidePath = path;
	}
	
	public String loadGuide(){
		played = true;
		return guidePath;
	}
	
	public String getName(){
		return name;
	}
	
	public LatLng getCordinate(){
		return cord;
	}
	
	public double getDistance(LatLng cur){
        int Radius=6371;
        double lat1 = cordY;
        double lon1 = cordX;
        double lat2 = cur.latitude;
        double lon2 = cur.longitude;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Radius * c;
	}
	
	public double getOrientation(LatLng cur, double curDir){
		double curX = cur.longitude;
		double curY = cur.latitude;
		double theta = 180*Math.atan2((cordX-curX),(cordY-curY))/Math.PI;
		double portalDir = -1*theta+180;
		double angle = portalDir-curDir;
		if(angle<0){angle = angle+360;}
		return angle;
	}
	
	public boolean inRange(LatLng cur, double curDir){
		double d = getDistance(cur);
		double theta = getOrientation(cur, curDir);
		double reTheta;
		if(theta>180){reTheta = (360-theta)/3;}
		else{reTheta = theta/3;}
		double bound = 0.5*Math.cos(reTheta);
		if(bound>d){return true;}
		else{return false;}
	}
	
	public String inKM(LatLng cur, double curDir){
		double d = getDistance(cur);
		double theta = getOrientation(cur, curDir);
		if(1>d){
			if (theta>=45 && theta<135){return "rihgt";}
			else if(theta>=135 && theta<225){return "back";}
			else if(theta>=225 && theta<315){return "left";}
			else{return "front";}
		}else{
			return null; 
		}
	}
}
