package com.example.variantD.repository;

import com.example.variantD.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.repository.query.Param;

@RepositoryRestResource(path = "items")
public interface ItemRepository extends JpaRepository<Item, Long> {
	Page<Item> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
}
