package nics.openid.idp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.DirectError;
import org.openid4java.message.IndirectError;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;
import org.openid4java.server.ServerManager;

public class OpenIDProvider {
    public static final String OPENID_MODE = "openid.mode";
    public static final String OPENID_PREFIX = "openid.";

    private AuthenticationManager authenticationManager;
    private ServerManager serverManager;
    private HttpServlet servlet;
    ProxyReEncryptionHandler pre;

    private enum MODE {

        associate, checkid_setup, checkid_immediate, check_authentication
    };
    private static final Log log = LogFactory.getLog(OpenIDProvider.class);

    public OpenIDProvider(ServerManager sm, AuthenticationManager am, HttpServlet s) {
        authenticationManager = am;
        serverManager = sm;
        servlet = s;
        try {
            pre = new ProxyReEncryptionHandler();
            //pre.setReencryptionKey(OPENID_MODE);
        } catch (Exception ex) {
            Logger.getLogger(OpenIDProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        if (isAuthenticationRequest(request)) {
            
            Object u = request.getParameter("username");
            Object p = request.getParameter("password");
            String username = u != null ? (String) u : null;
            String password = p != null ? (String) p : null;
            User authorizedUser = authenticationManager.authenticate(username, password);

            log.info("[IdP] handle : user = " + authorizedUser);

            if (authorizedUser == null) {
                //response.sendRedirect("login.jsp");
                request.getRequestDispatcher("/login").forward(request, response);
                return null;
            }



            log.info("[IdP] handle : parameterlist = " + session.getAttribute("parameterlist"));
            if (session.getAttribute("parameterlist") != null) {
                ParameterList requestp = (ParameterList) session.getAttribute("parameterlist");
                for(Object o : requestp.getParameters()){
                    Parameter param = (Parameter) o;
                    session.setAttribute(param.getKey(), param.getValue());
                }

                session.setAttribute("authenticatedAndApproved", authorizedUser);
            }

        }

        // extract the parameters from the request
        ParameterList parameterList = null;

        if (session.getAttribute("parameterlist") != null) {
            parameterList = (ParameterList) session.getAttribute("parameterlist");
        } else {
            parameterList = new ParameterList(request.getParameterMap());
        }

        String mode = parameterList.hasParameter(OPENID_MODE) ? parameterList.getParameterValue(OPENID_MODE) : null;
        log.info("[IdP] openid.mode = " + mode);
        if (StringUtils.isEmpty(mode)) {
            //request.getRequestDispatcher("login.jsp").forward(request, response);
            response.sendRedirect("/openid-idp/login");
            return null;
        }

        switch (MODE.valueOf(mode)) {
            case associate:
                return associate(request, response, parameterList);
            case checkid_setup:
                return checkId(request, response, parameterList);
            case checkid_immediate:
                return checkId(request, response, parameterList);
            case check_authentication:
                return checkAuthentication(request, response, parameterList);
            default:
                return unknownError(request, response, parameterList);
        }
    }

    private boolean isAuthenticationRequest(HttpServletRequest request) {
        if ("doLogin".equals(request.getParameter("action"))) {
            log.info("[IdP] isAuthenticationRequest : true");
            return true;
        }
        log.info("[IdP] isAuthenticationRequest : false");
        return false;
    }

    private String unknownError(HttpServletRequest request, HttpServletResponse response, ParameterList parameterList) throws IOException {
        Message messageResponse;
        String responseText;
        // --- error response ---
        // When openid.mode = null or does not match any of the standard modes.
        messageResponse = DirectError.createDirectError("Unknown request");
        responseText = messageResponse.keyValueFormEncoding();
        // return the result to the user
        return directResponse(response, messageResponse.keyValueFormEncoding());
    }

    private String checkAuthentication(HttpServletRequest request, HttpServletResponse response, ParameterList parameterList) throws IOException {

        log.info("[IdP] checkAuth: parameterList = " + parameterList);


        //HttpSession session = request.getSession();
        Message messageResponse;
        String responseText;

        // --- processing a verification request ---
        messageResponse = serverManager.verify(parameterList);
        responseText = messageResponse.keyValueFormEncoding();

        log.info("[IdP] checkAuth: responseText = " + responseText);

        return directResponse(response, messageResponse.keyValueFormEncoding());
    }

    private String checkId(HttpServletRequest request, HttpServletResponse response, ParameterList parameterList) throws ServletException, IOException {

        log.info("[IdP] checkId: parameterList = " + parameterList);

        
        HttpSession session = request.getSession();
        Message messageResponse;
        String responseText;

        // interact with the user and obtain data needed to continue
        String userSelectedId = null;
        String userSelectedClaimedId = null;
        User authenticatedAndApproved = null;


        if (session.getAttribute("authenticatedAndApproved") == null) {
            session.setAttribute("parameterlist", parameterList);
            String wwwParams = wwwFormEncoding(parameterList);
            String url = "login.jsp?" + wwwParams;

            log.info("[IdP] checkId: wwwParams = " + wwwParams);

            response.sendRedirect(url);
            return null;
        } else {
            userSelectedId = (String) session.getAttribute("openid.claimed_id");
            userSelectedClaimedId = (String) session.getAttribute("openid.identity");
            authenticatedAndApproved = (User) session.getAttribute("authenticatedAndApproved");
            // Remove the parameterlist so this provider can accept requests from elsewhere
            session.removeAttribute("parameterlist");

            //session.setAttribute("authenticatedAndApproved", null);
        }

        log.info("[IdP] checkId : userSelectedId = " + userSelectedId);
        log.info("[IdP] checkId : userSelectedClaimedId = " + userSelectedClaimedId);

        // --- process an authentication request ---
        AuthRequest authReq = null;
        String opLocalId = null;

        try {
            authReq = AuthRequest.createAuthRequest(parameterList, serverManager.getRealmVerifier());
            String authReqClaimed = authReq.getClaimed();
            log.info("[IdP] checkId : authReqClaimed = " + authReqClaimed);
            // if the user chose a different claimed_id than the one in request
            if (userSelectedClaimedId != null && userSelectedClaimedId.equals(authReqClaimed)) {
                opLocalId = EndpointServlet.BASE_URL + "/user";
                //lookupLocalId(userSelectedClaimedId);
            }
        } catch (MessageException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }

        // TODO: Averiguar que es opLocalId
        userSelectedId = opLocalId;
        userSelectedClaimedId = EndpointServlet.BASE_URL + "/user";

        // --- process an authentication request ---
        //messageResponse = manager.authResponse(parameterList, userSelectedId, userSelectedClaimedId, true);

        //opLocalId = "http://specs.openid.net/auth/2.0/identifier_select";
        log.info("[IdP] checkId : parameterList = " + parameterList);
        log.info("[IdP] checkId : opLocalId = " + opLocalId);
        log.info("[IdP] checkId : userSelectedClaimedId = " + userSelectedClaimedId);

        messageResponse = serverManager.authResponse(parameterList, userSelectedId, userSelectedClaimedId, true);


        if (messageResponse instanceof DirectError || messageResponse instanceof IndirectError) {
            return directResponse(response, messageResponse.keyValueFormEncoding());
        } else {

            try {
                // ATTRIBUTE EXTENSION
                if (authReq.hasExtension(AxMessage.OPENID_NS_AX)) {
                    processAttributeExchange(authReq, session, messageResponse);
                }

                // SIMPLE REGISTRATION
                if (authReq.hasExtension(SRegMessage.OPENID_NS_SREG)) {
                    processSimpleRegistration(authReq, messageResponse);
                }

                // Sign the auth success message.
                // This is required as AuthSuccess.buildSignedList has a `todo' tag now.
                serverManager.sign((AuthSuccess) messageResponse);
            } catch (Exception e) {
                log.info("[IdP] checkId: " + e.toString());
                e.printStackTrace();
            }

            // caller will need to decide which of the following to use:

            // option1: GET HTTP-redirect to the return_to URL
//
//            session.setAttribute("SUCCESS", Boolean.TRUE);
//
//            log.info("[IdP] checkId: responseText = " + messageResponse.keyValueFormEncoding());
//
//            response.sendRedirect(messageResponse.getDestinationUrl(true));

//             option2: HTML FORM Redirection
            RequestDispatcher dispatcher =
                    servlet.getServletContext().getRequestDispatcher("/formredirection.jsp");
            request.setAttribute("parameterMap", messageResponse.getParameterMap());
            request.setAttribute("destinationUrl", authReq.getReturnTo());

            dispatcher.forward(request, response);


            return null;
        }

    }

    private void processAttributeExchange(AuthRequest authReq, HttpSession session, Message messageResponse) throws UnsupportedOperationException, MessageException {
        log.info("[IdP] checkId : attributeExtension ");
        MessageExtension ext = authReq.getExtension(AxMessage.OPENID_NS_AX);
        if (ext instanceof FetchRequest) {
            FetchRequest fetchReq = (FetchRequest) ext;
            Map required = fetchReq.getAttributes(true);
            Map optional = fetchReq.getAttributes(false);
            User u = (User) session.getAttribute("authenticatedAndApproved");
            log.info("[IdP] checkId : attributeExtension : user = " + u);
            if (u != null) {
                Map responseMap = new HashMap();
                for (Object att : required.keySet()) {
                    Object type = required.get(att);
                    String val = u.getAttribute((String) att);
                    if(att.equals(User.FIRST_NAME)){
                        val = pre.reEncrypt(val);
                    }
                    responseMap.put(att, val);
                }
                for (Object att : optional.keySet()) {
                    Object type = required.get(att);
                    String val = u.getAttribute((String) att);
                    if(att.equals(User.FIRST_NAME)){
                        val = pre.reEncrypt(val);
                    }
                    responseMap.put(att, val);
                }
                log.info("[IdP] checkId : attributeExtension : responseMap = " + responseMap);
                // Crear FetchResponse con el resultado
                FetchResponse fetchResp = FetchResponse.createFetchResponse(fetchReq, responseMap);
                // Añadirlo al mensaje de respuesta
                messageResponse.addExtension(fetchResp);
            }
        } else //if (ext instanceof StoreRequest)
        {
            throw new UnsupportedOperationException("TODO");
        }
    }

    private void processSimpleRegistration(AuthRequest authReq, Message messageResponse) throws UnsupportedOperationException, MessageException {
        MessageExtension ext = authReq.getExtension(SRegMessage.OPENID_NS_SREG);
        if (ext instanceof SRegRequest) {
            SRegRequest sregReq = (SRegRequest) ext;
            List required = sregReq.getAttributes(true);
            List optional = sregReq.getAttributes(false);
            if (required.contains("email")) {
                // data released by the user
                Map userDataSReg = new HashMap();
                userDataSReg.put("email", "user@example.com");
                SRegResponse sregResp = SRegResponse.createSRegResponse(sregReq, userDataSReg);
                // (alternatively) manually add attribute values
                //sregResp.addAttribute("email", email);
                messageResponse.addExtension(sregResp);
            }
        } else {
            throw new UnsupportedOperationException("TODO");
        }
    }


    private String associate(HttpServletRequest request, HttpServletResponse response, ParameterList parameterList) throws IOException {
        log.info("[IdP] associate: parameterList = " + parameterList);

        // --- process an association parameterList ---
        Message messageResponse = serverManager.associationResponse(parameterList);
        String responseText = messageResponse.keyValueFormEncoding();

        log.info("[IdP] associate: responseText = " + responseText);

        return directResponse(response, responseText);

    }

    private String directResponse(HttpServletResponse response, String messageResponse)
            throws IOException {
        ServletOutputStream os = response.getOutputStream();
        os.write(messageResponse.getBytes());
        os.close();

        return null;
    }

    private String wwwFormEncoding(ParameterList parameterList) {
        StringBuffer allParams = new StringBuffer("");

        List parameters = parameterList.getParameters();
        for(Object o : parameters){
            Parameter parameter = (Parameter) o;

            // All of the keys in the request message MUST be prefixed with "openid."
            if (!parameter.getKey().startsWith(OPENID_PREFIX)) {
                allParams.append(OPENID_PREFIX);
            }

            try {
                allParams.append(URLEncoder.encode(parameter.getKey(), "UTF-8"));
                allParams.append('=');
                allParams.append(URLEncoder.encode(parameter.getValue(), "UTF-8"));
                allParams.append('&');
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }

        // remove the trailing '&'
        if (allParams.length() > 0) {
            allParams.deleteCharAt(allParams.length() - 1);
        }

        return allParams.toString();
    }
}
