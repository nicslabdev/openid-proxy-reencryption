package nics.openid.idp;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {

	public static final String FIRST_NAME = "firstname";
	public static final String LAST_NAME = "lastname";
	public static final String EMAIL = "email";
	//private static final String USERNAME = "username";
	//private static final String PASSWORD = "password";

        private Map<String,String> attributes;

        private String username, password;

	public User(String userName, String password) {
            setUserName(userName);
            setPassword(password);
            attributes = new HashMap<String,String>();
	}

	public User(String firstName, String lastName, String email,
			String userName, String password) {
            this(userName, password);

            attributes.put(FIRST_NAME, firstName);
            attributes.put(LAST_NAME, lastName);
            attributes.put(EMAIL, email);
	
	}

	public String getFirstName() {
		return attributes.get(FIRST_NAME);
	}

	public void setFirstName(String firstName) {
		attributes.put(FIRST_NAME, firstName);
	}

	public String getLastName() {
		return attributes.get(LAST_NAME);
	}

	public void setLastName(String lastName) {
		attributes.put(LAST_NAME, lastName);
	}

	public String getEmail() {
		return attributes.get(EMAIL);
	}

	public void setEmail(String email) {
		attributes.put(EMAIL, email);
	}

	public String getUserName() {
		return username;
	}

	public void setUserName(String userName) {
		this.username = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

        public void setAttribute(String attName, String attValue){
            attributes.put(attName, attValue);
        }

        public String getAttribute(String attName){
            return attributes.get(attName);
        }

        public Map<String,String> getAttributes(){
            return Collections.unmodifiableMap(attributes);
        }

        public void setAttributes(Map<String,String> m){
            attributes = m;
        }

    @Override
    public String toString() {
        return attributes.toString();
    }



}