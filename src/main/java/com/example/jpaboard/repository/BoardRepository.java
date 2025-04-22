package com.example.jpaboard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jpaboard.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Integer> {

	
	// findAll() : 원하는 컬럼만 가지고 오도록
	Page<Board> findByTitleContaining(Pageable pageable, String word);
}
