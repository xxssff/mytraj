package entity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import Exceptions.WrongConfException;

public class ConfReader {

	public HashMap<String, String> read(String inputFile) throws Exception {
		HashMap<String, String> conf = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(inputFile));

		// loop meta infor
		String line;
		while ((line = br.readLine()).startsWith("#")) {
			// do nothing
		}
		String outFile = line;
		conf.put("outFile", outFile);
		conf.put("systemTable", br.readLine());
		conf.put("eps", br.readLine());
		conf.put("minPts", br.readLine());
		conf.put("tau", br.readLine());
		conf.put("k", br.readLine());
		StringTokenizer st = new StringTokenizer(br.readLine(), " ");
		if (st.countTokens() != 3) {
			throw new WrongConfException(
					"Alpha, beta and Gamma should be provided");
		}
		conf.put("alpha", st.nextToken());
		conf.put("beta", st.nextToken());
		conf.put("gamma", st.nextToken());

		st = new StringTokenizer(br.readLine(), " ");
		if (st.countTokens() != 2) {
			throw new WrongConfException(
					"Start and end time should be provided");
		}
		conf.put("ts", st.nextToken());
		conf.put("te", st.nextToken());
		return conf;
	}
}
