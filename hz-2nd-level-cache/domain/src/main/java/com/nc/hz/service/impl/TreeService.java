package com.nc.hz.service.impl;

import java.util.List;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateOperations;
import org.springframework.stereotype.Component;

import com.nc.hz.domain.mixed_regions.VersionedTree;
import com.nc.hz.service.ITreeService;

@Component
public class TreeService implements ITreeService {

	@Autowired
	HibernateOperations ops;

	@SuppressWarnings("unchecked")
	@Override
	public List<VersionedTree> findWith(String label) {
		DetachedCriteria dc = DetachedCriteria.forClass(VersionedTree.class).add(Property.forName("label").eq(label));
		dc.createAlias("nodes", "nodes", CriteriaSpecification.LEFT_JOIN);

		return (List<VersionedTree>) ops.findByCriteria(dc);
		// DetachedCriteria criteria =
		// DetachedCriteria.forClass(VersionedTree.class).add(Property.forName("label").eq(label));
		// criteria.setProjection(Projections.id());
		//
		// List<Integer> ids = (List<Integer>) ops.findByCriteria(criteria);
		// List<VersionedTree> rv;
		//
		// if (!ids.isEmpty()) {
		// DetachedCriteria dc = DetachedCriteria.forClass(VersionedTree.class);
		// dc.createAlias("nodes", "nodes", CriteriaSpecification.LEFT_JOIN);
		// dc.add(Property.forName("id").in(ids));
		// dc.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		// rv = (List<VersionedTree>) ops.findByCriteria(dc);
		// } else {
		// rv = Collections.emptyList();
		// }
		//
		// return rv;
	}

	@Override
	public VersionedTree get(Integer id) {
		return ops.get(VersionedTree.class, id);
	}

	@Override
	// @Transactional
	public void save(VersionedTree tree) {
		ops.saveOrUpdate(tree);
	}

}