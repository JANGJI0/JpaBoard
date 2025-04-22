package com.example.jpaboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jpaboard.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Integer>{
	// member_id 중복검사 : existsBy 엔티티컬럼필드
	boolean existsByMemberId(String memberId);
	
	// 로그인 하는 추상 메서드 : findBy 엔티티컬럼필드... And엔티티컬럼필드
	// findBy컬럼명And컬럼명 And...
	// 쿼리선언해서 넣어도된다
	Member findByMemberIdAndMemberPw(String mameberId, String memberPw); // 로그인 실패시 null로 떨어진다
		
}
