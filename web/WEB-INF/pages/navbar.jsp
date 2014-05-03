<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>


<div class="navbar">
    <div class="navbar-inner">
        <a class="brand" href="${ctx}/mvc/main/index">BGM Platform</a>
        <ul class="nav">
            <li class="${param.index}"><a href="${ctx}/mvc/main/index">Главная</a></li>
            <%--<li class="${param.catalogs}"><a href="/admin/view/catalogs">Каталоги</a></li>--%>
            <li class="${param.reports}"><a href="${ctx}/mvc/reports/reports">Отчеты</a></li>
            <li class="${param.search}"><a href="${ctx}/mvc/search/search">Поиск</a></li>
            <li class="${param.massSearch}"><a href="${ctx}/mvc/main/mass-search">Массовый поиск</a></li>
            <li class="${param.customers}"><a href="${ctx}/mvc/admin/customers">Пользователи</a></li>
            <li><a href="${ctx}/mvc/auth/logout">Выход</a></li>
        </ul>
    </div>
</div>
