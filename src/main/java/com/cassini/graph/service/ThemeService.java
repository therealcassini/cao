package com.cassini.graph.service;

import com.cassini.graph.entity.Theme;
import java.util.List;
import java.util.Optional;

public interface ThemeService {
    List<Theme> getAllThemes();
    Optional<Theme> getThemeById(Integer id);
    Theme saveTheme(Theme theme);
    void deleteTheme(Integer id);
}
