package com.abdinegara.surabaya.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity(name= "pembelian_soal")
@Table(name= "pembelian_soal")
public class PembelianSoal extends BaseEntity implements Serializable{
	
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
	
	@Column(name = "harga_soal")
	private String hargaSoal;
	
	@Column(name = "kode_promo")
	private String kodePromo;
	
	@Column(name = "nama_siswa")
	private String namaSiswa;
	
	@JoinColumn(name= "siswa_uuid")
	@ManyToOne
	private Siswa siswa;

}
