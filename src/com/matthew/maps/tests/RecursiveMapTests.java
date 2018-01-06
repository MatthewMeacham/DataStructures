package com.matthew.maps.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.matthew.maps.RecursiveMap;

class RecursiveMapTests {

	@Test
	void testClear() {
		// arrange
		Map<Integer, Integer> map = new RecursiveMap<>();
		map.put(1, 1);
		map.put(2, 2);

		// act
		map.clear();

		// assert
		assertEquals(0, map.size());
		assertFalse(map.containsKey(1));
	}

	@Test
	void testContainsKey() {
		// arrange
		Map<Integer, Integer> map = new RecursiveMap<>();
		map.put(1, 1);
		map.put(2, 2);

		// act

		// assert
		assertTrue(map.containsKey(1));
		assertTrue(map.containsKey(2));
		assertFalse(map.containsKey(3));
	}

	@Test
	void testContainsValue() {
		// arrange
		Map<Integer, Integer> map = new RecursiveMap<>();
		map.put(1, 5);
		map.put(2, 6);

		// act

		// assert
		assertTrue(map.containsValue(5));
		assertTrue(map.containsValue(6));
		assertFalse(map.containsValue(3));
	}

	@Test
	void testEntrySet() {
		// arrange
		Map<Integer, Integer> map = new RecursiveMap<>();
		map.put(1, 1);
		map.put(2, 2);

		// act
		Set<Entry<Integer, Integer>> entrySet = map.entrySet();

		// assert
		assertEquals(2, entrySet.size());
		entrySet.forEach(entry -> assertTrue(entry.getKey() == 1 || entry.getKey() == 2));
		entrySet.forEach(entry -> assertTrue(entry.getValue() == 1 || entry.getValue() == 2));
	}

	@Test
	void testGet() {
		// arrange
		Map<Integer, Integer> map = new RecursiveMap<>();
		map.put(1, 1);
		map.put(2, 4);

		// act

		// assert
		assertTrue(1 == map.get(1));
		assertTrue(4 == map.get(2));
	}

	@Test
	void testIsEmpty() {
		// arrange
		Map<Integer, Integer> map = new RecursiveMap<>();
		
		// act
		
		// assert
		assertTrue(map.isEmpty());
		map.put(1, 1);		
		assertFalse(map.isEmpty());
	}

	@Test
	void testKeySet() {
		// arrange
		Map<Integer, Integer> map = new RecursiveMap<>();
		map.put(1, 1);
		map.put(2, 4);
		
		// act
		Set<Integer> keySet = map.keySet();
		
		// assert
		assertEquals(2, keySet.size());
		keySet.forEach(key -> assertTrue(key == 1 || key == 2));
	}

	@Test
	void testPut() {
		// arrange
		Map<Integer, Integer> map = new RecursiveMap<>();
		map.put(1, 1);
		
		// act
		map.put(2, 2);
		map.put(1, 5);
		
		// assert
		assertEquals(2, map.size());
		assertTrue(5 == map.get(1));
		assertTrue(2 == map.get(2));
	}

	@Test
	void testPutAll() {
		// arrange
		Map<Integer, Integer> map = new RecursiveMap<>();

		// act
		map.putAll(Map.of(1, 1, 2, 2, 3, 3, 4, 4));
		
		// assert
		assertEquals(4, map.size());
		assertTrue(1 == map.get(1));
		assertTrue(2 == map.get(2));
		assertTrue(3 == map.get(3));
		assertTrue(4 == map.get(4));
	}

	@Test
	void testRemove() {
		// arrange
		Map<Integer, Integer> map = new RecursiveMap<>();
		map.put(1, 1);
		map.put(2, 2);
		
		// act
		map.remove(1);
		
		// assert
		assertEquals(1, map.size());
		assertTrue(2 == map.get(2));
		assertFalse(map.containsKey(1));
		assertFalse(map.containsValue(1));
	}

	@Test
	void testSize() {
		// arrange
		Map<Integer, Integer> map = new RecursiveMap<>();
		map.put(1, 1);
		map.put(2, 2);
		
		// act
		
		// assert
		assertEquals(2, map.size());
	}

	@Test
	void testValues() {
		// arrange
		Map<Integer, Integer> map = new RecursiveMap<>();
		map.put(1, 1);
		map.put(2, 4);
		
		// act
		Collection<Integer> values = map.values();
		
		// assert
		values.forEach(value -> assertTrue(value == 1 || value == 4));
	}
	
	@Test
	void testRemapping() {
		// arrange
		Map<Integer, Integer> map = new RecursiveMap<>();
		int assumedMapSize = 32;
		int value2 = assumedMapSize + 1;
		int value3 = assumedMapSize * 2 + 1;
		map.put(1, 1);
		map.put(value2, value2); 
		map.put(value3, value3);
		
		// act
		
		// assert
		assertTrue(1 == map.get(1));
		assertTrue(value2 == map.get(value2));
		assertTrue(value3 == map.get(value3));
	}

}
