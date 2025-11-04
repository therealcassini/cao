package com.cassini.graph.repository;

import com.cassini.graph.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Integer> {
    // 可以添加自定义查询方法
    }
