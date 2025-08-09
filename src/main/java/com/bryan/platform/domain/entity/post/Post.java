package com.bryan.platform.domain.entity.post;

import com.bryan.platform.domain.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id; // MongoDB 的 @Id 注解
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex; // MongoDB 复合索引
import org.springframework.data.mongodb.core.index.Indexed; // MongoDB 单字段索引
import org.springframework.data.mongodb.core.mapping.Document; // MongoDB 文档注解
import org.springframework.data.mongodb.core.mapping.Field; // 用于字段映射

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList; // For list initialization
import java.util.List;

/**
 * 博文实体，作为 MongoDB 的顶层文档。
 *
 * @author Bryan Long
 */
@Data
@Document(collection = "posts") // 映射到 MongoDB 的 'posts' 集合
@CompoundIndex(def = "{'title': 'text', 'content': 'text'}") // MongoDB 全文索引定义
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post implements Serializable {
    @Id // MongoDB 文档的唯一标识符，通常为 String 类型
    private String id;

    @Indexed(unique = true) // 唯一索引，确保 slug 唯一
    @Field("slug")
    private String slug;

    // @NotBlank // 验证注解通常在 DTO 或 Controller 层使用
    @Field("title")
    private String title;

    // @NotBlank // 验证注解通常在 DTO 或 Controller 层使用
    @Field("content")
    private String content;

//    @DBRef // 引用用户文档，【已移除】
//    private User author; // 作者，通过 @DBRef 引用用户集合中的文档

    // 【关键修改点】：不再是 User 对象，而是存储用户ID和用户名
    @Field("authorId") // MongoDB 中存储的字段名
    private Long authorId; // 存储 MySQL 中的用户ID

    @Field("authorName") // MongoDB 中存储的字段名
    private String authorName; // 存储用户的显示名称（用户名）

    @Field("tags")
    private List<String> tags = new ArrayList<>(); // 标签列表

    // 评论列表，作为内嵌文档存储在 Post 文档中
    @Field("comments")
    private List<Comment> comments = new ArrayList<>();

    @Field("featuredImage")
    private String featuredImage; // 封面图片URL

    @CreatedDate // Spring Data MongoDB 自动填充创建时间
    @Field("createAt")
    private LocalDateTime createdAt;

    @LastModifiedDate // Spring Data MongoDB 自动填充最后修改时间
    @Field("updateAt")
    private LocalDateTime updatedAt;

    // 博文状态
    @Field("status")
    private PostStatus status = PostStatus.DRAFT;

    // 博文统计数据，作为内嵌对象
    @Field("stats") // 可以指定字段名，如果Java属性名和MongoDB字段名不同
    private PostStats stats = new PostStats(); // 初始化统计数据

    /**
     * 博文状态枚举
     */
    public enum PostStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }

    /**
     * 博文统计数据内嵌类。
     * 不需要 @Document 注解，因为它内嵌在 Post 中。
     */
    @Data
    public static class PostStats implements Serializable {
        private int views = 0;
        private int likes = 0;
        private int shares = 0;
    }
}
