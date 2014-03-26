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
    <title>Новая компания</title>
</head>
<body>

<c:import url="../navbar.jsp">
    <c:param name="customers" value="active"/>
</c:import>

<div class="container" style="height: 80%">
    <div class="row text-left">
        <h4>
            Новая компания
        </h4>
    </div>
    <div class="span4">

        <form action="${ctx}/mvc/admin/create-customer" method="post" class="form-horizontal">

            <div class="control-group">
                <label class="control-label" for="name">Наименование</label>

                <div class="controls">
                    <input type="text" id="name" name="name">
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="type">Вид деятельности</label>

                <div class="controls">
                    <select id="type" name="type" class="input-block-level">
                        <option value="MOBILE_AGGREGATOR">Мобильный агрегатор</option>
                        <option value="PUBLIC_RIGHTS_SOCIETY">Организация по коллективному управлению</option>
                        <option value="OTHER">Другое</option>
                    </select>
                </div>
            </div>


            <div class="control-group">
                <label class="control-label" for="rights">Тип прав</label>

                <div class="controls">
                    <select id="rights" name="rights" class="input-block-level">
                        <option value="AUTHOR">Авторские</option>
                        <option value="RELATED">Публичка</option>
                        <option value="AUTHOR_RELATED">Авторские и смежные</option>
                    </select>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="share-author">Доля по авторским правам</label>

                <div class="controls">
                    <input type="number" id="share-author" name="authorRoyalty" placeholder="0">
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="share-related">Доля по смежным правам</label>

                <div class="controls">
                    <input type="number" id="share-related" name="relatedRoyalty" placeholder="0">
                </div>
            </div>


            <div class="control-group">
                <div class="controls">
                    <button type="submit" class="btn">Создать</button>
                </div>
            </div>
        </form>
    </div>

    <style type="text/css">
        input#share {
            height: 27
        }
    </style>
</div>

<c:import url="../footer.jsp"/>

</body>
</html>