package nics.openid.idp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.apache.commons.lang.StringUtils;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.message.ParameterList;

/**
 * Servlet implementation class LoginServlet
 */
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
        // TODO Auto-generated constructor stub
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
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        if (StringUtils.equals(action, "doLogin")) {
            processLoginRequest(request, response);
        } else {
            //response.sendRedirect("/idp4java/endpoint");
            //request.getRequestDispatcher("login.jsp").forward(request, response);
            //response.sendRedirect("login.jsp");
            Object osc = session.getAttribute("SimpleConsumer");
            SimpleConsumer simpleConsumer = null;
            try {
                //Initialize SimpleConsumer
                if (osc != null) {
                    simpleConsumer = (SimpleConsumer) osc;
                } else {
                    simpleConsumer = new SimpleConsumer();
                    session.setAttribute("SimpleConsumer", simpleConsumer);
                }

                //Determine if we need to do Attribute Exchange or verify response
                if (isAttribueExchangeResponse(request)) {
                    simpleConsumer.verifyResponse(request);
                } else {
                    
                    String userSuppliedString = EndpointServlet.BASE_URL + "/idp";
                    simpleConsumer.authRequest(userSuppliedString, request, response);
                }

                Object o = session.getAttribute("SUCCESS");
                if (o != null && (Boolean) session.getAttribute("SUCCESS") == Boolean.TRUE) {
                    processLoginSuccess(request, response);
                }

            } catch (ConsumerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ParameterList pList = new ParameterList(request.getParameterMap());
            System.out.println("done");

        }
    }

    private void processLoginSuccess(HttpServletRequest request, HttpServletResponse response) {
        //Login was successful
        try {
            response.sendRedirect("welcome.jsp");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean isAttribueExchangeResponse(HttpServletRequest request) {
        ParameterList paramList = new ParameterList(request.getParameterMap());
        if ("id_res".equals(paramList.getParameterValue("openid.mode"))) {
            return true;
        }
        return false;
    }

    private void processLoginRequest(HttpServletRequest request,
            HttpServletResponse response) {
        System.out.println("[LoginServlet.processLoginRequest] process login info");
    }
}
