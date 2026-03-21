package com.mercato.Service;

import com.mercato.Entity.EcommUser;

public interface EmailVerificationService {

    void createAndSendVerificationToken(EcommUser user);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);
}
