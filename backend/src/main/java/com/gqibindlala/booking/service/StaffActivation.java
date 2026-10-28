package com.gqibindlala.booking.service;

import com.gqibindlala.booking.model.AppUser;

public interface StaffActivation {
    AppUser createPendingAccount(AppUser user);
    void sendActivation(AppUser user);
    void activate(String token, String password);
}