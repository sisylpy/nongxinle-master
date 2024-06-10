package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 07-30 18:51
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;


@Setter@Getter@ToString

public class NxLettersEntity implements Serializable , Comparable{
	private static final long serialVersionUID = 1L;

	private String letter;

	private List<NxDepartmentIndependentGoodsEntity> nxDepIndependentGoodsEntities;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxLettersEntity that = (NxLettersEntity) o;
		return Objects.equals(letter, that.letter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(letter);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof NxLettersEntity) {
			NxLettersEntity e = (NxLettersEntity) o;
			return this.letter.compareTo(e.letter);
		}
		return 0;
	}
}
