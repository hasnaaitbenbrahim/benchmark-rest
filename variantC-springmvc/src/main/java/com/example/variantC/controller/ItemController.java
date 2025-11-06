package com.example.variantC.controller;

import com.example.variantC.entity.Category;
import com.example.variantC.entity.Item;
import com.example.variantC.repository.CategoryRepository;
import com.example.variantC.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/items")
public class ItemController {
	private final ItemRepository itemRepository;
	private final CategoryRepository categoryRepository;

	public ItemController(ItemRepository itemRepository, CategoryRepository categoryRepository) {
		this.itemRepository = itemRepository;
		this.categoryRepository = categoryRepository;
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
	public ResponseEntity<Item> create(@RequestBody Item body) {
		if (body.getCategory() != null && body.getCategory().getId() != null) {
			Category category = categoryRepository.findById(body.getCategory().getId()).orElse(null);
			if (category == null) return ResponseEntity.badRequest().build();
			body.setCategory(category);
		}
		Item saved = itemRepository.save(body);
		return ResponseEntity.created(URI.create("/items/" + saved.getId())).body(saved);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Item> update(@PathVariable Long id, @RequestBody Item body) {
		return itemRepository.findById(id)
				.map(existing -> {
					existing.setSku(body.getSku());
					existing.setName(body.getName());
					existing.setPrice(body.getPrice());
					existing.setStock(body.getStock());
					if (body.getCategory() != null && body.getCategory().getId() != null) {
						Category category = categoryRepository.findById(body.getCategory().getId()).orElse(null);
						if (category == null) return ResponseEntity.badRequest().build();
						existing.setCategory(category);
					}
					Item saved = itemRepository.save(existing);
					return ResponseEntity.ok(saved);
				})
				.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		return itemRepository.findById(id)
				.map(existing -> {
					itemRepository.delete(existing);
					return ResponseEntity.noContent().build();
				})
				.orElse(ResponseEntity.notFound().build());
	}
}
