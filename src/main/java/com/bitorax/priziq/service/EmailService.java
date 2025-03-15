package com.bitorax.priziq.service;

import com.bitorax.priziq.domain.User;

public interface EmailService {
    void sendVerifyEmail(User user);

    void sendForgotPasswordEmail(User user);
}
