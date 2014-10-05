package srb.intelligent.intelliguide;

import java.lang.Math; 

public class GuidePortal {
	public double[] cord = new double[2];
	public double cordX;
	public double cordY;
	public String name;
	public String guidePath;
	public boolean played = false;
	
	public GuidePortal(double cordX, double cordY, String name, String path) {
		this.cord[0] = cordX;
		this.cord[1] = cordY;
		this.name = name;
		this.guidePath = path;
	}
	
	public double[] getCordinate(double curX, double curY) {
		return cord;
	}
	
	public String loadGuide(){
		played = true;
		return guidePath;
	}
	
	public double getOrientation(double curX, double curY, double curDir){
		double theta = 180*Math.atan2((cordX-curX),(cordY-curY))/Math.PI;
		return -1*theta+180;
	}
}
