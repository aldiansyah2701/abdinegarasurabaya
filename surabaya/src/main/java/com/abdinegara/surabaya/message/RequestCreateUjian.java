package com.abdinegara.surabaya.message;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCreateUjian {

	private String namaUjian;
	private String jenis;
	private String harga;
	private String deskripsi;
	private List<String> uuidSoalPilihanGanda;
	private List<String> uuidSoalEssay;
	private List<String> uuidSoalPauli;
	private List<String> uuidSoalVideo;
	private List<String> uuidSoalTKD;
	private List<String> uuidSoalHilang;
	private List<String> uuidSoalGanjilGenap;
	
}
