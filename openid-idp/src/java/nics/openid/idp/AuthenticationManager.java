package nics.openid.idp;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.apache.commons.lang.StringUtils;

public class AuthenticationManager {

    private static List<User> users = new LinkedList<User>();

    public AuthenticationManager() {
        init();
    }

    public User authenticate(String username, String password) {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            for (User u : users) {
                if (u.getUserName() != null && u.getPassword() != null
                        && StringUtils.equalsIgnoreCase(u.getUserName(), username)
                        && StringUtils.equals(u.getPassword(), password)) {
                    return u;
                }
            }
        }
        return null;
    }

    public void init() {
//        try {
//
//            //The XMLDecoder class reads an XML document that was created with XMLEncoder:
//            XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream("users.xml")));
//            users = (List<User>) decoder.readObject();
//            decoder.close();
//
//        } catch (FileNotFoundException ex) {




            try {

                ObjectInputStream is = new ObjectInputStream(new FileInputStream("/Users/david/Desktop/pre.object"));

                Map<String, byte[]> map = (Map<String, byte[]>) is.readObject();

                User u1 = new User("Joe", "Doe", "joe.dow@gmail.com", "test", "test");
                u1.setAttribute("language", "ES");
                u1.setAttribute("country", "Spain");
                u1.setAttribute("atributo", "valor");


                String c_a_base64 = new String(map.get("c_a_base64"));
                u1.setAttribute(User.FIRST_NAME, c_a_base64);

                users.add(u1);

//                XMLEncoder encoder;
//
//                encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("users.xml")));
//                encoder.writeObject(users);
//                encoder.close();
            } catch (Exception ex1) {
                Logger.getLogger(AuthenticationManager.class.getName()).log(Level.SEVERE, null, ex1);
            }

//        }
    }
}

