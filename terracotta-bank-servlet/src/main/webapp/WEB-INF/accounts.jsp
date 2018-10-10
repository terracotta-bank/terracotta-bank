<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container">
	<div class="row">
		<div class="col-md-12 col-sm-12">
			<h2 id="resultsHeader">Accounts</h2>
		</div>
		<div class="col-md-2 col-sm-2"></div>
		<div class="col-md-8 col-sm-8">
			<table class="table" id="resultsTable">
				<tr>
					<th>Account Number</th>
					<th>Balance</th>
				</tr>
				<c:choose>
					<c:when test="${empty accounts}">
					<tr>
						<td colspan="2">No results</td>
					</tr>
					</c:when>
					<c:otherwise>
						<c:forEach var="account" items="${accounts}">
							<tr>
								<td>${account.number}</td>
								<td>$${account.amount}</td>
							</tr>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</table>
		</div>
		<div class="col-md-2 col-sm-2"></div>
	</div>
</div>