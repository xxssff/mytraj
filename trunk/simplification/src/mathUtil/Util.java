package mathUtil;

import java.util.ArrayList;
import java.util.Arrays;

import org.joda.time.LocalTime;

import entity.MovingObject;

public class Util {
	public static boolean contains(Integer[] ints, Integer id) {
		for (Integer i : ints) {
			if (i == id) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Insert oid into ints and sort
	 * 
	 * @param ints
	 * @param oid
	 * @return
	 */
	public static Integer[] insertOneNumber(Integer[] ints, int oid) {
		Integer[] res = new Integer[ints.length + 1];
		System.arraycopy(ints, 0, res, 0, ints.length);
		res[ints.length] = oid;
		Arrays.sort(res);

		return res;
	}
	
	public static LocalTime getEnterTime(int range, MovingObject noiseObj,
			MovingObject coreObj, LocalTime refTime) {

		// compute distances
		double objDist = noiseObj.distance(coreObj) - range;

		// compute relative velocities
		double deltaX = noiseObj.getX() - coreObj.getX();
		double deltaY = noiseObj.getY() - coreObj.getY();
		double sinTheta = deltaX / Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		double cosTheta = Math.sqrt(1 - sinTheta * sinTheta);

		double deltaVX = noiseObj.getVx() - coreObj.getVx();
		double deltaVY = noiseObj.getVy() - coreObj.getVy();
		double along = deltaVX * sinTheta + deltaVY * cosTheta;

		return refTime.plusSeconds((int) (objDist / along));
	}

	/**
	 * 
	 * @param sArr
	 * @param toBeDel
	 * @return index of sArr in toBeDel; -1 if not found
	 */
	public static int getIndex(Integer[] intArr, ArrayList<Integer[]> toBeDel) {
		int i = 0;
		for (Integer[] s : toBeDel) {
			if (Arrays.equals(s, intArr)) {
				return i;
			}
			i++;
		}
		return -1;
	}
}
