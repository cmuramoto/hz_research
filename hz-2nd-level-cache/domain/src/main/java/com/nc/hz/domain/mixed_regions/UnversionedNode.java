package com.nc.hz.domain.mixed_regions;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "TB_UNVERSIONED_NODE")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = RegionSymbols.UNVERSIONED)
public class UnversionedNode {

	@Id
	@Column(name = "ID")
	String label;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnversionedNode other = (UnversionedNode) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
