package de.jungblut.datastructure.trie;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Fast model class with convenient key (String instead of charsequence or other
 * hacky thingz). Supports document merges on duplicate keys.
 * 
 * @author thomas.jungblut
 * 
 * @param <T>
 */
public final class RadixTrieNode<T> implements Comparable<RadixTrieNode<T>> {

	// the key which leads to this node, is a subset of value
	private String key;

	/*
	 * The value that would result if this is a leaf. If this is just a branch
	 * value is null. This is just a concat of the paths which leads to this
	 * node. Used for wildcard matching to get the distance between an input and
	 * this leaf.
	 */
	private String value;

	// list that contains all the docs if it is a leaf.
	// this is an arraylist for typesafty and convenience
	private ArrayList<T> doc;

	private RadixTrieNode<T>[] children;

	private RadixTrieNode() {
		super();
	}

	@SuppressWarnings("unchecked")
	final RadixTrieNode<T> addChild(RadixTrieNode<T> child) {
		if (children == null) {
			children = new RadixTrieNode[1];
		} else {
			ensureSize(children.length + 1);
		}
		children[children.length - 1] = child;
		Arrays.sort(children);
		return child;
	}

	final RadixTrieNode<T> purgeListAndAddChild(RadixTrieNode<T> child) {
		children = null;
		return addChild(child);
	}

	@SuppressWarnings("unchecked")
	final void ensureSize(int length) {
		if (length > children.length) {
			RadixTrieNode<T>[] temp = new RadixTrieNode[length];
			System.arraycopy(this.children, 0, temp, 0, children.length);
			this.children = temp;
		}
	}

	final void mergeDocs(T[] additionalDocs) {
		ArrayList<T> temp;
		if (doc == null) {
			temp = new ArrayList<T>(additionalDocs.length);
		} else {
			temp = new ArrayList<T>(doc.size() + additionalDocs.length);
			temp.addAll(doc);
		}
		for (T element : additionalDocs)
			temp.add(element);
		this.doc = temp;
	}

	final boolean hasChildren() {
		if (children != null && !(children.length == 0))
			return true;
		else
			return false;
	}

	public static final <T> RadixTrieNode<T> createTrieNode(String key, String value,
			T... documents) {
		RadixTrieNode<T> node = new RadixTrieNode<T>();
		node.key = key.intern();
		node.value = value;
		node.doc = new ArrayList<T>();
		for (T element : documents)
			node.doc.add(element);
		return node;
	}

	public static final <T> RadixTrieNode<T> createTrieNode(String key, String value,
			ArrayList<T> documents) {
		RadixTrieNode<T> node = new RadixTrieNode<T>();
		node.key = key.intern();
		node.value = value;
		node.doc = documents;
		return node;
	}

	public int getNumberOfMatchingCharacters(String key) {
		int numberOfMatchingCharacters = 0;
		while (numberOfMatchingCharacters < key.length()
				&& numberOfMatchingCharacters < this.getKey().length()) {
			if (key.charAt(numberOfMatchingCharacters) != this.getKey().charAt(
					numberOfMatchingCharacters)) {
				break;
			}
			numberOfMatchingCharacters++;
		}
		return numberOfMatchingCharacters;
	}

	static final <T> RadixTrieNode<T> createRootTrieNode() {
		RadixTrieNode<T> radixTrieNode = new RadixTrieNode<T>();
		radixTrieNode.setKey("");
		return radixTrieNode;
	}

	// length sorting
	@Override
	public final int compareTo(RadixTrieNode<T> o) {
		if (this.key.length() < o.key.length())
			return -1;
		else if (this.key.length() > o.key.length())
			return 1;
		else
			return 0;
	}

	@Override
	public final String toString() {
		return "RadixTrieNode [" + (key != null ? "key=" + key : "") + "]";
	}

	public final String getKey() {
		return key;
	}

	public final void setKey(String key) {
		this.key = key.intern();
	}

	public final String getValue() {
		return value;
	}

	public final void setValue(String value) {
		this.value = value;
	}

	public final ArrayList<T> getDoc() {
		return doc;
	}

	public final void setDoc(ArrayList<T> doc) {
		this.doc = doc;
	}

	public final void setDocArray(T[] doc) {
		this.doc = new ArrayList<T>(doc.length);
		for (T element : doc)
			this.doc.add(element);
	}

	public RadixTrieNode<T>[] getChildren() {
		return children;
	}

	public void setChildren(RadixTrieNode<T>[] children) {
		this.children = children;
	}

}
