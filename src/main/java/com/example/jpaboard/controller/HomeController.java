package com.example.jpaboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class HomeController {
		@GetMapping("/")
		public String home(Model model) {
			model.addAttribute("loginName", "구디");
			// System.out.println(model.getAttribute("loginName"));
			// log 프레임워크 사용(망치 , 톱은 라이브러리/ 집 프레임워크)
			// Log.trace("loginName: " + model.getAttribute("loginName"));
			log.debug("loginName: " + model.getAttribute("loginName"));
			// Log.info("loginName: " + model.getAttribute("loginName"));
			return "home";
		}
}
