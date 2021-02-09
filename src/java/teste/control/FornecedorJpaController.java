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
import teste.model.Produto;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import teste.control.exceptions.NonexistentEntityException;
import teste.control.exceptions.PreexistingEntityException;
import teste.control.exceptions.RollbackFailureException;
import teste.model.Fornecedor;

/**
 *
 * @author Tiago
 */
public class FornecedorJpaController implements Serializable {

    public FornecedorJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Fornecedor fornecedor) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (fornecedor.getProdutoCollection() == null) {
            fornecedor.setProdutoCollection(new ArrayList<Produto>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Produto> attachedProdutoCollection = new ArrayList<Produto>();
            for (Produto produtoCollectionProdutoToAttach : fornecedor.getProdutoCollection()) {
                produtoCollectionProdutoToAttach = em.getReference(produtoCollectionProdutoToAttach.getClass(), produtoCollectionProdutoToAttach.getIdproduto());
                attachedProdutoCollection.add(produtoCollectionProdutoToAttach);
            }
            fornecedor.setProdutoCollection(attachedProdutoCollection);
            em.persist(fornecedor);
            for (Produto produtoCollectionProduto : fornecedor.getProdutoCollection()) {
                Fornecedor oldCnpjFornecedorOfProdutoCollectionProduto = produtoCollectionProduto.getCnpjFornecedor();
                produtoCollectionProduto.setCnpjFornecedor(fornecedor);
                produtoCollectionProduto = em.merge(produtoCollectionProduto);
                if (oldCnpjFornecedorOfProdutoCollectionProduto != null) {
                    oldCnpjFornecedorOfProdutoCollectionProduto.getProdutoCollection().remove(produtoCollectionProduto);
                    oldCnpjFornecedorOfProdutoCollectionProduto = em.merge(oldCnpjFornecedorOfProdutoCollectionProduto);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findFornecedor(fornecedor.getCnpj()) != null) {
                throw new PreexistingEntityException("Fornecedor " + fornecedor + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Fornecedor fornecedor) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Fornecedor persistentFornecedor = em.find(Fornecedor.class, fornecedor.getCnpj());
            Collection<Produto> produtoCollectionOld = persistentFornecedor.getProdutoCollection();
            Collection<Produto> produtoCollectionNew = fornecedor.getProdutoCollection();
            Collection<Produto> attachedProdutoCollectionNew = new ArrayList<Produto>();
            for (Produto produtoCollectionNewProdutoToAttach : produtoCollectionNew) {
                produtoCollectionNewProdutoToAttach = em.getReference(produtoCollectionNewProdutoToAttach.getClass(), produtoCollectionNewProdutoToAttach.getIdproduto());
                attachedProdutoCollectionNew.add(produtoCollectionNewProdutoToAttach);
            }
            produtoCollectionNew = attachedProdutoCollectionNew;
            fornecedor.setProdutoCollection(produtoCollectionNew);
            fornecedor = em.merge(fornecedor);
            for (Produto produtoCollectionOldProduto : produtoCollectionOld) {
                if (!produtoCollectionNew.contains(produtoCollectionOldProduto)) {
                    produtoCollectionOldProduto.setCnpjFornecedor(null);
                    produtoCollectionOldProduto = em.merge(produtoCollectionOldProduto);
                }
            }
            for (Produto produtoCollectionNewProduto : produtoCollectionNew) {
                if (!produtoCollectionOld.contains(produtoCollectionNewProduto)) {
                    Fornecedor oldCnpjFornecedorOfProdutoCollectionNewProduto = produtoCollectionNewProduto.getCnpjFornecedor();
                    produtoCollectionNewProduto.setCnpjFornecedor(fornecedor);
                    produtoCollectionNewProduto = em.merge(produtoCollectionNewProduto);
                    if (oldCnpjFornecedorOfProdutoCollectionNewProduto != null && !oldCnpjFornecedorOfProdutoCollectionNewProduto.equals(fornecedor)) {
                        oldCnpjFornecedorOfProdutoCollectionNewProduto.getProdutoCollection().remove(produtoCollectionNewProduto);
                        oldCnpjFornecedorOfProdutoCollectionNewProduto = em.merge(oldCnpjFornecedorOfProdutoCollectionNewProduto);
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
                String id = fornecedor.getCnpj();
                if (findFornecedor(id) == null) {
                    throw new NonexistentEntityException("The fornecedor with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Fornecedor fornecedor;
            try {
                fornecedor = em.getReference(Fornecedor.class, id);
                fornecedor.getCnpj();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The fornecedor with id " + id + " no longer exists.", enfe);
            }
            Collection<Produto> produtoCollection = fornecedor.getProdutoCollection();
            for (Produto produtoCollectionProduto : produtoCollection) {
                produtoCollectionProduto.setCnpjFornecedor(null);
                produtoCollectionProduto = em.merge(produtoCollectionProduto);
            }
            em.remove(fornecedor);
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

    public List<Fornecedor> findFornecedorEntities() {
        return findFornecedorEntities(true, -1, -1);
    }

    public List<Fornecedor> findFornecedorEntities(int maxResults, int firstResult) {
        return findFornecedorEntities(false, maxResults, firstResult);
    }

    private List<Fornecedor> findFornecedorEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Fornecedor.class));
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

    public Fornecedor findFornecedor(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Fornecedor.class, id);
        } finally {
            em.close();
        }
    }

    public int getFornecedorCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Fornecedor> rt = cq.from(Fornecedor.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
