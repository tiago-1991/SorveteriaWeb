/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teste.control;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;
import teste.control.exceptions.NonexistentEntityException;
import teste.control.exceptions.PreexistingEntityException;
import teste.control.exceptions.RollbackFailureException;
import teste.model.Itenspedido;
import teste.model.ItenspedidoPK;
import teste.model.Pedido;
import teste.model.Produto;

/**
 *
 * @author Tiago
 */
public class ItenspedidoJpaController implements Serializable {

    public ItenspedidoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Itenspedido itenspedido) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (itenspedido.getItenspedidoPK() == null) {
            itenspedido.setItenspedidoPK(new ItenspedidoPK());
        }
        itenspedido.getItenspedidoPK().setIdproduto(itenspedido.getProduto().getIdproduto());
        itenspedido.getItenspedidoPK().setNropedido(itenspedido.getPedido().getNropedido());
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Pedido pedido = itenspedido.getPedido();
            if (pedido != null) {
                pedido = em.getReference(pedido.getClass(), pedido.getNropedido());
                itenspedido.setPedido(pedido);
            }
            Produto produto = itenspedido.getProduto();
            if (produto != null) {
                produto = em.getReference(produto.getClass(), produto.getIdproduto());
                itenspedido.setProduto(produto);
            }
            em.persist(itenspedido);
            if (pedido != null) {
                pedido.getItenspedidoCollection().add(itenspedido);
                pedido = em.merge(pedido);
            }
            if (produto != null) {
                produto.getItenspedidoCollection().add(itenspedido);
                produto = em.merge(produto);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findItenspedido(itenspedido.getItenspedidoPK()) != null) {
                throw new PreexistingEntityException("Itenspedido " + itenspedido + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Itenspedido itenspedido) throws NonexistentEntityException, RollbackFailureException, Exception {
        itenspedido.getItenspedidoPK().setIdproduto(itenspedido.getProduto().getIdproduto());
        itenspedido.getItenspedidoPK().setNropedido(itenspedido.getPedido().getNropedido());
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Itenspedido persistentItenspedido = em.find(Itenspedido.class, itenspedido.getItenspedidoPK());
            Pedido pedidoOld = persistentItenspedido.getPedido();
            Pedido pedidoNew = itenspedido.getPedido();
            Produto produtoOld = persistentItenspedido.getProduto();
            Produto produtoNew = itenspedido.getProduto();
            if (pedidoNew != null) {
                pedidoNew = em.getReference(pedidoNew.getClass(), pedidoNew.getNropedido());
                itenspedido.setPedido(pedidoNew);
            }
            if (produtoNew != null) {
                produtoNew = em.getReference(produtoNew.getClass(), produtoNew.getIdproduto());
                itenspedido.setProduto(produtoNew);
            }
            itenspedido = em.merge(itenspedido);
            if (pedidoOld != null && !pedidoOld.equals(pedidoNew)) {
                pedidoOld.getItenspedidoCollection().remove(itenspedido);
                pedidoOld = em.merge(pedidoOld);
            }
            if (pedidoNew != null && !pedidoNew.equals(pedidoOld)) {
                pedidoNew.getItenspedidoCollection().add(itenspedido);
                pedidoNew = em.merge(pedidoNew);
            }
            if (produtoOld != null && !produtoOld.equals(produtoNew)) {
                produtoOld.getItenspedidoCollection().remove(itenspedido);
                produtoOld = em.merge(produtoOld);
            }
            if (produtoNew != null && !produtoNew.equals(produtoOld)) {
                produtoNew.getItenspedidoCollection().add(itenspedido);
                produtoNew = em.merge(produtoNew);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                ItenspedidoPK id = itenspedido.getItenspedidoPK();
                if (findItenspedido(id) == null) {
                    throw new NonexistentEntityException("The itenspedido with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(ItenspedidoPK id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Itenspedido itenspedido;
            try {
                itenspedido = em.getReference(Itenspedido.class, id);
                itenspedido.getItenspedidoPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The itenspedido with id " + id + " no longer exists.", enfe);
            }
            Pedido pedido = itenspedido.getPedido();
            if (pedido != null) {
                pedido.getItenspedidoCollection().remove(itenspedido);
                pedido = em.merge(pedido);
            }
            Produto produto = itenspedido.getProduto();
            if (produto != null) {
                produto.getItenspedidoCollection().remove(itenspedido);
                produto = em.merge(produto);
            }
            em.remove(itenspedido);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Itenspedido> findItenspedidoEntities() {
        return findItenspedidoEntities(true, -1, -1);
    }

    public List<Itenspedido> findItenspedidoEntities(int maxResults, int firstResult) {
        return findItenspedidoEntities(false, maxResults, firstResult);
    }

    private List<Itenspedido> findItenspedidoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Itenspedido.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Itenspedido findItenspedido(ItenspedidoPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Itenspedido.class, id);
        } finally {
            em.close();
        }
    }

    public int getItenspedidoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Itenspedido> rt = cq.from(Itenspedido.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
