<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>

	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=Edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta name="keywords" content="">
	<meta name="description" content="">

	<title>Terracotta Bank</title>
<!--

Template 2080 Minimax

http://www.tooplate.com/view/2080-minimax

-->
	<!-- stylesheet css -->
	<link rel="stylesheet" href="css/bootstrap.min.css">
	<link rel="stylesheet" href="css/font-awesome.min.css">
	<link rel="stylesheet" href="css/nivo-lightbox.css">
	<link rel="stylesheet" href="css/nivo_themes/default/default.css">
	<link rel="stylesheet" href="css/style.css">
	<!-- google web font css -->
	<link href='http://fonts.googleapis.com/css?family=Raleway:400,300,600,700' rel='stylesheet' type='text/css'>
</head>
<body data-page-context="${pageContext.request.contextPath}" data-spy="scroll" data-target=".navbar-collapse" data-csrf-token="${csrfToken}">
	
<!-- navigation -->
<div class="navbar navbar-default navbar-fixed-top" role="navigation">
	<div class="container">
		<div class="navbar-header">
			<button class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
				<span class="icon icon-bar"></span>
				<span class="icon icon-bar"></span>
				<span class="icon icon-bar"></span>
			</button>
			<a href="#home" class="navbar-brand smoothScroll">Terracotta</a>
		</div>
		<div class="collapse navbar-collapse">
			<ul class="nav navbar-nav navbar-right">
				<c:choose>
					<c:when test="${empty authenticatedUser}">
						<li><a href="#home" class="smoothScroll">HOME</a></li>
						<li><a href="#login" class="smoothScroll">LOGIN</a></li>
					</c:when>
					<c:otherwise>
						<li><a href="#service" class="smoothScroll">HOME</a></li>
						<li><a href="${pageContext.request.contextPath}/logout">LOGOUT</a></li>
					</c:otherwise>
				</c:choose>
				<li><a href="#contact" class="smoothScroll">CONTACT</a></li>
				<li><a href="#about" class="smoothScroll">ABOUT</a></li>
			</ul>
		</div>
	</div>
</div>		

