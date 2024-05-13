package com.abdinegara.surabaya.entity;

import javax.persistence.*;

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
	
	@Column(name = "jenis")
	private String jenis;

	@Transient
	private String nilai;

}
