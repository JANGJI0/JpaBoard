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
import org.springframework.web.bind.annotation.ModelAttribute;
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
		
		return "redirect:/";
		
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
			return "redirect:/member/myPage";
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
									, @RequestParam(value = "rowPerPage", defaultValue = "3") int rowPerPage
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
	
	
		// 회원정보수정 폼, 로그인 안되었을 경우 로그인화면으로
		@GetMapping("/member/modifyMemberPw")
		public String modifyMemberModifyPw(HttpSession session) {
			if(session.getAttribute("loginMember") == null) {
				return "redirect:/member/login"; // 로그인 안되어 있으면 로그인 페이지로 리다이렉트
			}
			
			return "member/modifyMemberPw"; // 로그인이 되어있으면 비밀번호 변경폼 페이지로 이동
		}
		
		
		
		// 회원정보수정 액션 비밀번호 수정
		@PostMapping("/member/modifyMemberPw")
		public String modifyMemberPw(@ModelAttribute MemberForm memberForm, RedirectAttributes rda) { // RequestParam은 하나씩 받는거라 ModelAttribute 묶어서 받고 싶을 때 사용
			
			// 1. 아이디로 회원 조회(폼에서 이미 id도 같이 들어왔을 경우)
			Member member = memberRepository.findByMemberId(memberForm.getMemberId());
			
			if(member == null) {
				rda.addAttribute("errorMsg", "존재하지 않는 회원입니다.");
				return "redirect:/member/modifyMemberPw"; // 회원이 없으면 오류 메세지 출력, 수정페이지로 다시 보냄
			}
			
			// 2. 현재 비밀번호가 맞는지 확인
			// 사용자가 입력한 현재 비밀번호를 인코딩
			String inputCurrentPw = SHA256Util.encoding(memberForm.getCurrentPw());
			
			// DB에 저장된 인코딩된 비밀번호과 비교
			if(!member.getMemberPw().equals(inputCurrentPw)) {
				rda.addFlashAttribute("errorMsg", "현재 비밀번호가 일치하지 않습니다.");
					return "redirect:/member/modifyMemberPw";
			}
			
			// 3. 새 비밀번호와 확인 비밀번호가 같은지 확인
			if(!memberForm.getNewPw().equals(memberForm.getCheckPw())) {
				rda.addFlashAttribute("errorMsg", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
					return "redirect:/member/modifyMemberPw";
			}
			
			// 4. 새 비밀번호 인코딩 후 저장
			String encodedNewPw = SHA256Util.encoding(memberForm.getNewPw()); // 보이지 않게 인코딩
			member.setMemberPw(encodedNewPw);	// 회원 새 값 설정
			memberRepository.save(member);		// DB에 저장(업데이트)
			
			// 5. 성공 메세지 전달 후 마이페이지로 이동
			rda.addFlashAttribute("successMsg", "비밀번호가 성공적으로 변경되었습니다.");
			return "redirect:/member/myPage"; 
			
		}
			
		// 회원탈퇴 폼
		@GetMapping("/member/removeMember") // 탈퇴 폼 페이지
		public String removeMember(HttpSession session) {
			// 로그인 안 되어 있으면 로그인 페이지로 보냄
			if(session.getAttribute("loginMember") == null) {
				return "redirect:/member/login";
			}
			
			return "member/removeMember"; // 탈퇴 확인 폼페이지
		}
	
		// 회원탈퇴 액션
		@PostMapping("/member/removeMember")
		public String removeMember(@RequestParam String memberPw // 사용자가 입력한 비밀번호
									, HttpSession session, RedirectAttributes rda) {
			// 로그인된 회원정보 가져오기
			Member loginMember = (Member) session.getAttribute("loginMember");
			
			if(loginMember == null) {
				return "redirect:/member/login"; // 로그인 안되어 있으면 리다이렉트 // 폼에서 체크를 했지만 안전장치로 다시한번 체크
			}
			
			// 1. 입력한 비밀번호 인코딩해서 비교
			String encodePw = SHA256Util.encoding(memberPw);
			
			if(!encodePw.equals(loginMember.getMemberPw())) {
				rda.addFlashAttribute("errorMsg", "비밀번호가 일치하지 않습니다.");
					return "redirect:/member/removeMember"; // 비밀번호가 틀릴 경우 다시 폼으로
			}
			
			// 2. 회원 삭제
			memberRepository.delete(loginMember); // 회원삭제
			
			// 3. 세션종료 (로그아웃 처리)
			session.invalidate();
			
			rda.addFlashAttribute("successMsg", "회원탈되가 완료되었습니다.");
			return "redirect:/"; // 메인 페이지로 이동
			
		}
	
		//마이페이지
		@GetMapping("/member/myPage")
		public String myPage(HttpSession session, Model model) {
		    Member loginMember = (Member) session.getAttribute("loginMember");

		    if (loginMember == null) {
		        return "redirect:/member/login";
		    }

		    model.addAttribute("member", loginMember);
		    return "member/myPage";
		}
	
}















