/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teste.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Tiago
 */
@Embeddable
public class ItenspedidoPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "nropedido")
    private int nropedido;
    @Basic(optional = false)
    @NotNull
    @Column(name = "idproduto")
    private int idproduto;

    public ItenspedidoPK() {
    }

    public ItenspedidoPK(int nropedido, int idproduto) {
        this.nropedido = nropedido;
        this.idproduto = idproduto;
    }

    public int getNropedido() {
        return nropedido;
    }

    public void setNropedido(int nropedido) {
        this.nropedido = nropedido;
    }

    public int getIdproduto() {
        return idproduto;
    }

    public void setIdproduto(int idproduto) {
        this.idproduto = idproduto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) nropedido;
        hash += (int) idproduto;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ItenspedidoPK)) {
            return false;
        }
        ItenspedidoPK other = (ItenspedidoPK) object;
        if (this.nropedido != other.nropedido) {
            return false;
        }
        if (this.idproduto != other.idproduto) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "teste.model.ItenspedidoPK[ nropedido=" + nropedido + ", idproduto=" + idproduto + " ]";
    }
    
}
