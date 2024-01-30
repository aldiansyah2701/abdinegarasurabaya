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
@Entity(name= "buat_soal")
@Table(name= "buat_soal")
public class BuatSoal extends BaseEntity implements Serializable{
	
private static final long serialVersionUID = 4211854570169058068L;
	
	@Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
	
	@Column(name = "nama_soal")
	private String namaSoal;
	
	@Column(name = "jenis_soal")
	private String jenisSoal;
	
	@Column(name = "jenis_siswa")
	private String jenisSiswa;
	
	@Column(name = "file_path")
	private String filePath;

}
