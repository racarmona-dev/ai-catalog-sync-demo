package com.demo.seller.repository;

import com.demo.seller.entity.SellerProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerProductRepository extends JpaRepository<SellerProduct, Long> {
}
