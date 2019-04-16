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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

import org.apache.commons.codec.digest.DigestUtils;
import com.google.gson.Gson;

@Path("/register")
public class RegisterResource {

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	public static final String GS = "userGS";
	public static final String GBO = "userGBO";
	public static final String USER = "user";
	public static final String AUSER = "Auser";

	public RegisterResource() {
		// NOTHING TO BE DONE HERE...
	}

	@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doUserRegistration(pt.unl.fct.di.apdc.firstwebapp.util.RegisterData data) {

		TransactionOptions options = TransactionOptions.Builder.withXG(true); // deixa-nos mexer em varias entidades
		// dentro da mesma transaçao
		Transaction txn = datastore.beginTransaction(options);

		try {
			Key userKey = KeyFactory.createKey("User", data.username);
			Entity user = datastore.get(userKey);

			txn.rollback();

			return Response.status(Status.BAD_REQUEST).entity("User already exists").build();

		} catch (EntityNotFoundException e) {

			Entity user = new Entity("User", data.username);

			user.setProperty("Username", data.username);
			user.setProperty("Email", data.email);
			user.setProperty("Role", USER);
			user.setProperty("Profile", data.profile);
			user.setProperty("HomePhone", data.homePhone);
			user.setProperty("MobilePhone", data.mobilePhone);
			user.setProperty("Adress1", data.adress1);
			user.setProperty("Adress2", data.adress2);
			user.setProperty("Location", data.location);
			user.setProperty("PostCode", data.postcode);
			user.setProperty("Password", DigestUtils.sha512Hex(data.password).toString());

			user.setUnindexedProperty("CreationTime", new Date());

			datastore.put(txn, user);
			LOG.info("User registered" + data.username);

			txn.commit();

			return Response.ok().build();
		} finally {

			if (txn.isActive())
				txn.rollback();
		}
	}

	@POST
	@Path("/GS")
	public Response doUserGSRegistration() {

		TransactionOptions options = TransactionOptions.Builder.withXG(true); // deixa-nos mexer em varias entidades
		// dentro da mesma transaçao
		Transaction txn = datastore.beginTransaction(options);

		try {

			// if the entity exists then

			Key userKey = KeyFactory.createKey("User", "DiogoFernandes");
			Entity user = datastore.get(userKey);

			return Response.status(Status.BAD_REQUEST).entity("UserGS already exists").build();

			// if not then

		} catch (EntityNotFoundException e) {

			Entity user = new Entity("User", "DiogoFernandes");

			user.setProperty("Username", "DiogoFernandes");
			user.setProperty("Email", "diogofernandes@gmail.com");
			user.setProperty("Role", GS);
			user.setProperty("Profile", "public");
			user.setProperty("HomePhone", 213456789);
			user.setProperty("MobilePhone", 912345678);
			user.setProperty("Adress1", "Rua do Diogo");
			user.setProperty("Adress2", "Aqui pela zona");
			user.setProperty("Location", "Palmela");
			user.setProperty("PostCode", 2739284);
			user.setProperty("Password", DigestUtils.sha512Hex("diogopassword").toString());
			user.setProperty("Token", "tokendoadmin");

			user.setUnindexedProperty("CreationTime", new Date());

			datastore.put(txn, user);
			LOG.info("User registered" + "DiogoFernandes");

			txn.commit();

			return Response.ok().build();
		} finally {

			if (txn.isActive())
				txn.rollback();
		}
	}

	@POST
	@Path("/GBO")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doUserGBORegistration(pt.unl.fct.di.apdc.firstwebapp.util.RegisterData data) {

		TransactionOptions options = TransactionOptions.Builder.withXG(true); // deixa-nos mexer em varias entidades
		// dentro da mesma transaçao
		Transaction txn = datastore.beginTransaction(options);

		// verificacao da existencia do user GS
		try {

			Key adminKey = KeyFactory.createKey("User", data.adminName);

			// get ira mandar excecao caso nao exista o user
			Entity admin = datastore.get(adminKey);

			// verificar se o user que tenta registar um GBO, e um GS
			if (!admin.getProperty("Role").equals(GS))
				return Response.status(Status.BAD_REQUEST).entity("User does not have permission to regist!").build();

			if (data.tokenId != null && !data.tokenId.isEmpty()) {
				Key tokenKey = KeyFactory.createKey("Token", data.tokenId);
				datastore.get(tokenKey);
			} else {
				return Response.status(Status.BAD_REQUEST).entity("GS User not logged in").build();
			}

		} catch (EntityNotFoundException e) {
			// resposta enviada caso o GS nao exista
			return Response.status(Status.BAD_REQUEST).entity("GS User does not exist or not logged in").build();

		}

		// criaca do user GBO

		try {

			Key gboKey = KeyFactory.createKey("User", data.username);

			// queremos que lance a excecao, ou seja que o gboUser esteja a null
			Entity gboUser = datastore.get(gboKey);

			txn.rollback();

			// resposta se ja existir um user com o nome dado
			return Response.status(Status.BAD_REQUEST).entity("User already exists").build();

		} catch (EntityNotFoundException e) {

			Entity user = new Entity("User", data.username);

			user.setProperty("Username", data.username);
			user.setProperty("Email", data.email);
			user.setProperty("Role", GBO);
			user.setProperty("Profile", data.profile);
			user.setProperty("HomePhone", data.homePhone);
			user.setProperty("MobilePhone", data.mobilePhone);
			user.setProperty("Adress1", data.adress1);
			user.setProperty("Adress2", data.adress2);
			user.setProperty("Location", data.location);
			user.setProperty("PostCode", data.postcode);
			user.setProperty("Password", DigestUtils.sha512Hex(data.password).toString());

			user.setUnindexedProperty("CreationTime", new Date());

			datastore.put(txn, user);
			LOG.info("User registered" + data.username);

			txn.commit();

			return Response.ok().build();
		} finally {

			if (txn.isActive())
				txn.rollback();
		}
	}
}
