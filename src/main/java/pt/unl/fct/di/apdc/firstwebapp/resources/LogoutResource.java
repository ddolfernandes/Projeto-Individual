package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;


@Path("/logout") 
public class LogoutResource {

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	//remover o tokenID do user que quer fazer logout da datastore????


	public LogoutResource() {

	}

	@DELETE
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response logout(pt.unl.fct.di.apdc.firstwebapp.util.RegisterData data, @Context HttpServletRequest request, @Context HttpHeaders headers) {

		LOG.info("Attempt to logout user: " + data.username);

		TransactionOptions options = TransactionOptions.Builder.withXG(true); //deixa-nos mexer em varias entidades dentro da mesma transaçao
		Transaction txn = datastore.beginTransaction(options);

		try {
			Key userKey = KeyFactory.createKey("User", data.username); 
			Key tokenKey = KeyFactory.createKey("Token", data.tokenId);

			if (datastore.get(userKey).getProperty("Username").equals(data.username)) {
				datastore.delete(tokenKey);
				LOG.info("User '" + data.username + "' logged out successfully.");

				txn.commit();
				return Response.ok(this.g.toJson(true)).build();

			}

			System.out.println(datastore.get(userKey).getProperty("Username"));
			
			return Response.status(Response.Status.FORBIDDEN).build();
		} catch (EntityNotFoundException e) {
			
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}
}







