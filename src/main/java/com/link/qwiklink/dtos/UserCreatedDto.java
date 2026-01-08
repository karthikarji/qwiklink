package com.link.qwiklink.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCreatedDto {
    private Long id;
    private String userName;
    private String role;
}
