package com.matthew.maps;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is a curious variant of the Map where each "Bucket" is in one of three states: Empty, has one node, or contains another map.
 * Thus the term "RecursiveMap" because each bucket could possibly be another map itself. This gains the benefit of no comparisons in a bucket
 * like the BucketingMap, nor a collision method in a single node array implementation. So, one has essentially O(1) lookups for an element.
 * Each map has a depth which begins at 1, and the depth increases by 1 as a bucket changes to a map state. The depth is then used
 * to compute a new hashcode for each node in a deeper map (this is obviously necessary since two nodes only map to the same bucket index
 * if they had a hashcode % length that was the same, so this will *hopefully* change the bucket index). 
 * 
 * The big drawback of this is that two distinct nodes that have the SAME hashcode will cause a stackoverflow error because it will infinitely
 * remap the elements in a bucket because the elements are different by equality, but same by hashcode.
 * 
 * @author Matthew Meacham
 *
 * @param <K> The type of the keys
 * @param <V> The type of the values
 */
public class RecursiveMap<K, V> implements Map<K, V> {
	
	private final int DEFAULT_NUMBER_OF_BUCKETS = 32;
		
	private Bucket<K, V>[] buckets;
	private int size = 0;
	
	private final int DEPTH;
	
	public enum BucketState {
		EMPTY, ONE_NODE, REMAPPED_NODES
	}
	
	private RecursiveMap(int depth) {
		this.DEPTH = depth;
		this.buckets = createBuckets(DEFAULT_NUMBER_OF_BUCKETS);
	}
	
	public RecursiveMap() {
		this(1);
	}
	
	@Override
	public void clear() {
		this.buckets = createBuckets(this.buckets.length);
		this.size = 0;
	}

