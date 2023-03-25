package com.mysite.sbb.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserModifyForm {

    @NotEmpty(message = "수정할 닉네임을 입력해주세요.")
    private String nickname;

    @NotEmpty(message = "수정할 이메일을 입력해주세요.")
    @Email
    private String email;
}