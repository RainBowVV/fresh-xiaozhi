package com.xiaozhi.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaozhi.constant.MessageConstant;
import com.xiaozhi.constant.RedisKeyConstant;
import com.xiaozhi.dto.CategorySaveDTO;
import com.xiaozhi.entity.Category;
import com.xiaozhi.entity.Product;
import com.xiaozhi.exception.BusinessException;
import com.xiaozhi.mapper.CategoryMapper;
import com.xiaozhi.mapper.ProductMapper;
import com.xiaozhi.service.CategoryService;
import com.xiaozhi.vo.CategoryVO;
import com.xiaozhi.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public List<CategoryVO> list() {
        // 1. 查缓存
        Object cached = redisTemplate.opsForValue().get(RedisKeyConstant.CATEGORY_LIST_KEY);
        if (cached != null) {
            // 缓存命中，刷新 TTL
            redisTemplate.expire(RedisKeyConstant.CATEGORY_LIST_KEY,
                    RedisKeyConstant.CATEGORY_CACHE_TTL, RedisKeyConstant.CACHE_TTL_UNIT);
            return JSONUtil.toList(cached.toString(), CategoryVO.class);
        }

        // 2. 查数据库
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
        List<CategoryVO> result = categories.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setSort(c.getSort());
            return vo;
        }).toList();

        // 3. 回填缓存
        redisTemplate.opsForValue().set(RedisKeyConstant.CATEGORY_LIST_KEY,
                JSONUtil.toJsonStr(result), RedisKeyConstant.CATEGORY_CACHE_TTL, RedisKeyConstant.CACHE_TTL_UNIT);

        return result;
    }

    @Override
    public List<CategoryVO> listFromDB() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
        return categories.stream().map(this::toVO).toList();
    }

    @Override
    public PageResult<CategoryVO> page(Integer pageNum, Integer pageSize) {
        Page<Category> page = categoryMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
        List<CategoryVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private CategoryVO toVO(Category c) {
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setSort(c.getSort());
        return vo;
    }

    @Override
    public void save(CategorySaveDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);

        if (dto.getId() != null) {
            // 修改
            Category existing = categoryMapper.selectById(dto.getId());
            if (existing == null) {
                throw new BusinessException(MessageConstant.CATEGORY_NOT_FOUND);
            }
            category.setId(dto.getId());
            categoryMapper.updateById(category);
        } else {
            // 新增
            categoryMapper.insert(category);
        }

        // 清除分类缓存 + 所有商品列表缓存
        evictCategoryCache();
    }

    @Override
    public void delete(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        // 检查分类下是否有商品
        long count = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id));
        if (count > 0) {
            throw new BusinessException(MessageConstant.CATEGORY_HAS_PRODUCTS);
        }

        categoryMapper.deleteById(id);

        // 清除分类缓存 + 该分类的商品列表缓存
        redisTemplate.delete(RedisKeyConstant.PRODUCT_LIST_KEY + id);
        evictCategoryCache();
    }

    private void evictCategoryCache() {
        redisTemplate.delete(RedisKeyConstant.CATEGORY_LIST_KEY);
        // 分类变更可能影响商品列表分组，清除所有商品列表缓存
        redisTemplate.delete(RedisKeyConstant.PRODUCT_LIST_KEY + "all");
    }
}
