package trie;

public class StringNotExistException extends Exception {
	public StringNotExistException(String str){
		System.err.println(str);
	}
}
