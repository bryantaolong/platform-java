package com.bryan.platform.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bryan.platform.mapper.PostFavoriteMapper;
import com.bryan.platform.domain.entity.post.PostFavorite;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PostFavoriteRepository
 *
 * @author Bryan Long
 */
@Repository
@RequiredArgsConstructor
public class PostFavoriteRepository {

    private final PostFavoriteMapper postFavoriteMapper;

    public PostFavorite save(PostFavorite postFavorite) {
        postFavoriteMapper.insert(postFavorite);
        return postFavorite;
    }

    public List<PostFavorite> findAllByUserIdAndPostId(Long userId){
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .select(PostFavorite::getPostId);

        return postFavoriteMapper.selectList(wrapper);
    }

    public int deleteByUserIdAndPostId(Long userId, String postId) {
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .eq(PostFavorite::getPostId, postId);

        return postFavoriteMapper.delete(wrapper);
    }

    public Long count(Long userId) {
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId);
        return postFavoriteMapper.selectCount(wrapper);
    }

    public Long countByUserIdAndPostId(Long userId, String postId) {
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .eq(PostFavorite::getPostId, postId);
        return postFavoriteMapper.selectCount(wrapper);
    }
}
