package wsg.web;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.log4j.Logger;

import wsg.conexion.Conexion;
import wsg.interfaz.WsgServicioBeanRemote;
import wsg.interfaz.WsgServiciosLogBeanRemote;
import wsg.interfaz.WsgUsuarioServicioBeanRemote;
import wsg.modelo.WsgServicio;
import wsg.modelo.WsgServiciosLog;
import wsg.modelo.WsgUsuario;
import wsg.modelo.WsgUsuarioServicio;
import wsg.modelo.WsgUsuarioServicioPK;
import wsg.query.EjecutaQuery;


@WebService(serviceName = "GenericoService", name = "GenericoPortType", targetNamespace = "http://axis/EISApiOnlineWS.wsdl/types/")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public class ServicioWebGenerico {
	
	
	static final Logger logger = Logger.getLogger(ServicioWebGenerico.class);

	Properties propiedades = new Properties();
	
	@WebMethod(exclude=true)
	public Properties getPropiedades() {
		InputStream iostream = Thread.currentThread().getContextClassLoader().getResourceAsStream("properties/wsg-war-ear.properties");
		try {
			propiedades.load(iostream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return propiedades; 
		
	}
	
	
	
	
	@WebMethod(operationName = "obtenerXml", action = "http://axis/EISApiOnlineWS.wsdl/types//eipConsumeServicio")
	@WebResult(targetNamespace = "http://axis/EISApiOnlineWS.wsdl/types/", name = "result")
	@RequestWrapper(localName = "servicioElement", targetNamespace = "http://axis/EISApiOnlineWS.wsdl/types/")
	@ResponseWrapper(localName = "servicioResponseElement", targetNamespace = "http://axis/EISApiOnlineWS.wsdl/types/")
	public Servicio obtieneXml(
			@XmlElement(required = true, namespace = "http://axis/EISApiOnlineWS.wsdl/types/") @WebParam(name = "idServicio") long idServicio,
			@XmlElement(required = true, namespace = "http://axis/EISApiOnlineWS.wsdl/types/") @WebParam(name = "usuario") String usuario,
			@XmlElement(required = true, namespace = "http://axis/EISApiOnlineWS.wsdl/types/") @WebParam(name = "clave") String clave,
			@XmlElement(required = false, namespace = "http://axis/EISApiOnlineWS.wsdl/types/") @WebParam(name = "sentencias_binds") Bind sentencias_binds) {
	
		Servicio servicio = new Servicio();
		WsgServiciosLog wsgServiciosLog = new WsgServiciosLog();
		wsgServiciosLog.setIdServicio(new BigDecimal(idServicio));
		wsgServiciosLog.setUsuario(usuario);
		wsgServiciosLog.setClave(clave);
		wsgServiciosLog.setFechaInicio(new Date());
		
		//RECUPERAR TODOS LOS PARAMETRSO XML
		Utileria t = new Utileria();
		List<ListaParametros> arrayDeListasDeParametros;
		String xmlString = "";
		WsgServicioBeanRemote wsgServicioBeanRemote;
		WsgUsuarioServicioBeanRemote wsgUsuarioServicioBeanRemote;
		WsgServiciosLogBeanRemote wsgServiciosLogBeanRemote = null;
		WsgUsuario wsgUsuario;
		WsgUsuarioServicio wsgUsuarioServicio;
		WsgServicio wsgServicio;
		EjecutaQuery q = new EjecutaQuery();
		
		// recupero todos los parametros de todos los queries
		DataSource ds = null;
		
		Context ctx = null;
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put(Context.INITIAL_CONTEXT_FACTORY,
				"weblogic.jndi.WLInitialContextFactory");
		ht.put(Context.PROVIDER_URL, "t3://127.0.0.1:7001");
		
		

	    try {
	    	
			arrayDeListasDeParametros = t.extraerListaDeParametros(sentencias_binds);
			
			xmlString =  t.jaxbObjectToXMLString(sentencias_binds);
			
			ctx = new InitialContext(ht);
			
			wsgServicioBeanRemote = (WsgServicioBeanRemote)ctx.lookup("java:global.wsg_ejb_ear.wsg_ejb/WsgServicioBean"); 
			
			wsgServiciosLogBeanRemote = (WsgServiciosLogBeanRemote)ctx.lookup("java:global.wsg_ejb_ear.wsg_ejb/WsgServiciosLogBean"); 
			
			
			wsgServicio = wsgServicioBeanRemote.find(idServicio);
			
			ds = Conexion.obtenerFuenteDeDatos(wsgServicio.getWsgJndi().getJndi());
			
			
			//preguntar si tiene acceso, caso contrario devolver mensaje
			wsgUsuarioServicioBeanRemote = (WsgUsuarioServicioBeanRemote)ctx.lookup("java:global.wsg_ejb_ear.wsg_ejb/WsgUsuarioServicioBean"); 

			WsgUsuarioServicioPK id = new WsgUsuarioServicioPK();
			id.setIdServicio(idServicio);
			id.setIdUsuario(usuario);
			
			wsgUsuarioServicio =  wsgUsuarioServicioBeanRemote.find(id);			
			
			
			if(wsgUsuarioServicio == null ){
				
				servicio.setMensajeError(getPropiedades().getProperty("wsgwar.servicioUsuarioNoExiste"));
				
				servicio.setCodigoError(-1);

				//registro log bd
				
				wsgServiciosLog.setXml(xmlString);
				wsgServiciosLog.setCodError(new BigDecimal(servicio.getCodigoError()));
				wsgServiciosLog.setMsgError(servicio.getMensajeError());
				wsgServiciosLog.setFechaFin(new Date());
				wsgServiciosLogBeanRemote.create(wsgServiciosLog);
				return servicio;
				
				
				
			}
			
			wsgUsuario = wsgUsuarioServicio.getWsgUsuario(); 
			
			
			if(!wsgUsuario.getClave().equals(clave)){
				servicio.setMensajeError(getPropiedades().getProperty("wsgwar.claveIncorrecta"));
				servicio.setCodigoError(-2);

				//registro log bd
	
				wsgServiciosLog.setXml(xmlString);
				wsgServiciosLog.setCodError(new BigDecimal(servicio.getCodigoError()));
				wsgServiciosLog.setMsgError(servicio.getMensajeError());
				
				//Grabar actividades del WS
				try{						
					wsgServiciosLog.setFechaFin(new Date());
					wsgServiciosLogBeanRemote.create(wsgServiciosLog);
					return servicio;
				}catch (Exception ex) {
					
					Throwable t2 = getLastThrowable(ex);  //fetching Internal Exception
					SQLException sqlException = (SQLException) t2;  //casting Throwable object to SQL Exception									
					servicio.setCodigoError(sqlException.getErrorCode());
					servicio.setMensajeError(sqlException.getMessage());
					return servicio;
				}
				
			}
				
			Connection conn=null;
			String vendor="persistence";
			if(wsgUsuario.getCuentaNoBloqueada().equals("S")){
				
				if(wsgUsuarioServicio.getEstado().equals("A")){
					
					try{
						//abrir conexion
						
						conn = ds.getConnection();
						vendor = conn.getMetaData().getDatabaseProductName().toLowerCase();
						
						System.out.println("PROVEEDOR DE BASES DE DATOS--> "+vendor);
						
						if(arrayDeListasDeParametros.isEmpty()){
							servicio.setXml(q.obtenerServicioEnDocument(conn, wsgServicio, new ArrayList<String>() ));
						}else{
							servicio.setXml(q.obtenerServicioEnDocument(conn, wsgServicio, arrayDeListasDeParametros.get(0).getParametros()));
						}
						
						//Todo exitoso
						
						
						servicio.setCodigoError(777);
						servicio.setMensajeError(getPropiedades().getProperty("wsgwar.resultadoExitoso"));

						//registro log bd
						wsgServiciosLog.setXml(xmlString);
						wsgServiciosLog.setResultado(q.getResultadoTotal());
									
						//Grabar actividades del WS
						try{	
							wsgServiciosLog.setCodError(new BigDecimal(servicio.getCodigoError()));
							wsgServiciosLog.setMsgError(servicio.getMensajeError());
							wsgServiciosLog.setFechaFin(new Date());
							wsgServiciosLogBeanRemote.create(wsgServiciosLog);
							return servicio;
						}catch (Exception ex) {
							
							Throwable t2 = getLastThrowable(ex);  //fetching Internal Exception
							SQLException sqlException = (SQLException) t2;  //casting Throwable object to SQL Exception									
							servicio.setCodigoError(sqlException.getErrorCode());
							servicio.setMensajeError(sqlException.getMessage());
							return servicio;
						}
										
						
					} catch (SQLException sqlError) {
						
						String errorSql = getPropiedades().getProperty(vendor+"."+sqlError.getErrorCode());
						
						
						System.out.println("PROPIEDAD-->" + vendor+"."+sqlError.getErrorCode());
						
						if(errorSql == null ){
							errorSql = sqlError.getMessage();
						}
						
						
						servicio.setCodigoError(sqlError.getErrorCode());
						servicio.setMensajeError(errorSql);
						
						
						//Grabar actividades del WS
						try{	
							wsgServiciosLog.setCodError(new BigDecimal(servicio.getCodigoError()));
							wsgServiciosLog.setMsgError(servicio.getMensajeError());
							wsgServiciosLog.setFechaFin(new Date());
							wsgServiciosLogBeanRemote.create(wsgServiciosLog);
							return servicio;
						}catch (Exception ex) {
							
							Throwable t2 = getLastThrowable(ex);  //fetching Internal Exception
							SQLException sqlException = (SQLException) t2;  //casting Throwable object to SQL Exception									
							servicio.setCodigoError(sqlException.getErrorCode());
							servicio.setMensajeError(sqlException.getMessage());
							return servicio;
						}
					} catch (Exception e0) {
						
						servicio.setMensajeError(e0.getMessage());
						//Grabar actividades del WS
						try{
							wsgServiciosLog.setCodError(new BigDecimal(servicio.getCodigoError()));
							wsgServiciosLog.setMsgError(servicio.getMensajeError());
							wsgServiciosLog.setFechaFin(new Date());
							wsgServiciosLogBeanRemote.create(wsgServiciosLog);
							return servicio;
						}catch (Exception ex) {
							
							Throwable t2 = getLastThrowable(ex);  //fetching Internal Exception
							SQLException sqlException = (SQLException) t2;  //casting Throwable object to SQL Exception									
							servicio.setCodigoError(sqlException.getErrorCode());
							servicio.setMensajeError(sqlException.getMessage());
							return servicio;
						}
					} finally {
						if (conn != null)
							try {
								conn.close();
							} catch (SQLException e) {
								e.printStackTrace();
								servicio.setMensajeError(e.getMessage());
								//Grabar actividades del WS								
								try{	
									wsgServiciosLog.setCodError(new BigDecimal(servicio.getCodigoError()));
									wsgServiciosLog.setMsgError(servicio.getMensajeError());
									wsgServiciosLog.setFechaFin(new Date());
									wsgServiciosLogBeanRemote.create(wsgServiciosLog);
									return servicio;
								}catch (Exception ex) {
									
									Throwable t2 = getLastThrowable(ex);  //fetching Internal Exception
									SQLException sqlException = (SQLException) t2;  //casting Throwable object to SQL Exception									
									servicio.setCodigoError(sqlException.getErrorCode());
									servicio.setMensajeError(sqlException.getMessage());
									return servicio;
								}
								
							}
					}		
					

				
				
				
				}else{
					servicio.setMensajeError(getPropiedades().getProperty("wsgwar.servicioUsuarioInactivo"));
					servicio.setCodigoError(-3);

					//registro log bd
					wsgServiciosLog.setXml(xmlString);
					
					//Grabar actividades del WS								
					try{	
						wsgServiciosLog.setCodError(new BigDecimal(servicio.getCodigoError()));
						wsgServiciosLog.setMsgError(servicio.getMensajeError());
						wsgServiciosLog.setFechaFin(new Date());
						wsgServiciosLogBeanRemote.create(wsgServiciosLog);
						return servicio;
					}catch (Exception ex) {
						
						Throwable t2 = getLastThrowable(ex);  //fetching Internal Exception
						SQLException sqlException = (SQLException) t2;  //casting Throwable object to SQL Exception									
						servicio.setCodigoError(sqlException.getErrorCode());
						servicio.setMensajeError(sqlException.getMessage());
						return servicio;
					}
					
					
					
				}
			
			}else{
				servicio.setMensajeError(getPropiedades().getProperty("wsgwar.cuentaBloqueada"));
				servicio.setCodigoError(-4);

				//registro log bd
				wsgServiciosLog.setXml(xmlString);
				
				
				//Grabar actividades del WS								
				try{
					wsgServiciosLog.setCodError(new BigDecimal(servicio.getCodigoError()));
					wsgServiciosLog.setMsgError(servicio.getMensajeError());
					wsgServiciosLog.setFechaFin(new Date());
					wsgServiciosLogBeanRemote.create(wsgServiciosLog);
					return servicio;
				}catch (Exception ex) {
					
					Throwable t2 = getLastThrowable(ex);  //fetching Internal Exception
					SQLException sqlException = (SQLException) t2;  //casting Throwable object to SQL Exception									
					servicio.setCodigoError(sqlException.getErrorCode());
					servicio.setMensajeError(sqlException.getMessage());
					return servicio;
				}
				
			}
			

	   
	    }catch(NameNotFoundException e0 ){

			e0.printStackTrace();
			servicio.setMensajeError(e0.getMessage());
			ds = null;
			
			//Grabar actividades del WS								
			try{	
				wsgServiciosLog.setCodError(new BigDecimal(servicio.getCodigoError()));
				wsgServiciosLog.setMsgError(servicio.getMensajeError());
				wsgServiciosLog.setFechaFin(new Date());
				wsgServiciosLogBeanRemote.create(wsgServiciosLog);
				return servicio;
			}catch (Exception ex) {
				
				Throwable t2 = getLastThrowable(ex);  //fetching Internal Exception
				SQLException sqlException = (SQLException) t2;  //casting Throwable object to SQL Exception									
				servicio.setCodigoError(sqlException.getErrorCode());
				servicio.setMensajeError(sqlException.getMessage());
				return servicio;
			}
			
			
		}catch(SQLException e){
			
			servicio.setCodigoError(e.getErrorCode());
			servicio.setMensajeError(e.getMessage());
			e.printStackTrace();
			//Grabar actividades del WS	
			try{		
				wsgServiciosLog.setCodError(new BigDecimal(servicio.getCodigoError()));
				wsgServiciosLog.setMsgError(servicio.getMensajeError());
				wsgServiciosLog.setFechaFin(new Date());
				wsgServiciosLogBeanRemote.create(wsgServiciosLog);
				return servicio;
			}catch (Exception ex) {
				
				Throwable t2 = getLastThrowable(ex);  //fetching Internal Exception
				SQLException sqlException = (SQLException) t2;  //casting Throwable object to SQL Exception									
				servicio.setCodigoError(sqlException.getErrorCode());
				servicio.setMensajeError(sqlException.getMessage());
				return servicio;
			}

		}
	    catch (Exception e0) {
			// TODO Auto-generated catch block
			e0.printStackTrace();
			servicio.setMensajeError(e0.getMessage());
			ds = null;
			//Grabar actividades del WS		
			try{	
				wsgServiciosLog.setCodError(new BigDecimal(servicio.getCodigoError()));
				wsgServiciosLog.setMsgError(servicio.getMensajeError());
				wsgServiciosLog.setFechaFin(new Date());
				wsgServiciosLogBeanRemote.create(wsgServiciosLog);
				return servicio;
			}catch (Exception ex) {
				
				Throwable t2 = getLastThrowable(ex);  //fetching Internal Exception
				SQLException sqlException = (SQLException) t2;  //casting Throwable object to SQL Exception									
				servicio.setCodigoError(sqlException.getErrorCode());
				servicio.setMensajeError(sqlException.getMessage());
				return servicio;
			}
			
		}
	    
	}
	
	private Throwable getLastThrowable(Exception e) {
		Throwable t = null;
		for(t = e.getCause(); t.getCause() != null; t = t.getCause());
		return t;
	}

}