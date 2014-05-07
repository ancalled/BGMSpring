<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="ru_RU"  scope="session"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<html xmlns="http://www.w3.org/1999/html">

<head>
    <script src="${ctx}/js/jquery.js"></script>
    <script src="${ctx}/js/bootstrap.js"></script>
    <script src="${ctx}/js/bootstrap-tab.js"></script>

    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap.css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap-fileupload.css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/bootstrap-datetimepicker.min.css" media="screen"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/reports.css" media="screen"/>

    <title>Обработка отчета</title>
</head>


<body>

<c:import url="../navbar.jsp">
    <c:param name="reports" value="active"/>
</c:import>


<div class="container">

    <h3>Пользовательские отчеты</h3>

    <div class="span12">

        <section>
            <div class="months-by-quarters">
                <c:forEach var="year" items="${years}">
                    <div class="year">${year.year}</div>
                    <c:forEach var="quarter" items="${year.quarters}">
                        <c:forEach var="month" items="${quarter.months}">

                            <div class="incoming-report">
                                <div class="margins">
                                    <div class="content ${month.localDate gt now ? 'future' : ''}">
                                        <%--<div class="header"><fmt:formatDate pattern="MMMMM" value="${month.localDate}"/></div>--%>
                                        <div class="header">${month.localDate.month}</div>

                                        <table class="month-reports">
                                            <c:forEach items="${month.reports}" var="r">
                                                <tr class="${r.accepted ? 'active' : 'not-active'}">
                                                    <td class="title">
                                                        <a href="${ctx}/mvc/reports/report?id=${r.id}">
                                                                <%--${r.customer.shortName}--%>
                                                                ${not empty r.customer.shortName ? r.customer.shortName : 'N/A'}
                                                        </a>
                                                    </td>
                                                    <td class="qty">
                                                            ${r.detected}
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </table>

                                        <c:if test="${fn:length(month.reports) eq 0}">
                                            <div class="no-reports">
                                                Не было отчетов
                                            </div>
                                        </c:if>

                                        <div class="send-report">
                                            <button class="btn btn-mini" type="button">Отправить отчет</button>
                                                <%--<a href="#">Отправить отчет</a>--%>
                                        </div>

                                    </div>
                                </div>
                            </div>

                        </c:forEach>


                        <div class="quarter">
                            Q${quarter.number}
                        </div>

                        <div class="outgoing-report">

                            <dl class="dl-horizontal catalogs">
                                    <%--<dt><a href="#">WCH</a></dt>--%>
                                    <%--<dd>323</dd>--%>

                                    <%--<dt><a href="#">NMI Зап</a></dt>--%>
                                    <%--<dd>104</dd>--%>
                            </dl>
                        </div>

                        <div class="clear"></div>
                    </c:forEach>

                </c:forEach>

            </div>
        </section>


    </div>

</div>



<c:import url="../footer.jsp"/>

<%--<script>--%>
    <%--$(document).ready(function () {--%>

        <%--$('#tabs-nav a').click(function (e) {--%>
            <%--e.preventDefault();--%>
            <%--$(this).tab('show');--%>
        <%--});--%>

    <%--});--%>


<%--</script>--%>

<script src="${ctx}/js/bootstrap-fileupload.js"></script>
<script src="${ctx}/js/bootstrap-datetimepicker.min.js"></script>
<script>

    $(document).ready(function () {
        $('#upload-report-form').hide();


        var erParam = getParam("er");
        if (erParam == "Wrong type") {
            alert("Вероятно указан неверный тип отчета");
        }
    });


    //    window.onload(function () {
    //         $('#submitReport').onclick(sendReport());
    //    });

    $('#type-report-change').change(function () {
        var v = $(this).val();
        if (v == 1) {
            $("#report-load-form").attr("action", "/admin/action/load-public-reports");
        }
        else if (v == 2) {
            $("#report-load-form").attr("action", "/admin/action/load-mobile-reports");
        }
    });


    $(function () {
        $('#date').datetimepicker({
            pickTime: true
        });


    });


    function sendReport() {
        $.ajax({
            url: "/admin/action/upload-report",
            dataType: 'json',
            method: 'post',
            async: 'true',

            data: {
                'customer-id': $('#customer').val(),
                'dt': $('#dt').val(),
                'repType': $('#repType').val()
            },
            error: function () {
                alert("Неудалось выгрузить каталог в файл" + $('#catName').val() + " .csv");
            },
            success: function (data) {
                $("#file-link").append("<a href='" + data.path + "'>" +
                        "<i class='icon-download-alt'></i>" + "Скачать " +
                        $('#catName').val() + ".csv (" + Math.round(data.size / 1024 / 1024) + " Мб)" +
                        "</a>");
                $('#loading-gif').css('visibility', 'hidden');
            }
        });
    }

    $('#customer').change(function () {
                var customerType = $('#customer-' + this.options[this.selectedIndex].id).val();
                if (customerType == 'MOBILE_AGGREGATOR') {
                    $('#repType').val('MOBILE_AGGREGATOR');
                } else if (customerType == 'PUBLIC_RIGHTS_SOCIETY') {
                    $('#repType').val('PUBLIC_RIGHTS_SOCIETY');
                }
            }
    );
    //
    //    $('#repType').change(function(){
    //        $('#report-type').val(this.options[this.selectedIndex].id);
    //    });


    var el = document.getElementById('date');

    //    el.on('changeDate', function (e) {
    //        alert(e.date.toString());
    //        console.log(e.localDate.toString());
    //    });


    //    $('#report-tab a:first').tab('show');


    function getParam(name) {
        name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
                results = regex.exec(location.search);
        return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    }


</script>


</body>
</html>