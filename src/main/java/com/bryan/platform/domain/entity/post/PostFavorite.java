package com.bryan.platform.domain.entity.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Version;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 博文收藏实体类
 * 记录用户对特定博文的收藏行为。存储在 PostgreSQL 数据库。
 *
 * @author Bryan Long
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostFavorite implements Serializable {
    private Long id;

    private Long userId;

    private String postId;

    /** 逻辑删除 */
    private Integer deleted;

    /** 乐观锁 */
    @Version
    private Integer version;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 创建人 */
    private String createdBy;

    /** 更新人 */
    private String updatedBy;
}
