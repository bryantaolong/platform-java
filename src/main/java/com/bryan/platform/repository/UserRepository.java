package com.bryan.platform.repository;

import com.bryan.platform.domain.enums.UserStatusEnum;
import com.bryan.platform.domain.entity.user.User;
import com.bryan.platform.domain.request.user.UserExportRequest;
import com.bryan.platform.domain.request.user.UserSearchRequest;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * UserRepository
 *
 * @author Bryan Long
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    /* ---------- 基础单查 ---------- */
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    /* ---------- search ---------- */
    default Page<User> searchUsers(UserSearchRequest req, Pageable pageable) {
        return findAll(new UserSpec(req), pageable);
    }

    /* ---------- 批量更新字段（避免先查后改） ---------- */
    @Modifying
    @Query("UPDATE User u SET u.roles = :roles WHERE u.id = :id")
    int updateRoles(@Param("id") Long id, @Param("roles") String roles);

    @Modifying
    @Query("UPDATE User u SET u.password = :pwd, u.passwordResetTime = :time WHERE u.id = :id")
    int updatePassword(@Param("id") Long id,
                       @Param("pwd") String password,
                       @Param("time") LocalDateTime time);

    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") UserStatusEnum status);

    /* ---------- 逻辑删除计数 ---------- */
    long countByDeleted(Integer deleted);

    /* ========== 内部 Specification 实现 ========== */
    class UserSpec implements Specification<User> {
        private final UserSearchRequest req;

        public UserSpec(UserSearchRequest req) { this.req = req; }

        @Override
        public Predicate toPredicate(Root<User> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
            Predicate p = cb.conjunction(); // 1=1

            p = like(p, root, cb, "username",        req.getUsername());
            p = like(p, root, cb, "phoneNumber",     req.getPhoneNumber());
            p = like(p, root, cb, "email",           req.getEmail());
            p = like(p, root, cb, "roles",           req.getRoles());
            p = like(p, root, cb, "loginIp",         req.getLoginIp());
            p = like(p, root, cb, "createBy",        req.getCreateBy());
            p = like(p, root, cb, "updateBy",        req.getUpdateBy());

            p = eq(p, root, cb, "status",             req.getStatus());
            p = eq(p, root, cb, "loginTime",          req.getLoginTime());
            p = eq(p, root, cb, "passwordResetTime",  req.getPasswordResetTime());
            p = eq(p, root, cb, "loginFailCount",     req.getLoginFailCount());
            p = eq(p, root, cb, "accountLockTime",    req.getAccountLockTime());
            p = eq(p, root, cb, "deleted",            req.getDeleted());
            p = eq(p, root, cb, "version",            req.getVersion());

            p = between(p, root, cb, "createTime",
                    req.getCreateTimeStart(), req.getCreateTimeEnd());
            p = between(p, root, cb, "updateTime",
                    req.getUpdateTimeStart(), req.getUpdateTimeEnd());

            // 默认排序：创建时间倒序（与 MP 一致）
            cq.orderBy(cb.desc(root.get("createTime")));
            return p;
        }

        /* ---- 辅助方法 ---- */
        private Predicate like(Predicate p, Root<User> root, CriteriaBuilder cb,
                               String field, @Nullable String val) {
            if (val != null && !val.isBlank()) {
                p = cb.and(p, cb.like(root.get(field), "%" + val.trim() + "%"));
            }
            return p;
        }

        private <T> Predicate eq(Predicate p, Root<User> root, CriteriaBuilder cb,
                                 String field, @Nullable T val) {
            if (val != null) {
                p = cb.and(p, cb.equal(root.get(field), val));
            }
            return p;
        }

        private Predicate between(Predicate p, Root<User> root, CriteriaBuilder cb,
                                  String field,
                                  @Nullable LocalDateTime start,
                                  @Nullable LocalDateTime end) {
            if (start != null && end != null) {
                p = cb.and(p, cb.between(root.get(field), start, end));
            } else if (start != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get(field), start));
            } else if (end != null) {
                p = cb.and(p, cb.lessThanOrEqualTo(root.get(field), end));
            }
            return p;
        }
    }

    /* ---------- 专门给 UserExportRequest 使用的 Specification ---------- */
    class UserExportSpec implements Specification<User> {
        private final UserExportRequest req;
        public UserExportSpec(UserExportRequest req) { this.req = req; }

        @Override
        public Predicate toPredicate(Root<User> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
            Predicate p = cb.conjunction();

            // 与原 export 里的 buildQueryWrapper 完全一致
            if (req.getStatus() != null) {
                p = cb.and(p, cb.equal(root.get("status"), req.getStatus()));
            }
            // 如需增加其他字段，照抄原 buildQueryWrapper 即可

            cq.orderBy(cb.desc(root.get("createTime")));
            return p;
        }
    }
}
