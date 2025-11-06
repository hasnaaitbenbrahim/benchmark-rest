package com.example.variantC.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "item")
public class Item {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 64, nullable = false, unique = true)
	private String sku;

	@Column(length = 128, nullable = false)
	private String name;

	@Column(precision = 10, scale = 2, nullable = false)
	private BigDecimal price;

	@Column(nullable = false)
	private Integer stock;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	@JsonBackReference
	private Category category;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	@PreUpdate
	void touchTimestamp() {
		if (updatedAt == null) {
			updatedAt = OffsetDateTime.now();
		} else {
			updatedAt = OffsetDateTime.now();
		}
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getSku() { return sku; }
	public void setSku(String sku) { this.sku = sku; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public BigDecimal getPrice() { return price; }
	public void setPrice(BigDecimal price) { this.price = price; }

	public Integer getStock() { return stock; }
	public void setStock(Integer stock) { this.stock = stock; }

	public Category getCategory() { return category; }
	public void setCategory(Category category) { this.category = category; }

	public OffsetDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
