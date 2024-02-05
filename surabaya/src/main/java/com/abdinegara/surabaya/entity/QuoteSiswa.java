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
@Entity(name= "quote_siswa")
@Table(name= "quote_siswa")
public class QuoteSiswa extends BaseEntity implements Serializable{
	
private static final long serialVersionUID = 4211854570169058068L;
	
	@Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
	
	@Column(name = "title")
	private String title;

	@Column(name = "quote")
	private String quote;
	
	@Column(name = "file_path_gambar")
	private String filePathGambar;
	
	@Column(name = "file_path_video")
	private String filePathVideo;
}
