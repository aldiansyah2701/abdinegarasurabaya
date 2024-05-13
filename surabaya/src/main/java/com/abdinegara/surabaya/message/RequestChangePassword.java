package com.abdinegara.surabaya.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestChangePassword {
    private String username;
    private String currentPassword;
    private String newPassword;
}
