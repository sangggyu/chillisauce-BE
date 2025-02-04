package com.example.chillisauce.spaces.repository;

import com.example.chillisauce.spaces.entity.Box;
import com.example.chillisauce.spaces.entity.MultiBox;
import com.example.chillisauce.users.entity.Companies;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MultiBoxRepository extends JpaRepository<MultiBox, Long> {

    Optional<MultiBox> findByIdAndSpaceCompanies(Long multiBoxId, Companies companies);

}
