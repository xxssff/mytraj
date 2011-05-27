/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package comparison;

import java.util.ArrayList;

/**
 *
 * @author ceikute
 */
public class Group {
    public int id;
    public double score;
    public ArrayList<Integer> members;

    public Group(){

    }

    public Group(int id, double score, ArrayList<Integer> members) {
        this.id = id;
        this.score = score;
        this.members = members;
    }
}
