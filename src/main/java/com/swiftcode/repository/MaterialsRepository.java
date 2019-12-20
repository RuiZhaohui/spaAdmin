package com.swiftcode.repository;

import com.swiftcode.domain.Materials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author chen
 **/
public interface MaterialsRepository extends JpaRepository<Materials, String> {
    Optional<Materials> findByMaterialsCode(String code);
}
