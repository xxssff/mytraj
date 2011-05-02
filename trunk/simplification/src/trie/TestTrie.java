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

		Integer[] a1 = { 1, 2, 3 };
		Integer[] a2 = { 1, 5, 6};
		
	
		Trie<Integer[], String> trie = new PatriciaTrie<Integer[], String>();
		trie.put(a1, "a1");
		trie.put(a2, "a2");
		
		System.out.println(trie.get(a1)); //a1
		System.out.println(trie.get(a2)); //a2
		
		trie.remove(a1);
		System.out.println(trie.get(a1)); //a2
		
	}
}
