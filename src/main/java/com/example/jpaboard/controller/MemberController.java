package com.example.jpaboard.controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
			
			// 로그인 성공 코드 구현
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
		@GetMapping("/member/memberList") // 페이징, id검색 추가
		public String memberList(HttpSession session, Model model // 실무에서는 currentPage : number / rowPerPage : size로 함
									, @RequestParam(value = "currentPage", defaultValue = "0") int currentPage // value 생략가능
									, @RequestParam(value = "rowPerPage", defaultValue = "10") int rowPerPage
									, @RequestParam(value = "word", defaultValue = "") String word) {
			// session 인증 /인가 검사
			if(session.getAttribute("loginMember") == null) {
					return "redirect:/member/login";
			}
			// 1. 정렬 기준 설정 (memberId 오름차순)
			Sort sort = Sort.by("memberId").ascending();
			
			// 2. pageable 생성(페이지 번호, 페이지당 글 수, 정렬)
			PageRequest Pageable = PageRequest.of(currentPage, rowPerPage, sort); // 0페이지, 10개
			
			// 3. 검색어 기준으로 페이징 된 id 가져오기
			Page<Member> list = memberRepository.findByMemberIdContaining(Pageable, word);
			
			// 4. 로그로 페이징 정보 확인
			log.debug("list.getToalElements(): " + list.getTotalElements()); //전체 행의 사이즈
			log.debug("list.getTotalPages(): " + list.getTotalPages()); // 전체 페이지 사이즈 lastPage
			log.debug("list.getNumber(): " + list.getNumber()); // 현재 페이지 사이즈
			log.debug("list.getSize(): " + list.getSize()); // rowPerPage
			log.debug("list.isFirst(): " + list.isFirst()); // 1페이지인지 : 이전 링크유무
			log.debug("list.hasNext(): " + list.hasNext()); // 다음이 있는지 : 다음링크유무
			
			// 5. view에 전달할 데이터 추가
			model.addAttribute("list", list);
			model.addAttribute("currentPage", list.getNumber() + 1); // 시작 페이지 무조건 0값을 가져와서 +1 해야 1페이지부터 시작
			model.addAttribute("prePage", list.getNumber() - 1);
			model.addAttribute("nextPage", list.getNumber() + 1);
			model.addAttribute("lastPage", list.getTotalPages() - 1); // 마지막으로 가는 페이지
			model.addAttribute("word", word); // 페이징 할 때 검색어 유지
			
			return "member/memberList";
		}
	
	
	// 회원정보수정 비밀번호 수정
		@PostMapping("/member/updatePw")
		public String updatePassword(
							  @RequestParam("memberId") String memberId
							, @RequestParam("currentPw") String currentPw
							, @RequestParam("newPw") String newPw
							, @RequestParam("checkPw") String checkPw, RedirectAttributes rda)) {
			
		}
						
	
	
	
	// 회원탈퇴
}















