package com.abdinegara.surabaya.entity;

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
@Entity(name= "soal_pauli")
@Table(name= "soal_pauli")
public class SoalPauli extends BaseEntity{
	
	@Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
	
	@Column(name = "nama_soal")
	private String namaSoal;
	
	@Column(name = "durasi")
	private String durasi;
	
	@Column(name = "soal")
	private String soal;
	
	@Column(name = "jawaban")
	private String jawaban;
	
	@Column(name = "deskripsi")
	private String deskripsi;
	

}
