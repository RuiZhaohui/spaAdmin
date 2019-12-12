package com.swiftcode.repository;

import com.swiftcode.domain.SapUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author chen
 **/
public interface SapUserRepository extends JpaRepository<SapUser, Long> {
    /**
     * 根据员工工号查找
     *
     * @param userCode 工号
     * @return 员工信息
     */
    Optional<SapUser> findByUserCode(String userCode);
}
