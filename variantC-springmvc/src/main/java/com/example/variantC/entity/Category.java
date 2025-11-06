package com.example.variantC.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 32, nullable = false, unique = true)
	private String code;

	@Column(length = 128, nullable = false)
	private String name;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = false)
	@JsonManagedReference
	private List<Item> items = new ArrayList<>();

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

	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public OffsetDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

	public List<Item> getItems() { return items; }
	public void setItems(List<Item> items) { this.items = items; }
}
