package com.example.variantA.repository;

import com.example.variantA.entity.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class ItemRepository {
    private final EntityManager entityManager;

    public ItemRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Item save(Item item) {
        if (item.getId() == null) {
            entityManager.persist(item);
            return item;
        } else {
            return entityManager.merge(item);
        }
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Item.class, id));
    }

    public void delete(Item item) {
        Item managed = entityManager.contains(item) ? item : entityManager.merge(item);
        entityManager.remove(managed);
    }

    public List<Item> findAll(Integer categoryId, int page, int size) {
        String jpql = (categoryId == null) ?
            "SELECT i FROM Item i ORDER BY i.id" :
            "SELECT i FROM Item i WHERE i.category.id = :cid ORDER BY i.id";
        TypedQuery<Item> q = entityManager.createQuery(jpql, Item.class);
        if (categoryId != null) {
            q.setParameter("cid", categoryId.longValue());
        }
        q.setFirstResult(page * size);
        q.setMaxResults(size);
        return q.getResultList();
    }

    public long countAll(Integer categoryId) {
        String jpql = (categoryId == null) ?
            "SELECT COUNT(i) FROM Item i" :
            "SELECT COUNT(i) FROM Item i WHERE i.category.id = :cid";
        TypedQuery<Long> q = entityManager.createQuery(jpql, Long.class);
        if (categoryId != null) {
            q.setParameter("cid", categoryId.longValue());
        }
        return q.getSingleResult();
    }
}
