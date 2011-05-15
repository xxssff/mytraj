package data;

/**
 * @author xiaohui
 */
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import com.vividsolutions.jts.geom.Coordinate;

public class DataPoint {

	public LocalDateTime dateTime;
	public int routeId;
	public Coordinate p;
	public int time0; // the time of the point if starting time is 0
	public double vx;
	public double vy;
	

	public DataPoint(int routeId, Coordinate coordinate, double vx, double vy,
			LocalDateTime time, int time0) {
		this.routeId = routeId;
		this.p = coordinate;
		this.vx = vx;
		this.vy = vy;
		this.dateTime = time;
		this.time0 = time0;
	}

	
	public DataPoint() {
	}

	public String toString() {
		return "[DataPoint: " + routeId + ", " + p.toString() + ", "
				+ dateTime.toString() + " "+ time0+"]";
	}

}
