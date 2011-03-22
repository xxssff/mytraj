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

	public static String MINTIME = "16:23:24";
	public static String MAXTIME = "17:01:17";
	public static String testTable = "test_table";
}
