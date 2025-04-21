package com.example.jpaboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.jpaboard.dto.ArticleForm;
import com.example.jpaboard.entity.Article;
import com.example.jpaboard.repository.ArticleRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ArticleController {
	@Autowired // 의존성주입
	private ArticleRepository articleRepository; // new 할 수 없으니
	
	// 삭제
	@GetMapping("/articles/delete")
	public String delete(@RequestParam Long id, RedirectAttributes rda) {
		Article article = articleRepository.findById(id).orElse(null);
		articleRepository.delete(article);
		
		if(article == null) {
			rda.addFlashAttribute("msg", "삭제 실패"); // redirect 의 뷰의 모델에서 자동을 출력가능
			return "redirect:/articles/show?id= + id";
		}
		articleRepository.delete(article);
		rda.addFlashAttribute("msg", "삭제 성공");
		return "redirect:/articles/index";
	}
	
	// 수정
	@PostMapping("/articles/edit")
	public String update(ArticleForm articleForm) {
		Article article = articleForm.toEntity(); 
		//entity가 키값을 가지고 있으면 새로운 행을 추가하는게 아니고
		// 존재하는 키값의 행을 수정(update)
		articleRepository.save(article); // update
		return  "redirect:/articles/show?id=" + articleForm.getId();
	}
	
	@GetMapping("/articles/edit")
	public String edit(Model model, @RequestParam Long id) {
		Article article = articleRepository.findById(id).orElse(null); // null이아니면 들어가고 
		model.addAttribute("article", article);
		return "articles/edit";
	}

	
	@GetMapping("/articles/show")
	public String show(Model model, @RequestParam Long id) {
		Article article = articleRepository.findById(id).orElse(null); // null이아니면 들어가고 
		model.addAttribute("article", article);
		return "articles/show";
	}


	@GetMapping("/articles/index")
	public String articleList(Model model  // 실제는 currentPage : number / rowPerPage : size 로 한다
							, @RequestParam(value = "currentPage", defaultValue = "0") int currentPage // value 생략해도 된다.
							, @RequestParam(value =  "rowPerPage", defaultValue = "10") int rowPerPage
							, @RequestParam(value = "word", defaultValue = "") String word) {
		
		
		Sort s1 = Sort.by("title").ascending();
		Sort s2 = Sort.by("content").descending();
		Sort sort = s1.and(s2);
				
		PageRequest Pageable = PageRequest.of(currentPage, rowPerPage, sort); // 0 페이지, 10개
		Page<Article> list= articleRepository.findByTitleContaining(Pageable, word);
		
		// Page 의 추가 속성
		// list.size() : 10개
		log.debug("list.getTotalElements(): " + list.getTotalElements()); // 전체 행의 사이즈 
		log.debug("list.getTotalPages(): " + list.getTotalPages()); // 전체 페이지 사이즈 lastPage
		log.debug("list.getNember(): " + list.getNumber()); // 현재 페이지 사이즈
		log.debug("list.getSize(): " + list.getSize()); // rowPerPage
		log.debug("list.isFirst(): " + list.isFirst()); // 1페이지인지 : 이전링크유무
		log.debug("list.hasNext(): " + list.hasNext()); // 다음이 있는지 : 다음링크유무
		
		model.addAttribute("list", list);
		model.addAttribute("prePage", list.getNumber() - 1);
		model.addAttribute("nextPage", list.getNumber() + 1);
		model.addAttribute("word", word); // 페이징할때 다음 값도 같이 넘긴다
		// + RedirectAttributes.addribute() 값이 포함
		return "articles/index";
	}
	
	@GetMapping("/articles/new") // doGet()
	public String newAticleForm() {
		return "articles/new"; // forward
	}
	
	@PostMapping("/articles/create") // 문자열이어도 int로 알아서 처리해준다. // doPost()
	public String createAtrticle(ArticleForm form) { // @RequestParma, DTO 통으로 받는게(커멘드객체)/실무에선 잘 안쓴다.
		System.out.println(form.toString());
		
		// DTO -> Entity 바꿔줘야한다.
		Article entity = form.toEntity();
		
		/*
		Article entity = new Article(); 
		entity.setTitle(form.getTitle());
		entity.setContent(form.getContent());
		*/
		articleRepository.save(entity); // 레포지토리 호출할때는 Entity가 필요 //
		return "redirect:/article/list"; // "/article/list 리다이렉트 "redirect:/articles/list"
											// GET 호출 / articles/list
	}
	
	
	

	
	/* public String createAtrticle(@RequestParam(value = "currentPage", defaultValue = "1") int currentPage
									, @RequestParam(value = "title") String title
									, @RequestParam(value = "content") String content) {
		// String title = request.getParameter(title) API 
		System.out.println("currentPage: " + currentPage);
		System.out.println("title: " + title);
		System.out.println("content: " + content);
		return "";
	}
	*/
}
