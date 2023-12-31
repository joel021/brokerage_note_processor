package com.api.calculator.stockprice.ws.data.repository;

import com.api.calculator.stockprice.ws.data.model.PDFFile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PDFFileRepository extends JpaRepository<PDFFile, UUID> {

    List<PDFFile> findAllByUserId(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);

    List<PDFFile> findByUserId(UUID userId);

    Optional<PDFFile> findByFileIdAndUserId(UUID fileId, UUID userId);
}
