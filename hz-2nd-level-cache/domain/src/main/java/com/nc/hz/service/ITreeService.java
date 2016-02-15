package com.nc.hz.service;

import java.util.List;

import com.nc.hz.domain.mixed_regions.VersionedTree;

public interface ITreeService {

	List<VersionedTree> findWith(String label);

	VersionedTree get(Integer id);

	void save(VersionedTree tree);

}
