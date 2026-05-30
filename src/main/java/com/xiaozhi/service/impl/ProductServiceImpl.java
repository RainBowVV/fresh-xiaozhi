package com.xiaozhi.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaozhi.constant.MessageConstant;
import com.xiaozhi.constant.RedisKeyConstant;
import com.xiaozhi.dto.ProductSaveDTO;
import com.xiaozhi.exception.BusinessException;
import com.xiaozhi.entity.Product;
import com.xiaozhi.mapper.ProductMapper;
import com.xiaozhi.service.ProductService;
import com.xiaozhi.vo.PageResult;
import com.xiaozhi.vo.ProductDetailVO;
import com.xiaozhi.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    @SuppressWarnings("unchecked")
    public List<ProductVO> listByCategoryId(Long categoryId) {
        String cacheKey = RedisKeyConstant.PRODUCT_LIST_KEY + (categoryId != null ? categoryId : "all");

        // 1. 查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            // 缓存命中，刷新 TTL
            redisTemplate.expire(cacheKey, RedisKeyConstant.PRODUCT_CACHE_TTL, RedisKeyConstant.CACHE_TTL_UNIT);
            return JSONUtil.toList(cached.toString(), ProductVO.class);
        }

        // 2. 缓存未命中，查数据库
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1);
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        List<Product> products = productMapper.selectList(wrapper);
        List<ProductVO> result = products.stream().map(p -> {
            ProductVO vo = toProductVO(p);
            return vo;
        }).toList();

        // 3. 回填缓存
        redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result), RedisKeyConstant.PRODUCT_CACHE_TTL, RedisKeyConstant.CACHE_TTL_UNIT);

        return result;
    }

    @Override
    public List<ProductVO> listByCategoryIdFromDB(Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>();
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        wrapper.orderByAsc(Product::getId);
        List<Product> products = productMapper.selectList(wrapper);
        return products.stream().map(this::toProductVO).toList();
    }

    @Override
    public PageResult<ProductVO> pageFromDB(Long categoryId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>();
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        wrapper.orderByAsc(Product::getId);
        Page<Product> page = productMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<ProductVO> records = page.getRecords().stream().map(this::toProductVO).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ProductDetailVO getDetail(Long id) {
        String cacheKey = RedisKeyConstant.PRODUCT_DETAIL_KEY + id;

        // 1. 查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            // 缓存命中，刷新 TTL
            redisTemplate.expire(cacheKey, RedisKeyConstant.PRODUCT_CACHE_TTL, RedisKeyConstant.CACHE_TTL_UNIT);
            return JSONUtil.toBean(cached.toString(), ProductDetailVO.class);
        }

        // 2. 缓存未命中，查数据库
        Product product = productMapper.selectById(id);
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException(MessageConstant.PRODUCT_NOT_FOUND_OR_OFFLINE);
        }
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(product.getId());
        vo.setCategoryId(product.getCategoryId());
        vo.setName(product.getName());
        vo.setImage(product.getImage());
        vo.setPrice(product.getPrice());
        vo.setUnit(product.getUnit());
        vo.setDescription(product.getDescription());

        // 3. 回填缓存
        redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(vo), RedisKeyConstant.PRODUCT_CACHE_TTL, RedisKeyConstant.CACHE_TTL_UNIT);

        return vo;
    }

    @Override
    public void save(ProductSaveDTO dto) {
        Product product = new Product();
        product.setCategoryId(dto.getCategoryId());
        product.setName(dto.getName());
        product.setImage(dto.getImage() != null ? dto.getImage() : "");
        product.setPrice(dto.getPrice());
        product.setUnit(dto.getUnit() != null ? dto.getUnit() : "");
        product.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        if (dto.getId() != null) {
            // 修改
            Product existing = productMapper.selectById(dto.getId());
            if (existing == null) {
                throw new BusinessException(MessageConstant.PRODUCT_NOT_FOUND);
            }
            product.setId(dto.getId());
            productMapper.updateById(product);
            // 清除该商品缓存
            evictProductCache(dto.getId(), dto.getCategoryId());
        } else {
            // 新增
            productMapper.insert(product);
            // 清除列表缓存
            evictProductListCache(dto.getCategoryId());
        }
    }

    @Override
    public void delete(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(MessageConstant.PRODUCT_NOT_FOUND);
        }
        productMapper.deleteById(id);
        evictProductCache(id, product.getCategoryId());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(MessageConstant.PRODUCT_NOT_FOUND);
        }
        product.setStatus(status);
        productMapper.updateById(product);
        evictProductCache(id, product.getCategoryId());
    }

    /**
     * 清除单个商品详情缓存 + 所属分类列表缓存 + 全部列表缓存
     */
    private void evictProductCache(Long productId, Long categoryId) {
        redisTemplate.delete(RedisKeyConstant.PRODUCT_DETAIL_KEY + productId);
        evictProductListCache(categoryId);
    }

    /**
     * 清除分类列表缓存 + 全部列表缓存
     */
    private void evictProductListCache(Long categoryId) {
        redisTemplate.delete(RedisKeyConstant.PRODUCT_LIST_KEY + categoryId);
        redisTemplate.delete(RedisKeyConstant.PRODUCT_LIST_KEY + "all");
    }

    private ProductVO toProductVO(Product p) {
        ProductVO vo = new ProductVO();
        vo.setId(p.getId());
        vo.setCategoryId(p.getCategoryId());
        vo.setName(p.getName());
        vo.setImage(p.getImage());
        vo.setPrice(p.getPrice());
        vo.setUnit(p.getUnit());
        vo.setDescription(p.getDescription());
        vo.setStatus(p.getStatus());
        return vo;
    }
}
