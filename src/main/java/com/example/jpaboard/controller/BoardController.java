package com.example.jpaboard.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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
	// GET 방식으로 /boards 경로에 접속했을 때 실행
	@GetMapping("/board/boardList")
	public String boardList(Model model) {
		//DB에서 모든 게시글을 가져옴
		List<Board> boardList = boardRepository.findAll();
		
		// 가져온 목록을 모델에 담아서 뷰로 전달
		model.addAttribute("boardList", boardList);
		
		return "/board/boardList";
	}
	
	
	@GetMapping("/board/addBoard") // doGet()
	public String addBoardForm() {
		return "board/addBoard"; // forwoard
	}
	
	// addBoard
	@PostMapping("/board/create") // 문자열이어도 int를 알아서 처리해준다. // doPost()
	public String createBoard(BoardForm form) { // @RequestParma, DTO 등으로 받는게(커멘트객체)/실무에선 잘 안씀
		// DTO -> Entity 바꿔줘야함
		Board entity = form.toEntity();
		
		boardRepository.save(entity); // 레포지토리 호출할때 Entity가 필요
		return "redirect:/board/boardList"; 
		
	}

}
