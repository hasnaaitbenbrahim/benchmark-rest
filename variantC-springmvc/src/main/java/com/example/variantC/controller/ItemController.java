package com.example.variantC.controller;

import com.example.variantC.entity.Category;
import com.example.variantC.entity.Item;
import com.example.variantC.repository.CategoryRepository;
import com.example.variantC.repository.ItemRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/items")
public class ItemController {
	private final ItemRepository itemRepository;
	private final CategoryRepository categoryRepository;
	private final EntityManager entityManager;

	public ItemController(ItemRepository itemRepository, CategoryRepository categoryRepository, EntityManager entityManager) {
		this.itemRepository = itemRepository;
		this.categoryRepository = categoryRepository;
		this.entityManager = entityManager;
	}

	@GetMapping
	public Page<Item> list(@RequestParam(value = "categoryId", required = false) Long categoryId, Pageable pageable) {
		if (categoryId != null) {
			return itemRepository.findByCategory_Id(categoryId, pageable);
		}
		return itemRepository.findAll(pageable);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Item> get(@PathVariable Long id) {
		return itemRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	@Transactional
	public ResponseEntity<Item> create(@RequestBody Item body) {
		if (body.getCategory() != null && body.getCategory().getId() != null) {
			if (!categoryRepository.existsById(body.getCategory().getId())) {
				return ResponseEntity.badRequest().build();
			}
			// Use getReference for better performance (lazy loading)
			Category categoryRef = entityManager.getReference(Category.class, body.getCategory().getId());
			body.setCategory(categoryRef);
		}
		Item saved = itemRepository.save(body);
		return ResponseEntity.created(URI.create("/items/" + saved.getId())).body(saved);
	}

	@PutMapping("/{id}")
	@Transactional
	public ResponseEntity<Item> update(@PathVariable Long id, @RequestBody Item body) {
		// Validate category if provided
		if (body.getCategory() != null && body.getCategory().getId() != null) {
			if (!categoryRepository.existsById(body.getCategory().getId())) {
				return ResponseEntity.badRequest().build();
			}
		}
		
		return itemRepository.findById(id)
				.map(existing -> {
					existing.setSku(body.getSku());
					existing.setName(body.getName());
					existing.setPrice(body.getPrice());
					existing.setStock(body.getStock());
					if (body.getCategory() != null && body.getCategory().getId() != null) {
						// Use getReference for better performance (lazy loading)
						Category categoryRef = entityManager.getReference(Category.class, body.getCategory().getId());
						existing.setCategory(categoryRef);
					}
					Item saved = itemRepository.save(existing);
					return ResponseEntity.ok(saved);
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		if (!itemRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		itemRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
