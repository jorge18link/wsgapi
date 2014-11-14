package pnl.menu.vista;
import java.io.Serializable;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import pnl.interfaz.UsuarioGrupoBeanRemote;
import pnl.modelo.Grupo;
import pnl.modelo.Usuario;
import pnl.modelo.UsuarioGrupo;
import pnl.modelo.UsuarioGrupoPK;
import pnl.servicio.UsuarioServicio;


@ManagedBean
public class GrupoNuevo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private UsuarioGrupoBeanRemote usuarioGrupoBeanRemote;
    private Usuario usuario;
    private UsuarioGrupo usuarioGrupo;
    private Grupo grupo;

	

	
	@ManagedProperty("#{usuarioServicio}")
	private UsuarioServicio usuarioServicio;
	

	
	
	
	@PostConstruct
	public void init(){
		
		
		usuarioGrupo = new UsuarioGrupo();
		grupo = new Grupo();
		grupo.setEstado("A");
		usuarioGrupo.setGrupo(grupo);
		
		usuario = usuarioServicio.getUsuario();
		
    	try {
			
			Properties pr = new Properties();
			pr.put(Context.INITIAL_CONTEXT_FACTORY,
					"weblogic.jndi.WLInitialContextFactory");
			pr.put(Context.PROVIDER_URL, "t3://localhost:7001");

			InitialContext ic = new InitialContext(pr);

			usuarioGrupoBeanRemote = (UsuarioGrupoBeanRemote) ic
					.lookup("java:global.panel_ear.panel_ejb/UsuarioGrupoBean");
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
    	
		
	}
	
	
    public void guardarUsuarioGrupo(ActionEvent actionEvent) {
  	
    	 
    	UsuarioGrupoPK id = new UsuarioGrupoPK();
    	
    	if(this.hasRole("ROLE_ADMIN")){
    		
    		usuarioGrupo.setId(id);
    		usuarioGrupo.setUsuario(usuario);
    		usuarioGrupo.setEstado("A");
    		usuarioGrupoBeanRemote.persistUsuarioGrupo(usuarioGrupo);
    		
        		 //LISTO PARA EL UPDATE DEL INIT DEL FORMULARIO
        	//ACTUALIZAR MENU
        	
        	
            addMessage("Se guardo exitosamente!!");
            
           
    	
    	
    	}else{
    		addMessage("NO TIENE PERMISO DE ADMINISTRADOR PARA REALIZAR ESTA ACCION!!");
    	}
	
    }
     
    public void addMessage(String summary) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }


    protected boolean hasRole(String role) {
        // get security context from thread local
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null)
            return false;

        Authentication authentication = context.getAuthentication();
        if (authentication == null)
            return false;

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (role.equals(auth.getAuthority()))
                return true;
        }

        return false;
    }


	public void setUsuarioServicio(UsuarioServicio usuarioServicio) {
		this.usuarioServicio = usuarioServicio;
	}





	public Usuario getUsuario() {
		return usuario;
	}


	public UsuarioServicio getUsuarioServicio() {
		return usuarioServicio;
	}


	public UsuarioGrupo getUsuarioGrupo() {
		return usuarioGrupo;
	}


	public void setUsuarioGrupo(UsuarioGrupo usuarioGrupo) {
		this.usuarioGrupo = usuarioGrupo;
	}

	

    
    
}