package simplification;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author ceikute
 */
public class Database {

    Connection conn;

    public Database() {
        String dburl = "jdbc:postgresql://localhost:5432/routes";
        String dbuser = "postgres";
        String dbpass = "BEcomno1";

        try {
            System.out.println("Creating JDBC connection...");
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(dburl, dbuser, dbpass);

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public String[] transform(int sridFrom, int sridTo, String x, String y) throws java.sql.SQLException {
        String[] res = new String[2];
        ResultSet result = null;
        String select = "select astext(ST_Transform(GeomFromText('POINT(" + x + " " + y + ")', " + sridFrom + "), " + sridTo + ")) as p";

        PreparedStatement ps = conn.prepareStatement(select);

        result = ps.executeQuery();
        if (result.next()) {
            String r = result.getString("p");
            r = r.substring(6, r.length() - 1);
            res = r.split(" ");
        }
        result.close();
        ps.close();

        return res;
    }

    public void insertData(String dataStr) throws SQLException, Exception {
        ResultSet result = null;
        PreparedStatement ps = null;
        String[] data = dataStr.split(" ");
//        System.out.println(data.length);
        String insert = "select * from simpl_insert_data(" + data[0] + ", '" + data[1] + "', " + data[2] + ", " + data[3] + ", " + data[4] + ", " + data[5] + ", '" + data[6] + "', '" + data[7] + "', '" + data[8] + "', '" + data[9] + "', " + data[10] + ", " + data[11] + ", " + data[12] + ", " + data[13] + ", " + data[14] + ") as p";
//        System.out.println(insert);
        try {
            ps = conn.prepareStatement(insert);
            result = ps.executeQuery();

            result.close();
            ps.close();
        } finally {
            if (result != null) {
                result.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void generateRouteFiles(String carId, String driverId) throws SQLException, IOException {
        ResultSet result = null;
        PreparedStatement ps = null;

        String getDates = "select distinct rdate from simpl_route_data where carid=" + carId + " and driverid=" + driverId + " order by rdate asc";
        ArrayList<String> dates = new ArrayList();

        try {
            ps = conn.prepareStatement(getDates);
            result = ps.executeQuery();

            while (result.next()) {
                String date = result.getString("rdate");

                dates.add(date);
            }

            result.close();
            ps.close();

            String url = "C:\\routes\\test\\";
            for (int i = 0; i < dates.size(); i++) {
                String date = dates.get(i);
                String getDataPoint = "select * from simpl_route_data where carid=" + carId + " and driverid=" + driverId + " and rdate='" + date + "' order by rtime asc";
                ps = conn.prepareStatement(getDataPoint);
                result = ps.executeQuery();

                String fName = carId + "_" + driverId + "_" + date + ".txt";
                String line = "";

                FileWriter fstreamOut = new FileWriter(url + fName);
                BufferedWriter out = new BufferedWriter(fstreamOut);

                while (result.next()) {
                    line = result.getInt(1) + " " + result.getInt(3) + " " +  result.getInt(4) + " " + result.getInt(5) + " " + result.getInt(6) + " " + result.getString(7) + " " + result.getString(8) + " " + result.getString(9) + " " + result.getString(10) + " " + result.getInt(11) + " " + result.getInt(12) + " " + result.getInt(13) + " " + result.getInt(14) + " " + result.getInt(15) + " " + result.getString(16) + " " + result.getString(17);
//                    System.out.println(line);
                    if (line.length() > 0) {
                        out.write(line.trim() + "\n");
                        out.flush();
                    }
                }
                out.close();
                result.close();
                ps.close();
            }
        } finally {
            if (result != null) {
                result.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void closeConnection() throws SQLException {
        if (conn != null) {
            conn.close();
            System.out.println("Connection closed..");
        }
    }
    
    public static void main (String args[]) throws Exception{
    	String sql = "select * from simpl_trajectories_5 limit 2";
    	Database db = new Database();
    	Statement st = db.conn.createStatement();
    	ResultSet rs = st.executeQuery(sql);
    	while (rs.next()){
    		System.out.println(rs.getString(1));
    		System.out.println(rs.getString(2));
    	}
    	
    	db.closeConnection();
    }
}
