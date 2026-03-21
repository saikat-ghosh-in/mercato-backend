package com.mercato.Service;

import com.mercato.Entity.EcommUser;
import com.mercato.Entity.EmailVerificationToken;
import com.mercato.ExceptionHandler.CustomBadRequestException;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Repository.EmailVerificationTokenRepository;
import com.mercato.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${mercato.email.verification.expiry-hours:24}")
    private int expiryHours;

    @Transactional
    @Override
    public void createAndSendVerificationToken(EcommUser user) {
        tokenRepository.findByUserIdAndVerifiedAtIsNull(user.getId())
                .ifPresent(tokenRepository::delete);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .expiresAt(Instant.now().plus(expiryHours, ChronoUnit.HOURS))
                .build();

        tokenRepository.save(token);

        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), token.getToken());

        log.info("Verification token created for user: {}", user.getUserId());
    }

    @Transactional
    @Override
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new CustomBadRequestException("Invalid verification token"));

        if (verificationToken.isVerified()) {
            throw new CustomBadRequestException("Email already verified");
        }

        if (verificationToken.isExpired()) {
            throw new CustomBadRequestException("Verification token has expired. Please request a new one.");
        }

        verificationToken.setVerifiedAt(Instant.now());
        tokenRepository.save(verificationToken);

        EcommUser user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        log.info("Email verified for user: {}", user.getUserId());
    }

    @Transactional
    @Override
    public void resendVerificationEmail(String email) {
        EcommUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.isEmailVerified()) {
            throw new CustomBadRequestException("Email is already verified");
        }

        createAndSendVerificationToken(user);
    }
}
