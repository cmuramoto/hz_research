package com.nc.hz.support;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nc.hz.domain.mixed_regions.UnversionedNode;
import com.nc.hz.domain.mixed_regions.VersionedTree;
import com.nc.hz.service.ITreeService;

@Component
public class TestSupport {

	static void assertFalse(boolean value) {
		if (value) {
			throw new AssertionError();
		}
	}

	@Autowired
	ITreeService service;

	public void test_1667() throws IOException {
		VersionedTree tree = new VersionedTree();
		tree.setLabel("tree");
		UnversionedNode node = new UnversionedNode();
		node.setLabel("node#0");
		tree.add(node);

		service.save(tree);

		System.out.println("First Query");

		List<VersionedTree> trees = service.findWith("tree");

		assertFalse(trees.isEmpty());

		System.out.println("Second query");

		// Will NOT throw NPE
		trees = service.findWith("tree");

		assertFalse(trees.isEmpty());

		System.out.println("Worked");
	}
}
