package com.bitorax.priziq.dto.request.user;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateUserProfileRequest {
    @Size(min = 2, max = 50, message = "FIRSTNAME_INVALID_LENGTH")
    String firstName;

    @Size(min = 2, max = 50, message = "LASTNAME_INVALID_LENGTH")
    String lastName;

    @Size(min = 2, max = 50, message = "NICKNAME_INVALID_LENGTH")
    String nickname;

    String avatar;
    Instant birthDate;
    String gender;
    String nationality;
}
