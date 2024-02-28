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
@Entity(name= "soal_asset_image")
@Table(name= "soal_asset_image")
public class SoalAssetImage {
	
	@Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
	
	@Column(name = "uuid_soal")
	private String uuidSoal;
	
	@Column(name = "file_path")
	private String filePath;
	
	@Column(name = "soal_type")
	private String soalType;

}
