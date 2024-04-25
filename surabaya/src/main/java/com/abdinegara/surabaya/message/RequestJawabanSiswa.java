package com.abdinegara.surabaya.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestJawabanSiswa {

    private String soalType;

    private String jawaban;

    private String userUuid;

    private String soalUuid;

    private String ujianUuid;
}
