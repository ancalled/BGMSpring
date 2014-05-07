<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<html>
<head>
    <script src="${ctx}/js/jquery.js"></script>
    <script src="${ctx}/js/bootstrap.js"></script>
    <script src="${ctx}/js/bootstrap-tab.js"></script>
    <script src="${ctx}/js/bootstrap-fileupload.js"></script>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap.css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap-fileupload.css" media="screen"/>
    <title>Обновление каталога</title>
    <style>
        table.smallcaps {
            font-size: 10pt;
        }

        li.deleted {
            text-decoration: line-through;
            color: #8f6e5f;
        }

        .invariant {
            color: #1a6383;
        }

        ul.bottom-aligned li {
            vertical-align: top;
        }

        .apply-btn {
            margin: 20px 0 0 0;
        }

        div.apply-pane {
            padding: 10px 0 5px 0;
            background-color: #edf9f9;
            width: 60%;
            margin: 5px 0 15px 0;
        }

        .disabled {
            pointer-events: none;
            cursor: default;
        }

        ul.warnings {
            height: 200px;
            overflow: scroll;
        }


        #progress {
            width: 300px;
        }

        .bar {
            height: 10px;
            background: green;
        }
    </style>
</head>
<body>

<c:import url="../navbar.jsp">
    <c:param name="index" value="active"/>
</c:import>

<div class="container">
<div class="row">
<div class="span12">

<section>
    <h4>Обновления по каталогу ${catalog.name}</h4>

    <div class="apply-pane">

        <dl class="unstyled dl-horizontal">
            <dt>Файл с обновлением</dt>
            <dd>${update.fileName}</dd>
            <dt>Всего треков:</dt>
            <dd>
                <strong>
                <fmt:formatNumber type="number" maxFractionDigits="3" value="${update.tracks}"/>
                </strong>
                /  <fmt:formatNumber type="number" maxFractionDigits="3" value="${catalog.tracks}"/> в каталоге
            </dd>

            <dt>Новых:</dt>
            <dd><fmt:formatNumber type="number" maxFractionDigits="3" value="${update.newTracks}"/></dd>

            <dt>Изменилось:</dt>
            <dd><fmt:formatNumber type="number" maxFractionDigits="3" value="${update.changedTracks}"/></dd>

            <dt>Сменилась доля у:</dt>
            <dd><fmt:formatNumber type="number" maxFractionDigits="3" value="${update.rateChangedTracks}"/></dd>


            <dt>Не изменились:</dt>
            <dd><fmt:formatNumber type="number" maxFractionDigits="3" value="${update.crossing - update.changedTracks}"/></dd>


            <dt></dt>
            <dd>
                <c:choose>
                    <c:when test="${update.applied}">
                        <strong>Применен</strong>
                    </c:when>
                    <c:otherwise>
                        <%--<c:if test="${update.status == 'OK'}">--%>
                        <%--<form action="../action/apply-catalog-update" method="post">--%>
                            <%--<input class="btn btn-small btn-primary apply-btn" type="submit"--%>
                                   <%--value="Применить!"> --%>
                            <button id="apply-submit-btn" class="btn btn-small btn-primary apply-btn">Применить!</button>
                            <input type="hidden" name="id" value="${update.id}">

                            <div id="progress" class="progress progress-success progress-striped">
                                <div class="bar" style="width: 0%;"></div>
                            </div>
                            <div id="status-bar"></div>
                        <%--</form>--%>
                        <%--</c:if>--%>
                    </c:otherwise>
                </c:choose>

            </dd>
        </dl>

    </div>

</section>

<c:if test="${!update.applied}">
<section>
<c:if test="${update.status == 'HAS_WARNINGS'}">
    <h4>Ошибки (${fn:length(update.warnings)}):</h4>

    <ul class="unstyled warnings">
        <c:forEach items="${update.warnings}" var="w">
            <li>Строка <strong>${w.row}</strong> поле <strong>${w.column}</strong>: ${w.message}
            </li>
        </c:forEach>
    </ul>

</c:if>

<h4>Изменения ${from + 1} &ndash; ${from + pageSize}</h4>

<br>

