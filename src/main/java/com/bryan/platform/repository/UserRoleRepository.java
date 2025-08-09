package com.bryan.platform.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bryan.platform.domain.entity.user.UserRole;
import com.bryan.platform.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * UserRoleRepository
 *
 * @author Bryan Long
 */
@Repository
@RequiredArgsConstructor
public class UserRoleRepository {

    private final UserRoleMapper userRoleMapper;

    public Optional<UserRole> findByIsDefaultTrue() {
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("is_default", true)
                .last("limit 1");
        return Optional.ofNullable(userRoleMapper.selectOne(wrapper));
    }

    public List<UserRole> findAll() {
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        return userRoleMapper.selectList(wrapper);
    }

    public List<UserRole> findAllByIdIn(Collection<Long> ids) {
        return userRoleMapper.selectBatchIds(ids);
    }
}
