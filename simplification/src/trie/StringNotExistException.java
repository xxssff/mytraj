package trie;

import java.util.Arrays;

public class StringNotExistException extends Exception {
	public StringNotExistException(String str) {
		System.err.println(str);
	}

	public StringNotExistException(Object[] strArr) {
		System.out.println(Arrays.toString(strArr));
	}
}
