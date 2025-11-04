package com.cassini.graph.controller;

import com.cassini.graph.entity.Theme;
import com.cassini.graph.service.ThemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/themes")
public class ThemeController {

    @Autowired
    private ThemeService themeService;

    @GetMapping
    public ResponseEntity<List<Theme>> getAllThemes() {
        List<Theme> themes = themeService.getAllThemes();
        return new ResponseEntity<>(themes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Theme> getThemeById(@PathVariable Integer id) {
        Optional<Theme> theme = themeService.getThemeById(id);
        return theme.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Theme> createTheme(@RequestBody Theme theme) {
        Theme savedTheme = themeService.saveTheme(theme);
        return new ResponseEntity<>(savedTheme, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Theme> updateTheme(@PathVariable Integer id, @RequestBody Theme theme) {
        if (!themeService.getThemeById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        theme.setId(id);
        Theme updatedTheme = themeService.saveTheme(theme);
        return new ResponseEntity<>(updatedTheme, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Integer id) {
        if (!themeService.getThemeById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        themeService.deleteTheme(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
