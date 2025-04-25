package com.bitorax.priziq.service;

import com.bitorax.priziq.domain.User;

public interface EmailService {
    void sendVerifyActiveAccountEmail(User user);

    void sendForgotPasswordEmail(User user);

    void sendVerifyChangeEmail(User user);
}
