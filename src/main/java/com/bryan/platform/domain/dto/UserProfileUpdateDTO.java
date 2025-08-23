package com.bryan.platform.domain.dto;

import com.bryan.platform.domain.enums.GenderEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * UserProfileUpdateDTO
 *
 * @author Bryan Long
 */
@Data
@Builder
public class UserProfileUpdateDTO {

    private Long userId;

    private String realName;

    private GenderEnum gender;

    private LocalDateTime birthday;

    private String avatar;
}
