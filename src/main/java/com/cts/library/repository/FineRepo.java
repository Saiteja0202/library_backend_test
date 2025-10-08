package com.cts.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.cts.library.model.Fine;

public interface FineRepo extends JpaRepository<Fine, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO fine (amount, fine_status, member_id, transaction_id, transaction_date)
        SELECT 
            DATEDIFF(CURRENT_DATE, bt.return_date) * 20.0 AS amount,
            'PENDING' AS fine_status,
            bt.member_id,
            bt.transaction_id,
            CURRENT_DATE AS transaction_date
        FROM borrowing_transaction bt
        WHERE bt.status = 'BORROWED'
          AND bt.return_date < CURRENT_DATE
          AND NOT EXISTS (
              SELECT 1 FROM fine f
              WHERE f.transaction_id = bt.transaction_id
              AND LOWER(f.fine_status) = 'pending'
          )
        """, nativeQuery = true)
    int insertPendingFinesForOverdueTransactions();

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE fine f
        JOIN borrowing_transaction bt ON f.transaction_id = bt.transaction_id
        SET f.amount = f.amount + 20,
            f.transaction_date = CURRENT_DATE
        WHERE f.fine_status = 'PENDING'
          AND bt.status = 'BORROWED'
          AND CURRENT_DATE != f.transaction_date
        """, nativeQuery = true)
    int updatePendingFineAmountsDaily();

    void deleteByMember_MemberId(Long memberId);
}
