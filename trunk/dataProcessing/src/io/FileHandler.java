/**
 * 
 */
package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author xiaohui <br>
 *         IO utility
 */
public class FileHandler {
	
	

	public BufferedReader getReader(String filename) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			System.err.println("ERR in DataProcessing.FileHandler.getReader()");
			e.printStackTrace();
		}
		return br;
	}

	public BufferedWriter getWriter(String filename) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e) {
			System.err.println("ERR in DataProcessing.FileHandler.getWriter()");
			e.printStackTrace();
		}
		return bw;
	}

	public static void closeReader(BufferedReader br) {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				System.err
						.println("ERR in DataProcessing.FileHandler.closeReader()");
				e.printStackTrace();
			}
		}
	}

	public static void closeWriter(BufferedWriter bw) {
		if (bw != null) {
			try {
				bw.close();
			} catch (IOException e) {
				System.err
						.println("ERR in DataProcessing.FileHandler.closeWriter()");
				e.printStackTrace();
			}
		}
	}

}
