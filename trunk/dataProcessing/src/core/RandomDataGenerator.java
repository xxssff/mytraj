/**
 * 
 */
package core;

import io.FileHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

/**
 * @author xiaohui <br>
 *         This class generates random sequences to simulate trajectories.
 *         Trajectories will be generated in the following format: traj num,
 *         r?r? -- question marks are random numbers.
 * 
 *         To make sure trajectories with consecutive road id', e.g. <r1, r2,
 *         r2>, don't appear, a variable called last point is maintained.
 */
public class RandomDataGenerator {
	long seed = 15739479;
	Random rand;
	int lastPoint;

	public RandomDataGenerator() {
		rand = new Random(seed);
		lastPoint = 0;
	}

	/**
	 * 
	 * @param numSeq
	 * @param roadSize
	 * @return A random trajectory sequence roadSize is the number of roads in
	 *         the network, which defines the range of road id's in the
	 *         trajectories.
	 */
	public Vector<String> genSequence(int numSeq, int seqLength, int roadSize) {
		Vector<String> strVector = new Vector<String>(numSeq);
		for (int trajID = 1; trajID <= numSeq; trajID++) {
			int num = rand.nextInt(roadSize);
			lastPoint = num;
			// StringBuilder sb = new StringBuilder(seqLength*15);
			String str = "##" + trajID + "," + "r" + num + " ";
			// sb.append("##" + trajID + "," + "r" + num + " ");
			for (int j = 1; j < seqLength; j++) {
				num = rand.nextInt(roadSize);
				while (num == lastPoint) {
					num = rand.nextInt(roadSize);
				}
				lastPoint = num;
				str += "r" + num + " ";

			}
			System.out.println("Processing: " + trajID);
			strVector.add(str);
		}
		return strVector;
	}

	public void genSequenceToFile(int numSeq, int seqLength, int roadSize,
			BufferedWriter bw) {
		// Vector<String> strVector = new Vector<String>(numSeq);
		for (int trajID = 1; trajID <= numSeq; trajID++) {
			int num = rand.nextInt(roadSize);
			lastPoint = num;
			// StringBuilder sb = new StringBuilder(seqLength*15);
			String str = "##" + trajID + "," + "r" + num + " ";
			// sb.append("##" + trajID + "," + "r" + num + " ");
			for (int j = 1; j < seqLength; j++) {
				num = rand.nextInt(roadSize);
				while (num == lastPoint) {
					num = rand.nextInt(roadSize);
				}
				lastPoint = num;
				str += "r" + num + " ";

			}
			System.out.println("Writing: " + trajID + "/" + numSeq);
			try {
				bw.append(str);
				bw.newLine();
			} catch (IOException e) {
				System.err.println("ERR in RandomDataGenerator.simNumSeqs()");
				e.printStackTrace();
			}
		}
	}

	public void simNumSeqsToFile(String dirname, int seqLength, int roadSize) {

//		int[] numSeqs = { 2000, 4000, 6000, 8000, 10000 }; // numseqs to sim
//		int[] numSeqs = {10000}; // numseqs to sim
		int [] numSeqs={6000};
		for (int numSeq : numSeqs) {
			System.out.println("simNumSeqs on " + numSeq);
			String filename = dirname + "syn" + numSeq + ".txt";
			FileHandler fh = new FileHandler();
			BufferedWriter bw = fh.getWriter(filename);
			genSequenceToFile(numSeq, seqLength, roadSize, bw);
			FileHandler.closeWriter(bw);
		}
	}

	public void simTrajSeqLength(String dirname, int numSeq, int roadSize){
//		int[] seqlengths = {2000, 4000, 6000, 8000};
		int[] seqlengths = {8000};
		for (int l : seqlengths){
			System.out.println("simTrajSeqLength on " + l);
			String filename = dirname + "trajLength" + l + ".txt";
			FileHandler fh = new FileHandler();
			BufferedWriter bw = fh.getWriter(filename);
			genSequenceToFile(numSeq, l, roadSize, bw);
			FileHandler.closeWriter(bw);
		}
	}
	public static void main(String[] args) throws Exception {
		//Destination directory name
		String dirname = "/home/zhoujian/traj/data1_small/";
		int roadSize = 1000000; // 1 million roads size
		int seqLength = 6000; //default length of every trajectory sequence
		int numSeq = 6000; //default number of sequences
		RandomDataGenerator gen = new RandomDataGenerator();
//		gen.simNumSeqsToFile(dirname, seqLength, roadSize);
		gen.simTrajSeqLength(dirname, numSeq, roadSize);
	}
}
