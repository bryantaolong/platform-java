package com.bryan.platform.service;

import com.bryan.platform.dao.repository.PostRepository;
import com.bryan.platform.model.entity.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.mongodb.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ClassName: PostServiceTest
 * Package: com.bryan.platform.service
 * Description:
 * Author: Bryan Long
 * Create: 2025/6/20 - 20:02
 * Version: v1.0
 */
@SpringBootTest
public class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Test
    public void testCreatePost() {
        Post post = new Post();
        post.setTitle("Test Post");
        post.setContent("This is a test post content");

        Post savedPost = postService.createPost(post, 1L, "admin");

        assertNotNull(savedPost.getId());
        assertEquals("test-post", savedPost.getSlug());
        assertEquals(Post.PostStatus.DRAFT, savedPost.getStatus());
    }

    @Test
    public void testFullTextSearch() {
        // 创建测试数据
        Post post1 = new Post();
        post1.setTitle("Spring Boot Tutorial");
        post1.setContent("Learn how to use Spring Boot with MongoDB");
        post1.setStatus(Post.PostStatus.PUBLISHED);
        postRepository.save(post1);

        Post post2 = new Post();
        post2.setTitle("MongoDB Basics");
        post2.setContent("Introduction to MongoDB for beginners");
        post2.setStatus(Post.PostStatus.PUBLISHED);
        postRepository.save(post2);

        // 测试搜索
        List<Post> results = postService.fullTextSearch("Spring MongoDB");
        assertEquals(1, results.size());
        assertEquals("Spring Boot Tutorial", results.get(0).getTitle());
    }
}