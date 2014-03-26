<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>


<html>
<head>
    <script src="${ctx}/js/jquery.js"></script>
    <script src="${ctx}/js/bootstrap.js"></script>
    <script src="${ctx}/js/bootstrap-fileupload.js"></script>
    <script src="${ctx}/js/plugin-parser.js"></script>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap.css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap-fileupload.css" media="screen"/>
    <title>Создание пользователя</title>
</head>
<body>

<c:import url="../navbar.jsp">
    <c:param name="customers" value="active"/>
</c:import>



<div class="container">


    <div class="row text-left">
        <h4>
            ${customer.name} | Создание пользователя
        </h4>
    </div>
    <div class="span4">

        <form action="${ctx}/mvc/admin/create-user" method="post">
            <label>
                Имя
                <input type="text" name="login" class="input-block-level" required="true">
            </label>
            <label>
                Пароль
                <input type="password" name="pass" class="input-block-level" required="true">
            </label>
            <label>
                Полное имя
                <input type="text" name="full_name" class="input-block-level" required="false">
            </label>
            <label>
                Электронная почта
                <input type="text" name="email" class="input-block-level" required="false">
            </label>

            <input type="hidden" name="customer-id" id="customer" value="${customer.id}"/>
            <button class="btn" type="submit">Создать</button>
        </form>
    </div>



</div>

<c:import url="../footer.jsp"/>

</body>
</html>