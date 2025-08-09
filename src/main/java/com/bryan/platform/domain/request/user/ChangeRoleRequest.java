package com.bryan.platform.domain.request.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * ChangeRoleRequest
 *
 * @author Bryan Long
 */
@Data
public class ChangeRoleRequest {
    @NotEmpty
    private List<@NotNull Long> roleIds;
}
