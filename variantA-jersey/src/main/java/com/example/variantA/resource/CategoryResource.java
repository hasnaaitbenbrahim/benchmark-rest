package com.example.variantA.resource;

import com.example.variantA.entity.Category;
import com.example.variantA.repository.CategoryRepository;
import com.example.variantA.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @GET
    public Response list(@QueryParam("page") @DefaultValue("0") int page,
                         @QueryParam("size") @DefaultValue("20") int size) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            CategoryRepository repo = new CategoryRepository(em);
            List<Category> data = repo.findAll(page, size);
            long total = repo.countAll();
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
            CategoryRepository repo = new CategoryRepository(em);
            return repo.findById(id)
                .map(c -> Response.ok(c).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
        } finally {
            em.close();
        }
    }

    @POST
    public Response create(Category body) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            CategoryRepository repo = new CategoryRepository(em);
            Category saved = repo.save(body);
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
    public Response update(@PathParam("id") long id, Category body) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            CategoryRepository repo = new CategoryRepository(em);
            Category existing = repo.findById(id).orElse(null);
            if (existing == null) {
                em.getTransaction().rollback();
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            existing.setCode(body.getCode());
            existing.setName(body.getName());
            Category saved = repo.save(existing);
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
            CategoryRepository repo = new CategoryRepository(em);
            Category existing = repo.findById(id).orElse(null);
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
