package de.jungblut.datastructure;

import java.util.LinkedList;

/**
 * Memory sparse hashtable implementation
 * 
 * @author thomas.jungblut
 * 
 * @param <K>
 * @param <V>
 */
public final class HashTable<K, V> {

	private LinkedList<Entry<K, V>>[] entries;
	private double loadFactor = 0.75;
	private int initialSize = 10;
	private int size = 0;
	private int threshold;

	@SuppressWarnings("unchecked")
	public HashTable() {
		entries = (LinkedList<Entry<K, V>>[]) new LinkedList[initialSize];
		threshold = (int) (loadFactor * entries.length);
	}

	@SuppressWarnings("unchecked")
	public HashTable(int size) {
		entries = (LinkedList<Entry<K, V>>[]) new LinkedList[size];
		threshold = (int) (loadFactor * entries.length);
	}

	public final void put(K key, V value) {
		addInternal(new Entry<K, V>(key, value), entries);
	}

	public final V get(K key) {
		int bucket = getBucketIndexFor(key.hashCode(), entries.length);
		LinkedList<Entry<K, V>> list = entries[bucket];
		if (list == null) {
			return null;
		} else {
			for (Entry<K, V> entry : list) {
				if (entry.getKey().equals(key))
					return entry.getValue();
			}
		}
		return null;
	}

	private final void addInternal(Entry<K, V> entry,
			LinkedList<Entry<K, V>>[] entryArray) {
		int bucket = getBucketIndexFor(entry.getKey().hashCode(),
				entryArray.length);
		if (entryArray[bucket] == null) {
			entryArray[bucket] = new LinkedList<Entry<K, V>>();
		}

		int index = getIndexForKey(entry.getKey(), entryArray[bucket]);

		if (index == -1) {
			entryArray[bucket].add(entry);
			size++;
		} else {
			entryArray[bucket].set(index, entry);
		}

		if (size > threshold) {
			rehash();
		}
	}

	private final int getIndexForKey(K key, LinkedList<Entry<K, V>> list) {
		int count = -1;
		for (Entry<K, V> element : list) {
			count++;
			if (element.getKey().equals(key)) {
				return count;
			}
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	private final void rehash() {
		// we're just doubling the size available now
		LinkedList<Entry<K, V>>[] temp = (LinkedList<Entry<K, V>>[]) new LinkedList[entries.length * 2];
		threshold = (int) (loadFactor * temp.length);
		size = 0;
		for (LinkedList<Entry<K, V>> list : entries) {
			if (list != null) {
				for (Entry<K, V> entry : list) {
					addInternal(entry, temp);
				}
			}
		}
		entries = temp;
	}

	public final boolean contains(K key) {
		int bucket = getBucketIndexFor(key.hashCode(), entries.length);
		if (entries[bucket] != null) {
			for (Entry<K, V> s : entries[bucket]) {
				if (key.equals(s.getKey()))
					return true;
			}
		}
		return false;
	}

	private final int getBucketIndexFor(int hash, int size) {
		return Math.abs(hash % (size - 1));
	}

	private static final class Entry<K, V> {
		private final K k;
		private final V v;

		public Entry(K k, V v) {
			super();
			this.k = k;
			this.v = v;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((k == null) ? 0 : k.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			@SuppressWarnings("rawtypes")
			Entry other = (Entry) obj;
			if (k == null) {
				if (other.k != null)
					return false;
			} else if (!k.equals(other.k))
				return false;
			return true;
		}

		public K getKey() {
			return k;
		}

		public V getValue() {
			return v;
		}
	}
}
