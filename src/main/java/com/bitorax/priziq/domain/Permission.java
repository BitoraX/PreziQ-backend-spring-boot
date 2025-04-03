package com.bitorax.priziq.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String permissionId;

    String name;
    String apiPath;
    String httpMethod;
    String module;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "permissions")
    @JsonIgnore
    List<Role> roles;

    public Permission(String name, String apiPath, String httpMethod, String module) {
        this.name = name;
        this.apiPath = apiPath;
        this.httpMethod = httpMethod;
        this.module = module;
    }
}
