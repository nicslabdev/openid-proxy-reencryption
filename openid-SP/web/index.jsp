<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/html; charset=UTF-8" import="java.util.Map,java.util.LinkedHashMap" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Hello World!</title>
	<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
	<link rel="stylesheet" type="text/css" href="consumer-servlet.css" />
	<script type="text/javascript">
	<!--
	function changeAll(v) {
		var inputs = document.getElementsByTagName("input");
		for (var i = 0; i < inputs.length; i++) {
			if (inputs[i].value == v) {
				inputs[i].checked = true;
			}
		}
	}
	//-->
	</script>
</head>
<body>
	<div>
		<fieldset>
			<legend>Sample 1:</legend>
			<form action="consumer" method="post">
				<div>
					<input type="text" name="openid_identifier" />
				</div>
				<div>
					<button type="submit" name="login">Login</button>
				</div>
			</form>
		</fieldset>

		<fieldset>
			<legend>Sample 3: using the Attribute Exchange extension(doc: <a href="http://code.google.com/p/openid4java/wiki/AttributeExchangeHowTo">AttributeExchangeHowTo</a>)</legend>
			<form action="consumer" method="post">
				<div id="ax">
                                    Google: https://www.google.com/accounts/o8/id<br/>
					<input type="text" name="openid_identifier" value="http://localhost:8080/openid-idp/idp" />

					<table>
						<thead>
							<tr>
								<th>Alias</th>
								<th>TypeUri</th>
								<th>Required</th>
								<th>Count</th>
							</tr>
						</thead>
						<tbody>
							<%
							Map<String, String> attributes = new LinkedHashMap<String, String>();
							attributes.put("country", "http://axschema.org/contact/country/home");
							attributes.put("email", "http://axschema.org/contact/email");
							attributes.put("firstname", "http://axschema.org/namePerson/first");
							attributes.put("lastname", "http://axschema.org/namePerson/last");
							attributes.put("language", "http://axschema.org/pref/language");
                                                        attributes.put("custom", "");
							%>
							<c:forEach items="<%=attributes%>" var="attribute" varStatus="status">
							<tr>
								<th><input type="text" name="alias" value="${attribute.key}" /></th>
								<td><input type="text" name="typeUri" value="${attribute.value}" /></td>
								<td><input type="checkbox" name="required${status.index}" id="required${status.index}" checked="checked" /><label for="required${status.index}">Required</label></td>
								<td><input type="text" name="count" value="1" /></td>
							</tr>
							</c:forEach>
						</tbody>
						<!-- TODO
						<tfoot>
							<tr>
								<td colspan="4"><button type="button">Add row</button></td>
							</tr>
						</tfoot>
						-->
					</table>
					<button type="submit" name="login">Login</button>
				</div>
			</form>
		</fieldset>
	</div>
</body>
</html>
