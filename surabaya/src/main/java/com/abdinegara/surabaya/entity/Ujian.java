package com.abdinegara.surabaya.entity;

import java.util.List;

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
@Entity(name= "ujian")
@Table(name= "ujian")
public class Ujian extends BaseEntity{
	
	@Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
	
	@Column(name = "nama_ujian")
	private String namaUjian;
	
	@Column(name = "jenis")
	private String jenis;
	
	@Column(name = "harga")
	private String harga;

	@Column(columnDefinition = "TEXT")
	private String deskripsi;


}
