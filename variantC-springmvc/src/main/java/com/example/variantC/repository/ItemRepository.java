package com.example.variantC.repository;

import com.example.variantC.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
	Page<Item> findByCategory_Id(Long categoryId, Pageable pageable);
}
