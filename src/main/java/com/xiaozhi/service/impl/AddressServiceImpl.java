package com.xiaozhi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xiaozhi.constant.MessageConstant;
import com.xiaozhi.exception.BusinessException;
import com.xiaozhi.dto.AddressSaveDTO;
import com.xiaozhi.entity.Address;
import com.xiaozhi.mapper.AddressMapper;
import com.xiaozhi.service.AddressService;
import com.xiaozhi.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    @Override
    public List<AddressVO> list(Long userId) {
        List<Address> addresses = addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .orderByDesc(Address::getIsDefault)
                        .orderByDesc(Address::getCreateTime));
        return addresses.stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public void save(Long userId, AddressSaveDTO dto) {
        Address address = new Address();
        address.setUserId(userId);
        address.setName(dto.getName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);

        if (dto.getId() != null) {
            // 修改：校验归属
            Address existing = addressMapper.selectById(dto.getId());
            if (existing == null || !existing.getUserId().equals(userId)) {
                throw new BusinessException(MessageConstant.ADDRESS_NOT_FOUND);
            }
            address.setId(dto.getId());
        }

        // 如果设为默认，先取消其他默认
        if (address.getIsDefault() == 1) {
            clearDefault(userId);
        }

        if (dto.getId() != null) {
            addressMapper.updateById(address);
        } else {
            addressMapper.insert(address);
        }
    }

    @Override
    public void delete(Long userId, Long id) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(MessageConstant.ADDRESS_NOT_FOUND);
        }
        addressMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long id) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(MessageConstant.ADDRESS_NOT_FOUND);
        }
        clearDefault(userId);
        address.setIsDefault(1);
        addressMapper.updateById(address);
    }

    private void clearDefault(Long userId) {
        addressMapper.update(null,
                new LambdaUpdateWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .eq(Address::getIsDefault, 1)
                        .set(Address::getIsDefault, 0));
    }

    private AddressVO toVO(Address address) {
        AddressVO vo = new AddressVO();
        vo.setId(address.getId());
        vo.setName(address.getName());
        vo.setPhone(address.getPhone());
        vo.setProvince(address.getProvince());
        vo.setCity(address.getCity());
        vo.setDistrict(address.getDistrict());
        vo.setDetail(address.getDetail());
        vo.setIsDefault(address.getIsDefault());
        return vo;
    }
}
