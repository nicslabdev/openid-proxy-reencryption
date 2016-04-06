package nics.openid.idp;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.server.ServerManager;


public class EndpointServlet extends HttpServlet {

    //TODO
    public static final String BASE_URL = "http://localhost:8080/openid-idp";
//    public static final String BASE_URL = "http://192.168.1.30/openid-idp";
    
    private static final long serialVersionUID = 1L;
    private ServerManager serverManager;
    private AuthenticationManager authManager;

    private OpenIDProvider idp;


    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        // ServerManager init
        serverManager = new ServerManager();

        String endpoint = BASE_URL + "/endpoint";
        serverManager.setOPEndpointUrl(endpoint);
        // for a working demo, not enforcing RP realm discovery
        // since this new feature is not deployed
        serverManager.getRealmVerifier().setEnforceRpId(false);
        serverManager.setEnforceRpId(false);
        //server = new SampleServer(BASE_URL);

        // Authentication Manager
        authManager = new AuthenticationManager();

        // OpenIDProvider
        idp = new OpenIDProvider(serverManager, authManager, this);

    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


            idp.handle(request, response);

    }


}
