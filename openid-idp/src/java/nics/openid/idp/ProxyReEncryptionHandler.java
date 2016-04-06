/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nics.openid.idp;

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

    private byte[] reencryptionKey;

    public ProxyReEncryptionHandler() throws Exception{
        ObjectInputStream is = new ObjectInputStream(new FileInputStream("/Users/david/Desktop/pre.object"));

        Map<String,byte[]> map = (Map<String, byte[]>) is.readObject();

        reencryptionKey = map.get("rk_a_b");
        global = new GlobalParameters(map.get("global"));
    }

    public byte[] getReencryptionKey() {
        return reencryptionKey;
    }

    public void setReencryptionKey(String base64) {
        byte[] b = Base64.decodeBase64(base64);
        
        this.reencryptionKey = b;//AFGH.bytesToElement(b, global.getG1());
    }

    public void revokeReencryptionKey(){
        this.reencryptionKey = null;
    }

    public String reEncrypt(String att_base64){
        byte[] b = Base64.decodeBase64(att_base64);
        byte[] r = AFGH.reEncryption(b, reencryptionKey, global);
        return Base64.encodeBase64URLSafeString(r);
    }


}
