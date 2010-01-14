package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * 
 * @author Xiaohui Assumed line start from number 0
 */
public class QueryGen {
	public static void main(String[] args) throws Exception {

		long seed1 = 123854398;
		long seed2 = 898720792;
		int totalLine = 999;
		int numQueries = 100; // number of queries to generate
		int numSeq = 100; // num of road ids to take
		String filename = "D:/research/traj_indexing/Traj_Olden_1k_3k.txt";
		String outfile = "D:/research/traj_indexing/Traj_Olden_1k_3k_q100.txt";
		BufferedReader br = new BufferedReader(new FileReader(filename));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
		Random rand = new Random(seed1);
		int currLine = 0;
		// generate random number for 100 times
		for (int i = 0; i < numQueries; i++) {
			int linenumber = (rand.nextInt(10) + 10 * i) % totalLine;
			// repeat moving cursor to this line number
			for (int j = 0; j < linenumber - currLine; j++) {
				br.readLine();
			}
			currLine = linenumber;
			System.out.println("linenumber: " + linenumber);
			String line = br.readLine();
			if (line != null) {
				StringTokenizer st = new StringTokenizer(line, " ");
				int numTokens = st.countTokens();
				Random r1 = new Random(seed2);
				int startToken = r1.nextInt(numTokens - numSeq - 1) + 1;
				// repeat moving cursor to the token position
				for (int k = 0; k < startToken; k++) {
					st.nextElement();
				}
				// start writing
//				String str2Write = linenumber + "\t";
				String str2Write="";
				for (int ntoken=0; ntoken<numSeq; ntoken++){
					str2Write += st.nextToken() + " ";
				}
				bw.append(str2Write);
				bw.newLine();
			}
		}
		// close reader and writer
		br.close();
		bw.close();
		System.out.println("Done!");
	}
}
