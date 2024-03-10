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
@Entity(name= "soal_tkd")
@Table(name= "soal_tkd")
public class SoalTKD extends BaseEntity {
	
	@Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
	
	@Column(name = "nama_soal")
	private String namaSoal;
	
	@Column(name = "durasi")
	private String durasi;
	
	@Column(name = "jenis")
	private String jenis;
	
	@Column(name = "deskripsi")
	private String deskripsi;
	
	@Column(name = "jawaban_twk")
	private String jawabanTwk;
	
	@Column(name = "file_pathtwk")
	private String filePathTwk;
	
	@Transient
	private List<SoalAssetImage> assetImageTwk;
	
	@Column(name = "jawaban_tiu")
	private String jawabanTiu;
	
	@Column(name = "file_pathtiu")
	private String filePathTiu;
	
	@Transient
	private List<SoalAssetImage> assetImageTiu;
	
	@Column(name = "jawaban_tkp")
	private String jawabanTkp;
	
	@Column(name = "file_pathtkp")
	private String filePathTkp;
	
	@Transient
	private List<SoalAssetImage> assetImageTkp;

}
