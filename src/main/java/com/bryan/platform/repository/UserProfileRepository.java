package com.bryan.platform.repository;

import com.bryan.platform.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * UserProfileRepository
 *
 * @author Bryan Long
 */
@Repository
@RequiredArgsConstructor
public class UserProfileRepository {

    private final UserProfileMapper userProfileMapper;

}
