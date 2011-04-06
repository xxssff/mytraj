package entity;

import java.io.BufferedWriter;
import java.io.IOException;

public class Statistics {
	public int numGroups = 0;
	public int numMos = 0;
	public int numDataPoints = 0;
	public String startTime = "";
	public String endTime = "";
	public double elapsedTime = 0; //second

	public void toFile(BufferedWriter bw) throws Exception {
//		bw.write("start time: " + startTime);
//		bw.newLine();
//		bw.write("end time: " + endTime);
//		bw.newLine();
		bw.write("Num Groups: " + numGroups);
		bw.newLine();
		bw.write("Num Mos: " + numMos);
		bw.newLine();
		bw.write("Num DataPoints: " + numDataPoints);
		bw.newLine();
		bw.write("Elapsed Time: " + elapsedTime);
		bw.newLine();
	}
}