	@Override
	public boolean containsKey(Object key) {
		int hashIndex = hashKey(key) % this.buckets.length;
		
		Bucket<K, V> bucket = this.buckets[hashIndex];
		
		if (bucket.getBucketState() == BucketState.ONE_NODE) {
			return bucket.getOneNodePayload().getKey().equals(key);
		}
		
		if (bucket.getBucketState() == BucketState.REMAPPED_NODES) {
			return bucket.getRemappedNodesPayload().containsKey(key);
		}
		
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		for (Bucket<K, V> bucket : this.buckets) {
			if (bucket.getBucketState() == BucketState.ONE_NODE) {
				if (bucket.getOneNodePayload().getValue().equals(value)) {
					return true;
				}
			} else if (bucket.getBucketState() == BucketState.REMAPPED_NODES) {
				if (bucket.getRemappedNodesPayload().containsValue(value)) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return Arrays.stream(this.buckets)
				.<Stream<Entry<K, V>>>map(bucket -> {
					if (bucket.getBucketState() == BucketState.ONE_NODE) return Stream.of(bucket.getOneNodePayload());
					if (bucket.getBucketState() == BucketState.REMAPPED_NODES) return bucket.getRemappedNodesPayload().entrySet().stream();
					return Stream.empty();
				})
				.flatMap(Function.identity())
				.collect(Collectors.toSet());
	}

	@Override
	public V get(Object key) {
		int hashIndex = hashKey(key) % this.buckets.length;
		Bucket<K, V> bucket = this.buckets[hashIndex];
		
		if (bucket.getBucketState() == BucketState.ONE_NODE) {
			if (bucket.getOneNodePayload().getKey().equals(key)) {
				return bucket.getOneNodePayload().getValue();
			} else {
				return null;
			}
		}
		
		if (bucket.getBucketState() == BucketState.REMAPPED_NODES) {
			return bucket.getRemappedNodesPayload().get(key);
		}
		
		return null;
	}

	@Override
	public boolean isEmpty() {
		return this.size == 0;
	}

	@Override
	public Set<K> keySet() {
		return Arrays.stream(this.buckets)
				.<Stream<K>>map(bucket -> {
					if (bucket.getBucketState() == BucketState.ONE_NODE) return Stream.of(bucket.getOneNodePayload().getKey());
					if (bucket.getBucketState() == BucketState.REMAPPED_NODES) return bucket.getRemappedNodesPayload().keySet().stream();
					return Stream.empty();
				})
				.flatMap(Function.identity())
				.collect(Collectors.toSet());
	}

	@Override
	public V put(K key, V value) {
		int hashIndex = this.hashKey(key) % this.buckets.length;
		Bucket<K, V> bucket = this.buckets[hashIndex];
		
		if (bucket.getBucketState() == BucketState.EMPTY) {
			bucket.setBucketState(BucketState.ONE_NODE);
			bucket.setOneNodePayload(new Node<K, V>(key, value));
			this.size++;
		} else if (bucket.getBucketState() == BucketState.ONE_NODE) {
			Node<K, V> oneNodePayload = bucket.getOneNodePayload();
			
			if (oneNodePayload.getKey().equals(key)) {
				oneNodePayload.setValue(value);
			} else {
				bucket.setBucketState(BucketState.REMAPPED_NODES);
				bucket.setRemappedNodesPayload(new RecursiveMap<K, V>());
				
				bucket.getRemappedNodesPayload().put(oneNodePayload.getKey(), oneNodePayload.getValue());
				bucket.getRemappedNodesPayload().put(key, value);

				this.size++;
			}
		} else if (bucket.getBucketState() == BucketState.REMAPPED_NODES) {
			bucket.getRemappedNodesPayload().put(key, value);
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
		int hashIndex = this.hashKey(key) % this.buckets.length;
		Bucket<K, V> bucket = this.buckets[hashIndex];
		
		if (bucket.getBucketState() == BucketState.ONE_NODE) {
			Node<K, V> node = bucket.getOneNodePayload();
			
			if (node.getKey().equals(key)) {
				bucket.setBucketState(BucketState.EMPTY);
				this.size--;
				return node.getValue();
			}
		}
		
		if (bucket.getBucketState() == BucketState.REMAPPED_NODES) {
			V removeResult = bucket.getRemappedNodesPayload().remove(key);
			
			// Change it to a one node if there is only one node
			if (bucket.getRemappedNodesPayload().size() == 1) {
				Node<K, V> node = (Node<K, V>) bucket.getRemappedNodesPayload().entrySet().stream().findAny().get();
				
				bucket.setBucketState(BucketState.ONE_NODE);
				bucket.setOneNodePayload(node);
			}
			
			return removeResult;
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
				.<Stream<V>>map(bucket -> {
					if (bucket.getBucketState() == BucketState.ONE_NODE) return Stream.of(bucket.getOneNodePayload().getValue());
					if (bucket.getBucketState() == BucketState.REMAPPED_NODES) return bucket.getRemappedNodesPayload().values().stream();
					return Stream.empty();
				})
				.flatMap(Function.identity())
				.collect(Collectors.toSet());
	}
	
	private int hashKey(Object key) {
		return (int) (key.hashCode() * Math.PI * this.DEPTH);
	}
	
	@SuppressWarnings("unchecked")
	private Bucket<K, V>[] createBuckets(int size) {
		Bucket<K, V>[] newBuckets = (Bucket<K, V>[]) new Bucket[size];
		for (int i = 0; i < size; i++) {
			newBuckets[i] = new Bucket<K, V>();
		}
		return newBuckets;
	}
	
	
	
	static class Bucket<K, V> {
		
		
		private BucketState bucketState = BucketState.EMPTY;
		
		// TODO it would be great if we used the visitor pattern here or something so that I don't require to separate methods for the payload retrieval
		
		private Node<K, V> oneNodePayload;
		
		public Node<K, V> getOneNodePayload() {
			if (bucketState != BucketState.ONE_NODE) return null;
			return oneNodePayload;
		}
		
		public void setOneNodePayload(Node<K, V> oneNodePayload) {
			this.oneNodePayload = oneNodePayload;
		}
		
		private RecursiveMap<K, V> remappedNodesPayload;
		
		public RecursiveMap<K, V> getRemappedNodesPayload() {
			if (bucketState != BucketState.REMAPPED_NODES) return null;
			return remappedNodesPayload;
		}
		
		public void setRemappedNodesPayload(RecursiveMap<K, V> remappedNodesPayload) {
			this.remappedNodesPayload = remappedNodesPayload;
		}
	
		public BucketState getBucketState() {
			return this.bucketState;
		}
		
		public void setBucketState(BucketState bucketState) {
			this.bucketState = bucketState;
		}
	}
	
	static class Node<K, V> implements Map.Entry<K, V> {

		final K key;
		V value;

		public Node(K key, V value) {
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