<div class="tabbable" style="margin-bottom: 18px;">
<ul class="nav nav-tabs">
    <c:choose>
        <c:when test="${tab1!='active'&&tab2!='active'}">
            <li class=""><a href="#tab1" data-toggle="tab">Имзененные треки</a></li>
            <li class=""><a href="#tab2" data-toggle="tab">Новые треки</a></li>
        </c:when>
        <c:otherwise>
            <li class="${tab1 == 'active'? 'active' : ''}"><a href="#tab1" data-toggle="tab">Имзененные треки</a></li>
            <li class="${tab2 == 'active'? 'active' : ''}"><a href="#tab2" data-toggle="tab">Новые треки</a></li>
        </c:otherwise>
    </c:choose>


</ul>
<div class="tab-content" style="padding-bottom: 9px; border-bottom: 1px solid #ddd;">
<div class="tab-pane ${tab1 == 'active'? 'active' : ''}" id="tab1">

<div class="pagination pagination-centered">
    <ul>
        <c:choose>
            <c:when test="${from >= pageSize}">
                <li><a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from=0&page=0&active-tab=tab1">&laquo;</a></li>
            </c:when>
            <c:otherwise>
                <li class="disabled"><a href="#">&laquo;</a></li>
            </c:otherwise>
        </c:choose>

        <c:set var="left" value="4"/>
        <c:set var="right" value="6"/>
        <c:set var="idx_right" value="0"/>

        <c:if test="${page}==0">
            <c:set var="page" value="1"/>
        </c:if>

        <c:set var="pages_end" value="${(update.crossing / pageSize) + 1}"/>

        <c:forEach var="i" begin="1" end="${pages_end}" step="1"
                   varStatus="status">

            <c:choose>
                <c:when test="${from == (i - 1) * pageSize}">
                    <li class="active">
                        <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from=${(i - 1) * pageSize}&page=${i}&active-tab=tab1">${i}</a>
                    </li>
                </c:when>
                <c:otherwise>

                    <c:if test="${pages_end - page<=right}">
                        <c:set var="right" value="${right-1}"/>
                        <c:set var="left" value="${left+1}"/>
                    </c:if>

                    <c:if test="${page-i<=left&&page-i>0}">
                        <li>
                            <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from=${(i - 1) * pageSize}&page=${i}&active-tab=tab1">${i}</a>
                        </li>
                    </c:if>

                    <c:if test="${page<=left}">
                        <c:set var="right" value="${right+1}"/>
                        <c:set var="left" value="${left-1}"/>
                    </c:if>

                    <c:if test="${i-page<right&&i-page>0}">
                        <li>
                            <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from=${(i - 1) * pageSize}&page=${i}&active-tab=tab1">${i}</a>
                        </li>
                    </c:if>

                </c:otherwise>
            </c:choose>


        </c:forEach>
        <fmt:formatNumber var="pages_end"
                          value="${update.tracks/pageSize}"
                          maxFractionDigits="0"/>
        <li>
            <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from=${update.crossing}&page=${pages_end}&active-tab=tab1">&raquo;</a>
        </li>

    </ul>
</div>
<table class="table smallcaps">
    <thead>
    <tr>
        <th>#</th>
        <th>Код</th>
        <th>Композиция</th>
        <th>Исполнитель</th>
        <th>Авторы</th>
        <th>Мобильный контент</th>
        <th>Публичка</th>
    </tr>
    </thead>
    <tbody>

    <c:forEach items="${diffs}" var="d">
        <tr>
            <td class="invariant">${d.number + 1}</td>
            <td class="invariant">${d.code}</td>
            <td>
                <c:if test="${d.oldTrack.name != d.newTrack.name}">
                    <ul class="unstyled">
                        <li class="deleted">${d.oldTrack.name}</li>
                        <li>${d.newTrack.name}</li>
                    </ul>
                </c:if>
            </td>
            <td>
                <c:if test="${d.oldTrack.artist != d.newTrack.artist}">
                    <ul class="unstyled">
                        <li class="deleted">${d.oldTrack.artist}</li>
                        <li>${d.newTrack.artist}</li>
                    </ul>
                </c:if>
            </td>
            <td>
                <c:if test="${d.oldTrack.composer != d.newTrack.composer}">
                    <ul class="unstyled">
                        <li class="deleted">${d.oldTrack.composer}</li>
                        <li>${d.newTrack.composer}</li>
                    </ul>
                </c:if>
            </td>
            <td>
                <c:if test="${d.oldTrack.mobileShare != d.newTrack.mobileShare}">
                    <ul class="unstyled">
                        <li class="deleted">${d.oldTrack.mobileShare}</li>
                        <li>${d.newTrack.mobileShare}</li>
                    </ul>
                </c:if>
            </td>
            <td>
                <c:if test="${d.oldTrack.publicShare != d.newTrack.publicShare}">
                    <ul class="unstyled">
                        <li class="deleted">${d.oldTrack.publicShare}</li>
                        <li>${d.newTrack.publicShare}</li>
                    </ul>
                </c:if>
            </td>
        </tr>
    </c:forEach>

    </tbody>
