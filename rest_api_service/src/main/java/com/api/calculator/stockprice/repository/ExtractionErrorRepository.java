package com.api.calculator.stockprice.repository;

import com.api.calculator.stockprice.model.ExtractionError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExtractionErrorRepository extends JpaRepository<ExtractionError, Integer> {

    List<ExtractionError> findByFileId(UUID fileId);

    int countByFileId(UUID fileId);
}
