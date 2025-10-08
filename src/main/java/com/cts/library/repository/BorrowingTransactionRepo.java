package com.cts.library.repository;

import com.cts.library.model.BorrowingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowingTransactionRepo extends JpaRepository<BorrowingTransaction, Long> {
    List<BorrowingTransaction> findByMember_MemberId(Long memberId);
    void deleteByMember_MemberId(Long memberId);
    boolean existsByBook_BookIdAndStatus(Long bookId, BorrowingTransaction.Status status);
}

