<%-- Copyright 2020. The Regents of the University of California. All Rights Reserved. --%>
<%@ page import = "java.util.Map" %>
<%@ page import = "org.apache.commons.text.StringEscapeUtils" %>
<html>
<body onload="document.forms['b2bform'].submit()">
<form method="post" action="b2b.do?methodToCall=returnFromShopping" name="b2bform">
<%
Map<String, String[]> parameters = request.getParameterMap();
for(String parameter : parameters.keySet()) {
    String[] values = parameters.get(parameter);
    for (String v : values) {
    	out.println("<input name=\"" + parameter + "\" value=\"" + StringEscapeUtils.escapeHtml4(v) + "\" type=\"hidden\">");
    }
}
%>
</form>
</body>