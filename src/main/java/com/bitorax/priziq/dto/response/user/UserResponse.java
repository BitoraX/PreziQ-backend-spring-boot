package com.bitorax.priziq.dto.response.user;

import com.bitorax.priziq.dto.response.common.AuditResponse;
import com.bitorax.priziq.dto.response.role.RoleResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse extends AuditResponse {
    String id;
    String email;
    String firstName;
    String lastName;
    String nickname;
    String phoneNumber;
    String avatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    Instant birthDate;

    String gender;
    String nationality;

    String providerUserId;
    String provider;
    Boolean isVerified;
    List<RoleResponse> roles;
}
