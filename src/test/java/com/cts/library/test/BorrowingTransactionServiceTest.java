package com.cts.library.test;

import com.cts.library.authentication.CurrentUser;
import com.cts.library.exception.BookNotFoundException;
import com.cts.library.model.Book;
import com.cts.library.model.BorrowingTransaction;
import com.cts.library.model.Member;

import com.cts.library.repository.BookRepo;
import com.cts.library.repository.BorrowingTransactionRepo;
import com.cts.library.repository.MemberRepo;
import com.cts.library.service.BorrowingTransactionServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BorrowingTransactionServiceTest {

    private BorrowingTransactionRepo transactionRepo;
    private BookRepo bookRepo;
    private MemberRepo memberRepo;
    private CurrentUser currentUser;
    private BorrowingTransactionServiceImpl service;

    @BeforeEach
    void setUp() {
        transactionRepo = mock(BorrowingTransactionRepo.class);
        bookRepo = mock(BookRepo.class);
        memberRepo = mock(MemberRepo.class);
        currentUser = mock(CurrentUser.class);

        service = new BorrowingTransactionServiceImpl(transactionRepo, bookRepo, memberRepo, currentUser);
    }

    @Test
    void testBorrowBook_ValidBook_ShouldSucceed() {
        Long bookId = 1L;
        Long memberId = 10L;

        Book book = new Book();
        book.setBookId(bookId);
        book.setAvailableCopies(1);

        Member member = new Member();
        member.setMemberId(memberId);
        member.setBorrowingLimit(2);

        when(bookRepo.findById(bookId)).thenReturn(Optional.of(book));
        when(memberRepo.findById(memberId)).thenReturn(Optional.of(member));
        when(currentUser.getCurrentUser()).thenReturn(member);

        String result = service.borrowBook(bookId, memberId);

        assertEquals("Book borrowed successfully.", result);
        assertEquals(0, book.getAvailableCopies());
        assertEquals(1, member.getBorrowingLimit());
        verify(transactionRepo, times(1)).save(any(BorrowingTransaction.class));
        System.out.println("Borrowing successful.");
    }

    @Test
    void testBorrowBook_BookNotFound_ShouldThrowException() {
        Long bookId = 99L;
        Long memberId = 10L;

        Member member = new Member();
        member.setMemberId(memberId);
        member.setBorrowingLimit(2);

        when(bookRepo.findById(bookId)).thenReturn(Optional.empty());
        when(currentUser.getCurrentUser()).thenReturn(member);

        assertThrows(BookNotFoundException.class, () -> service.borrowBook(bookId, memberId));
        System.out.println("Exception thrown as expected.");
    }
}
