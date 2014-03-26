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

        <legend>
            ${platform.name} | Новый каталог
        </legend>

        <form action="${ctx}/mvc/admin/create-catalog" method="post">

            <label for="name">Название </label>
            <input type="text" id="name" name="name">

            <label for="type">Тип прав</label>
            <select id="type" name="rights" class="input-block-level">
                <option value="AUTHOR">Авторские</option>
                <option value="RELATED">Смежные</option>
            </select>

            <label for="name">Роялти </label>
            <input type="number" id="royal" name="royal" placeholder="0">

            <input type="hidden" name="platformId" value="${platform.id}">
            <input type="submit" class="btn" value="Создать каталог">
        </form>
    </div>
</div>

<c:import url="../footer.jsp"/>


</body>
</html>