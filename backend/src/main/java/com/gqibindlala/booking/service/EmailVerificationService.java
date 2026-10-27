package com.gqibindlala.booking.service;

import com.gqibindlala.booking.model.AppUser;

public interface EmailVerificationService {
    void sendVerification(AppUser user);
    void verify(String token);
}