package data;

/**
 * @author xiaohui
 */
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import com.vividsolutions.jts.geom.Coordinate;

public class DataPoint {

	public LocalTime time;
	public LocalDateTime dateTime;
	public int routeId;
	public Coordinate p;
	public int time0; // the time of the point if starting time is 0
	public double vx;
	public double vy;
	public boolean lastPoint;
	
	public DataPoint() {
	}

	public DataPoint(int routeId, Coordinate coordinate, double vx, double vy,
			LocalTime time, LocalDateTime dateTime, int time0, boolean lastPoint) {
		this.routeId = routeId;
		this.p = coordinate;
		this.vx = vx;
		this.vy = vy;
		this.time = time;
		this.dateTime = dateTime;
		this.time0 = time0;
		this.lastPoint = lastPoint;
	}

	public String toString() {
		return "[DataPoint: " + routeId + ", " + p.toString() + ", "
				+ time.toString() + " "+ time0+"]";
	}

}
