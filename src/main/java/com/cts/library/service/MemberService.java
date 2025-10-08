package com.cts.library.service;

import java.util.List;

import com.cts.library.model.LoginDetails;
import com.cts.library.model.Member;

public interface MemberService {
		
	public String createAdmin(Member adminMember);
	public String registerMember(Member member);
	public String updateMember(Long id, Member member);
	public String updatePassword(Long id, String plainText);
	public String deleteMemberById(Long id);
	public List<Member> getAllMembers();
	public Member getMemberById(Long id);
	public String activateMembership(Long memberId, int monthsToExtend);
	public void updateMembershipStatus(Member member);
	public Member loginMember(LoginDetails loginDetails);
}
	