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
@Entity(name= "soal_pilihan_ganda")
@Table(name= "soal_pilihan_ganda")
public class SoalPilihanGanda extends BaseEntity{
	
	@Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
	
	@Column(name = "nama_soal")
	private String namaSoal;
	
	@Column(name = "durasi")
	private String durasi;
	
	@Column(name = "jawaban")
	private String jawaban;
	
	@Column(name = "deskripsi")
	private String deskripsi;
	
	@Column(name = "file_path")
	private String filePath;

}
