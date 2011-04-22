package trie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ardverk.collection.CharArrayKeyAnalyzer;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;
import org.ardverk.collection.Trie;
import org.joda.time.LocalTime;


public class TestTrie {
	public static void main(String[] args) {

		char[] a1 = { 'a', 'b', 'c' };
		char[] a2 = { 'd', 'e', 'f' };
		
	
		Trie<char[], String> trie = new PatriciaTrie<char[], String>(CharArrayKeyAnalyzer.INSTANCE);
		trie.put(a1, "a1");
		trie.put(a2, "a2");
		
		System.out.println(trie.get(a1)); //a1
		System.out.println(trie.get(a2)); //a2
		
		trie.remove(a1);
		System.out.println(trie.get(a1)); //a2
		
	}
}