<c:choose>
	<c:when test="${empty authenticatedUser}">
		<!-- home section -->
		<div id="home">
			<div class="container">
				<div class="row">
					<div class="col-md-offset-6 col-md-6 col-sm-offset-6 col-sm-6">
						<h2>Welcome to</h2>
						<h1>Terracotta <strong>Bank</strong></h1>
						<p>Keep your money in the most secure place imagineable: Our patent-pending Terracotta vases.</p>
						<a href="#login" class="btn btn-default smoothScroll">Get Started</a>
					</div>
				</div>
			</div>
		</div>

		<!-- login section -->
		<div id="login">
			<div class="container">
				<div class="row">
					<div class="col-md-12 col-sm-12">
						<h2>Login or Register</h2>
					</div>
					<div class="col-md-6 col-sm-6">
						<i class="fa fa-group"></i>
						<h3>Login</h3>
						<p>Enter your virtual terracotta vault</p>
						<form action="${pageContext.request.contextPath}/login" method="post" role="form">
							<div class="col-md-12 col-sm-12 messages">${loginErrorMessage}</div>
							<div class="col-md-12 col-sm-12">
								<input name="username" type="text" class="form-control" id="username" placeholder="Username">
						  	</div>
							<div class="col-md-12 col-sm-12">
								<input name="password" type="password" class="form-control" id="password" placeholder="Password">
						  	</div>
						  	<input type="hidden" name="relay" value="${param['relay']}"/>
						  	<input type="hidden" name="csrfToken" value="${csrfToken}"/>
							<div class="col-md-6 col-sm-6"></div>
							<div class="col-md-6 col-sm-6">
								<input name="login" type="submit" class="form-control" value="LOGIN">
							</div>
						</form>
					</div>
					<div class="col-md-6 col-sm-6">
						<i class="fa fa-cogs"></i>
						<h3>Register</h3>
						<p>Not signed up yet? Get your own terrcotta vase vault! <a rel="nofollow" href="http://fortawesome.github.io/Font-Awesome/examples/" target="_parent">examples</a>.</p>
						<form action="${pageContext.request.contextPath}/register" method="post" role="form">
							<div class="col-md-12 col-sm-12 messages">${registrationErrorMessage}</div>
							<div class="col-md-6 col-sm-6">
								<input name="registerUsername" type="text" class="form-control" id="registerUsername" placeholder="Username">
						  	</div>
							<div class="col-md-6 col-sm-6">
								<input name="registerPassword" type="password" class="form-control" id="registerPassword" placeholder="Password">
						  	</div>
						  	<div class="col-md-6 col-sm-6">
								<input name="registerName" type="text" class="form-control" id="registerName" placeholder="Your Name">
						  	</div>
						  	<div class="col-md-6 col-sm-6">
								<input name="registerEmail" type="email" class="form-control" id="registerEmail" placeholder="Your Email">
						  	</div>
						  	<input type="hidden" name="csrfToken" value="${csrfToken}"/>
						  	<div class="col-md-6 col-sm-6"></div>
							<div class="col-md-6 col-sm-6">
								<input name="register" type="submit" class="form-control" value="REGISTER">
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>

		<!-- divider section -->
		<div class="container">
			<div class="row">
				<div class="col-md-1 col-sm-1"></div>
				<div class="col-md-10 col-sm-10">
					<hr>
				</div>
				<div class="col-md-1 col-sm-1"></div>
			</div>
		</div>

		<!-- forgot password section -->
		<div id="forgotPassword">
			<div class="container">
				<div class="row">
					<div class="col-md-12 col-sm-12">
						<i class="fa fa-group"></i>
						<h3>Forgot Your Password</h3>
						<p>We can help!</p>
						<form id="restore" action="#" method="post" role="form">
							<div class="col-md-12 col-sm-12 messages"></div>
							<div class="col-md-6 col-sm-6">
								<input name="forgotPasswordAccount" type="text" class="form-control" id="forgotPasswordAccount" placeholder="Username">
						  	</div>
						  	<input type="hidden" name="csrfToken" value="${csrfToken}"/>
							<div class="col-md-2 col-sm-2">
								<input name="forgotPassword" type="submit" class="form-control" value="RESTORE">
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>

        <!-- divider section -->
        <div class="container">
            <div class="row">
                <div class="col-md-1 col-sm-1"></div>
                <div class="col-md-10 col-sm-10">
                    <hr>
                </div>
                <div class="col-md-1 col-sm-1"></div>
            </div>
        </div>
	</c:when>
	<c:otherwise>
		<!-- service section -->
		
		<div id="service">
			<div class="container">
				<div class="row">
					<div class="col-md-12 col-sm-12">
						<h2>Welcome, ${authenticatedUser.name}!</h2>
						
						<c:forEach var="account" items="${authenticatedAccounts}">
							<h3 class="col-md-12 col-sm-12">Your Account (#<span id="accountNumber-${account.number}">${account.number}</span>) currently has $<span id="accountBalance-${account.number}">${account.amount}</span>.</h3>
						</c:forEach>
						
					</div>
					<div class="col-md-4 col-sm-4">
						<i class="fa fa-cubes"></i>
						<h3>Make Deposit</h3>
						<p>Drop some money into our terracotta vases!</p>
						<form id="deposit" action="#" method="post" role="form">
							<div class="col-md-12 col-sm-12 messages"></div>
							<div class="col-md-12 col-sm-12">
								<input name="depositAccountNumber" type="text" class="form-control" id="depositAccountNumber" placeholder="Account Number">
						  	</div>
							<div class="col-md-12 col-sm-12">
								<input name="depositCheckNumber" type="text" class="form-control" id="depositCheckNumber" placeholder="Check Number">
						  	</div>
		                    <div class="col-md-12 col-sm-12">
								<input name="depositAmount" type="text" class="form-control" id="depositAmount" placeholder="Amount">
			    	  	  	</div>
							<div class="col-md-12 col-sm-12">
								<input name="depositCheckImage" type="file" class="form-control" id="depositCheckImage" placeholder="Image">
							</div>
							<div class="col-md-12 col-sm-12">
								<input name="deposit" type="submit" class="form-control" value="DEPOSIT">
							</div>
							<input type="hidden" name="csrfToken" value="${csrfToken}"/>
						</form>
					</div>
					<div class="col-md-4 col-sm-4">
						<i class="fa fa-cogs"></i>
						<h3>Transfer Money</h3>
						<p>We dutifully pour money from one vase into another.</p>
						<form id="transfer" action="#" method="post" role="form">
							<div class="col-md-12 col-sm-12 messages"></div>
							<div class="col-md-12 col-sm-12">
								<input name="fromAccountNumber" type="text" class="form-control" id="fromAccountNumber" placeholder="From Account Number">
						  	</div>
							<div class="col-md-12 col-sm-12">
								<input name="toAccountNumber" type="text" class="form-control" id="toAccountNumber" placeholder="To Account Number">
						  	</div>
		                    <div class="col-md-12 col-sm-12">
								<input name="transferAmount" type="text" class="form-control" id="transferAmount" placeholder="Amount">
			    	  	  	</div>
							<div class="col-md-12 col-sm-12">
								<input name="transfer" type="submit" class="form-control" value="TRANSFER">
							</div>
							<input type="hidden" name="csrfToken" value="${csrfToken}"/>
						</form>
					</div>
					<div class="col-md-4 col-sm-4">
						<i class="fa fa-group"></i>
						<h3>Lookup a Check</h3>
						<p>Can't remember what that check was for?</p>
						<form id="lookup" action="#" method="post" role="form">
							<div class="col-md-12 col-sm-12 messages"></div>
							<img id="checkImageDisplay" class="col-md-12 col-sm-12"/>
							<div class="col-md-12 col-sm-12">
								<input name="checkLookupNumber" type="text" class="form-control" id="checkLookupNumber" placeholder="Check Number">
						  	</div>
							<div class="col-md-12 col-sm-12">
								<input name="lookup" type="submit" class="form-control" value="LOOKUP">
							</div>
							<input type="hidden" name="csrfToken" value="${csrfToken}"/>
						</form>
					</div>
				</div>
			</div>
		</div>


		<!-- divider section -->
		<div class="container">
			<div class="row">
				<div class="col-md-1 col-sm-1"></div>
				<div class="col-md-10 col-sm-10">
					<hr>
				</div>
				<div class="col-md-1 col-sm-1"></div>
			</div>
		</div>

		<!-- account section -->
        <div id="account">
            <div class="container">
                <div class="row">
                    <div class="col-md-12 col-sm-12">
                        <h2>Account</h2>
                    </div>
                    <div class="col-md-6 col-sm-6">
                        <i class="fa fa-group"></i>
                        <h3>Current Password</h3>
                        <p>${authenticatedUser.password}</p>
                        <h3>Change Your Password</h3>
                        <p>Make sure it's a good one</p>
                        <form id="change" action="#" method="post" role="form">
                            <div class="col-md-12 col-sm-12 messages"></div>
                            <div class="col-md-6 col-sm-6">
                                <input name="changePassword" type="password" class="form-control" id="changePassword" placeholder="Password">
                            </div>
                            <div class="col-md-6 col-sm-6">
                                <input name="verifyChangePassword" type="password" class="form-control" id="verifyPassword" placeholder="Verify Password">
                            </div>
                            <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                            <div class="col-md-6 col-sm-6"></div>
                            <div class="col-md-6 col-sm-6">
                                <input name="change" type="submit" class="form-control" value="CHANGE">
                            </div>
                        </form>
                    </div>
                    <div class="col-md-6 col-sm-6"/>
                </div>
            </div>
        </div>
		
		<!-- divider section -->
		<div class="container">
			<div class="row">
				<div class="col-md-1 col-sm-1"></div>
				<div class="col-md-10 col-sm-10">
					<hr>
				</div>
				<div class="col-md-1 col-sm-1"></div>
			</div>
		</div>
	</c:otherwise>
</c:choose>

<!-- contact section -->
<div id="contact">
	<div class="container">
		<div class="row">
			<div class="col-md-12 col-sm-12">
				<h2>Keep in touch</h2>
			</div>
			<form action="#" method="post" role="form">
				<div class="col-md-1 col-sm-1"></div>
				<div class="col-md-10 col-sm-10">
					<div class="col-md-12 col-sm-12 messages"></div>
					<div class="col-md-6 col-sm-6">
						<input name="contactName" type="text"
							class="form-control" id="contactName" placeholder="Name">
				  	</div>
					<div class="col-md-6 col-sm-6">
						<input name="contactEmail" type="email"
							class="form-control" id="contactEmail" placeholder="Email">
				  	</div>
                    <div class="col-md-12 col-sm-12">
						<input name="contactSubject" type="text"
							class="form-control" id="contactSubject" placeholder="Subject">
	    	  	  	</div>
					<div class="col-md-12 col-sm-12">
						<textarea name="contactMessage" rows="5"
							class="form-control" id="contactMessage" placeholder="Message"></textarea>
					</div>
					<div class="col-md-8 col-sm-8">
						<p>Questions, comments? We will send a response within 24 hours.</p>
					</div>
					<div class="col-md-4 col-sm-4">
						<input name="submit" type="submit" class="form-control" id="submit" value="SEND MESSAGE">
					</div>
				</div>
				<div class="col-md-1 col-sm-1"></div>
			</form>
		</div>
	</div>
</div>

<!-- divider section -->
<div class="container">
	<div class="row">
		<div class="col-md-1 col-sm-1"></div>
		<div class="col-md-10 col-sm-10">
			<hr>
		</div>
		<div class="col-md-1 col-sm-1"></div>
	</div>
</div>

<!-- about section -->
<div id="about">
	<div class="container">
		<div class="row">
			<div class="col-md-12 col-sm-12">
				<h2>Terracotta Story</h2>
			</div>
			<div class="col-md-6 col-sm-6">
				<img src="images/about-img.jpg" class="img-responsive" alt="about img">
			</div>
			<div class="col-md-6 col-sm-6">
				<h3>ABOUT US</h3>
				<h4>Security-Minded Software Engineers</h4>
				<p>This website is an experiment similar in nature to Damn-Vulnerable Web Application in PHP. Feel free to poke around to find a dearth of both famous and infamous security vulnerabilities.</p>
				<p>How many can you spot? Are you able to somehow exploit the security vulnerabilities to get secret information about the company, steal funds, or simply deface or DoS the site? Can you successfully close the vulnerabilities?</p>
			</div>
		</div>
	</div>
</div>

<!-- divider section -->
<div class="container">
	<div class="row">
		<div class="col-md-1 col-sm-1"></div>
		<div class="col-md-10 col-sm-10">
			<hr>
		</div>
		<div class="col-md-1 col-sm-1"></div>
	</div>
</div>

<!-- team section -->
<div id="team">
	<div class="container">
		<div class="row">
			<div class="col-md-12 col-sm-12">
				<h2>People behind the project</h2>
			</div>
			<div class="col-md-3 col-sm-6">
				<img src="images/team1.jpg" class="img-responsive" alt="team img">
				<h3>Josh Cummings</h3>
				<h4>Software Engineer</h4>
			</div>
			<div class="col-md-3 col-sm-6">
				<img src="images/team2.jpg" class="img-responsive" alt="team img">
				<h3>Wink Martindale </h3>
				<h4>Tech Evangelist</h4>
			</div>
			<div class="col-md-3 col-sm-6">
				<img src="images/team3.jpg" class="img-responsive" alt="team img">
				<h3>Happy Gilmore</h3>
				<h4>Golfer</h4>
			</div>
			<div class="col-md-3 col-sm-6">
				<img src="images/team4.jpg" class="img-responsive" alt="team img">
				<h3>Kristi Cummings</h3>
				<h4>Supportive Wife</h4>
			</div>
		</div>
	</div>
</div>

<!-- divider section 
<div class="container">
	<div class="row">
		<div class="col-md-1 col-sm-1"></div>
		<div class="col-md-10 col-sm-10">
			<hr>
		</div>
		<div class="col-md-1 col-sm-1"></div>
	</div>
</div>-->

<!-- portfolio section 
<div id="portfolio">
	<div class="container">
		<div class="row">
			<div class="col-md-12 col-sm-12">
				<div class="title">
					<h2>Our Portfolio</h2>
				</div>
				
				<div class="iso-section">
					<ul class="filter-wrapper clearfix">
                   		 <li><a href="#" data-filter="*" class="selected opc-main-bg">All</a></li>
                   		 <li><a href="#" class="opc-main-bg" data-filter=".html">HTML</a></li>
                   		 <li><a href="#" class="opc-main-bg" data-filter=".photoshop">Photoshop</a></li>
                    	 <li><a href="#" class="opc-main-bg" data-filter=".wordpress">Wordpress</a></li>
                    	 <li><a href="#" class="opc-main-bg" data-filter=".mobile">Mobile</a></li>
               		</ul>
               		<div class="iso-box-section">
               			<div class="iso-box-wrapper col4-iso-box">

               				<div class="iso-box html photoshop wordpress mobile col-md-4 col-sm-6 col-xs-12">
               				 	<a href="images/portfolio-img1.jpg" data-lightbox-gallery="portfolio-gallery"><img src="images/portfolio-img1.jpg" alt="portfolio img"></a>
               				 </div>

               				 <div class="iso-box html wordpress mobile col-md-4 col-sm-6 col-xs-12">
               				 	<a href="images/portfolio-img2.jpg" data-lightbox-gallery="portfolio-gallery"><img src="images/portfolio-img2.jpg" alt="portfolio img"></a>
               				 </div>

               				 <div class="iso-box wordpress col-md-4 col-sm-6 col-xs-12">
               				 	<a href="images/portfolio-img3.jpg" data-lightbox-gallery="portfolio-gallery"><img src="images/portfolio-img3.jpg" alt="portfolio img"></a>
               				 </div>

               				 <div class="iso-box html mobile col-md-6 col-sm-6 col-xs-12">
               				 	<a href="images/portfolio-img4.jpg" data-lightbox-gallery="portfolio-gallery"><img src="images/portfolio-img4.jpg" alt="portfolio img"></a>
               				 </div>

               				 <div class="iso-box wordpress col-md-6 col-sm-6 col-xs-12">
               				 	<a href="images/portfolio-img5.jpg" data-lightbox-gallery="portfolio-gallery"><img src="images/portfolio-img5.jpg" alt="portfolio img"></a>
               				 </div>

               				 <div class="iso-box html photoshop col-md-4 col-sm-6 col-xs-12">
               				 	<a href="images/portfolio-img6.jpg" data-lightbox-gallery="portfolio-gallery"><img src="images/portfolio-img6.jpg" alt="portfolio img"></a>
               				 </div>

               				 <div class="iso-box photoshop col-md-4 col-sm-6 col-xs-12">
               				 	<a href="images/portfolio-img7.jpg" data-lightbox-gallery="portfolio-gallery"><img src="images/portfolio-img7.jpg" alt="portfolio img"></a>
               				 </div>

               				 <div class="iso-box wordpress col-md-4 col-sm-6 col-xs-12">
               				 	<a href="images/portfolio-img8.jpg" data-lightbox-gallery="portfolio-gallery"><img src="images/portfolio-img8.jpg" alt="portfolio img"></a>
               				 </div>

               			</div>
               		</div>

				</div>
			</div>
		</div>
	</div>
</div>		-->

<!-- divider section 
<div class="container">
	<div class="row">
		<div class="col-md-1 col-sm-1"></div>
		<div class="col-md-10 col-sm-10">
			<hr>
		</div>
		<div class="col-md-1 col-sm-1"></div>
	</div>
</div> -->

<!-- pricing section
<div id="pricing">
	<div class="container">
		<div class="row">
			<div class="col-md-12 col-sm-12">
				<h2>Our Pricing</h2>
			</div>
			<div class="col-md-4 col-sm-6 col-xs-10">
				<div class="plan">
					<div class="plan-title">
						<h3>Starter</h3>
						<small>$100 per month</small>
					</div>
					<ul>
						<li>100 GB STORAGE</li>
						<li>10 TB BANDWIDTH</li>
						<li>10 BASIC THEMES</li>
						<li>24-HOUR RESPONSE</li>
					</ul>
					<button class="btn btn-warning">Sign Up</button>
				</div>
			</div>
			<div class="col-md-4 col-sm-6 col-xs-10">
				<div class="plan">
					<div class="plan-title">
						<h3>Business</h3>
						<small>$350 per month</small>
					</div>
					<ul>
						<li>1,000 GB STORAGE</li>
						<li>50 TB BANDWIDTH</li>
						<li>50 PRO THEMES</li>
						<li>1-HOUR RESPONSE</li>
					</ul>
					<button class="btn btn-warning">Sign Up</button>
				</div>
			</div>
			<div class="col-md-4 col-sm-6 col-xs-10">
				<div class="plan">
					<div class="plan-title">
						<h3>Advanced</h3>
						<small>$500 per month</small>
					</div>
					<ul>
						<li>2,000 GB STORAGE</li>
						<li>100 TB BANDWIDTH</li>
						<li>100 PREMIUM THEMES</li>
						<li>30-MIN RESPONSE</li>
					</ul>
					<button class="btn btn-warning">Sign Up</button>
				</div>
			</div>
		</div>
	</div>
</div>-->

<!-- divider section -->
<div class="container">
	<div class="row">
		<div class="col-md-1 col-sm-1"></div>
		<div class="col-md-10 col-sm-10">
			<hr>
		</div>
		<div class="col-md-1 col-sm-1"></div>
	</div>
</div>



<!-- footer section -->
<footer>
	<div class="container">
		<div class="row">
			<div class="col-md-6 col-sm-6">
				<h2>Our Office</h2>
				<p>101 Terracotta Row, San Francisco, CA 10110</p>
				<p>Email: <span>vases@terracottabank.com</span></p>
				<p>Phone: <span>010-020-0340</span></p>
			</div>
			<div class="col-md-6 col-sm-6">
				<h2>Social Us</h2>
				<ul class="social-icons">
					<li><a href="#" class="fa fa-facebook"></a></li>
					<li><a href="#" class="fa fa-twitter"></a></li>
                    <li><a href="#" class="fa fa-google-plus"></a></li>
					<li><a href="#" class="fa fa-dribbble"></a></li>
				</ul>
			</div>
		</div>
	</div>
</footer>

<!-- divider section -->
<div class="container">
	<div class="row">
		<div class="col-md-1 col-sm-1"></div>
		<div class="col-md-10 col-sm-10">
			<hr>
		</div>
		<div class="col-md-1 col-sm-1"></div>
	</div>
</div>

<!-- copyright section -->
<div class="copyright">
	<div class="container">
		<div class="row">
			<div class="col-md-12 col-sm-12">
				<p>Copyright &copy; 2016 Minimax Digital Firm 
                
                - Design: <a rel="nofollow" href="http://www.tooplate.com" target="_parent">Tooplate</a></p>
			</div>
		</div>
	</div>
</div>

<!-- scrolltop section -->
<a href="#top" class="go-top"><i class="fa fa-angle-up"></i></a>


<!-- javascript js -->	
<script src="https://code.jquery.com/jquery-3.1.1.js"
			  integrity="sha256-16cdPddA6VdVInumRGo6IbivbERE8p7CQR3HzTBuELA="
			  crossorigin="anonymous"></script>
<script src="js/bootstrap.min.js"></script>	
<script src="js/nivo-lightbox.min.js"></script>
<script src="js/smoothscroll.js"></script>
<script src="js/jquery.nav.js"></script>
<script src="js/isotope.js"></script>
<script src="js/imagesloaded.min.js"></script>
<script src="js/custom.js"></script>
<script src="js/forms.js"></script>
</body>
</html>