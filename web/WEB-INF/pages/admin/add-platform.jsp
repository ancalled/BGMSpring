<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<html>
<head>

    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap.css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap-fileupload.css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/csv-preview.css" media="screen"/>


    <title>Создание каталога</title>
</head>
<body>

<c:import url="../navbar.jsp">
    <c:param name="index" value="active"/>
</c:import>

<div class="container">
    <div class="span4">

        <h4>
            Создание платформы
        </h4>

        <form action="${ctx}/mvc/admin/create-platform" method="post">

            <label for="name">Название платформы </label>
            <input type="text" id="name" name="name">

            <input type="submit" value="Создать платформу">

            <c:import url="../footer.jsp"/>

        </form>
    </div>
</div>
</body>
</html>