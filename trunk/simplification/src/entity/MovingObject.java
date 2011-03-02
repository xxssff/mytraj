package entity;

/**
 * Base class for moving object
 * 
 * @author xiaohui
 * 
 */

public class MovingObject {
	public int oid;
	public TimeCoord location;
	public Velocity v;

	/**
	 * @return dissimilarity between two objects as defined in JDO07
	 */
	public double getDistance(MovingObject anotherObj) {
		//TODO
		return 0;
	}

	/**
	 * 
	 * @param dissimilarity
	 *            between a object and a cluster as defined in JDO07
	 */
	public double getDissimilarity(Cluster aCluster) {
		//TODO
		return 0;
	}
}
