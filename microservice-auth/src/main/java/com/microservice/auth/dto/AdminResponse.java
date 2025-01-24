package com.microservice.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private Boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Boolean isDeleted;
}
