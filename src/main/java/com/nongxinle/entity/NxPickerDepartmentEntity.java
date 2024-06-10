package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-26 16:23
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;


@Setter@Getter@ToString

public class NxPickerDepartmentEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *
	 */
	private Integer pickerUserId;

	private List<NxDepartmentEntity> nxDepartmentEntities;


}
