package com.bitorax.priziq.dto.request.session.session_participant;

import com.bitorax.priziq.validation.annotation.UserOrGuestValid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@UserOrGuestValid
public class JoinSessionRequest {
    @NotBlank(message = "SESSION_CODE_REQUIRED")
    String sessionCode;

    String userId; // user already has an account

    String guestName;
    String guestAvatar;
}