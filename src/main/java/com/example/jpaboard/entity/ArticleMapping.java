package com.example.jpaboard.entity;

public interface ArticleMapping {
	Long getId();
	String getTitle();
	String getContent();
	// String getPw(); 안들고 와도 된다
}
