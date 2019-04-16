package pt.unl.fct.di.apdc.firstwebapp.util;


public class ModifyData {

	public String username;
	public String newUsername;
	
	public String email;
	public String role;
	public String profile;
	public long homePhone;
	public long mobilePhone;
	public String adress1;
	public String adress2;
	public String location;
	public String postcode;
	public String password;
	
	public String adminName;
	public int adminPassword;
	public String tokenId;

	public ModifyData() {}
	
	public ModifyData(String username, String newUsername, String email, String role, String profile, long homePhone, 
			long mobilePhone, String adress1, String adress2, String location, String postcode, String password, String adminName, int adminPassword, String tokenId ) {
		
		this.username = username;
		this.newUsername = newUsername;
		this.email = email;
		this.role = role;
		this.profile = profile;
		this.homePhone = homePhone;
		this.mobilePhone = mobilePhone;
		this.adress1 = adress1;
		this.adress2 = adress2;
		this.location = location;
		this.postcode = postcode;
		this.password = password;
		
		this.adminName = adminName;
		this.adminPassword = adminPassword;
		this.tokenId = tokenId;
	}
	
	public static boolean check(String attribute) {
		return attribute != null && !attribute.isEmpty();
	}
	
}