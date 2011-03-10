package entity;

/**
 * Some global parameters and functions
 * @author xiaohui
 *
 */
public class Global {
	private static int CID=1; //strictly increasing
	public static int nextCid(){
		return CID++;
	}

	public static String MINTIME = "14:10:18";
	public static String MAXTIME = "22:00:12";
	public static String testTable = "simpl_trajectories_5";
}
