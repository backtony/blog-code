package com.example.cleancode.service;


import com.example.cleancode.repository.ApartmentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;

    @Transactional(readOnly = true)
    public Long getPriceOrThrow(Long apartmentId){
        return apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new RuntimeException("not found"))
                .getPrice();
    }
}
