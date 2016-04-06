/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nics.openid.sp;

import java.io.*;
import java.util.Map;
import nics.crypto.proxy.afgh.AFGH;
import nics.crypto.proxy.afgh.GlobalParameters;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author david
 */
public class ProxyReEncryptionHandler {

    private GlobalParameters global;

    private byte[] sk_b;

    public ProxyReEncryptionHandler() throws Exception{
        ObjectInputStream is = new ObjectInputStream(new FileInputStream("/Users/david/Desktop/pre.object"));

        Map<String,byte[]> map = (Map<String, byte[]>) is.readObject();

        sk_b = map.get("sk_b");
        global = new GlobalParameters(map.get("global"));
    }

    

    public String decrypt(String att_base64){
        byte[] b = Base64.decodeBase64(att_base64);
        byte[] r = AFGH.firstLevelDecryption(b, sk_b, global);
        return new String(r);
    }


}
