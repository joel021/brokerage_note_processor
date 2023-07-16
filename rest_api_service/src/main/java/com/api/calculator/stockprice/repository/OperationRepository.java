package com.api.calculator.stockprice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.api.calculator.stockprice.model.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
public interface OperationRepository extends JpaRepository<Operation, Long> {

    Optional<Operation> findByIdAndUserId(Long id, UUID userId);

    List<Operation> findByUserId(UUID userId);

    @Query("SELECT op.closeMonth, SUM(op.value) as sum_value FROM operation op WHERE " +
            "op.userId = :userId and op.wallet='CLOSED' GROUP BY op.closeMonth ORDER BY op.closeMonth ASC")
    List<Object> sumValuesPerMonthByUserId(UUID userId);

    @Query("SELECT op.closeMonth, SUM(op.value) as sum_value FROM operation op WHERE op.userId = :userId " +
            "AND op.wallet='CLOSED' AND typeMarket = :typeMarket AND typeOp = :typeOp " +
            "GROUP BY op.closeMonth ORDER BY op.closeMonth ASC")
    List<Object> sumValuesPerMonthByUserIdAndTypeMarketAndTypeOp(UUID userId, String typeMarket, String typeOp);

    @Query("SELECT op.name, SUM(op.value) as sum_value FROM operation op WHERE op.userId = :userId " +
            "and op.wallet='CLOSED' GROUP BY op.name ORDER BY sum_value DESC")
    List<Object> sumValuesPerActive(UUID userId);

    int countByUserId(UUID userId);

    List<Operation> findAllByUserId(UUID userId, Pageable pageable);

    void deleteByIdAndUserId(Long id, UUID userId);

    void deleteByFileIdAndUserId(UUID fileId, UUID userId);
}
