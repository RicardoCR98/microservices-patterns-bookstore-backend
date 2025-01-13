package com.microservice.notifications.dto;


import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
}