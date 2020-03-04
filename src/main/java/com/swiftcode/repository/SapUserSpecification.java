package com.swiftcode.repository;

import com.swiftcode.domain.SapUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;

/**
 * @author chen
 */
public class SapUserSpecification {
    public static Specification<SapUser> search(String userCode, String userName) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (!StringUtils.isEmpty(userCode)) {
                predicate.getExpressions().add(
                    cb.like(root.get("userCode"), "%" + userCode + "%")
                );
            }
            if (!StringUtils.isEmpty(userName)) {
                predicate.getExpressions().add(
                    cb.like(root.get("userName"), "%" + userName + "%")
                );
            }
            query.where(predicate);
            return null;
        };
    }
}
