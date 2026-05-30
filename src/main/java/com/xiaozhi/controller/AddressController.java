package com.xiaozhi.controller;

import com.xiaozhi.annotation.RequireAuth;
import com.xiaozhi.common.Result;
import com.xiaozhi.dto.AddressSaveDTO;
import com.xiaozhi.service.AddressService;
import com.xiaozhi.util.UserContext;
import com.xiaozhi.vo.AddressVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 查询当前用户地址列表
     * @return 地址列表
     */
    @RequireAuth
    @GetMapping("/list")
    public Result<List<AddressVO>> list() {
        return Result.ok(addressService.list(UserContext.getUserId()));
    }

    /**
     * 添加或更新地址
     * @param dto
     * @return
     */
    @RequireAuth
    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody AddressSaveDTO dto) {
        addressService.save(UserContext.getUserId(), dto);
        return Result.ok();
    }

    /**
     * 根据地址ID删除地址
     * @param id 地址ID
     * @return
     */
    @RequireAuth
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(UserContext.getUserId(), id);
        return Result.ok();
    }

    /**
     * 设置默认地址
     * @param id 地址ID
     * @return
     */
    @RequireAuth
    @PutMapping("/default/{id}")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(UserContext.getUserId(), id);
        return Result.ok();
    }
}
