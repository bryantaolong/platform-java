package com.bryan.platform.repository;

import com.bryan.platform.model.entity.user.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * UserProfileRepository
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/8/7
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Page<UserProfile> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);
}
