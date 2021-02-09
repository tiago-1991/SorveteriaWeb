package teste.view;

import teste.model.Itenspedido;
import teste.view.util.JsfUtil;
import teste.view.util.PaginationHelper;
import teste.model.ItenspedidoFacade;

import java.io.Serializable;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

@Named("itenspedidoController")
@SessionScoped
public class ItenspedidoController implements Serializable {

    private Itenspedido current;
    private DataModel items = null;
    @EJB
    private teste.model.ItenspedidoFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public ItenspedidoController() {
    }

    public Itenspedido getSelected() {
        if (current == null) {
            current = new Itenspedido();
            current.setItenspedidoPK(new teste.model.ItenspedidoPK());
            selectedItemIndex = -1;
        }
        return current;
    }

    private ItenspedidoFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Itenspedido) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Itenspedido();
        current.setItenspedidoPK(new teste.model.ItenspedidoPK());
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            current.getItenspedidoPK().setNropedido(current.getPedido().getNropedido());
            current.getItenspedidoPK().setIdproduto(current.getProduto().getIdproduto());
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ItenspedidoCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Itenspedido) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            current.getItenspedidoPK().setNropedido(current.getPedido().getNropedido());
            current.getItenspedidoPK().setIdproduto(current.getProduto().getIdproduto());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ItenspedidoUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Itenspedido) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ItenspedidoDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public Itenspedido getItenspedido(teste.model.ItenspedidoPK id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Itenspedido.class)
    public static class ItenspedidoControllerConverter implements Converter {

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ItenspedidoController controller = (ItenspedidoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "itenspedidoController");
            return controller.getItenspedido(getKey(value));
        }

        teste.model.ItenspedidoPK getKey(String value) {
            teste.model.ItenspedidoPK key;
            String values[] = value.split(SEPARATOR_ESCAPED);
            key = new teste.model.ItenspedidoPK();
            key.setNropedido(Integer.parseInt(values[0]));
            key.setIdproduto(Integer.parseInt(values[1]));
            return key;
        }

        String getStringKey(teste.model.ItenspedidoPK value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getNropedido());
            sb.append(SEPARATOR);
            sb.append(value.getIdproduto());
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Itenspedido) {
                Itenspedido o = (Itenspedido) object;
                return getStringKey(o.getItenspedidoPK());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Itenspedido.class.getName());
            }
        }

    }

}
