package com.example.jpaboard.controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.jpaboard.dto.MemberForm;
import com.example.jpaboard.entity.Member;
import com.example.jpaboard.repository.MemberRepository;
import com.example.jpaboard.util.SHA256Util;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class MemberController {
	@Autowired
	MemberRepository memberRepository;
	
	// 회원가입 + member_id 중복확인
	@GetMapping("/member/joinMember")
	public String joinMember() {
		return "member/joinMember";
	}
	
	@PostMapping("/member/joinMember")
	public String joinMember(MemberForm memberForm, RedirectAttributes rda) {
		// memberForm.getMemberId() DB에 존재한다면
		
		// 디버깅
		log.debug(memberForm.toString());
		log.debug("isMemberId : " + memberRepository.existsByMemberId(memberForm.getMemberId()));
		
		if(memberRepository.existsByMemberId(memberForm.getMemberId())) {
			rda.addFlashAttribute("msg", memberForm.getMemberId() + "ID가 이미 존재합니다.");
			 return "redirect:/member/joinMember";
		}
		
		// false 이면 회원가입 진행
		// memberForm.getMemberPw() 값을 SHA-256방식으로 암호화
		memberForm.setMemberPw(SHA256Util.encoding(memberForm.getMemberPw()));
		
		/*
		String s = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(memberForm.getMemberPw().getBytes()); // 문자열의 바이트를 암호화 / byte타입으로 바꿔야한다
			
			for(byte b : md.digest()) {
				s = s + String.format("%02x", b); // 16진수 %x 두자리수 %02x
				// 나중에 String vs StringBuffer 차이
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			log.debug("PW 암호화 실패");
			
		}
		log.debug(s);
		memberForm.setMemberPw(s);
		*/
		// Member member = memberForm.toEntity();
		// memberRepository.save(member);
		
		Member member = memberForm.toEntity();
		memberRepository.save(member); // 최종 커밋시
		
		return "redirect:/member/login";
		
		}
		
		// 로그인
		@GetMapping("/member/login")
		public String login() {
			return "member/login";
		}
		
		// 로그인 액션
		@PostMapping("/member/login")
		public String login(HttpSession session, MemberForm memberForm, RedirectAttributes rda) {
			// pw 암호화
			memberForm.setMemberPw(SHA256Util.encoding(memberForm.getMemberPw()));
			
			// 로그인 확인 메서드
			Member loginMember
				= memberRepository.findByMemberIdAndMemberPw(memberForm.getMemberId(), memberForm.getMemberPw());
			
			if(loginMember == null) {
				log.debug("로그인 실패");
				rda.addFlashAttribute("msg", "로그인 실패");
				return "redirect:/member/login";
			}
			
			// 로그인 성공 코드 구형
			session.setAttribute("loginMember", loginMember); // ISSUE : pw정보까지 세션에 저장 나중에는 이렇게 하면 안된다.
			return "redirect:/member/memberList";
		}
		
		// 로그아웃
		@GetMapping("/member/logout")
		public String logout(HttpSession session) {
			session.invalidate();
			return "redirect:/member/login";
		}
		
		// 회원목록
		@GetMapping("/member/memberList")
		public String memberList(HttpSession session) {
			// session 인증 /인가 검사
			if(session.getAttribute("loginMember") == null) {
					return "redirect:/member/login";
			}
			
			
			//사용자 목록 + 페이징 + id 검색
			// Page<Member> = memberRepository.findByMemberIdContaining(Pageable pageable, String word);
			return " member/memberList";
		}
	
	
	// 회원정보수정
	
	
	
	// 회원탈퇴
}















