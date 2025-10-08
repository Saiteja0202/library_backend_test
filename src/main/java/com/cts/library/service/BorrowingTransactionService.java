package com.cts.library.service;



public interface BorrowingTransactionService {
    String borrowBook(Long bookId, Long memberId);
    String returnBook(Long bookId, Long memberId);
}

