/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teste.control;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import teste.model.Cliente;
import teste.model.Itenspedido;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import teste.control.exceptions.IllegalOrphanException;
import teste.control.exceptions.NonexistentEntityException;
import teste.control.exceptions.PreexistingEntityException;
import teste.control.exceptions.RollbackFailureException;
import teste.model.Pedido;

/**
 *
 * @author Tiago
 */
public class PedidoJpaController implements Serializable {

    public PedidoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Pedido pedido) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (pedido.getItenspedidoCollection() == null) {
            pedido.setItenspedidoCollection(new ArrayList<Itenspedido>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Cliente idcliente = pedido.getIdcliente();
            if (idcliente != null) {
                idcliente = em.getReference(idcliente.getClass(), idcliente.getIdcliente());
                pedido.setIdcliente(idcliente);
            }
            Collection<Itenspedido> attachedItenspedidoCollection = new ArrayList<Itenspedido>();
            for (Itenspedido itenspedidoCollectionItenspedidoToAttach : pedido.getItenspedidoCollection()) {
                itenspedidoCollectionItenspedidoToAttach = em.getReference(itenspedidoCollectionItenspedidoToAttach.getClass(), itenspedidoCollectionItenspedidoToAttach.getItenspedidoPK());
                attachedItenspedidoCollection.add(itenspedidoCollectionItenspedidoToAttach);
            }
            pedido.setItenspedidoCollection(attachedItenspedidoCollection);
            em.persist(pedido);
            if (idcliente != null) {
                idcliente.getPedidoCollection().add(pedido);
                idcliente = em.merge(idcliente);
            }
            for (Itenspedido itenspedidoCollectionItenspedido : pedido.getItenspedidoCollection()) {
                Pedido oldPedidoOfItenspedidoCollectionItenspedido = itenspedidoCollectionItenspedido.getPedido();
                itenspedidoCollectionItenspedido.setPedido(pedido);
                itenspedidoCollectionItenspedido = em.merge(itenspedidoCollectionItenspedido);
                if (oldPedidoOfItenspedidoCollectionItenspedido != null) {
                    oldPedidoOfItenspedidoCollectionItenspedido.getItenspedidoCollection().remove(itenspedidoCollectionItenspedido);
                    oldPedidoOfItenspedidoCollectionItenspedido = em.merge(oldPedidoOfItenspedidoCollectionItenspedido);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findPedido(pedido.getNropedido()) != null) {
                throw new PreexistingEntityException("Pedido " + pedido + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Pedido pedido) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Pedido persistentPedido = em.find(Pedido.class, pedido.getNropedido());
            Cliente idclienteOld = persistentPedido.getIdcliente();
            Cliente idclienteNew = pedido.getIdcliente();
            Collection<Itenspedido> itenspedidoCollectionOld = persistentPedido.getItenspedidoCollection();
            Collection<Itenspedido> itenspedidoCollectionNew = pedido.getItenspedidoCollection();
            List<String> illegalOrphanMessages = null;
            for (Itenspedido itenspedidoCollectionOldItenspedido : itenspedidoCollectionOld) {
                if (!itenspedidoCollectionNew.contains(itenspedidoCollectionOldItenspedido)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Itenspedido " + itenspedidoCollectionOldItenspedido + " since its pedido field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (idclienteNew != null) {
                idclienteNew = em.getReference(idclienteNew.getClass(), idclienteNew.getIdcliente());
                pedido.setIdcliente(idclienteNew);
            }
            Collection<Itenspedido> attachedItenspedidoCollectionNew = new ArrayList<Itenspedido>();
            for (Itenspedido itenspedidoCollectionNewItenspedidoToAttach : itenspedidoCollectionNew) {
                itenspedidoCollectionNewItenspedidoToAttach = em.getReference(itenspedidoCollectionNewItenspedidoToAttach.getClass(), itenspedidoCollectionNewItenspedidoToAttach.getItenspedidoPK());
                attachedItenspedidoCollectionNew.add(itenspedidoCollectionNewItenspedidoToAttach);
            }
            itenspedidoCollectionNew = attachedItenspedidoCollectionNew;
            pedido.setItenspedidoCollection(itenspedidoCollectionNew);
            pedido = em.merge(pedido);
            if (idclienteOld != null && !idclienteOld.equals(idclienteNew)) {
                idclienteOld.getPedidoCollection().remove(pedido);
                idclienteOld = em.merge(idclienteOld);
            }
            if (idclienteNew != null && !idclienteNew.equals(idclienteOld)) {
                idclienteNew.getPedidoCollection().add(pedido);
                idclienteNew = em.merge(idclienteNew);
            }
            for (Itenspedido itenspedidoCollectionNewItenspedido : itenspedidoCollectionNew) {
                if (!itenspedidoCollectionOld.contains(itenspedidoCollectionNewItenspedido)) {
                    Pedido oldPedidoOfItenspedidoCollectionNewItenspedido = itenspedidoCollectionNewItenspedido.getPedido();
                    itenspedidoCollectionNewItenspedido.setPedido(pedido);
                    itenspedidoCollectionNewItenspedido = em.merge(itenspedidoCollectionNewItenspedido);
                    if (oldPedidoOfItenspedidoCollectionNewItenspedido != null && !oldPedidoOfItenspedidoCollectionNewItenspedido.equals(pedido)) {
                        oldPedidoOfItenspedidoCollectionNewItenspedido.getItenspedidoCollection().remove(itenspedidoCollectionNewItenspedido);
                        oldPedidoOfItenspedidoCollectionNewItenspedido = em.merge(oldPedidoOfItenspedidoCollectionNewItenspedido);
                    }
                }
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
                Integer id = pedido.getNropedido();
                if (findPedido(id) == null) {
                    throw new NonexistentEntityException("The pedido with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Pedido pedido;
            try {
                pedido = em.getReference(Pedido.class, id);
                pedido.getNropedido();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The pedido with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Itenspedido> itenspedidoCollectionOrphanCheck = pedido.getItenspedidoCollection();
            for (Itenspedido itenspedidoCollectionOrphanCheckItenspedido : itenspedidoCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Pedido (" + pedido + ") cannot be destroyed since the Itenspedido " + itenspedidoCollectionOrphanCheckItenspedido + " in its itenspedidoCollection field has a non-nullable pedido field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Cliente idcliente = pedido.getIdcliente();
            if (idcliente != null) {
                idcliente.getPedidoCollection().remove(pedido);
                idcliente = em.merge(idcliente);
            }
            em.remove(pedido);
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

    public List<Pedido> findPedidoEntities() {
        return findPedidoEntities(true, -1, -1);
    }

    public List<Pedido> findPedidoEntities(int maxResults, int firstResult) {
        return findPedidoEntities(false, maxResults, firstResult);
    }

    private List<Pedido> findPedidoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Pedido.class));
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

    public Pedido findPedido(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Pedido.class, id);
        } finally {
            em.close();
        }
    }

    public int getPedidoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Pedido> rt = cq.from(Pedido.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
