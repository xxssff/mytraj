package trie;

public class StringNotExistException extends Exception {
	public StringNotExistException(String str) {
		System.err.println(str);
	}

	public StringNotExistException(String[] strArr) {
		for (String s : strArr)
			System.err.print(s + " ");
		System.out.println();
	}
}
