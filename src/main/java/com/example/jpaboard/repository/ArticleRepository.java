package com.example.jpaboard.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jpaboard.entity.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {

	 // void save(ArticleForm form);
	// CrudRepository : insert, select one, select all, update, delete
	// 의 자식
	// JpaRepository(CrudRepository 자식 인터페이스) : select limit, select order by, .... 
	
	// findAll() : 원하는 컬럼만 가지고 오도록 .....
	Page<Article> findByTitleContaining(Pageable pageable, String word);
}
