<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container">
	<div class="row">
		<div class="col-md-12 col-sm-12">
			<h2 id="resultsHeader">Messages</h2>
		</div>
		<div class="col-md-2 col-sm-2"></div>
		<div class="col-md-8 col-sm-8">
			<table class="table" id="resultsTable">
				<tr>
					<th>Name</th>
					<th>Email</th>
					<th>Subject</th>
					<th>Message</th>
				</tr>
				<c:choose>
					<c:when test="${empty messages}">
					<tr>
						<td colspan="4">No results</td>
					</tr>
					</c:when>
					<c:otherwise>
						<c:forEach var="message" items="${messages}">
							<tr>
								<td>${message.name}</td>
								<td>${message.email}</td>
			  					<td>${message.subject}</td>
			  					<td>${message.message}</td>
							</tr>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</table>
		</div>
		<div class="col-md-2 col-sm-2"></div>
	</div>
</div>