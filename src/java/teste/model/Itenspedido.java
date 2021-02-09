/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teste.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Tiago
 */
@Entity
@Table(name = "itenspedido", catalog = "bdsorveteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Itenspedido.findAll", query = "SELECT i FROM Itenspedido i")
    , @NamedQuery(name = "Itenspedido.findByNropedido", query = "SELECT i FROM Itenspedido i WHERE i.itenspedidoPK.nropedido = :nropedido")
    , @NamedQuery(name = "Itenspedido.findByIdproduto", query = "SELECT i FROM Itenspedido i WHERE i.itenspedidoPK.idproduto = :idproduto")
    , @NamedQuery(name = "Itenspedido.findByQuantidade", query = "SELECT i FROM Itenspedido i WHERE i.quantidade = :quantidade")})
public class Itenspedido implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ItenspedidoPK itenspedidoPK;
    @Column(name = "quantidade")
    private Integer quantidade;
    @JoinColumn(name = "nropedido", referencedColumnName = "nropedido", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Pedido pedido;
    @JoinColumn(name = "idproduto", referencedColumnName = "idproduto", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Produto produto;

    public Itenspedido() {
    }

    public Itenspedido(ItenspedidoPK itenspedidoPK) {
        this.itenspedidoPK = itenspedidoPK;
    }

    public Itenspedido(int nropedido, int idproduto) {
        this.itenspedidoPK = new ItenspedidoPK(nropedido, idproduto);
    }

    public ItenspedidoPK getItenspedidoPK() {
        return itenspedidoPK;
    }

    public void setItenspedidoPK(ItenspedidoPK itenspedidoPK) {
        this.itenspedidoPK = itenspedidoPK;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (itenspedidoPK != null ? itenspedidoPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Itenspedido)) {
            return false;
        }
        Itenspedido other = (Itenspedido) object;
        if ((this.itenspedidoPK == null && other.itenspedidoPK != null) || (this.itenspedidoPK != null && !this.itenspedidoPK.equals(other.itenspedidoPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "teste.model.Itenspedido[ itenspedidoPK=" + itenspedidoPK + " ]";
    }
    
}
