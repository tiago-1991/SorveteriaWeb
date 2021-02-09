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
import teste.model.Emailcliente;
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
import teste.model.Cliente;
import teste.model.Pedido;

/**
 *
 * @author Tiago
 */
public class ClienteJpaController implements Serializable {

    public ClienteJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Cliente cliente) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (cliente.getEmailclienteCollection() == null) {
            cliente.setEmailclienteCollection(new ArrayList<Emailcliente>());
        }
        if (cliente.getPedidoCollection() == null) {
            cliente.setPedidoCollection(new ArrayList<Pedido>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Emailcliente> attachedEmailclienteCollection = new ArrayList<Emailcliente>();
            for (Emailcliente emailclienteCollectionEmailclienteToAttach : cliente.getEmailclienteCollection()) {
                emailclienteCollectionEmailclienteToAttach = em.getReference(emailclienteCollectionEmailclienteToAttach.getClass(), emailclienteCollectionEmailclienteToAttach.getEmailclientePK());
                attachedEmailclienteCollection.add(emailclienteCollectionEmailclienteToAttach);
            }
            cliente.setEmailclienteCollection(attachedEmailclienteCollection);
            Collection<Pedido> attachedPedidoCollection = new ArrayList<Pedido>();
            for (Pedido pedidoCollectionPedidoToAttach : cliente.getPedidoCollection()) {
                pedidoCollectionPedidoToAttach = em.getReference(pedidoCollectionPedidoToAttach.getClass(), pedidoCollectionPedidoToAttach.getNropedido());
                attachedPedidoCollection.add(pedidoCollectionPedidoToAttach);
            }
            cliente.setPedidoCollection(attachedPedidoCollection);
            em.persist(cliente);
            for (Emailcliente emailclienteCollectionEmailcliente : cliente.getEmailclienteCollection()) {
                Cliente oldClienteOfEmailclienteCollectionEmailcliente = emailclienteCollectionEmailcliente.getCliente();
                emailclienteCollectionEmailcliente.setCliente(cliente);
                emailclienteCollectionEmailcliente = em.merge(emailclienteCollectionEmailcliente);
                if (oldClienteOfEmailclienteCollectionEmailcliente != null) {
                    oldClienteOfEmailclienteCollectionEmailcliente.getEmailclienteCollection().remove(emailclienteCollectionEmailcliente);
                    oldClienteOfEmailclienteCollectionEmailcliente = em.merge(oldClienteOfEmailclienteCollectionEmailcliente);
                }
            }
            for (Pedido pedidoCollectionPedido : cliente.getPedidoCollection()) {
                Cliente oldIdclienteOfPedidoCollectionPedido = pedidoCollectionPedido.getIdcliente();
                pedidoCollectionPedido.setIdcliente(cliente);
                pedidoCollectionPedido = em.merge(pedidoCollectionPedido);
                if (oldIdclienteOfPedidoCollectionPedido != null) {
                    oldIdclienteOfPedidoCollectionPedido.getPedidoCollection().remove(pedidoCollectionPedido);
                    oldIdclienteOfPedidoCollectionPedido = em.merge(oldIdclienteOfPedidoCollectionPedido);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findCliente(cliente.getIdcliente()) != null) {
                throw new PreexistingEntityException("Cliente " + cliente + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Cliente cliente) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Cliente persistentCliente = em.find(Cliente.class, cliente.getIdcliente());
            Collection<Emailcliente> emailclienteCollectionOld = persistentCliente.getEmailclienteCollection();
            Collection<Emailcliente> emailclienteCollectionNew = cliente.getEmailclienteCollection();
            Collection<Pedido> pedidoCollectionOld = persistentCliente.getPedidoCollection();
            Collection<Pedido> pedidoCollectionNew = cliente.getPedidoCollection();
            List<String> illegalOrphanMessages = null;
            for (Emailcliente emailclienteCollectionOldEmailcliente : emailclienteCollectionOld) {
                if (!emailclienteCollectionNew.contains(emailclienteCollectionOldEmailcliente)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Emailcliente " + emailclienteCollectionOldEmailcliente + " since its cliente field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Emailcliente> attachedEmailclienteCollectionNew = new ArrayList<Emailcliente>();
            for (Emailcliente emailclienteCollectionNewEmailclienteToAttach : emailclienteCollectionNew) {
                emailclienteCollectionNewEmailclienteToAttach = em.getReference(emailclienteCollectionNewEmailclienteToAttach.getClass(), emailclienteCollectionNewEmailclienteToAttach.getEmailclientePK());
                attachedEmailclienteCollectionNew.add(emailclienteCollectionNewEmailclienteToAttach);
            }
            emailclienteCollectionNew = attachedEmailclienteCollectionNew;
            cliente.setEmailclienteCollection(emailclienteCollectionNew);
            Collection<Pedido> attachedPedidoCollectionNew = new ArrayList<Pedido>();
            for (Pedido pedidoCollectionNewPedidoToAttach : pedidoCollectionNew) {
                pedidoCollectionNewPedidoToAttach = em.getReference(pedidoCollectionNewPedidoToAttach.getClass(), pedidoCollectionNewPedidoToAttach.getNropedido());
                attachedPedidoCollectionNew.add(pedidoCollectionNewPedidoToAttach);
            }
            pedidoCollectionNew = attachedPedidoCollectionNew;
            cliente.setPedidoCollection(pedidoCollectionNew);
            cliente = em.merge(cliente);
            for (Emailcliente emailclienteCollectionNewEmailcliente : emailclienteCollectionNew) {
                if (!emailclienteCollectionOld.contains(emailclienteCollectionNewEmailcliente)) {
                    Cliente oldClienteOfEmailclienteCollectionNewEmailcliente = emailclienteCollectionNewEmailcliente.getCliente();
                    emailclienteCollectionNewEmailcliente.setCliente(cliente);
                    emailclienteCollectionNewEmailcliente = em.merge(emailclienteCollectionNewEmailcliente);
                    if (oldClienteOfEmailclienteCollectionNewEmailcliente != null && !oldClienteOfEmailclienteCollectionNewEmailcliente.equals(cliente)) {
                        oldClienteOfEmailclienteCollectionNewEmailcliente.getEmailclienteCollection().remove(emailclienteCollectionNewEmailcliente);
                        oldClienteOfEmailclienteCollectionNewEmailcliente = em.merge(oldClienteOfEmailclienteCollectionNewEmailcliente);
                    }
                }
            }
            for (Pedido pedidoCollectionOldPedido : pedidoCollectionOld) {
                if (!pedidoCollectionNew.contains(pedidoCollectionOldPedido)) {
                    pedidoCollectionOldPedido.setIdcliente(null);
                    pedidoCollectionOldPedido = em.merge(pedidoCollectionOldPedido);
                }
            }
            for (Pedido pedidoCollectionNewPedido : pedidoCollectionNew) {
                if (!pedidoCollectionOld.contains(pedidoCollectionNewPedido)) {
                    Cliente oldIdclienteOfPedidoCollectionNewPedido = pedidoCollectionNewPedido.getIdcliente();
                    pedidoCollectionNewPedido.setIdcliente(cliente);
                    pedidoCollectionNewPedido = em.merge(pedidoCollectionNewPedido);
                    if (oldIdclienteOfPedidoCollectionNewPedido != null && !oldIdclienteOfPedidoCollectionNewPedido.equals(cliente)) {
                        oldIdclienteOfPedidoCollectionNewPedido.getPedidoCollection().remove(pedidoCollectionNewPedido);
                        oldIdclienteOfPedidoCollectionNewPedido = em.merge(oldIdclienteOfPedidoCollectionNewPedido);
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
                Integer id = cliente.getIdcliente();
                if (findCliente(id) == null) {
                    throw new NonexistentEntityException("The cliente with id " + id + " no longer exists.");
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
            Cliente cliente;
            try {
                cliente = em.getReference(Cliente.class, id);
                cliente.getIdcliente();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The cliente with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Emailcliente> emailclienteCollectionOrphanCheck = cliente.getEmailclienteCollection();
            for (Emailcliente emailclienteCollectionOrphanCheckEmailcliente : emailclienteCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Cliente (" + cliente + ") cannot be destroyed since the Emailcliente " + emailclienteCollectionOrphanCheckEmailcliente + " in its emailclienteCollection field has a non-nullable cliente field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Pedido> pedidoCollection = cliente.getPedidoCollection();
            for (Pedido pedidoCollectionPedido : pedidoCollection) {
                pedidoCollectionPedido.setIdcliente(null);
                pedidoCollectionPedido = em.merge(pedidoCollectionPedido);
            }
            em.remove(cliente);
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

    public List<Cliente> findClienteEntities() {
        return findClienteEntities(true, -1, -1);
    }

    public List<Cliente> findClienteEntities(int maxResults, int firstResult) {
        return findClienteEntities(false, maxResults, firstResult);
    }

    private List<Cliente> findClienteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Cliente.class));
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

    public Cliente findCliente(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Cliente.class, id);
        } finally {
            em.close();
        }
    }

    public int getClienteCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Cliente> rt = cq.from(Cliente.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
