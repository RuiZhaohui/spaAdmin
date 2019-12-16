package com.swiftcode.repository;

import com.swiftcode.domain.FunPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author chen
 **/
public interface FunPositionRepository extends JpaRepository<FunPosition, Long> {
    List<FunPosition> findAllByBranchCompanyCode(String branchCode);
}
