/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package comparison;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author ceikute
 */
public class Compare {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        Util u = new Util();
        u.compareVaryingM();

        System.out.println("------------------------------------------");
        u.compareVaryingE();

//        String urlRaw = "C:\\paper_2\\list_comparision\\varying_m\\elk_0_5.out";
//        String urlSimpl = "C:\\paper_2\\list_comparision\\varying_m\\elk_50_5.out";
//        u.do_nDCG(urlRaw, urlSimpl);
    }
}
