package com.cts.library.controller;

import com.cts.library.exception.UnauthorizedAccessException;
import com.cts.library.model.LoginDetails;
import com.cts.library.model.Member;
import com.cts.library.model.MemberToken;
import com.cts.library.repository.MemberTokenRepo;
import com.cts.library.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Validated
public class MemberController {
	
	private MemberTokenRepo memberTokenRepo;

    private final MemberService memberService;

    public MemberController(MemberService memberService, MemberTokenRepo memberTokenRepo) {
        this.memberService = memberService;
        this.memberTokenRepo = memberTokenRepo;
    }

    @PostMapping("/admin/admin-register")
    public ResponseEntity<String> createAdmin(@RequestBody Member admin) {
        return ResponseEntity.ok(memberService.createAdmin(admin));
    }
    @GetMapping("/admin/allmembers")
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/admin/get-member/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        Member member = memberService.getMemberById(id);
        return ResponseEntity.ok(member);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> loginMember(@RequestBody LoginDetails loginDetails) {
        Member member = memberService.loginMember(loginDetails);
        MemberToken token = memberTokenRepo.findByMember(member)
            .orElseThrow(() -> new UnauthorizedAccessException("Token generation failed"));

        return ResponseEntity.ok(Map.of(
            "token", token.getMemberToken(),
            "memberId", member.getMemberId(),
            "role", member.getRole()
        ));
    }


    @PostMapping("/member/register")
    public ResponseEntity<String> registerMember(@RequestBody Member member) {
        return ResponseEntity.ok(memberService.registerMember(member));
    }

    @GetMapping("/member/{id}/profile")
    public ResponseEntity<Member> getMemberProfile(@PathVariable Long id) {
        Member member = memberService.getMemberById(id);
        memberService.updateMembershipStatus(member);
        return ResponseEntity.ok(member);
    }

    @PutMapping("/member/{id}/update")
    public ResponseEntity<String> updateMember(@PathVariable Long id,@RequestBody Member member) {

        return ResponseEntity.ok(memberService.updateMember(id, member));
    }
    @PutMapping("/member/{id}/update-password")
    public ResponseEntity<String> updateMemberPassword(@PathVariable Long id,
    		@RequestBody String plainText) {
    	return ResponseEntity.ok(memberService.updatePassword(id, plainText));
    }

    @DeleteMapping("/member/{id}/delete")

    public ResponseEntity<String> deleteMember(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.deleteMemberById(id));
    }

    @PutMapping("/member/{id}/activate")
    public ResponseEntity<String> activateMembership(@PathVariable Long id,
                                                     @RequestParam int months) {
        return ResponseEntity.ok(memberService.activateMembership(id, months));
    }
}
