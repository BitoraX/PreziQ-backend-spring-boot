package com.bitorax.priziq.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateUserPasswordRequest {
        @NotBlank(message = "CURRENT_PASSWORD_NOT_BLANK")
        String currentPassword;

        @NotBlank(message = "NEW_PASSWORD_NOT_BLANK")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "PASSWORD_INVALID_FORMAT")
        String newPassword;

        @NotBlank(message = "CONFIRM_PASSWORD_NOT_BLANK")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "PASSWORD_INVALID_FORMAT")
        String confirmPassword;
}
