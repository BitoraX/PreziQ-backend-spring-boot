package com.bitorax.priziq.dto.request.session;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CreateSessionRequest {
    @NotBlank(message = "COLLECTION_ID_REQUIRED")
    String collectionId;
}