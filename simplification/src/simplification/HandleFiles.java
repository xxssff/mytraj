/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simplification;

/**
 *
 * @author ceikute
 */
public class HandleFiles {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        String url = args[0];
        HandleFolder hf = new HandleFolder();
//        String resUrl = hf.removeWhiteSpacesInFiles(url);
        String resUrl = hf.generateSimplifiedVersion("C:\\routes\\data_routes", "C:\\routes\\data_routes_simpl");
//        System.out.println(resUrl);
//        hf.imoprtDataToDatabase(url + "\\result");
//        hf.getRoutes("1", "0");
//        hf.cutUserRoutes("C:\\routes\\test\\", "C:\\routes\\test_diff");
//             PrepareFiles pf = new PrepareFiles();
//        pf.prepareAllFiles("C:\\routes\\gps_car2_no_home\\result", "C:\\routes\\data", 0);
//        C:\routes\data_routes
//            pf.prepareAllFiles("C:\\routes\\data", "C:\\routes\\data_routes", 1);
//        String s = pf.correctTime("2018");
//        System.out.println(s);
//        Simplification s = new Simplification();
//        double d = s.distToSeg3D(-1.0, 3.0, 2.0, 1.0, 4.0, 0.0, 5.0, 1.0, 2.0);
//        double d = s.distToSeg3D(1.0, 1.0, 0.0, 6.0, 6.0, 0.0, 3.0, 0.0, 0.0);
//        System.out.println(d);
    }

}
