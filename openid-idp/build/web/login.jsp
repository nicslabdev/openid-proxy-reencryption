<%@ page session="true" %>
<%@ page import="java.util.Map,java.util.Iterator,org.openid4java.discovery.Identifier,org.openid4java.discovery.DiscoveryInformation,org.openid4java.message.ax.FetchRequest,org.openid4java.message.ax.FetchResponse,org.openid4java.message.ax.AxMessage,org.openid4java.message.*,org.openid4java.OpenIDException,java.util.List,java.io.IOException,javax.servlet.http.HttpSession,javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse,org.openid4java.consumer.ConsumerManager,org.openid4java.consumer.InMemoryConsumerAssociationStore,org.openid4java.consumer.InMemoryNonceVerifier" %>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>This is the login page</title>
</head>
<body>
<B>This is the Login Page</B>
<form name="login" action="endpoint" method="POST" accept-charset="utf-8">
	<input type="hidden" name="action" id="action" value="doLogin" />
	<input type="text" name="username" value="test"  />
	<input type="text" name="password" value="test" />	
	<input type="submit" />

</form>

</body>
</html>