/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teste.model;

import java.io.Serializable;
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
@Table(name = "emailcliente", catalog = "bdsorveteria", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Emailcliente.findAll", query = "SELECT e FROM Emailcliente e")
    , @NamedQuery(name = "Emailcliente.findByIdcliente", query = "SELECT e FROM Emailcliente e WHERE e.emailclientePK.idcliente = :idcliente")
    , @NamedQuery(name = "Emailcliente.findByEmail", query = "SELECT e FROM Emailcliente e WHERE e.emailclientePK.email = :email")})
public class Emailcliente implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected EmailclientePK emailclientePK;
    @JoinColumn(name = "idcliente", referencedColumnName = "idcliente", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Cliente cliente;

    public Emailcliente() {
    }

    public Emailcliente(EmailclientePK emailclientePK) {
        this.emailclientePK = emailclientePK;
    }

    public Emailcliente(int idcliente, String email) {
        this.emailclientePK = new EmailclientePK(idcliente, email);
    }

    public EmailclientePK getEmailclientePK() {
        return emailclientePK;
    }

    public void setEmailclientePK(EmailclientePK emailclientePK) {
        this.emailclientePK = emailclientePK;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (emailclientePK != null ? emailclientePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Emailcliente)) {
            return false;
        }
        Emailcliente other = (Emailcliente) object;
        if ((this.emailclientePK == null && other.emailclientePK != null) || (this.emailclientePK != null && !this.emailclientePK.equals(other.emailclientePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "teste.model.Emailcliente[ emailclientePK=" + emailclientePK + " ]";
    }
    
}
