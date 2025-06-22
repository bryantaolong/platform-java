package com.bryan.platform.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id; // MongoDB 的 @Id 注解
import org.springframework.data.mongodb.core.mapping.Field; // 新增，用于字段映射

import java.io.Serializable;
import java.time.LocalDateTime;
// import org.springframework.data.mongodb.core.mapping.DBRef; // 不再需要 DBRef

/**
 * ClassName: Comment
 * Package: com.bryan.platform.model.entity
 * Description: 评论实体，作为 MongoDB 博文的内嵌文档。
 * Author: Bryan Long
 * Create: 2025/6/20
 * Version: v1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment implements Serializable {

    @Id // MongoDB 文档的 ID，通常为 String 类型。对于内嵌文档，通常由应用生成 UUID。
    private String id;

    // @NotBlank // 验证注解通常在 DTO 或 Controller 层使用
    private String content;

    // 【关键修改点】：不再是 User 对象，而是存储用户ID和用户名
    // @org.springframework.data.mongodb.core.mapping.DBRef // 已移除
    // private User author; // 原来的引用用户文档

    @Field("authorId") // MongoDB 中存储的字段名
    private Long authorId; // 存储 MySQL 中的用户ID

    @Field("authorName") // MongoDB 中存储的字段名
    private String authorName; // 存储用户的显示名称（用户名）

    // 如果支持多级回复，且回复也内嵌，则可以有这个字段
    // private List<Comment> replies = new ArrayList<>(); // PDF 中有此字段，表示回复也内嵌

    @CreatedDate // Spring Data MongoDB 自动填充创建时间
    private LocalDateTime createdAt;

    // 注意：MongoDB 内嵌文档通常不直接支持 @TableLogic 或 @TableField 等MyBatis-Plus注解
    // 如果需要逻辑删除，需要在业务逻辑中手动处理或添加一个 boolean isDeleted 字段
}