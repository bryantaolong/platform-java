package com.bryan.platform.domain.dto;

import lombok.Builder;
import lombok.Data;

/**
 * UserUpdateDTO
 *
 * @author Bryan Long
 */
@Data
@Builder
public class UserUpdateDTO {

    private String username;

    private String phone;

    private String email;
}
