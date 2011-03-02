package entity;

import java.util.ArrayList;

public class Cluster {
	ArrayList<MovingObject> members;
	double[] CF;

	public Cluster() {

	}

	public void add(MovingObject aMO) {
		members.add(aMO);
		updateCF();
	}

	public void delete(MovingObject aMo) {
		members.remove(aMo);
		updateCF();
	}

	private void updateCF() {
		// TODO Auto-generated method stub
	}

	public double getAvgRadius(int time) {
		return 0;
	}

	/**
	 * 
	 * @return the imaginative center object.
	 */
	public TimeCoord getCenter() {
		// TODO Auto-generated method stub
		return null;
	}

	

	/**
	 * 
	 * @return ranking score
	 */
	public double getScore() {
		return 0;
	}

	public int getSize() {
		return members.size();
	}

}
