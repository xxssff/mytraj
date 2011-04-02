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

	public static String Test_MINTIME = "16:23:24";
	public static String Test_MAXTIME = "17:01:17";
	public static String testTable = "test_table";
	
	public static String infati_MINTIME = "00:00:00";
	public static String infati_MAXTIME = "23:59:59";
	public static String infatiTable = "real_table";
	
	public static String combination_file="comb_file.txt";
}
