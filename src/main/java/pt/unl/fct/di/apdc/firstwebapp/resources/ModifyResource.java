package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.Date;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
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

import pt.unl.fct.di.apdc.firstwebapp.util.ModifyData;

@Path("/modify")

public class ModifyResource {

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	public static final String GS = "userGS";
	public static final String GBO = "userGBO";
	public static final String USER = "user";
	public static final String AUSER = "Auser";

	public ModifyResource() {
		// NOTHING TO BE DONE HERE...
	}

	@POST
	@Path("/normaluser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response userModify(pt.unl.fct.di.apdc.firstwebapp.util.ModifyData data) {

		TransactionOptions options = TransactionOptions.Builder.withXG(true); // deixa-nos mexer em varias entidades
																				// dentro da mesma transaçao
		Transaction txn = datastore.beginTransaction(options);

		try {
			Key userKey = KeyFactory.createKey("User", data.username);
			Entity user = datastore.get(userKey);
			
			if (data.tokenId != null && !data.tokenId.isEmpty()) { // verifico se o token esta na base de dados
				Key tokenKey = KeyFactory.createKey("Token", data.tokenId);
				datastore.get(tokenKey);
				
			} else {
				return Response.status(Status.BAD_REQUEST).entity("GBO User not logged in").build();
			}

			if (user.getProperty("Role").equals(USER)) {

				/*
				if (ModifyData.check(data.newUsername)) {
					user.setProperty("Username", data.newUsername);
				}
				*/

				if (ModifyData.check(data.email)) {
					user.setProperty("Email", data.email);
				}

				// user.removeProperty("Role");
				// user.setProperty("Role", USER);

				if (ModifyData.check(data.profile)) {
					user.setProperty("Profile", data.profile);
				}

				if (data.homePhone != 0) {
					user.setProperty("HomePhone", data.homePhone);
				}

				if (data.mobilePhone != 0) {
					user.setProperty("MobilePhone", data.mobilePhone);
				}

				if (ModifyData.check(data.adress1)) {
					user.setProperty("Adress1", data.adress1);
				}

				if (ModifyData.check(data.adress2)) {
					user.setProperty("Adress2", data.adress2);
				}

				if (ModifyData.check(data.location)) {
					user.setProperty("Location", data.location);
				}

				if (ModifyData.check(data.postcode)) {
					user.setProperty("PostCode", data.postcode);
				}

				if (ModifyData.check(data.password)) {
					user.setProperty("Password", DigestUtils.sha512Hex(data.password).toString());
				}

			} else {

				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not have permission to modify").build();

			}
			
			datastore.put(txn, user);
			txn.commit();

			return Response.ok().build();

		} catch (EntityNotFoundException e) {
			txn.commit();
			return Response.status(Status.BAD_REQUEST).entity("User does not exist or not logged in").build();
		} finally {

			if (txn.isActive())
				txn.rollback();
		}
	}

	@POST
	@Path("/GBO")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8") // DOES NOT WORK CORRECTLY YET
	public Response GBOModify(pt.unl.fct.di.apdc.firstwebapp.util.ModifyData data) {

		TransactionOptions options = TransactionOptions.Builder.withXG(true); // deixa-nos mexer em varias entidades
																				// dentro da mesma transaçao
		Transaction txn = datastore.beginTransaction(options);

		try {

		

			Key adminKey = KeyFactory.createKey("User", data.adminName);
			Entity admin = datastore.get(adminKey);
			
			if (data.tokenId == null || data.tokenId.isEmpty()) {
				return Response.status(Status.BAD_REQUEST).entity("GBO User not logged in").build();
			}
			
			Key userKey = KeyFactory.createKey("User", data.username);
			Entity user = datastore.get(userKey);

			if (admin.getProperty("Role").equals(GBO)) {

				if (ModifyData.check(data.email)) {
					user.setProperty("Email", data.email);
				}

				// user.removeProperty("Role");
				// user.setProperty("Role", USER);

				if (ModifyData.check(data.profile)) {
					user.setProperty("Profile", data.profile);
				}

				if (data.homePhone != 0) {
					user.setProperty("HomePhone", data.homePhone);
				}

				if (data.mobilePhone != 0) {
					user.setProperty("MobilePhone", data.mobilePhone);
				}

				if (ModifyData.check(data.adress1)) {
					user.setProperty("Adress1", data.adress1);
				}

				if (ModifyData.check(data.adress2)) {
					user.setProperty("Adress2", data.adress2);
				}

				if (ModifyData.check(data.location)) {
					user.setProperty("Location", data.location);
				}

				if (ModifyData.check(data.postcode)) {
					user.setProperty("PostCode", data.postcode);
				}

				if (ModifyData.check(data.password)) {
					user.setProperty("Password", DigestUtils.sha512Hex(data.password).toString());
				}
			} else {

				return Response.status(Status.BAD_REQUEST).entity("User does not have permission to modify").build();

			}
			
			datastore.put(txn, user);
			txn.commit();

			return Response.ok().build();

		} catch (EntityNotFoundException e) {
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
		} finally {

			if (txn.isActive())
				txn.rollback();
		}
	}

}
