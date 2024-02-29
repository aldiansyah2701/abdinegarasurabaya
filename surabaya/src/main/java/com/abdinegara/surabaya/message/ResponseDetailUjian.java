package com.abdinegara.surabaya.message;

import java.util.List;

import com.abdinegara.surabaya.entity.PembelajaranVideo;
import com.abdinegara.surabaya.entity.SoalEssay;
import com.abdinegara.surabaya.entity.SoalPauli;
import com.abdinegara.surabaya.entity.SoalPilihanGanda;
import com.abdinegara.surabaya.entity.Ujian;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDetailUjian {
	
	private Ujian ujian;
	private List<SoalPilihanGanda> detailPilihanGandas;
	private List<SoalEssay> detailEssays;
	private List<SoalPauli> detailPaulis;
	private List<PembelajaranVideo> detailVideos;
	

}
