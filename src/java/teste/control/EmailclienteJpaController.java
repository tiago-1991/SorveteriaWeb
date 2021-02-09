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
import teste.model.Cliente;
import teste.model.Emailcliente;
import teste.model.EmailclientePK;

/**
 *
 * @author Tiago
 */
public class EmailclienteJpaController implements Serializable {

    public EmailclienteJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Emailcliente emailcliente) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (emailcliente.getEmailclientePK() == null) {
            emailcliente.setEmailclientePK(new EmailclientePK());
        }
        emailcliente.getEmailclientePK().setIdcliente(emailcliente.getCliente().getIdcliente());
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Cliente cliente = emailcliente.getCliente();
            if (cliente != null) {
                cliente = em.getReference(cliente.getClass(), cliente.getIdcliente());
                emailcliente.setCliente(cliente);
            }
            em.persist(emailcliente);
            if (cliente != null) {
                cliente.getEmailclienteCollection().add(emailcliente);
                cliente = em.merge(cliente);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findEmailcliente(emailcliente.getEmailclientePK()) != null) {
                throw new PreexistingEntityException("Emailcliente " + emailcliente + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Emailcliente emailcliente) throws NonexistentEntityException, RollbackFailureException, Exception {
        emailcliente.getEmailclientePK().setIdcliente(emailcliente.getCliente().getIdcliente());
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Emailcliente persistentEmailcliente = em.find(Emailcliente.class, emailcliente.getEmailclientePK());
            Cliente clienteOld = persistentEmailcliente.getCliente();
            Cliente clienteNew = emailcliente.getCliente();
            if (clienteNew != null) {
                clienteNew = em.getReference(clienteNew.getClass(), clienteNew.getIdcliente());
                emailcliente.setCliente(clienteNew);
            }
            emailcliente = em.merge(emailcliente);
            if (clienteOld != null && !clienteOld.equals(clienteNew)) {
                clienteOld.getEmailclienteCollection().remove(emailcliente);
                clienteOld = em.merge(clienteOld);
            }
            if (clienteNew != null && !clienteNew.equals(clienteOld)) {
                clienteNew.getEmailclienteCollection().add(emailcliente);
                clienteNew = em.merge(clienteNew);
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
                EmailclientePK id = emailcliente.getEmailclientePK();
                if (findEmailcliente(id) == null) {
                    throw new NonexistentEntityException("The emailcliente with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(EmailclientePK id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Emailcliente emailcliente;
            try {
                emailcliente = em.getReference(Emailcliente.class, id);
                emailcliente.getEmailclientePK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The emailcliente with id " + id + " no longer exists.", enfe);
            }
            Cliente cliente = emailcliente.getCliente();
            if (cliente != null) {
                cliente.getEmailclienteCollection().remove(emailcliente);
                cliente = em.merge(cliente);
            }
            em.remove(emailcliente);
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

    public List<Emailcliente> findEmailclienteEntities() {
        return findEmailclienteEntities(true, -1, -1);
    }

    public List<Emailcliente> findEmailclienteEntities(int maxResults, int firstResult) {
        return findEmailclienteEntities(false, maxResults, firstResult);
    }

    private List<Emailcliente> findEmailclienteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Emailcliente.class));
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

    public Emailcliente findEmailcliente(EmailclientePK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Emailcliente.class, id);
        } finally {
            em.close();
        }
    }

    public int getEmailclienteCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Emailcliente> rt = cq.from(Emailcliente.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
