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
    public static void main(String[] args) {
        String url = args[0];
        HandleFolder hf = new HandleFolder();
        String resUrl = hf.prehandleFiles(url);

    }

}
