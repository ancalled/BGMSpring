<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<html>
<head>
    <title>Авторизация</title>
    <script src="${ctx}/js/bootstrap.js" type="text/javascript"></script>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap.css" media="screen"/>
</head>
<body>
<div class="container">

    <form class="form-signin span5" action="${ctx}/mvc/main/auth/admin-authorization" method="get">
        <h2 class="form-signin-heading">Доборо пожаловать</h2>
        <input type="text" class="input-block-level" name="user" placeholder="Имя">
        <input type="password" class="input-block-level" name="pass" placeholder="Пароль">
        <label class="checkbox">
            <input type="checkbox" name="_spring_security_remember_me" value="remember-me"> Запомнить меня
        </label>
        <button class="btn btn-primary" type="submit">Вход</button>
    </form>
</div>
</body>
</html>
