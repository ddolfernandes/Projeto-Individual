package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.Date;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.gson.Gson;

@Path("/remove")
public class RemoveResource {


	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	public static final String GS = "userGS";
	public static final String GBO = "userGBO";
	public static final String USER = "user";
	public static final String AUSER = "Auser";



	public RemoveResource() {
		//NOTHING TO BE DONE HERE...
	}


	@DELETE
	@Path("/normaluser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response removeNormalUser(pt.unl.fct.di.apdc.firstwebapp.util.RegisterData data) {

		TransactionOptions options = TransactionOptions.Builder.withXG(true); //deixa-nos mexer em varias entidades dentro da mesma transaçao
		Transaction txn = datastore.beginTransaction(options);

		try {
			Key userKey = KeyFactory.createKey("User", data.username);
			Entity user = datastore.get(userKey); 
				
			
			if (data.tokenId != null && !data.tokenId.isEmpty()) {
				Key tokenKey = KeyFactory.createKey("Token", data.tokenId);
				datastore.get(tokenKey);
			} else {
				return Response.status(Status.BAD_REQUEST).entity(" That User is not logged in").build();
			
			}
			
			//se o user a remover for do tipo USER
			if (user.getProperty("Role").equals(USER)) {
				datastore.delete(userKey);
				LOG.info(data.username + " was removed successfully");

				txn.commit();

			} else {

				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists").build();

			}

			return Response.ok().build();


		}
			catch (EntityNotFoundException e) {
			txn.commit();
			return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
			
			
		} finally {

			if(txn.isActive())
				txn.rollback();
		}
	}




	@DELETE
	@Path("/anyuser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response removeAnyUser(pt.unl.fct.di.apdc.firstwebapp.util.RegisterData data) {


		TransactionOptions options = TransactionOptions.Builder.withXG(true); //deixa-nos mexer em varias entidades dentro da mesma transaçao
		Transaction txn = datastore.beginTransaction(options);


		//verificacao da existencia do user GBO
		try {

			//vou usar o adminName como parametro do username do GBO que vai remover um user (tenho que o por no postman)
			Key gboKey = KeyFactory.createKey("User", data.adminName); 

			//get ira mandar excecao caso nao exista o user GBO
			Entity gboUser = datastore.get(gboKey);

			
			if (data.tokenId != null && !data.tokenId.isEmpty()) {
				Key tokenKey = KeyFactory.createKey("Token", data.tokenId);
				datastore.get(tokenKey);
			} else {
				return Response.status(Status.BAD_REQUEST).entity("GS User not logged in").build();
			
			}
			
			System.out.println("ROLE: "+gboUser.getProperty("Role"));

			//verificar se o user que tenta remover qualquer user, e um GBO
			if (!gboUser.getProperty("Role").equals(GBO)) 
				return Response.status(Status.BAD_REQUEST).entity("User does not have permisson to remove").build();

			

		
		}catch (EntityNotFoundException e) {

			return Response.status(Status.BAD_REQUEST).entity("  User GBO does not exist").build();

		}

		try {

			Key userKey = KeyFactory.createKey("User", data.username);
			Entity user = datastore.get(userKey);
			
			datastore.delete(userKey);
			
			LOG.info(data.username + "was removed successfully");

			
			txn.commit();

			return Response.ok().build();

		} catch (EntityNotFoundException e) {

			return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();


		} finally {

			if(txn.isActive())
				txn.rollback();
		}
	}

}
