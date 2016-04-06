<%@ page contentType="application/xrds+xml"%><%@ page import="nics.openid.idp.EndpointServlet" %><?xml version="1.0" encoding="UTF-8"?>
<xrds:XRDS
  xmlns:xrds="xri://$xrds"
  xmlns:openid="http://openid.net/xmlns/1.0"
  xmlns="xri://$xrd*($v*2.0)">
  <XRD>
  <Service priority="0">
  <Type>http://specs.openid.net/auth/2.0/server</Type>
  <Type>http://openid.net/srv/ax/1.0</Type>
  <Type>http://specs.openid.net/extensions/ui/1.0/mode/popup</Type>
  <Type>http://specs.openid.net/extensions/ui/1.0/icon</Type>
  <Type>http://specs.openid.net/extensions/pape/1.0</Type>
  <URI><%=EndpointServlet.BASE_URL%>/endpoint</URI>
  </Service>
  </XRD>
</xrds:XRDS>
