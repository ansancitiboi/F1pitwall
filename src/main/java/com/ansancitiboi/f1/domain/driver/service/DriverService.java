package com.ansancitiboi.f1.domain.driver.service;

import com.ansancitiboi.f1.domain.driver.dto.DriverResponse;
import com.ansancitiboi.f1.domain.driver.repository.DriverRepository;
import com.ansancitiboi.f1.global.common.exception.BusinessException;
import com.ansancitiboi.f1.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DriverService {

    private final DriverRepository driverRepository;

    public List<DriverResponse> findAll() {
        return driverRepository.findAll().stream()
                .map(DriverResponse::from)
                .toList();
    }

    public DriverResponse findById(Long driverId) {
        return driverRepository.findById(driverId)
                .map(DriverResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRIVER_NOT_FOUND));
    }
}
