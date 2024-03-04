package com.abdinegara.surabaya.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@Entity(name= "pembelajaran_video")
@Table(name= "pembelajaran_video")
public class PembelajaranVideo extends BaseEntity{
	
	@Setter
	@Getter
	@Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
	
	@Setter
	@Getter
	@Column(name = "nama_video")
	private String namaVideo;
	
	@Setter
	@Getter
	@Column(name = "deskripsi")
	private String deskripsi;
	
	@Setter
	@Getter
	@Column(name = "jenis")
	private String jenis;
	
//	@JsonIgnore
	@Getter
	@Setter
	@Column(name = "file_path")
	private String filePath;
	
	@Getter
	@Setter
	private String filePathEncrypt;
	
	
}
