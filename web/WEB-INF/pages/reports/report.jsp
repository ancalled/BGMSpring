<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<html xmlns="http://www.w3.org/1999/html">

<head>
    <script src="${ctx}/js/jquery.js"></script>
    <script src="${ctx}/js/bootstrap.js"></script>
    <%--<script src="/js/bootstrap-fileupload.js"></script>--%>
    <%--<script src="/js/bootstrap-datetimepicker.min.js"></script>--%>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap.css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap-fileupload.css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap-datetimepicker.min.css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap-responsive.css" media="screen"/>
    <title>Главная</title>
    <style>
        table.smallcaps {
            font-size: 10pt;
        }

        .invariant {
            color: #1a6383;
        }

        ul.bottom-aligned li {
            vertical-align: top;
        }

        .disabled {
            pointer-events: none;
            cursor: default;
        }

        tr.same-track td {
            border-top: none;
            /*padding: 5px 0 0 0;*/
            line-height: 15px;
        }

        td.number {
            text-align: right;
            padding-right: 20px;
        }

        .nonwrapped {
            white-space: nowrap;
        }

        .action-panel {
            padding-top: 10px;
        }

        .sent {
            color: #114713;
        }

        .edit {
            color: #8f8e8e;
        }

        td.author {
            background: rgba(183, 107, 106, 0.06);
        }

        td.related {
           background: rgba(22, 134, 140, 0.06);
        }
    </style>

</head>

<body>

<c:import url="../navbar.jsp">
    <c:param name="reports" value="active"/>
</c:import>



<div class="container">
<section>
    <div class="row">
        <div class="span8">
            <h4>
                <c:choose>
                    <c:when test="${report.type == 'MOBILE'}">
                        Отчет по мобильному контенту
                    </c:when>
                    <c:when test="${report.type == 'PUBLIC'}">
                        Отчет по публичному исполнению
                    </c:when>
                </c:choose>
            </h4>

            <dl class="dl-horizontal">
                <dt>Пользователь</dt>
                <dd>${customer.name}</dd>

                <dt>За период</dt>
                <dd>${report.startDate}</dd>

                <dt>Доля (авт./смежн.)</dt>
                <dd>${customer.authorRoyalty}% / ${customer.relatedRoyalty}%</dd>


                <dt>Треков</dt>
                <dd>${report.tracks} / ${fn:length(items)}</dd>
                <dt>Статус</dt>
                <dd>
                    <c:choose>
                        <c:when test="${report.accepted}">
                            <span class="sent">Отправлен</span>
                        </c:when>
                        <c:otherwise>
                            <span class="edit">Редактируется</span>
                        </c:otherwise>
                    </c:choose>
                </dd>
            </dl>
        </div>

        <div class="span4">
            <form action="${ctx}/mvc/reports/accept-report" method="post">
                <c:if test="${not report.accepted}">
                    <div class="btn-group action-panel">
                        <input type="hidden" name="reportId" value="${report.id}">
                        <button class="btn btn-primary" type="submit">Отправить</button>
                        <button class="btn disabled">Отменить удаление</button>
                    </div>
                </c:if>
            </form>
        </div>

    </div>

</section>

<section>

