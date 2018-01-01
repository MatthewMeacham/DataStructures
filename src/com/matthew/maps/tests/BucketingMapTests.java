package com.matthew.maps.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.matthew.maps.BucketingMap;

class BucketingMapTests {

	@Test
	void testClear() {
		// arrange
		Map<Integer, Integer> map = new BucketingMap<>();

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
		Map<Integer, Integer> map = new BucketingMap<>();

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
		Map<Integer, Integer> map = new BucketingMap<>();

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
		Map<Integer, Integer> map = new BucketingMap<>();

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
		fail("Not yet implemented");
	}

	@Test
	void testIsEmpty() {
		fail("Not yet implemented");
	}

	@Test
	void testKeySet() {
		fail("Not yet implemented");
	}

	@Test
	void testPut() {
		fail("Not yet implemented");
	}

	@Test
	void testPutAll() {
		fail("Not yet implemented");
	}

	@Test
	void testRemove() {
		fail("Not yet implemented");
	}

	@Test
	void testSize() {
		fail("Not yet implemented");
	}

	@Test
	void testValues() {
		fail("Not yet implemented");
	}

}