</table>

<div class="pagination pagination-centered">
    <ul>
        <c:choose>
            <c:when test="${from >= pageSize}">
                <li><a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from=0&page=0&active-tab=tab1">&laquo;</a></li>
            </c:when>
            <c:otherwise>
                <li class="disabled"><a href="#">&laquo;</a></li>
            </c:otherwise>
        </c:choose>

        <c:set var="left" value="4"/>
        <c:set var="right" value="6"/>
        <c:set var="idx_right" value="0"/>

        <c:if test="${page}==0">
            <c:set var="page" value="1"/>
        </c:if>

        <c:set var="pages_end" value="${(update.crossing / pageSize) + 1}"/>

        <c:forEach var="i" begin="1" end="${pages_end}" step="1"
                   varStatus="status">

            <c:choose>
                <c:when test="${from == (i - 1) * pageSize}">
                    <li class="active">
                        <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from=${(i - 1) * pageSize}&page=${i}&active-tab=tab1">${i}</a>
                    </li>
                </c:when>
                <c:otherwise>

                    <c:if test="${pages_end - page<=right}">
                        <c:set var="right" value="${right-1}"/>
                        <c:set var="left" value="${left+1}"/>
                    </c:if>

                    <c:if test="${page-i<=left&&page-i>0}">
                        <li>
                            <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from=${(i - 1) * pageSize}&page=${i}&active-tab=tab1">${i}</a>
                        </li>
                    </c:if>

                    <c:if test="${page<=left}">
                        <c:set var="right" value="${right+1}"/>
                        <c:set var="left" value="${left-1}"/>
                    </c:if>

                    <c:if test="${i-page<right&&i-page>0}">
                        <li>
                            <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from=${(i - 1) * pageSize}&page=${i}&active-tab=tab1">${i}</a>
                        </li>
                    </c:if>

                </c:otherwise>
            </c:choose>


        </c:forEach>
        <fmt:formatNumber var="pages_end"
                          value="${update.tracks/pageSize}"
                          maxFractionDigits="0"/>
        <li>
            <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from=${update.crossing}&page=${pages_end}&active-tab=tab1">&raquo;</a>
        </li>

    </ul>
</div>

</div>
<div class="tab-pane ${tab2 == 'active'? 'active' : ''}" id="tab2">

<H4>Все загруженные композиции</H4>

    <%--<div class="pagination pagination-centered">--%>
    <%--<ul>--%>
    <%--<c:choose>--%>
    <%--<c:when test="${fromNew >= pageSize}">--%>
    <%--<li>--%>
    <%--<a href="catalog-update?id=${update.id}&from-new=${fromNew - pageSize}&active-tab=tab2">&laquo;</a>--%>
    <%--</li>--%>
    <%--</c:when>--%>
    <%--<c:otherwise>--%>
    <%--<li class="disabled"><a href="#">&laquo;</a></li>--%>
    <%--</c:otherwise>--%>
    <%--</c:choose>--%>
    <%----%>
    <%--<c:forEach var="page_idx" begin="1" end="${update.tracks / pageSize + 1}" step="1"--%>
    <%--varStatus="status">--%>
    <%--<li class="${fromNew == (page_idx - 1) * pageSize ? 'active' : ''}">--%>
    <%--<a href="catalog-update?id=${update.id}&from-new=${(page_idx - 1) * pageSize}&active-tab=tab2">${page_idx}</a>--%>
    <%--</li>--%>
    <%--</c:forEach>--%>
    <%----%>
    <%--<c:choose>--%>
    <%--<c:when test="${fromNew + pageSize < fn:length(tracks)}">--%>
    <%--<li>--%>
    <%--<a href="catalog-update?id=${update.id}&from-new=${fromNew + pageSize}">&raquo;</a>--%>
    <%--</li>--%>
    <%--</c:when>--%>
    <%--<c:otherwise>--%>
    <%--<li class="disabled"><a href="#">&raquo;</a></li>--%>
    <%--</c:otherwise>--%>
    <%--</c:choose>--%>
    <%--</ul>--%>
    <%--</div>--%>

