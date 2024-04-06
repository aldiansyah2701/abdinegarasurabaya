package com.abdinegara.surabaya.message;

import java.util.List;

import com.abdinegara.surabaya.entity.*;

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
	private List<SoalTKD> detailTKDs;
	private List<SoalHilang> detailSoalHilangs;
	private List<SoalGanjilGenap> detailGanjilGenaps;

}
