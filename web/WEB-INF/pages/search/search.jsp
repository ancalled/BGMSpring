<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<html>
<head>
    <script src="${ctx}/js/jquery.js"></script>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap.css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/temp.css" media="screen"/>

    <title>Поиск</title>
</head>
<body>
<c:import url="../navbar.jsp">
    <c:param name="search" value="active"/>
</c:import>

<div class="container">

    <div class="row">

        <div class="span10">

            <form id="searcher" action="${ctx}/mvc/search/do-search" method="post" class="form-search">

                <input type="hidden" name="from" id="from-p">
                <input type="hidden" name="pageSize" id="till-p">
                <input type="hidden" name="extend" id="extend-search" value="">

                <input type="text" name="q" id="query" class="input-block-level">
                <input type="submit" value="Найти" class="btn">

                <div class="toggler">
                    <a class="accordion-toggle"
                       data-toggle="collapse"
                       data-parent="#accordion2"
                       id="extra"
                       href="#">
                        Параметры поиска
                    </a>
                </div>

                <div id="collapseOne" class="search-params collapse in">
                    <div class="container search-params">

                        <div class="span4">
                            <h4>Поиск</h4>
                            <fieldset>
                                <label for="field-all" class="radio">
                                    <input type="radio" name="type" value="ALL" id="field-all"
                                    ${result.searchType eq 'ALL' or empty result ? 'checked="checked"' : ""}>
                                    по всем полям
                                </label>

                                <label for="field-name" class="radio">
                                    <input type="radio" name="type" value="TRACK" id="field-name"
                                    ${result.searchType eq 'TRACK' ? 'checked="checked"' : ""}>
                                    по названию трека
                                </label>

                                <label for="field-artist" class="radio separated">
                                    <input type="radio" name="type" value="ARTIST" id="field-artist"
                                    ${result.searchType eq 'ARTIST' ? 'checked="checked"' : ""}>
                                    по артисту
                                </label>

                                <label for="field-composer" class="radio">
                                    <input type="radio" name="type" value="COMPOSER" id="field-composer"
                                    ${result.searchType eq 'COMPOSER' ? 'checked="checked"' : ""}>
                                    по композитору
                                </label>

                                <label for="field-code" class="radio">
                                    <input type="radio" name="type" value="CODE" id="field-code"
                                    ${result.searchType eq 'CODE' ? 'checked="checked"' : ""}>
                                    по коду
                                </label>

                                <label for="field-artist-track" class="radio separated">
                                    <input type="radio" name="type" value="ARTIST_TRACK" id="field-artist-track"
                                    ${result.searchType eq 'ARTIST_TRACK' ? 'checked="checked"' : ""}>
                                    по артисту и треку (через «;»)
                                </label>

                                <label for="field-composer-track" class="radio">
                                    <input type="radio" name="type" value="COMPOSER_TRACK" id="field-composer-track"
                                    ${result.searchType eq 'COMPOSER_TRACK' ? 'checked="checked"' : ""}>
                                    по композитору и треку (через «;»)
                                </label>


                            </fieldset>
                        </div>


                        <div class="span6">
                            <h4>Фильтр по каталогам</h4>

                            <div class="row">
                                <c:forEach var="p" items="${platforms}">
                                    <div class="span2">
                                        <div class="catalog-title">${p.name}</div>
                                        <fieldset>
                                            <c:forEach var="c" items="${p.catalogs}" varStatus="loop">
                                                <c:set var="checked" value="yes"/>
                                                <c:if test="${not empty result}">
                                                    <c:set var="checked" value="no"/>
                                                    <c:forEach var="cid" items="${result.catalogIds}">
                                                        <c:if test="${cid == c.id}">
                                                            <c:set var="checked" value="yes"/>
                                                        </c:if>
                                                    </c:forEach>
                                                </c:if>

                                                <label for="checkbox-${loop.index}" class="checkbox">
                                                    <input type="checkbox"
                                                           name="catalogs" value="${c.id}"
                                                           id="checkbox-${loop.index}" ${checked eq 'yes'? 'checked="checked"' : ''}>
                                                        ${c.name}
                                                </label>
                                            </c:forEach>
                                        </fieldset>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>

                    </div>

                </div>
            </form>
        </div>

    </div>

    <div class="row">

        <div class="span10 search-result">
            <c:if test="${not empty result}">

                <legend>
                    Результат поиска (${fn:length(result.tracks) gt 99 ? 'больше 100' : fn:length(result.tracks)}
                    треков)
                </legend>


                <table class="table">
                    <thead>
                    <tr>
                            <%--<th>#</th>--%>
                        <th class="score"></th>
                        <th>Код</th>
                        <th>Композиция</th>
                        <th>Исполнитель</th>
                        <th>Авторы</th>
                        <th>Мобильный контент</th>
                        <th>Публичка</th>
                        <th>Каталог</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>


                    <c:forEach var="g" items="${result.groups}" varStatus="s">
                        <c:forEach var="r" items="${g.tracks}" varStatus="loop">
                            <tr class="${loop.index gt 0 ? 'same-track' : ''} ${r.score eq bestScore ? 'best-score' : ''}">
                                <td class="score">
                                    <fmt:formatNumber type="number" pattern="##.##" value="${r.score}"/>
                                </td>
                                <td>${r.track.code}</td>
                                <td>${r.track.name}</td>
                                <td>${r.track.artist}</td>
                                <td>${r.track.composer}</td>
                                <td>${r.track.mobileShare}</td>
                                <td>${r.track.publicShare}</td>
                                <td>
                                <span id="catalog-${r.track.id}"
                                      class="catalog ${fn:toLowerCase(r.track.foundCatalog.rightType)}">
                                      <%--style="background: <c:out value="${colorMap[r.track.catalog]}"/>">--%>
                                        ${r.track.catalog}
                                </span>
                                </td>
                                <td class="light" id=${r.track.id}><i class="icon-wrench"></i></td>
                            </tr>
                        </c:forEach>
                    </c:forEach>

                    </tbody>
                </table>

            </c:if>
        </div>
    </div>

    <hr/>
