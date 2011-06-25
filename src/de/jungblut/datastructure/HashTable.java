package de.jungblut.datastructure;

import java.util.LinkedList;

public class HashTable {

	private LinkedList<String>[] entries;
	private double loadFactor = 0.75;
	private int initialSize = 10;
	private int size = 0;
	private int threshold;

	@SuppressWarnings("unchecked")
	public HashTable() {
		entries = (LinkedList<String>[]) new LinkedList[initialSize];
		threshold = (int) (loadFactor * entries.length);
	}

	public void add(String key) {
		addInternal(key, entries);
	}

	private void addInternal(String key, LinkedList<String>[] entries) {
		int bucked = getBuckedIndexFor(key.hashCode(), entries.length);

		if (entries[bucked] == null) {
			entries[bucked] = new LinkedList<String>();
		}
		if (!entries[bucked].contains(key)) {
			entries[bucked].add(key);
			size++;
		}

		if (size > threshold) {
			rehash();
		}
	}

	@SuppressWarnings("unchecked")
	private void rehash() {
		// we're just doubling the size available now
		LinkedList<String>[] temp = (LinkedList<String>[]) new LinkedList[entries.length * 2];
		threshold = (int) (loadFactor * temp.length);
		size = 0;
		for (LinkedList<String> list : entries) {
			if (list != null) {
				for (String s : list) {
					addInternal(s, temp);
				}
			}
		}
		entries = temp;
	}

	public boolean contains(String key) {
		int bucked = getBuckedIndexFor(key.hashCode(), entries.length);
		if (entries[bucked] != null) {
			for (String s : entries[bucked]) {
				if (key.equals(s))
					return true;
			}
		}
		return false;
	}

	private int getBuckedIndexFor(int hash, int size) {
		return Math.abs(hash % (size - 1));
	}

	public static void main(String[] args) {
		HashTable table = new HashTable();
		table.add("ABCDEa123abc");
		System.out.println(table.contains("ABCDEa123abc"));
		table.add("ABCDFB123abc");
		System.out.println(table.contains("ABCDFB123abc"));
		table.add("ABC");
		System.out.println(table.contains("ABC"));
		table.add("CAB");
		System.out.println(table.contains("CAB"));
		table.add("BD");
		System.out.println(table.contains("BD"));
		// testing the rehashing

		table.add("ASDASDA");
		table.add("ASDQWEAGGAS");
		table.add("ASDHASLÖKA");
		table.add("ASKZHO");
		table.add("kfgkgk");
		table.add("jdfjsj");

		table.add("asdffdsferahhhh");
		table.add("jkasjkaasdf");
		table.add("adsjklöajka");
		table.add("ASsdjksjksjkKZHO");
		table.add("fgajknanaöjk");
		System.out.println(table.contains("fgajknanaöjk"));
		System.out.println(table.contains("CAB"));
		table.add("jdfjshhhhhhhhj");

	}

}
