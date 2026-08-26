<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title><tiles:insertAttribute name="title"/></title>
</head>
<body>
  <tiles:insertAttribute name="header"/>
  <main>
    <tiles:insertAttribute name="body"/>
  </main>
  <tiles:insertAttribute name="footer"/>
</body>
</html>
