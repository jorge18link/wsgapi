package wsg.interfaz;

import java.util.List;

import javax.ejb.Remote;

import wsg.modelo.WsgServiciosLog;

@Remote
public interface WsgServiciosLogBeanRemote {

	void create(WsgServiciosLog wsgServiciosLog);

	void edit(WsgServiciosLog wsgServiciosLog);

	void remove(WsgServiciosLog wsgServiciosLog);

	public WsgServiciosLog find(Object id);
	
    public List<WsgServiciosLog> findAll();

    public List<WsgServiciosLog> findRange(int[] range);

    int count();
    
}
