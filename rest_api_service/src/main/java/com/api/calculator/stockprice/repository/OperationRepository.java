package com.api.calculator.stockprice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.api.calculator.stockprice.model.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
public interface OperationRepository extends JpaRepository<Operation, Long> {

    Optional<Operation> findByOperationIdAndUserId(Long operationId, UUID userId);

    List<Operation> findByUserId(UUID userId);

    @Modifying
    @Query("UPDATE operation op SET op.deletedAt = :deletedAt WHERE op.userId = :userId and op.fileId = :fileId")
    void setDeletedAtByUserIdAndFileId(Date deletedAt, UUID userId, UUID fileId);

    @Query("SELECT op.closeMonth, SUM(op.value) as sum_value FROM operation op WHERE op.deletedAt IS NULL " +
            "AND op.userId = :userId and op.wallet='CLOSED' GROUP BY op.closeMonth ORDER BY op.closeMonth ASC")
    List<Object> sumValuesPerMonthByUserId(UUID userId);

    @Query("SELECT op.closeMonth, SUM(op.value) as sum_value FROM operation op WHERE op.userId = :userId " +
            "AND op.wallet='CLOSED' AND typeMarket = :typeMarket AND typeOp = :typeOp AND op.deletedAt IS NULL " +
            "GROUP BY op.closeMonth ORDER BY op.closeMonth ASC")
    List<Object> sumValuesPerMonthByUserIdAndTypeMarketAndTypeOp(UUID userId, String typeMarket, String typeOp);

    @Query("SELECT op.name, SUM(op.value) as sum_value FROM operation op WHERE op.userId = :userId and op.wallet='CLOSED' GROUP BY op.name ORDER BY sum_value DESC")
    List<Object> sumValuesPerActive(UUID userId);

    int countByUserIdAndDeletedAt(UUID userId, Date deletedAt);

    List<Operation> findAllByUserIdAndDeletedAt(UUID userId, Date deletedAt, Pageable pageable);

    @Modifying
    @Query("UPDATE operation op SET op.deletedAt = :deletedAt WHERE op.userId = :userId and op.operationId = :operationId")
    void setDeletedAtByUserIdAndId(Date deletedAt, UUID userId, Long operationId);
}
