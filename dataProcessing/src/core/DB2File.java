package core;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 
 */

/**
 * @author Xiaohui
 * 
 *         This class read trajectory data from MySQL and write trajectory into
 *         file with the following format. trajID, rid1 rid2 rid3...
 * 
 *         The main class expects two arguments for start tid and end tid. Then
 *         the trajectories of these ids would be retrieved from MySQL.
 */
public class DB2File {

	/**
	 * @param args
	 * 
	 */
	private static String url = "jdbc:mysql://localhost:3306/synthetic_data";
	private static String username = "root";
	private static String password = "";
	private static String tableName = "oldenburg_1k_3k";
	private static String outFileName = "D:/Research/trajectory indexing/networkGen/MOGenNetwork_scripts/gen/Traj_Olden_1k_3k.txt";
	private static Connection con;

	//This function gurantee there is only one connection
	private Connection getConn() {
		if (con == null) {
			try {
				// Register the JDBC driver for MySQL.
				Class.forName("com.mysql.jdbc.Driver");
				con = DriverManager.getConnection(url, username, password);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return con;
	}

	public String getString(int tid) {
		StringBuilder sb = new StringBuilder();
		int lastRid = 0; // remember the last rid to eliminate redundant rids
		try {
			Connection myconn = getConn();
			String sql = "SELECT id, curr_time, road_id FROM " + tableName
					+ " WHERE id=" + tid + " AND road_id<>0 ORDER BY curr_time";
			Statement stmt = myconn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			sb.append(tid + ", ");
			if (rs.next()) {
				lastRid = rs.getInt("road_id");
				sb.append(lastRid + " ");
			}
			while (rs.next()) {
				int roadId = rs.getInt("road_id");
				if (roadId != lastRid) {
					sb.append(roadId + " "); // eliminate redundant rids here
					lastRid = roadId;
				}
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	void write2File(String filename, String content) {
		BufferedWriter bufferedWriter = null;
		try {

			// Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new FileWriter(filename));
			bufferedWriter.write(content);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			// Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	public void closeConn(){
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		int start = Integer.parseInt(args[0]);
		int end = Integer.parseInt(args[1]);
		DB2File dbhelper = new DB2File();
		StringBuilder sb = new StringBuilder();
		System.out.println("Start querying...");
		long startTime = System.currentTimeMillis();
		for (int i = start; i <= end; i++) {
			sb.append(dbhelper.getString(i) + "\n");
			System.out.println("Tid: " + i);
		}
		long endTime = System.currentTimeMillis();
		// write to file
		System.out.println("Time taken to query: " + (endTime - startTime)
				/ 1000.0);
		System.out.println("Start writing to file...");
		dbhelper.write2File(outFileName, sb.toString());
		dbhelper.closeConn();
		System.out.println("Done!");
	}
	
}
