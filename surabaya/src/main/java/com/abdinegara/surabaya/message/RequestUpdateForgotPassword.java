package com.abdinegara.surabaya.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUpdateForgotPassword {
    String otp;
    String password;
    String username;
}
