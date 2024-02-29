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
@Entity(name= "ujian_asset_soal")
@Table(name= "ujian_asset_soal")
public class UjianAssetSoal {
	
	@Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
	
	@Column(name = "uuid_soal")
	private String uuidSoal;
	
	@Column(name = "soalType")
	private String soalType;
	
	@Column(name = "uuid_ujian")
	private String uuidUjian;

}
