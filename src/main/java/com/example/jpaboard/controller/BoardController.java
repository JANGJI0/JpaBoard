package com.example.jpaboard.controller;

import java.util.List;

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

import com.example.jpaboard.dto.BoardForm;
import com.example.jpaboard.entity.Board;
import com.example.jpaboard.repository.BoardRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class BoardController {
	@Autowired // 의존성주입
	private BoardRepository boardRepository; // new 할 수 없으니

	
	// boardList
	// 페이징, 검색기능 추가
	// GET 방식으로 /boards 경로에 접속했을 때 실행
	@GetMapping("/board/boardList")
	public String boardList(Model model // 실제는 currentPage : number / reowPerPage : size 로 한다
								// 현재 페이지 번호 (페이지 이동 시 사용)
								// "currentPage 값을 받아오고 없어면 기본값은 0
							, @RequestParam(value = "currentPage", defaultValue = "0") int currentPage // value 는 생략해도 된다.
								// 한 페이지에 보여줄 게시글 수
								// "rowPerPage" 값을 받아오고, 없으면 기본값은 0
							, @RequestParam(value = "rowPerPage", defaultValue =  "10") int rowPerPage
								// 검색어 (게시글, 제목, 내용에 포함된 단어 검색)
								// 없으면 빈 문자열로 "" 처리
							, @RequestParam(value = "word", defaultValue = "") String word){
		
		// 1. 정렬 기준 설청 (title 오름차순 -> content 내림차순)
		Sort s1 = Sort.by("title").ascending(); // title 오름차순
		Sort s2 = Sort.by("content").descending(); // content 내림차숨
		Sort sort = s1.and(s2); // 두 정렬 조합( title 다음 content)
		
		// 2. Pageable 생성 (페이지 번호, 페이지당 글 수, 정렬)
		PageRequest pageable = PageRequest.of(currentPage, rowPerPage, sort);
		
		// 3. 검색어 기준으로 페이징된 게시글 가져오기
		Page<Board> list = boardRepository.findByTitleContaining(pageable, word);
		
		// 4. 로그로 페이징 정보 확인
		log.debug("list.getToalElements(): " + list.getTotalElements()); // 전체 행의 사이즈
		log.debug("list.getTotalPages(): " + list.getTotalPages()); // 전체 페이지 사이즈 lastPage
		log.debug("list.getNumer(): " + list.getNumber()); //현재 페이지 사이즈
		log.debug("list.getSize(): " + list.getSize()); //rowPerPage
		log.debug("list.isFirst(): " + list.isFirst()); // 1페이지인지 : 이전링크유무
		log.debug("list.hasNext(): " + list.hasNext()); //다음이 있는지 : 다음리크유무
		
		// 5. view에 전달할 데이터 추가
		model.addAttribute("list", list);
		model.addAttribute("prePage", list.getNumber() - 1);
		model.addAttribute("nextPage", list.getNumber() + 1);
		model.addAttribute("lastPage", list.getTotalPages() - 1); // 마지막으로가는페이지
		model.addAttribute("word", word); //페이징 할때 검색어 유지
		
		return "board/boardList";
	}
	
	// addBoard
	@GetMapping("/board/addBoard") // doGet()
	public String addBoardForm() {
		return "board/addBoard"; // forwoard
	}
	
	
	@PostMapping("/board/create") // 문자열이어도 int를 알아서 처리해준다. // doPost()
	public String createBoard(BoardForm form) { // @RequestParma, DTO 등으로 받는게(커멘트객체)/실무에선 잘 안씀
		// DTO -> Entity 바꿔줘야함
		Board entity = form.toEntity();
		
		boardRepository.save(entity); // 레포지토리 호출할때 Entity가 필요
		return "redirect:/board/boardList"; 
		
	}
	
	// 상세보기
	@GetMapping("/board/boardOne")
	public String boardOne(Model model, @RequestParam int no) {
		// 1. 전달된 boardNo 값으로 게시글 1개를 조회
		// no = 3인 게시글을 찾음
		Board board = boardRepository.findById(no).orElse(null);
		model.addAttribute("board", board);
		
		return "board/boardOne"; // foward ( 뷰를 그대로 보여주기 때문에)
	}
	
	// 수정하기
	@GetMapping("/board/modify")
	public String modifyBoard(Model model, @RequestParam int no) {
		// 1. 전달받은 게시글 번호(no)로 게시글 1개를 DB에서 조회
		Board board = boardRepository.findById(no).orElse(null); // 값이 없으면 null
		
		// 2. 조회한 게시글을 모델에 담는다. -> 뷰에서 사용할 수 있도록
		model.addAttribute("board", board);
		
		// 3. modifyBoard로 이동
		return "board/modifyBoard";
	}
	
	@PostMapping("/board/modifyBoard")
	public String update(BoardForm boardForm) {
		// 1. 폼에서 받아온 데이터(dto)를 Entity로 변환
		Board board = boardForm.toEntity(); // no가 있으므로 update됨
		
		boardRepository.save(board);
		
		// 3. 수정 후 해당 글의 상세보기 페이지로 이동
		return "redirect:/board/boardOne?no=" + boardForm.getNo();
		
	}
	
	// 삭제하기
	@GetMapping("/board/delete")
	public String delete(@RequestParam int no, RedirectAttributes rda) {
		// 1. 전달받은 게시글 번호(no)로 게시글 1개를 DB에서 조회
		Board board = boardRepository.findById(no).orElse(null);
		
		// 2. 게시글이 존재하지 않으면 실패 메세지를 담고 상세보기로 리다이렉트
		if(board == null) {
			rda.addFlashAttribute("msg", "삭제 실패: 존재하지 않는 글입니다.");
			return " redirect:/board/boardOne?no=" + no;
		}
		
		// 3. 게시글이 존재하면 삭제처리
		boardRepository.delete(board);
		
		// 4. 삭제 성공 메세지를 담고 목록 페이지로 리다이렉트
		rda.addFlashAttribute("msg", "삭제 성공!");
		return "redirect:/board/boardList";
		
	}

}














