package com.example.tomatomall.repository;

import com.example.tomatomall.po.AdvertisementPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface AdvertisementRepository extends JpaRepository<AdvertisementPO, Integer> {
} 