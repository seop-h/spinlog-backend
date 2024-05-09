package com.example.spinlog.article.repository;

import com.example.spinlog.article.dto.SearchCond;
import com.example.spinlog.article.dto.ViewArticleResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleRepositoryCustom {
    Page<ViewArticleResponseDTO> search(SearchCond cond, Pageable pageable);
}