package entity;

/**
 * location with timepoint
 * 
 * @author xiaohui
 * 
 */
public class TLocation {
	public double locx;
	public double locy;
	public double timepoint;

	public TLocation(double locx, double locy, double time) {
		this.locx = locx;
		this.locy = locy;
		this.timepoint = time;
	}

	public static double[] getVelocity(TLocation loc1, TLocation loc2) {
		double[] res = new double[2];
		double period = loc2.timepoint - loc1.timepoint;
		res[0] = (loc2.locx - loc1.locx) / period;
		res[0] = (loc2.locy - loc1.locy) / period;

		return res;
	}

	public static TLocation getTLocation(TLocation loc1, TLocation loc2,
			double midTime) {
		if (midTime < loc1.timepoint || midTime >= loc2.timepoint) {
			System.err
					.println("getTLocation: midTime should fall in time of loc1 and loc2");
			;
			System.exit(0);
		}
		double r = (midTime - loc1.timepoint) / (loc2.timepoint - loc1.timepoint);

		return new TLocation(loc1.locx + r * (loc2.locx - loc1.locx), loc1.locy
				+ r * (loc2.locy - loc1.locy), midTime);

	}
}
