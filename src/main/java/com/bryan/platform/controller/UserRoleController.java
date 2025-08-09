package com.bryan.platform.controller;

import com.bryan.platform.domain.dto.RoleOptionDTO;
import com.bryan.platform.domain.response.Result;
import com.bryan.platform.service.user.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * UserRoleController
 *
 * @author Bryan Long
 */
@RestController
@RequestMapping("/api/user/role")
@RequiredArgsConstructor
@Validated
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<RoleOptionDTO>> listRoles() {
        return Result.success(userRoleService.listAll());
    }
}
