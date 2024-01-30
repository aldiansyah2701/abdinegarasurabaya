package com.abdinegara.surabaya.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@MappedSuperclass
public class BaseEntity {
	
	@Column
	private Date createdDate;
	
	@Column
	private Date updateDate;

}