<div class="pagination pagination-centered">
    <ul id="pages">
        <c:choose>
            <c:when test="${from >= size}">
                <li><a href="${ctx}/mvc/reports/report?id=${report.id}&from=0&page=0">&laquo;</a></li>
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

        <c:set var="pages_end" value="${(fn:length(items) / size) + 1}"/>

        <c:forEach var="i" begin="1" end="${pages_end}" step="1"
                   varStatus="status">

            <c:choose>
                <c:when test="${from == (i - 1) * size}">
                    <li class="active">
                        <a href="${ctx}/mvc/reports/report?id=${report.id}&from=${(i - 1) * size}&page=${i}">${i}</a>
                    </li>
                </c:when>
                <c:otherwise>

                    <c:if test="${pages_end - page<=right}">
                        <c:set var="right" value="${right-1}"/>
                        <c:set var="left" value="${left+1}"/>
                    </c:if>

                    <c:if test="${page-i<=left&&page-i>0}">
                        <li>
                            <a href="${ctx}/mvc/reports/report?id=${report.id}&from=${(i - 1) * size}&page=${i}">${i}</a>
                        </li>
                    </c:if>

                    <c:if test="${page<=left}">
                        <c:set var="right" value="${right+1}"/>
                        <c:set var="left" value="${left-1}"/>
                    </c:if>

                    <c:if test="${i-page<right&&i-page>0}">
                        <li>
                            <a href="${ctx}/mvc/reports/report?id=${report.id}&from=${(i - 1) * size}&page=${i}">${i}</a>
                        </li>
                    </c:if>

                </c:otherwise>
            </c:choose>


        </c:forEach>
        <fmt:formatNumber var="pages_end"
                          value="${fn:length(items)/size}"
                          maxFractionDigits="0"/>
        <li>
            <a href="${ctx}/mvc/reports/report?id=${report.id}&from=${fn:length(items)}&page=${pages_end}">&raquo;</a>
        </li>

    </ul>
</div>

<table class="table smallcaps">
    <thead>
    <tr>
        <th>Номер</th>
        <th>Исполнитель &mdash; трек</th>
        <th class="nonwrapped">Кол-во</th>
        <c:if test="${customer.customerType eq 'MOBILE_AGGREGATOR'}">
            <th>Цена</th>
        </c:if>
        <th></th>
        <th class="nonwrapped">Каталог</th>
        <th>Код</th>
        <th>Доля</th>
        <c:if test="${customer.customerType eq 'MOBILE_AGGREGATOR'}">
            <th>Сумма сбора</th>
        </c:if>
    </tr>
    </thead>
    <tbody>

    <c:set var="lastNum" value="0"/>
    <c:forEach items="${items}" var="i" varStatus="loop">
        <tr class="${i.detected ? '' : 'not-found'} ${lastNum == i.number ? 'same-track' : ''}">
            <td class="invariant number">
                <c:if test="${lastNum != i.number}">
                    ${i.number}
                </c:if>
            </td>
            <td style="width: 30%;">
                <c:if test="${lastNum != i.number}">
                    ${i.artist} &mdash; ${i.track}
                </c:if>
            </td>

            <td class="number">
                <c:if test="${lastNum != i.number}">
                    ${i.qty}
                </c:if>
            </td>

            <c:if test="${customer.customerType eq 'MOBILE_AGGREGATOR'}">
                <td class="number">
                    <c:if test="${lastNum != i.number}">
                        ${i.price}
                    </c:if>
                </td>

            </c:if>


            <c:if test="${i.detected}">
                <c:set var="rt" value="${fn:toLowerCase(i.foundTrack.foundCatalog.rightType)}"/>


                <td style="width: 10%"></td>

                <td class="${rt}">
                    <span class="nonwrapped">
                        ${i.foundTrack.foundCatalog.name}
                    </span>
                </td>

                <td class="${rt}">
                      <span class="nonwrapped"
                            style="padding-left: 10px; font-style: italic">
                          <a href="${ctx}/mvc/admin/edit-track?id=${i.foundTrack.id}"
                             title="${i.foundTrack.artist} &mdash; ${i.foundTrack.name}">
                                  ${i.foundTrack.code}
                          </a>
                      </span>
                </td>

                <td class="number ${rt}">
                    <c:choose>
                        <c:when test="${customer.customerType eq 'MOBILE_AGGREGATOR'}">
                            ${i.foundTrack.mobileShare}%
                        </c:when>
                        <c:when test="${customer.customerType eq 'PUBLIC_RIGHTS_SOCIETY'}">
                            ${i.foundTrack.publicShare}%
                        </c:when>
                    </c:choose>

                </td>

                <c:if test="${customer.customerType eq 'MOBILE_AGGREGATOR'}">
                    <td class="number ${rt}">
                            <c:set var="royalty"
                                   value="${i.foundTrack.foundCatalog.rightType eq 'AUTHOR' ?
                                   customer.authorRoyalty :
                                   customer.relatedRoyalty}"/>
                            <fmt:formatNumber
                                    value="${i.qty * i.price * (i.foundTrack.mobileShare / 100) * (royalty / 100)}"
                                    pattern="###,##0.00"/>
                    </td>

                </c:if>

                <c:if test="${not report.accepted}">
                    <td>
                        <a href="#_" class="remove-track" id="${i.id}"
                           title="Этот трек определился неверно, убрать">
                            <i class="icon-remove"></i>
                        </a>
                    </td>
                </c:if>
            </c:if>

        </tr>

        <c:set var="lastNum" value="${i.number}"/>
    </c:forEach>


    </tbody>
