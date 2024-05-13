package com.abdinegara.surabaya.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity(name= "soal_essay")
@Table(name= "soal_essay")
public class SoalEssay extends BaseEntity{
	
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
	
	@Column(name = "jenis")
	private String jenis;
	
	@Transient
	private List<SoalAssetImage> assetImage;

	@Transient
	private String nilai;

}
