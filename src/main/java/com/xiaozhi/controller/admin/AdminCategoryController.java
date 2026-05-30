package com.xiaozhi.controller.admin;

import com.xiaozhi.common.Result;
import com.xiaozhi.dto.CategorySaveDTO;
import com.xiaozhi.service.CategoryService;
import com.xiaozhi.vo.CategoryVO;
import com.xiaozhi.vo.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<PageResult<CategoryVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(categoryService.page(pageNum, pageSize));
    }

    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody CategorySaveDTO dto) {
        categoryService.save(dto);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
