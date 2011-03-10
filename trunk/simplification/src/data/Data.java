/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package data;

import com.vividsolutions.jts.geom.Coordinate;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import simplification.Database;

/**
 *
 * @author ceikute
 */
public class Data {

    /**
     * method to retrieve defines trajectories from provided DB table and time range
     * type = {1, 0} : 1 - considering date and time, 0 - considering only time <br>
     * 
     *time format: "00:00:00" or "2001-01-01 00:00:00"<br>
     *
     *
     * @param tbName
     * @param timeFrom
     * @param timeTo
     * @param type 0
     * @return hashmap : route id -> arraylist of datapoints within provided time period
     * @throws Exception
     */
    public HashMap getDefinedTrajectories(String tbName, String timeFrom, String timeTo, int type) throws Exception {
        ArrayList<Integer> trajList = new ArrayList<Integer>();

        Database db = new Database();
        String select = "";

        if (type == 1) {
            select = "select * from simpl_get_defined_routes('" + tbName + "', '" + timeFrom + "', '" + timeTo + "') as p(routeid int8, mpx text, mpy text, time time, stamp timestamp, time0 bigint)";
        }
        else if (type == 0) {
            select = "select * from simpl_get_defined_routes_t('" + tbName + "', '" + timeFrom + "', '" + timeTo + "') as p(routeid int8, mpx text, mpy text, time time, stamp timestamp, time0 bigint)";
        }
        PreparedStatement ps = null;
        ResultSet result = null;

        ps = db.getConnection().prepareStatement(select);
        result = ps.executeQuery();

        HashMap hm = new HashMap();

        ArrayList<DataPoint> points = null;

        int routeId = -1;

        while (result.next()) {
            int routeNext = result.getInt("routeid");
            if (routeId == -1) {
                routeId = routeNext;
                points = new ArrayList<DataPoint>();

            }
            else if (routeId != routeNext) {
                hm.put(routeId, points);

                routeId = routeNext;
                points = new ArrayList<DataPoint>();
            }
            Coordinate c = new Coordinate(Double.parseDouble(result.getString("mpx")), Double.parseDouble(result.getString("mpy")));
            DataPoint dp = new DataPoint(c, result.getString("time"), result.getString("stamp"), result.getInt("time0"));
            points.add(dp);
        }
        hm.put(routeId, points);
        
        ps.close();
        result.close();
        db.closeConnection();

        return hm;
    }
}
