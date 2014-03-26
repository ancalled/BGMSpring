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
    <title>Корректировка композиции</title>


</head>
<body>

<c:import url="../navbar.jsp">
    <c:param name="index" value="active"/>
</c:import>


<div class="container">


    <div class="row text-left">
        <legend>
            Редактирование композиции
        </legend>
    </div>
    <div class="span4">

        <form action="${ctx}/mvc/admin/update-track" method="post">
            <input type="hidden" name="id" value="${track.id}">
            <label>
                Код
                <input type="text" name="code" class="input-block-level" value="${track.code}" required="true">
            </label>
            <label>
                Имя
                <input type="text" name="name" class="input-block-level" value="${track.name}" required="true">
            </label>
            <label>
                Артист
                <input type="text" name="artist" class="input-block-level" value="${track.artist}">
            </label>
            <label>
                Автор
                <input type="text" name="composer" class="input-block-level" value="${track.composer}">
            </label>
            <label>
                Мобильный контент
                <input type="text" name="mobile-share" class="input-block-level" value="${track.mobileShare}"
                       required="true">
            </label>
            <label>
                Публичка
                <input type="text" name="public-share" class="input-block-level" value="${track.publicShare}"
                       required="true">
            </label>

            <label>
                Каталог <br>
                <select name="catalog">
                    <c:forEach var="c" items="${catalogs}" varStatus="s">
                        <option value="${c.id}">${c.name}</option>
                    </c:forEach>
                </select>
            </label>
            <br>
            <button class="btn" type="submit">Изменить</button>
        </form>
    </div>

</div>


<c:import url="../footer.jsp"/>


</body>
</html>