</table>


<div class="pagination pagination-centered">
    <ul id="pages-2">
        <c:choose>
            <c:when test="${from >= size}">
                <li><a href="${ctx}/mvc/reports/report?id=${report.id}&from=0&page=0">&laquo;</a></li>
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

        <c:set var="pages_end" value="${(fn:length(items) / size) + 1}"/>

        <c:forEach var="i" begin="1" end="${pages_end}" step="1"
                   varStatus="status">

            <c:choose>
                <c:when test="${from == (i - 1) * size}">
                    <li class="active">
                        <a href="${ctx}/mvc/reports/report?id=${report.id}&from=${(i - 1) * size}&page=${i}">${i}</a>
                    </li>
                </c:when>
                <c:otherwise>

                    <c:if test="${pages_end - page<right}">
                        <c:set var="right" value="${right-1}"/>
                        <c:set var="left" value="${left+1}"/>
                    </c:if>

                    <c:if test="${page-i<=left&&page-i>0}">
                        <li>
                            <a href="${ctx}/mvc/reports/report?id=${report.id}&from=${(i - 1) * size}&page=${i}">${i}</a>
                        </li>
                    </c:if>

                    <c:if test="${page<=left}">
                        <c:set var="right" value="${right+1}"/>
                        <c:set var="left" value="${left-1}"/>
                    </c:if>

                    <c:if test="${i-page<right&&i-page>0}">
                        <li>
                            <a href="r${ctx}/mvc/reports/eport?id=${report.id}&from=${(i - 1) * size}&page=${i}">${i}</a>
                        </li>
                    </c:if>

                </c:otherwise>
            </c:choose>


        </c:forEach>
        <fmt:formatNumber var="last"
                          value="${fn:length(items)/size}"
                          maxFractionDigits="0"/>
        <li>
            <a href="${ctx}/mvc/reports/report?id=${report.id}&from=${fn:length(items)}&page=${last}">&raquo;</a>
        </li>

    </ul>
</div>
</section>

<div id="track-remove-modal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
     aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myModalLabel">Удаление трека</h3>
    </div>
    <div class="modal-body">
        <p>Удалить строку?</p>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal">Нет</button>
        <button class="btn btn-primary" id="modal-remove-btn" aria-hidden="true">Да</button>
    </div>
</div>

</div>


<script>
    $(document).ready(function () {

        $('a.remove-track').click(function () {
            var a = $(this);
            var itemId = a.attr('id');
            var tr = a.closest('tr');
            var clazz = tr.attr('class');
            $.post('/admin/api/remove-from-report',
                    {report_id: "${report.id}",
                        item_id: itemId,
                        found_track: clazz

                    }, function (data) {
                        console.log(data);
                        if (data.status == 'ok') {
                            $('#' + itemId).parent().parent().hide();
                            window.location.reload();
                        } else if (data.status == 'error') {
                            alert(data.error);
                        }
                    });
        });

//        alert($('#pages li').eq(-2).remove());

    });


</script>


</body>
</html>