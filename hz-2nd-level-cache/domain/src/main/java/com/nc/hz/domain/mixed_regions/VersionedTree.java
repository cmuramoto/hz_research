package com.nc.hz.domain.mixed_regions;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "TB_VERSIONED_TREE")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = RegionSymbols.VERSIONED)
public class VersionedTree {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "tree_gen")
	@TableGenerator(name = "tree_gen", pkColumnName = "NM_TABLE", pkColumnValue = "TB_VERSIONED_TREE", valueColumnName = "NR_SEQ", initialValue = 1, allocationSize = 10)
	@Column(name = "ID")
	Integer id;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = RegionSymbols.UNVERSIONED)
	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "CD_VTREE", nullable = false)
	@Cascade({ CascadeType.ALL, CascadeType.LOCK, CascadeType.DETACH })
	@Fetch(FetchMode.SUBSELECT)
	Set<UnversionedNode> nodes = new HashSet<>();

	@Column(name = "NM_LABEL")
	String label;

	@Version
	@Column(name = "NM_VERSION")
	Long version;

	public void add(UnversionedNode node) {
		nodes.add(node);
	}

	public Integer getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public Set<UnversionedNode> getNodes() {
		return nodes;
	}

	public Long getVersion() {
		return version;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setNodes(Set<UnversionedNode> nodes) {
		this.nodes = nodes;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

}