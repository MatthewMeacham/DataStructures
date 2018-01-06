package com.matthew.maps;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is a simple implementation of a HashMap that uses the bucketing method. I did have some fun with Streams in this, so it's definitely
 * not incredibly performant.
 * 
 * @author Matthew Meacham
 *
 * @param <K> The type of the keys
 * @param <V> The type of the values
 */
public class BucketingMap<K, V> implements Map<K, V> {

	private static final int DEFAULT_INITIAL_NUMBER_OF_BUCKETS = 32;
	private static final int DEFAULT_SCALING_FACTOR = 2;
	private static final int DEFAULT_PREFERRED_BUCKET_SIZE = 5;
	private static final double DEFAULT_LOAD_FACTOR = 0.75d;
	
	private final int SCALING_FACTOR;
	private final int PREFERRED_BUCKET_SIZE;
	private final double LOAD_FACTOR;
	
	private List<Node<K, V>>[] buckets;

	private int size = 0;

	/**
	 * Creates an empty {@code BucketingMap} with the specified initial number of buckets, the specified scaling factor,
	 * the specified preferred bucket size, and the specified load factor
	 * 
	 * @param initialNumberOfBuckets The initial number of buckets
	 * @param scalingFactor The scaling factor
	 * @param preferredBucketSize The preferred size of the buckets
	 * @param loadFactor The load factor
	 * 
	 * @throws IllegalArgumentException if the initial number of buckets is less than 0
	 * or if the scaling factor is less than or equal to 1
	 * or if the preferred bucket size is less than or equal to 0
	 * or if the load factor is non-positive, greater than 1.0, or NaN
	 */
	public BucketingMap(int initialNumberOfBuckets, int scalingFactor, int preferredBucketSize, double loadFactor) {
		if (initialNumberOfBuckets <= 0) throw new IllegalArgumentException("initialNumberOfBuckets cannot be less than or equal to 0.");
		if (scalingFactor <= 1) throw new IllegalArgumentException("scalingFactor cannot be less than or equal to 1.");
		if (preferredBucketSize <= 0) throw new IllegalArgumentException("preferredBucketSize cannot be less than or equal to 0.");
		if (loadFactor <= 0.0d || loadFactor > 1.0d || Double.isNaN(loadFactor)) throw new IllegalAccessError("loadFactor must be a number and cannot be less than or equal to 0, and not greater than 1.");
		
		this.buckets = createNewBuckets(initialNumberOfBuckets);
		this.SCALING_FACTOR = scalingFactor;
		this.PREFERRED_BUCKET_SIZE = preferredBucketSize;
		this.LOAD_FACTOR = loadFactor;
	}
	
	/**
	 * Constructs an empty {@code BucketingMap} with the specified initial number of buckets, the specified scaling factor,
	 * the specified preferred bucket size, and the default load factor (0.75)
	 * 
	 * @param initialNumberOfBuckets the initial number of buckets
	 * @param scalingFactor The scaling factor
	 * @param preferredBucketSize The preferred size of the buckets
	 */
	public BucketingMap(int initialNumberOfBuckets, int scalingFactor, int preferredBucketSize) {
		this(initialNumberOfBuckets, scalingFactor, preferredBucketSize, DEFAULT_LOAD_FACTOR);
	}
	
	/**
	 * Constructs an empty {@code BucketingMap} with the specified initial number of buckets, the specified scaling factor, 
	 * the default preferred bucket size (5), and the default load factor (0.75)
	 * 
	 * @param initialNumberOfBuckets The initial number of buckets
	 * @param scalingFactor The scaling factor
	 */
	public BucketingMap(int initialNumberOfBuckets, int scalingFactor) {
		this(initialNumberOfBuckets, scalingFactor, DEFAULT_PREFERRED_BUCKET_SIZE);
	}
	
	/**
	 * Constructs an empty {@code BucketingMap} with the specified initial number of buckets, the default scaling factor (2),
	 * the default preferred bucket size (5), and the default load factor (0.75)
	 * 
	 * @param initialNumberOfBuckets The initial number of buckets
	 */
	public BucketingMap(int initialNumberOfBuckets) {
		this(initialNumberOfBuckets, DEFAULT_SCALING_FACTOR);
	}
	
