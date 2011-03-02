package entity;
/**
 * 
 * @author xiaohui
 *
 */
public class Velocity {
	double vx;

	double vy;

	
	public Velocity(double vx, double vy) {
		this.vx = vx;
		this.vy = vy;
	}
	
	public double getVx() {
		return this.vx;
	}

	public double getVy() {
		return this.vy;
	}

	public String toString() {
		return "(" + this.vx + "," + this.vy + ")";
	}

}
