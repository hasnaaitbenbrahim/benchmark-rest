package com.example.variantC.controller;

import com.example.variantC.entity.Category;
import com.example.variantC.entity.Item;
import com.example.variantC.repository.CategoryRepository;
import com.example.variantC.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/categories")
public class CategoryController {
	private final CategoryRepository categoryRepository;
	private final ItemRepository itemRepository;

	public CategoryController(CategoryRepository categoryRepository, ItemRepository itemRepository) {
		this.categoryRepository = categoryRepository;
		this.itemRepository = itemRepository;
	}

	@GetMapping
	public Page<Category> list(Pageable pageable) {
		return categoryRepository.findAll(pageable);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Category> get(@PathVariable Long id) {
		return categoryRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	@Transactional
	public ResponseEntity<Category> create(@RequestBody Category body) {
		Category saved = categoryRepository.save(body);
		return ResponseEntity.created(URI.create("/categories/" + saved.getId())).body(saved);
	}

	@PutMapping("/{id}")
	@Transactional
	public ResponseEntity<Category> update(@PathVariable Long id, @RequestBody Category body) {
		return categoryRepository.findById(id)
				.map(existing -> {
					existing.setCode(body.getCode());
					existing.setName(body.getName());
					Category saved = categoryRepository.save(existing);
					return ResponseEntity.ok(saved);
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		if (!categoryRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		categoryRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/items")
	public Page<Item> items(@PathVariable Long id, Pageable pageable) {
		return itemRepository.findByCategory_Id(id, pageable);
	}
}
