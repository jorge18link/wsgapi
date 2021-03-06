package wsg.beanAbstracto;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.Query;


/**
 *
 * @author Desarrollo
 */
public abstract class AbstractBean<T> {
	
	
    private Class<T> entityClass;

    public AbstractBean(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();


    public void create(T entity){
        getEntityManager().persist(entity);
    }

    public void edit(T entity) {
        getEntityManager().merge(entity);
    }

    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    public T find(Object id) {
    	
        T objeto = getEntityManager().find(entityClass, id);
        return objeto;
        
        
        
    }
    
    @SuppressWarnings("unchecked")
    public List<T> findAll() {
    	@SuppressWarnings("rawtypes")
		CriteriaQuery  cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        List<T> objetos = getEntityManager().createQuery(cq).getResultList();
        return objetos;
    }

  
    @SuppressWarnings("unchecked")
    public List<T> findRange(int[] range) {
        @SuppressWarnings("rawtypes")
		CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0]);
        q.setFirstResult(range[0]);
        List<T> objetos = q.getResultList();
        return objetos;
    }

    @SuppressWarnings("unchecked")
    public int count() {
    	@SuppressWarnings("rawtypes")
        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        Query q = getEntityManager().createQuery(cq);
        int cant =  ((Long) q.getSingleResult()).intValue();
        return cant;
    }
    
    
}
