package algo;

import java.util.ArrayList;

import entity.MovingObject;

/**
 * 
 * @author xiaohui
 *
 */
public class DBScan {

	/**
	 * 
	 */
	public static void doDBScan(ArrayList<MovingObject> objects, double eps, double minPts){
		int cid = nextId(0);
		for (MovingObject obj : objects){
			if (obj.cid==0){
				if(expandCluster(objects, obj, cid, eps, minPts)){
					cid = nextId(cid);
				}
			}
		}
	}

	private static boolean expandCluster(ArrayList<MovingObject> objects,
			MovingObject obj, int cid, double eps, double minPts) {
		ArrayList<MovingObject> seeds = rangeQuery(obj, objects, eps);
		if(seeds.size() < minPts){
			obj.cid= -1;
			return false;
		}else{
			for (MovingObject seed : seeds){
				seed.cid = cid;
			}
			obj.label = true;
			seeds.remove(obj);
			while(!seeds.isEmpty()){
				MovingObject currP = seeds.get(0);
				ArrayList<MovingObject> result = rangeQuery(currP, objects, eps);
				
				if(result.size()>=minPts){
					currP.label=true;
					for (MovingObject resultP : result){
						if (resultP.cid ==-1 || resultP.cid==0){
							resultP.cid = cid;
							if(resultP.cid==0){
								seeds.add(resultP);
							}
						}
						
					}
				}
				seeds.remove(currP);
			}
			return true;
		}
}

	
	private static int nextId(int start) {
		return start+1;
	}
	
	/**
	 * naive in-memory implementation 
	 * @param currP
	 * @param objects
	 * @param eps
	 * @return all points in range eps
	 */
	public static ArrayList<MovingObject> rangeQuery(MovingObject currMo,
			ArrayList<MovingObject> objects, double eps) {
		ArrayList<MovingObject> res = new ArrayList<MovingObject>();
		
		for(MovingObject mo : objects){
			if (mo.distance(currMo)<=eps){
				res.add(mo);
			}
		}
		
		return res;
	}

}