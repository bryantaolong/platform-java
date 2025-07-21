package com.bryan.platform.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Moment 动态/朋友圈实体类
 *
 * @author Bryan Long
 * @version 1.0
 * @since 2025/7/11
 */
@Data
@Document(collection = "moments")
// 创建复合索引
@CompoundIndexes({
        @CompoundIndex(name = "author_created_idx", def = "{'authorId': 1, 'created_at': -1}"),
        @CompoundIndex(name = "like_created_idx", def = "{'likeCount': -1, 'created_at': -1}")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Moment implements Serializable {
    @Id
    private String id;

    @Field("content")
    private String content;

    @Field("images")
    private String images;

    @Field("authorId")
    private Long authorId;

    @Field("authorName")
    private String authorName;

    @Field("likeCount")
    private Integer likeCount;

    @Field("comments")
    private List<Comment> comments = new ArrayList<>();

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}
