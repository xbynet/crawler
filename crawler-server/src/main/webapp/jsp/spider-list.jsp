<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="../css/bootstrap.min.css">
        <script src="../js/jquery.min.js"></script>   		
        <script src="../js/bootstrap.min.js"></script>
        <script>
        	var baseUrl='${root}';
        </script>
        <title>爬虫监控</title>       
    </head>

    <body>          
        <div class="container">
            <h2>爬虫监控</h2>
                <c:choose> 
                    <c:when test="${not empty spiders}">
                        <table  class="table table-striped">
                            <thead>
                                <tr>
                                    <td>标识</td>
                                    <td>页面处理器类名</td>
                                    <td>状态</td>
                                    <td>操作</td>
                                    <td>运行信息</td>
                                </tr>
                            </thead>
                            <c:forEach var="spider" items="${spiders}">
                                <c:set var="classSucess" value=""/>
                                <tr class="${classSucess}">
                                    <td>${spider.name}</td>
                                    <td>${spider.processor}</td>
                                    <td>${spider.status}</td>
                                    <td>
                                    	<button id="stateBtn" type="button" class="btn btn-info" onclick="changeState('${spider.name}');"> 
                                    	<c:if test ="${spider.status=='running'}">停止</c:if><c:if test ="${spider.status=='stopped' || spider.status=='notrun'}">开始</c:if></button>
                                    </td>
                                    <td>${spider.info}</td>
                                </tr>
                            </c:forEach>               
                        </table>  
                    </c:when>                    
                    <c:otherwise>
                        <br>           
                        <div class="alert alert-info">
                            	没有正在运行的爬虫
                        </div>
                    </c:otherwise>
                </c:choose>                        
        </div>
    </body>
    <script src="../js/spider-list.js"></script>
</html>