	/**
	 * Construct an empty {@code BucketingMap} with the default initial number of buckets (32), the default scaling factor (2),
	 * the default preferred bucket size (5), and the default load factor (0.75)
	 */
	public BucketingMap() {
		this(DEFAULT_INITIAL_NUMBER_OF_BUCKETS);
	}

	@Override
	public void clear() {
		this.buckets = createNewBuckets(this.buckets.length);
		this.size = 0;
	}

	@Override
	public boolean containsKey(Object key) {
		int bucketIndex = key.hashCode() % this.buckets.length;
		if (Objects.isNull(this.buckets[bucketIndex])) {
			return false;
		}
		
		return this.buckets[bucketIndex].stream()
				.anyMatch(node -> node.getKey().equals(key));
	}

	@Override
	public boolean containsValue(Object value) {
		return Arrays.stream(this.buckets)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.anyMatch(node -> node.getValue().equals(value));
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return Arrays.stream(this.buckets)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.collect(Collectors.toSet());
	}

	@Override
	public V get(Object key) {
		int bucketIndex = key.hashCode() % this.buckets.length;
		return this.buckets[bucketIndex].stream()
				.filter(node -> node.getKey().equals(key))
				.findFirst()
				.map(Entry::getValue)
				.orElse(null); // :(
	}

	@Override
	public boolean isEmpty() {
		return this.size == 0;
	}

	@Override
	public Set<K> keySet() {
		return Arrays.stream(this.buckets)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.map(Entry::getKey)
				.collect(Collectors.toSet());
	}

	@Override
	public V put(K key, V value) {
		int hash = key.hashCode();
		int bucketIndex = hash % this.buckets.length;
		if (Objects.isNull(this.buckets[bucketIndex])) {
			this.buckets[bucketIndex] = new LinkedList<Node<K, V>>();
		}
		
		for (Node<K, V> nodes : this.buckets[bucketIndex]) {
			if (nodes.getKey().equals(key)) {
				nodes.setValue(value);
				return value;
			}
		}
		
		this.buckets[bucketIndex].add(new Node<>(hash, key, value));
		this.size++;
		
		if (needsResize()) {
			resize();
		}
		
		return value;
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public V remove(Object key) {
		int bucketIndex = key.hashCode() % this.buckets.length;
		if (Objects.isNull(this.buckets[bucketIndex])) {
			return null;
		}
		
		Iterator<Node<K, V>> iterator = this.buckets[bucketIndex].iterator();
		while (iterator.hasNext()) {
			Node<K, V> node = iterator.next();
			
			if (node.getKey().equals(key)) {
				iterator.remove();
				this.size--;
				return node.getValue();
			}
		}
		
		return null;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public Collection<V> values() {
		return Arrays.stream(this.buckets)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.map(Entry::getValue)
				.collect(Collectors.toSet());
	}
	
	private final boolean needsResize() {
		double bucketsTotalSize = (double) Arrays.stream(this.buckets)
				.mapToInt(bucket -> Math.max(Objects.isNull(bucket) ? 0 : bucket.size(), PREFERRED_BUCKET_SIZE))
				.sum();
		return this.size / bucketsTotalSize > LOAD_FACTOR;
	}

	private final void resize() {
		List<Node<K, V>>[] currentBuckets = this.buckets;
		List<Node<K, V>>[] newBuckets = createNewBuckets(currentBuckets.length * SCALING_FACTOR);

		for (List<Node<K, V>> bucket : currentBuckets) {
			for (Node<K, V> node : bucket) {
				int bucketIndex = node.hash % newBuckets.length;

				newBuckets[bucketIndex].add(node);
			}
		}

		this.buckets = newBuckets;
	}

	@SuppressWarnings("unchecked")
	private final List<Node<K, V>>[] createNewBuckets(int size) {
		return (LinkedList<Node<K, V>>[]) new LinkedList[size];
	}

	static class Node<K, V> implements Map.Entry<K, V> {

		final int hash;
		final K key;
		V value;

		public Node(int hash, K key, V value) {
			this.hash = hash;
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return this.key;
		}

		@Override
		public V getValue() {
			return this.value;
		}

		@Override
		public V setValue(V newValue) {
			this.value = newValue;
			return newValue;
		}
	}

}
