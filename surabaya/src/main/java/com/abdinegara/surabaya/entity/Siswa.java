package com.abdinegara.surabaya.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.abdinegara.surabaya.message.ResponsePembelianSoal;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity(name= "siswa")
@Table(name= "siswa")
public class Siswa extends BaseEntity implements Serializable{
	
private static final long serialVersionUID = 4211854570169058068L;
	
	@Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "handphone")
	private String handphone;
	
	@Column(name = "pembelian_soal")
	private String pembelianSoal;
	
	@Transient
	private List<ResponsePembelianSoal> pembelianSoals;
	
	@Column(name = "user_uuid")
	private String userUuid;

}