<div class="pagination pagination-centered">
    <ul>
        <c:choose>
            <c:when test="${fromNew >= pageSize}">
                <li><a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from-new=0&page=0&active-tab=tab2">&laquo;</a></li>
            </c:when>
            <c:otherwise>
                <li class="disabled"><a href="#">&laquo;</a></li>
            </c:otherwise>
        </c:choose>

        <c:set var="left" value="4"/>
        <c:set var="right" value="6"/>
        <c:set var="idx_right" value="0"/>

        <c:if test="${page}==0">
            <c:set var="page" value="1"/>
        </c:if>

        <c:set var="pages_end" value="${(update.tracks / pageSize) + 1}"/>

        <c:forEach var="i" begin="1" end="${pages_end}" step="1"
                   varStatus="status">

            <c:choose>
                <c:when test="${fromNew == (i - 1) * pageSize}">
                    <li class="active">
                        <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from-new=${(i - 1) * pageSize}&page=${i}&active-tab=tab2">${i}</a>
                    </li>
                </c:when>
                <c:otherwise>

                    <c:if test="${pages_end - page<=right}">
                        <c:set var="right" value="${right-1}"/>
                        <c:set var="left" value="${left+1}"/>
                    </c:if>

                    <c:if test="${page-i<=left&&page-i>0}">
                        <li>
                            <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from-new=${(i - 1) * pageSize}&page=${i}&active-tab=tab2">${i}</a>
                        </li>
                    </c:if>

                    <c:if test="${page<=left}">
                        <c:set var="right" value="${right+1}"/>
                        <c:set var="left" value="${left-1}"/>
                    </c:if>

                    <c:if test="${i-page<right&&i-page>0}">
                        <li>
                            <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from-new=${(i - 1) * pageSize}&page=${i}&active-tab=tab2">${i}</a>
                        </li>
                    </c:if>

                </c:otherwise>
            </c:choose>


        </c:forEach>
        <fmt:formatNumber var="pages_end"
                          value="${update.tracks/pageSize}"
                          maxFractionDigits="0"/>
        <li>
            <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from-new=${update.tracks}&page=${pages_end}&active-tab=tab2">&raquo;</a>
        </li>

    </ul>
</div>


<table class="table smallcaps">
    <thead>
    <tr>
            <%--<th>#</th>--%>
        <th>Код</th>
        <th>Композиция</th>
        <th>Исполнитель</th>
        <th>Авторы</th>
        <th>Мобильный контент</th>
        <th>Публичка</th>
    </tr>
    </thead>
    <tbody>

    <c:forEach items="${tracks}" var="d">
        <tr>
                <%--<td class="invariant">${d.number + 1}</td>--%>
            <td class="invariant">${d.code}</td>
            <td>${d.name}</td>
            <td>${d.artist}</td>
            <td>${d.composer}</td>
            <td>${d.mobileShare}</td>
            <td>${d.publicShare}</td>
        </tr>
    </c:forEach>

    </tbody>
</table>

