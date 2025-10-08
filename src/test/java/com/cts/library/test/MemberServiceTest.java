package com.cts.library.test;

import com.cts.library.authentication.CurrentUser;
import com.cts.library.exception.ResourceNotFoundException;
import com.cts.library.model.Member;
import com.cts.library.model.Role;
import com.cts.library.repository.*;
import com.cts.library.service.MemberServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MemberServiceTest {

    private MemberRepo memberRepo;
    private BorrowingTransactionRepo transactionRepo;
    private FineRepo fineRepo;
    private NotificationRepo notificationRepo;
    private MemberTokenRepo memberTokenRepo;
    private CurrentUser currentUser;
    private MemberServiceImpl memberService;

    @BeforeEach
    public void setup() {
        memberRepo = mock(MemberRepo.class);
        transactionRepo = mock(BorrowingTransactionRepo.class);
        fineRepo = mock(FineRepo.class);
        notificationRepo = mock(NotificationRepo.class);
        memberTokenRepo = mock(MemberTokenRepo.class);
        currentUser = mock(CurrentUser.class);

        memberService = new MemberServiceImpl(
                memberRepo,
                transactionRepo,
                fineRepo,
                notificationRepo,
                memberTokenRepo,
                currentUser
        );
    }

    @Test
    public void testRegisterMember_ShouldSucceed() {
        Member member = new Member();
        member.setUsername("sai");
        member.setPassword("password");

        when(memberRepo.existsByUsername("sai")).thenReturn(false);
        when(memberRepo.save(any(Member.class))).thenReturn(member);

        String result = memberService.registerMember(member);
        assertEquals("Member registered successfully.", result);
        System.out.println("Member registered.");
    }
    
    
    

    @Test
    public void testGetMemberById_Found() {
        Member admin = new Member();
        admin.setRole(Role.ADMIN);
        admin.setMemberId(1L);

        Member target = new Member();
        target.setMemberId(2L);

        when(currentUser.getCurrentUser()).thenReturn(admin);
        when(memberRepo.findById(2L)).thenReturn(Optional.of(target));

        Member result = memberService.getMemberById(2L);
        assertEquals(2L, result.getMemberId());
        System.out.println("Member retrieved.");
    }

    @Test
    public void testGetMemberById_NotFound() {
        Member admin = new Member();
        admin.setRole(Role.ADMIN);
        admin.setMemberId(1L);

        when(currentUser.getCurrentUser()).thenReturn(admin);
        when(memberRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> memberService.getMemberById(1L));
        System.out.println("Exception thrown as expected.");
    }
}
