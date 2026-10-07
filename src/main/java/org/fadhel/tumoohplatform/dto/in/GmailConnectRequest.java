package org.fadhel.tumoohplatform.dto.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GmailConnectRequest {

//    @NotBlank(message = "Gmail address is required")
//    @Email(message = "Gmail address is not valid")
//    private String gmailAddress;

    @NotBlank(message = "App Password is required")
    private String appPassword;
}