package entity;

import org.joda.time.LocalDateTime;

/**
 * Some global parameters and functions
 * 
 * @author xiaohui
 * 
 */
public class Global {
	private static int CID = 1; // strictly increasing

	public static int nextCid() {
		return CID++;
	}

	public static String Test_MINTIME = "16:23:24";
	public static String Test_MAXTIME = "17:01:17";
	public static String testTable = "test_table";

	public static String infati_MINTIME = "2001-03-27T00:00:00";
	public static String infati_MAXTIME = "2001-03-27T23:59:59";
	public static String infatiDay = "2001-03-27";
	public static String infatiTable = "real_table";
	public static LocalDateTime infatiMaxDateTIME = new LocalDateTime(
			"2001-03-27T23:59:59");
	public static LocalDateTime TruckMaxDateTIME = new LocalDateTime(
			"2001-04-03T23:58:37");
	public static LocalDateTime elkMinDateTIME = new LocalDateTime(
			"1993-05-06T17:00:21");
	public static LocalDateTime elkMaxDateTIME = new LocalDateTime(
			"1996-07-18T13:40:07");
	public static LocalDateTime currMaxDateTIME = infatiMaxDateTIME;

	public static String combination_file = "comb_file.txt";
}
