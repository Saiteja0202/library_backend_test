package com.cts.library.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.cts.library.authentication.CurrentUser;
import com.cts.library.exception.ResourceNotFoundException;
import com.cts.library.exception.UnauthorizedAccessException;
import com.cts.library.model.LoginDetails;
import com.cts.library.model.Member;
import com.cts.library.model.MemberToken;
import com.cts.library.model.MembershipStatus;
import com.cts.library.model.Role;
import com.cts.library.repository.BorrowingTransactionRepo;
import com.cts.library.repository.FineRepo;
import com.cts.library.repository.MemberRepo;
import com.cts.library.repository.MemberTokenRepo;
import com.cts.library.repository.NotificationRepo;

import jakarta.transaction.Transactional;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepo memberRepo;
    private final BorrowingTransactionRepo transactionRepo;
    private final FineRepo fineRepo;
    private final NotificationRepo notificationRepo;
    private final MemberTokenRepo memberTokenRepo;
    private final CurrentUser currentUser;

    public MemberServiceImpl(MemberRepo memberRepo,
    		BorrowingTransactionRepo transactionRepo, 
    		FineRepo fineRepo,
    		NotificationRepo notificationRepo,
    		MemberTokenRepo memberTokenRepo,
    		CurrentUser currentUser) {
        this.memberRepo = memberRepo;
        this.memberTokenRepo = memberTokenRepo;
        this.currentUser = currentUser;
        this.transactionRepo = transactionRepo;
        this.fineRepo = fineRepo;
        this .notificationRepo = notificationRepo;
    }

     
    public String registerMember(Member member) {
        if (memberRepo.existsByUsername(member.getUsername())) {
            throw new RuntimeException("Username already taken.");
        }

        member.setRole(Role.MEMBER);
        member.setPassword(hashPassword(member.getPassword()));
        memberRepo.save(member);

        return "Member registered successfully.";
    }

     
    public String createAdmin(Member newAdmin) {
        newAdmin.setRole(Role.ADMIN);
        newAdmin.setPassword(hashPassword(newAdmin.getPassword()));
        memberRepo.save(newAdmin);

        return "Admin created successfully.";
    }

     @Transactional
    public String updateMember(Long id, Member updated) {
    	 
    	 if (currentUser.getCurrentUser().getMemberId() != id) {
    		 throw new UnauthorizedAccessException("You are not allowed to update other's profile");
    	 }
    	 
        Member existing = getMemberById(id);

        if (currentUser.getCurrentUser().getRole() != Role.MEMBER) {
            throw new UnauthorizedAccessException("Admin not allowed to update member details.");
        }
        
        if(currentUser.getCurrentUser() == null) {
        	throw new UnauthorizedAccessException("Please Login");
        }

        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        existing.setUsername(updated.getUsername());
        memberRepo.save(existing);

        return "Member profile updated.";
    }
    @Transactional
    public String updatePassword(Long id, String plainText) {
    	Member existing = getMemberById(id);
    	if(currentUser.getCurrentUser() == null) {
    		throw new UnauthorizedAccessException("Please Login");
    	}
    	if (currentUser.getCurrentUser().getRole() != Role.MEMBER) {
            throw new UnauthorizedAccessException("Admin not allowed to update member details.");
        }
    	
    	existing.setPassword(hashPassword(plainText));
    	memberRepo.save(existing);
    	return "Password Updated Successfully";
    }
    
    @Transactional
    public String UpdateRole(Long id, Long adminId) {
        Member member = getMemberById(id);
        if(currentUser.getCurrentUser() == null) {
        	throw new UnauthorizedAccessException("Please Login");
        }
        if (currentUser.getCurrentUser().getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Only admins can promote members.");
        }

        member.setRole(Role.ADMIN);
        memberRepo.save(member);

        return "Congrats, you have been promoted to ADMIN.";
    }

     @Transactional
    public String deleteMemberById(Long id) {
        Member member = getMemberById(id);
        if(currentUser.getCurrentUser() == null) {
        	throw new UnauthorizedAccessException("Please Login");
        }
        if (currentUser.getCurrentUser().getRole() != Role.MEMBER) {
            throw new UnauthorizedAccessException("Only members can delete their own accounts.");
        }
        transactionRepo.deleteByMember_MemberId(id);
        fineRepo.deleteByMember_MemberId(id);
        notificationRepo.deleteByMember_MemberId(id);
        memberTokenRepo.deleteByMember_MemberId(id);
        memberRepo.delete(member);
        return "Member deleted successfully.";
    }

     
    public List<Member> getAllMembers() {
    	if(currentUser.getCurrentUser() == null) {
        	throw new UnauthorizedAccessException("Please Login");
        }
    	 if (currentUser.getCurrentUser().getRole() != Role.ADMIN) {
             throw new UnauthorizedAccessException("You are not allowed to view other's details");
         }
        return memberRepo.findAll();
    }

     
    public Member getMemberById(Long id) {
    	if(currentUser.getCurrentUser() == null) {
        	throw new UnauthorizedAccessException("Please Login");
        }
    	 if (currentUser.getCurrentUser().getRole() != Role.ADMIN && currentUser.getCurrentUser().getMemberId() != id) {
             throw new UnauthorizedAccessException("You are not allowed to view other person profile");
         }
    	
        return memberRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + id + " not found."));
    }

     
    public String activateMembership(Long id, int months) {
        Member member = getMemberById(id);
        if(currentUser.getCurrentUser() == null) {
        	throw new UnauthorizedAccessException("Please Login");
        }
        member.setMembershipStatus(MembershipStatus.PRIME);

        LocalDate newExpiry = (member.getMembershipExpiryDate() == null)
                ? LocalDate.now().plusMonths(months)
                : member.getMembershipExpiryDate().plusMonths(months);

        member.setMembershipExpiryDate(newExpiry);
        memberRepo.save(member);

        return "Membership activated until " + newExpiry + ".";
    }

    
    public void updateMembershipStatus(Member member) {
    	if(currentUser.getCurrentUser() == null) {
        	throw new UnauthorizedAccessException("Please Login");
        }
        if (member.getMembershipExpiryDate() != null &&
            LocalDate.now().isAfter(member.getMembershipExpiryDate())) {

            member.setMembershipStatus(MembershipStatus.EXPIRED);
            memberRepo.save(member);
        }
    }


    public Member loginMember(LoginDetails loginDetails) {
        Member member = memberRepo.findByUsername(loginDetails.getUserName());
        if (member == null) {
            throw new UnauthorizedAccessException("Member not found.");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(loginDetails.getUserPassword(), member.getPassword())) {
            throw new UnauthorizedAccessException("Invalid credentials.");
        }

        Optional<MemberToken> existingToken = memberTokenRepo.findByMember(member);
        if (existingToken.isPresent()) {
            return member;
        }

        MemberToken memberToken = new MemberToken();
        memberToken.setMemberToken(UUID.randomUUID().toString());
        memberToken.setGeneratedAt(LocalDateTime.now());
        memberToken.setMember(member);
        memberTokenRepo.save(memberToken);

        return member;
    }


    private String hashPassword(String plainText) {
        return new BCryptPasswordEncoder().encode(plainText);
    }
}
