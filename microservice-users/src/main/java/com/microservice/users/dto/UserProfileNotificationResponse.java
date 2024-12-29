package com.microservice.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileNotificationResponse {
    private Long userId;
    private String email;
    private String fullName;
}
