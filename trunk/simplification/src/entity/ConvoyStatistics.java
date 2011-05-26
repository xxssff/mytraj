package entity;

import java.io.BufferedWriter;

public class ConvoyStatistics {
	public int numClusters = 0;
	public int numConvoys = 0;
	public int numMos = 0;
	public int numDataPoints = 0;
	public String startTime = "";
	public String endTime = "";
	public double elapsedTime = 0; // second
	public double loadDataTime = 0;
	public int numCandidates = 0;
	public long memUsage = 0;

	public void toFile(BufferedWriter bw) throws Exception {
		bw.write("Num Convoys: " + numConvoys);
		bw.newLine();
		bw.write("Num Clusters: " + numClusters);
		bw.newLine();
		bw.write("Num Mos: " + numMos);
		bw.newLine();
		bw.write("Num DataPoints: " + numDataPoints);
		bw.newLine();
//		bw.write("Num Checked Candidates: " + numCandidates);
//		bw.newLine();

		bw.write("Loading Data Time: " + loadDataTime);
		bw.newLine();
		bw.write("Elapsed Time: " + elapsedTime);
		bw.newLine();
//		bw.write("Max Mem Usage: " + memUsage / 1024.0 / 1024.0 + "MBybtes");
//		bw.newLine();
	}
}
