package com.abdinegara.surabaya.message;

import javax.persistence.Column;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResponsePembelianSoal {
	
	private String namaSoal;
	
	private String jenisSoal;
	
	private String hargaSoal;
	
	private String kodePromo;
	
	private String namaSiswa;

}
