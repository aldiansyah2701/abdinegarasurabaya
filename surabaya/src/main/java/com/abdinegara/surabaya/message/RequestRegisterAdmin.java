package com.abdinegara.surabaya.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestRegisterAdmin {

    private String email;

    private String name;

    private String handphone;
}
