package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;



@Path("/login") 
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	/*
	 * A Logger Object
	 */

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName()); // Logs reportam o que acontece na execucao do nosso servico
	private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


	public LoginResource() { } //Nothing to be done here, construtores vazios permitam que estas classes sejam facilmente instanciadas pelo runtime do jersey

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(pt.unl.fct.di.apdc.firstwebapp.util.LoginData data, @Context HttpServletRequest request, @Context HttpHeaders headers) {


		LOG.fine("Login attempt by user: " + data.username);
		
		TransactionOptions options = TransactionOptions.Builder.withXG(true); //deixa-nos mexer em varias entidades dentro da mesma transaçao
		Transaction txn = datastore.beginTransaction(options);
		
		
		Key userKey = KeyFactory.createKey("User", data.username);

		try { // the entity must exist in the system
			
			
			
			Entity user = datastore.get(userKey);

			Query ctrQuery = new Query("UserStats").setAncestor(userKey);
			List <Entity> results = datastore.prepare(ctrQuery).asList(FetchOptions.Builder.withDefaults());
			Entity ustats = null;

			if(results.isEmpty()) {
				ustats = new Entity("UserStats", user.getKey());
				ustats.setProperty("user_stats_logins", 0L);
				ustats.setProperty("user_stats_failed", 0L);

			} else {
				ustats = results.get(0);
			}

			String hashedPWD = (String) user.getProperty("Password");
		if(hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {  // if the login is successful
				
			
				AuthToken token = new AuthToken(data.username);
				Entity aux = new Entity("Token", token.tokenID); //Esta entity serve para associar tokens a users
				
				aux.setProperty("Username", data.username);
				
			
				datastore.put(txn,aux);
			
				
								
				Entity log = new Entity("UserLog", user.getKey());

				log.setProperty("user_login_ip", request.getRemoteAddr());
				log.setProperty("user_login_host", request.getRemoteHost());
				log.setProperty("user_login_latlon", headers.getHeaderString("X-AppEngine-CityLatLong"));
				log.setProperty("user_login_city", headers.getHeaderString("X-AppEngine-City"));
				log.setProperty("user_login_country", headers.getHeaderString("X-AppEngine-Country"));
				log.setProperty("user_login_time", new Date());

				ustats.setProperty("user_stats_logins", 1L + (long) ustats.getProperty("user_stats_logins"));
				ustats.setProperty("user_stats_failed", 0L);
				ustats.setProperty("user_stats_last", new Date());

				List<Entity> logs = Arrays.asList(log,ustats);
				datastore.put(txn,logs);
				txn.commit();

				//return token

				LOG.info("User '" + data.username + "'logged in sucessfully.");

				return Response.ok().entity(g.toJson(token)).build();

			} else {
				//incorrect password

				ustats.setProperty("user_stats_failed", 1L + (long) ustats.getProperty("user_stats_failed"));
				datastore.put(txn,ustats);
				txn.commit();

				LOG.warning("Wrong password for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();

			}
		} catch (EntityNotFoundException e) {


			LOG.warning("Failed login attempt for username " + data.username);
			return Response.status(Status.FORBIDDEN).build();
			
		} finally { 
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();

			}

		} 
	}
	
	
	
	
	
	
	
	
	
	
}


/*if(data.username.equals("jleitao") && data.password.equals("password")) {
			AuthToken at = new AuthToken(data.username);
			return Response.ok(g.toJson(at)).build();
		}
		return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
	}


	@GET
	@Path("/{username}")
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		if(!username.equals("jleitao"))    {
			return Response.ok().entity(g.toJson(false)).build();
		} else {
			return Response.ok().entity(g.toJson(true)).build();
		}
	}*/