</div>


<script>

    var typeEl = document.getElementById('type');
    //    var searchForm = document.getElementById("searcher");
    var from_page_input = document.getElementById("from-p");
    var till_page_input = document.getElementById("till-p");
    var collapse = $('#collapseOne');
    var extend = $('#extend-search');

    var search_input = document.getElementById("query");
    var query = getParameterByName('q');
    search_input.value = query;

    updateParams();
    extendedSearchUpdate();

    $('#extra').click(function () {
        if (collapse.is(":visible")) {
            collapse.hide();
            extend.val('false');
        } else {
            collapse.show();
            extend.val('true');
        }

    });


    function extendedSearchUpdate() {
        var isExtended = getParameterByName('extend');
        var collapse = $('#collapseOne');
        if (isExtended != null) {
            if (isExtended == 'true') {
                collapse.show();
                extend.val('true');
            } else {
                collapse.hide();
                extend.val('false');
            }
        }
    }

    function updateParams() {
        $('[type=checkbox]').each(function () {
            var param = getParameterByName((this).name);
            if (param == this.value) {
                $(this).prop('checked', true);
            }
        });

        var field = getParameterByName('field');
        var filedEmpty = true;
        $('[type=radio]').each(function () {
            if (this.value == field) {
                filedEmpty = false;
                $(this).prop('checked', true);
            }
        });

        if (filedEmpty == true) {
            $('#all-field').prop('checked', true);
        }


    }

    function getParameterByName(name) {
        name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var regexS = "[\\?&]" + name + "=([^&#]*)";
        var regex = new RegExp(regexS);
        var results = regex.exec(window.location.search);
        if (results == null)
            return "";
        else
            return decodeURIComponent(results[1].replace(/\+/g, " "));
    }

    var lightElements = document.getElementsByClassName("light");

    for (var i = 0; i < lightElements.length; i++) {

        lightElements[i].onmouseover = function () {
            this.className = 'dark';

        };

        lightElements[i].onmouseout = function () {
            this.className = 'light';
        };


        lightElements[i].onmousedown = function () {
            var catalogElement = document.getElementById("catalog-" + this.id);
            window.location.replace(
                            "/admin/view/edit-track?id=" + this.id + "&catalog=" + $.trim(catalogElement.textContent));
        }
    }


    var submitParam = getParameterByName('submit');
    if (submitParam == 'true') {
        document.getElementById('searcher').submit();
    }

</script>
<%--<c:import url="footer.jsp"/>--%>

</body>
</html>