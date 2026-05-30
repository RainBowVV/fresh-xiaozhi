package com.xiaozhi.service;

import com.xiaozhi.dto.AddressSaveDTO;
import com.xiaozhi.vo.AddressVO;
import java.util.List;

public interface AddressService {
    List<AddressVO> list(Long userId);
    void save(Long userId, AddressSaveDTO dto);
    void delete(Long userId, Long id);
    void setDefault(Long userId, Long id);
}
