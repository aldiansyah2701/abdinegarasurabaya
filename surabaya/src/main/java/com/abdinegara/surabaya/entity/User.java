package com.abdinegara.surabaya.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity(name= "users")
@Table(name= "users")
public class User extends BaseEntity implements Serializable{
	
	private static final long serialVersionUID = 4211854570169058068L;
	
	@Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
	
	@Column(name = "name")
	private String name;
	
	@Column
	private String password;
	
	@Column(name= "is_active")
	private boolean isActive;
	
	@Column
	private String passwordData;
	
	@Column(name= "user_type")
	private String userType;

	@Column
	private String adminName;

	@Column
	private String adminPhone;

	@Column
	private String adminEmaill;

}
