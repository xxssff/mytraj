/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author ceikute
 */
public class TestData {
    public static void main(String[] args) throws Exception {
        Data d = new Data();
        HashMap hm = d.getDefinedTrajectories("simpl_data_double_1_25", "00:00:00", "00:01:15", 0);


        System.out.println(hm.toString());
        
    }
}
