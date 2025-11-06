package com.example.variantA.resource;

import com.example.variantA.entity.Category;
import com.example.variantA.entity.Item;
import com.example.variantA.repository.CategoryRepository;
import com.example.variantA.repository.ItemRepository;
import com.example.variantA.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemResource {

    @GET
    public Response list(@QueryParam("categoryId") Long categoryId,
                         @QueryParam("page") @DefaultValue("0") int page,
                         @QueryParam("size") @DefaultValue("20") int size) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            ItemRepository repo = new ItemRepository(em);
            List<Item> data = repo.findAll(categoryId == null ? null : categoryId.intValue(), page, size);
            long total = repo.countAll(categoryId == null ? null : categoryId.intValue());
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", data);
            payload.put("page", page);
            payload.put("size", size);
            payload.put("totalElements", total);
            return Response.ok(payload).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            ItemRepository repo = new ItemRepository(em);
            return repo.findById(id)
                .map(i -> Response.ok(i).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
        } finally {
            em.close();
        }
    }

    @POST
    public Response create(Item body) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            ItemRepository repo = new ItemRepository(em);
            if (body.getCategory() != null && body.getCategory().getId() != null) {
                Category categoryRef = em.getReference(Category.class, body.getCategory().getId());
                body.setCategory(categoryRef);
            }
            Item saved = repo.save(body);
            em.getTransaction().commit();
            return Response.status(Response.Status.CREATED).entity(saved).build();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") long id, Item body) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            ItemRepository repo = new ItemRepository(em);
            Item existing = repo.findById(id).orElse(null);
            if (existing == null) {
                em.getTransaction().rollback();
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            existing.setSku(body.getSku());
            existing.setName(body.getName());
            existing.setPrice(body.getPrice());
            existing.setStock(body.getStock());
            if (body.getCategory() != null && body.getCategory().getId() != null) {
                existing.setCategory(em.getReference(Category.class, body.getCategory().getId()));
            }
            Item saved = repo.save(existing);
            em.getTransaction().commit();
            return Response.ok(saved).build();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            ItemRepository repo = new ItemRepository(em);
            Item existing = repo.findById(id).orElse(null);
            if (existing == null) {
                em.getTransaction().rollback();
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            repo.delete(existing);
            em.getTransaction().commit();
            return Response.noContent().build();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
