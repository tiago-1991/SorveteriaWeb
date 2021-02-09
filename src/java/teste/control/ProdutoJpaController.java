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
import teste.model.Fornecedor;
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
import teste.model.Produto;

/**
 *
 * @author Tiago
 */
public class ProdutoJpaController implements Serializable {

    public ProdutoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Produto produto) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (produto.getItenspedidoCollection() == null) {
            produto.setItenspedidoCollection(new ArrayList<Itenspedido>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Fornecedor cnpjFornecedor = produto.getCnpjFornecedor();
            if (cnpjFornecedor != null) {
                cnpjFornecedor = em.getReference(cnpjFornecedor.getClass(), cnpjFornecedor.getCnpj());
                produto.setCnpjFornecedor(cnpjFornecedor);
            }
            Collection<Itenspedido> attachedItenspedidoCollection = new ArrayList<Itenspedido>();
            for (Itenspedido itenspedidoCollectionItenspedidoToAttach : produto.getItenspedidoCollection()) {
                itenspedidoCollectionItenspedidoToAttach = em.getReference(itenspedidoCollectionItenspedidoToAttach.getClass(), itenspedidoCollectionItenspedidoToAttach.getItenspedidoPK());
                attachedItenspedidoCollection.add(itenspedidoCollectionItenspedidoToAttach);
            }
            produto.setItenspedidoCollection(attachedItenspedidoCollection);
            em.persist(produto);
            if (cnpjFornecedor != null) {
                cnpjFornecedor.getProdutoCollection().add(produto);
                cnpjFornecedor = em.merge(cnpjFornecedor);
            }
            for (Itenspedido itenspedidoCollectionItenspedido : produto.getItenspedidoCollection()) {
                Produto oldProdutoOfItenspedidoCollectionItenspedido = itenspedidoCollectionItenspedido.getProduto();
                itenspedidoCollectionItenspedido.setProduto(produto);
                itenspedidoCollectionItenspedido = em.merge(itenspedidoCollectionItenspedido);
                if (oldProdutoOfItenspedidoCollectionItenspedido != null) {
                    oldProdutoOfItenspedidoCollectionItenspedido.getItenspedidoCollection().remove(itenspedidoCollectionItenspedido);
                    oldProdutoOfItenspedidoCollectionItenspedido = em.merge(oldProdutoOfItenspedidoCollectionItenspedido);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findProduto(produto.getIdproduto()) != null) {
                throw new PreexistingEntityException("Produto " + produto + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Produto produto) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Produto persistentProduto = em.find(Produto.class, produto.getIdproduto());
            Fornecedor cnpjFornecedorOld = persistentProduto.getCnpjFornecedor();
            Fornecedor cnpjFornecedorNew = produto.getCnpjFornecedor();
            Collection<Itenspedido> itenspedidoCollectionOld = persistentProduto.getItenspedidoCollection();
            Collection<Itenspedido> itenspedidoCollectionNew = produto.getItenspedidoCollection();
            List<String> illegalOrphanMessages = null;
            for (Itenspedido itenspedidoCollectionOldItenspedido : itenspedidoCollectionOld) {
                if (!itenspedidoCollectionNew.contains(itenspedidoCollectionOldItenspedido)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Itenspedido " + itenspedidoCollectionOldItenspedido + " since its produto field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (cnpjFornecedorNew != null) {
                cnpjFornecedorNew = em.getReference(cnpjFornecedorNew.getClass(), cnpjFornecedorNew.getCnpj());
                produto.setCnpjFornecedor(cnpjFornecedorNew);
            }
            Collection<Itenspedido> attachedItenspedidoCollectionNew = new ArrayList<Itenspedido>();
            for (Itenspedido itenspedidoCollectionNewItenspedidoToAttach : itenspedidoCollectionNew) {
                itenspedidoCollectionNewItenspedidoToAttach = em.getReference(itenspedidoCollectionNewItenspedidoToAttach.getClass(), itenspedidoCollectionNewItenspedidoToAttach.getItenspedidoPK());
                attachedItenspedidoCollectionNew.add(itenspedidoCollectionNewItenspedidoToAttach);
            }
            itenspedidoCollectionNew = attachedItenspedidoCollectionNew;
            produto.setItenspedidoCollection(itenspedidoCollectionNew);
            produto = em.merge(produto);
            if (cnpjFornecedorOld != null && !cnpjFornecedorOld.equals(cnpjFornecedorNew)) {
                cnpjFornecedorOld.getProdutoCollection().remove(produto);
                cnpjFornecedorOld = em.merge(cnpjFornecedorOld);
            }
            if (cnpjFornecedorNew != null && !cnpjFornecedorNew.equals(cnpjFornecedorOld)) {
                cnpjFornecedorNew.getProdutoCollection().add(produto);
                cnpjFornecedorNew = em.merge(cnpjFornecedorNew);
            }
            for (Itenspedido itenspedidoCollectionNewItenspedido : itenspedidoCollectionNew) {
                if (!itenspedidoCollectionOld.contains(itenspedidoCollectionNewItenspedido)) {
                    Produto oldProdutoOfItenspedidoCollectionNewItenspedido = itenspedidoCollectionNewItenspedido.getProduto();
                    itenspedidoCollectionNewItenspedido.setProduto(produto);
                    itenspedidoCollectionNewItenspedido = em.merge(itenspedidoCollectionNewItenspedido);
                    if (oldProdutoOfItenspedidoCollectionNewItenspedido != null && !oldProdutoOfItenspedidoCollectionNewItenspedido.equals(produto)) {
                        oldProdutoOfItenspedidoCollectionNewItenspedido.getItenspedidoCollection().remove(itenspedidoCollectionNewItenspedido);
                        oldProdutoOfItenspedidoCollectionNewItenspedido = em.merge(oldProdutoOfItenspedidoCollectionNewItenspedido);
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
                Integer id = produto.getIdproduto();
                if (findProduto(id) == null) {
                    throw new NonexistentEntityException("The produto with id " + id + " no longer exists.");
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
            Produto produto;
            try {
                produto = em.getReference(Produto.class, id);
                produto.getIdproduto();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The produto with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Itenspedido> itenspedidoCollectionOrphanCheck = produto.getItenspedidoCollection();
            for (Itenspedido itenspedidoCollectionOrphanCheckItenspedido : itenspedidoCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Produto (" + produto + ") cannot be destroyed since the Itenspedido " + itenspedidoCollectionOrphanCheckItenspedido + " in its itenspedidoCollection field has a non-nullable produto field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Fornecedor cnpjFornecedor = produto.getCnpjFornecedor();
            if (cnpjFornecedor != null) {
                cnpjFornecedor.getProdutoCollection().remove(produto);
                cnpjFornecedor = em.merge(cnpjFornecedor);
            }
            em.remove(produto);
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

    public List<Produto> findProdutoEntities() {
        return findProdutoEntities(true, -1, -1);
    }

    public List<Produto> findProdutoEntities(int maxResults, int firstResult) {
        return findProdutoEntities(false, maxResults, firstResult);
    }

    private List<Produto> findProdutoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Produto.class));
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

    public Produto findProduto(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Produto.class, id);
        } finally {
            em.close();
        }
    }

    public int getProdutoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Produto> rt = cq.from(Produto.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
