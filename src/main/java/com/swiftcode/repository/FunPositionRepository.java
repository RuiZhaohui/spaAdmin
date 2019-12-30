package com.swiftcode.repository;

import com.swiftcode.domain.FunPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author chen
 **/
public interface FunPositionRepository extends JpaRepository<FunPosition, Long> {
    List<FunPosition> findAllByBranchCompanyCode(String branchCode);

    Optional<FunPosition> findByPositionCode(String positionCode);
}
