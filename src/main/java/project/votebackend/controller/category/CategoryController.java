package project.votebackend.controller.category;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.votebackend.domain.category.Category;
import project.votebackend.repository.category.CategoryRepository;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryRepository categoryRepository;

    //카테고리 생성
    @PostMapping
    @Operation(summary = "카테고리 생성 API", description = "카테고리를 생성합니다.")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    //카테고리 조회
    @GetMapping
    @Operation(summary = "카테고리 조회 API", description = "카테고리를 조회합니다.")
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }
}