<div class="pagination pagination-centered">
    <ul>
        <c:choose>
            <c:when test="${fromNew >= pageSize}">
                <li><a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from-new=0&page=0&active-tab=tab2">&laquo;</a></li>
            </c:when>
            <c:otherwise>
                <li class="disabled"><a href="#">&laquo;</a></li>
            </c:otherwise>
        </c:choose>

        <c:set var="left" value="4"/>
        <c:set var="right" value="6"/>
        <c:set var="idx_right" value="0"/>

        <c:if test="${page}==0">
            <c:set var="page" value="1"/>
        </c:if>

        <c:set var="pages_end" value="${(update.tracks / pageSize) + 1}"/>

        <c:forEach var="i" begin="1" end="${pages_end}" step="1"
                   varStatus="status">

            <c:choose>
                <c:when test="${fromNew == (i - 1) * pageSize}">
                    <li class="active">
                        <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from-new=${(i - 1) * pageSize}&page=${i}&active-tab=tab2">${i}</a>
                    </li>
                </c:when>
                <c:otherwise>

                    <c:if test="${pages_end - page<=right}">
                        <c:set var="right" value="${right-1}"/>
                        <c:set var="left" value="${left+1}"/>
                    </c:if>

                    <c:if test="${page-i<=left&&page-i>0}">
                        <li>
                            <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from-new=${(i - 1) * pageSize}&page=${i}&active-tab=tab2">${i}</a>
                        </li>
                    </c:if>

                    <c:if test="${page<=left}">
                        <c:set var="right" value="${right+1}"/>
                        <c:set var="left" value="${left-1}"/>
                    </c:if>

                    <c:if test="${i-page<right&&i-page>0}">
                        <li>
                            <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from-new=${(i - 1) * pageSize}&page=${i}&active-tab=tab2">${i}</a>
                        </li>
                    </c:if>

                </c:otherwise>
            </c:choose>


        </c:forEach>
        <fmt:formatNumber var="pages_end"
                          value="${update.tracks/pageSize}"
                          maxFractionDigits="0"/>
        <li>
            <a href="${ctx}/mvc/catalog-update/catalog-update?id=${update.id}&from-new=${update.tracks}&page=${pages_end}&active-tab=tab2">&raquo;</a>
        </li>

    </ul>
</div>

</div>
</div>
</div>
<!-- /tabbable -->


</section>

</c:if>

<hr/>

</div>
</div>

</div>

<c:import url="../footer.jsp"/>


<script>
    var updateId = ${update.id}; //inline during page generation

    $(document).ready(function () {
        $('#apply-submit-btn').click(function() {
            $.ajax({
                url: '${ctx}/mvc/catalog-update/apply-catalog-update',
                dataType: 'json',
                method: 'post',
                async: 'true',
                data: {'uid': updateId},

                success: function (data) {
                    if (data.status = 'ok') {
                        $('#status-bar').html("<strong>Загруженные изменения применяются к каталогу...</strong>");
                        startTaskStatusChecker(updateId);
                    }
                },

                error: function (jqXHR, textStatus, errorThrown) {
                    console.log('Got error: ' + textStatus + ', ' + errorThrown);
                    $('#status-bar').html("<strong>Ошибка!</strong>");
                }
            });
        });

    });



    function startTaskStatusChecker(updateId) {
        var stop = false;
        (function worker() {
            $.ajax({
                url: '/admin/action/check-apply-status',
                dataType: 'json',
                method: 'get',
                async: 'true',
                data: {'uid': updateId},

                success: function (data) {
                    if (data.status == 'found') {
                        var taskStatus = data.taskStatus;
                        console.log('Got task-status: ' + taskStatus);

                        if (taskStatus == 'APPLY_CATALOG_STEP1') {

                        } else if (taskStatus == 'APPLY_CATALOG_STEP2') {
                            $('#status-bar').html("<strong>Загруженные изменения применяются к каталогу...</strong>");
                            $('#progress .bar').css('width', '25%');

                        } else if (taskStatus == 'APPLY_CATALOG_STEP3') {
                            $('#status-bar').html("<strong>Загруженные изменения применяются к каталогу...</strong>");
                            $('#progress .bar').css('width', '50%');

                        } else if (taskStatus == 'INDEX_REBUILD') {
                            $('#status-bar').html("<strong>Загруженные изменения применяются к каталогу...</strong>");
                            $('#progress .bar').css('width', '75%');

                        } else if (taskStatus == 'FINISHED') {
                            $('#status-bar').html("<strong>Обновление закончено.</strong>");
                            stop = true;
                            $('#progress .bar').css('width', '100%');
                            $('#apply-submit-btn').remove();

//                            window.location.href = '/admin/view/catalog?id=' + catId;
                        }
                    } else {
                        $('#status-bar').html("<strong>Задача " + updateId + " не найдена!</strong>");
                        stop = true;
                    }

                },
                complete: function () {
                    // Schedule the next request when the current one's complete
                    if (!stop) {
                        setTimeout(worker, 2000);
                    }
                },

                error: function (jqXHR, textStatus, errorThrown) {
                    console.log(textStatus, errorThrown);
                    stop = true;
                }
            });
        })();
    }
</script>

</body>
</html>