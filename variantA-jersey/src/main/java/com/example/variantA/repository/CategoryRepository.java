package com.example.variantA.repository;

import com.example.variantA.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class CategoryRepository {
    private final EntityManager entityManager;

    public CategoryRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Category save(Category category) {
        if (category.getId() == null) {
            entityManager.persist(category);
            return category;
        } else {
            return entityManager.merge(category);
        }
    }

    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Category.class, id));
    }

    public void delete(Category category) {
        Category managed = entityManager.contains(category) ? category : entityManager.merge(category);
        entityManager.remove(managed);
    }

    public List<Category> findAll(int page, int size) {
        TypedQuery<Category> q = entityManager.createQuery("SELECT c FROM Category c ORDER BY c.id", Category.class);
        q.setFirstResult(page * size);
        q.setMaxResults(size);
        return q.getResultList();
    }

    public long countAll() {
        return entityManager.createQuery("SELECT COUNT(c) FROM Category c", Long.class).getSingleResult();
    }
}
