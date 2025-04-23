package com.example.jpaboard.dto;


import com.example.jpaboard.entity.Member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberForm {
	private int memberNo;
	private String memberId;
	private String memberPw;
	
	// 비밀번호 변경 시 사용하는 추가 필드들
	private String currentPw; // 비밀번호 맞는지 확인용
	private String newPw;	 // 새로운 비밀번호
	private String checkPw;	// 새로운 비밀번호 확인용
	
	public Member toEntity() {
		Member entity = new Member();
		entity.setMemberNo(this.memberNo);
		entity.setMemberId(this.memberId);
		entity.setMemberPw(this.memberPw);
		
		return entity;
	}
}
