package com.bryan.platform.service.user;

import com.bryan.platform.domain.dto.RoleOptionDTO;
import com.bryan.platform.domain.entity.user.UserRole;
import com.bryan.platform.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * UserRoleService
 *
 * @author Bryan Long
 */
@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleMapper userRoleMapper;

    public List<RoleOptionDTO> listAll() {
        return userRoleMapper.selectAll()
                .stream()
                .map(r -> new RoleOptionDTO(r.getId(), r.getRoleName()))
                .toList();
    }

    public List<UserRole> findByIds(Collection<Long> ids) {
        return userRoleMapper.selectByIdList(ids);
    }
}