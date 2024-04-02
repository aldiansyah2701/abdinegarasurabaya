package com.abdinegara.surabaya.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCreateSoalHilang {

    private String namaSoal;
    private String durasi;
    private String masterSoal;
    private String soal;
    private String jawaban;
    private String deskripsi;
    private String jenis;
}
