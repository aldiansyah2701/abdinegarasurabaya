package com.abdinegara.surabaya.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestJawabanSiswaTKD extends RequestJawabanSiswa {
    private String jawabanTIU;
    private String jawabanTKP;
    private String jawabanTWK;
}
