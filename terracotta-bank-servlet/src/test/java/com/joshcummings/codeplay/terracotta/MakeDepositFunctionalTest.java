/*
 * Copyright 2015-2018 Josh Cummings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.joshcummings.codeplay.terracotta;

import com.joshcummings.codeplay.terracotta.testng.XssCheatSheet;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

public class MakeDepositFunctionalTest extends AbstractEmbeddedTomcatSeleniumTest {
	@BeforeClass(alwaysRun = true)
	public void doLogin() {
		login("john.coltraine", "j0hn");
	}

	@AfterClass(alwaysRun = true)
	public void doLogout() {
		logout();
	}

	protected void makeDeposit(String accountNumber, String checkNumber, String amount, File image) {
		driver.findElement(By.name("depositAccountNumber")).sendKeys(accountNumber);
		driver.findElement(By.name("depositCheckNumber")).sendKeys(checkNumber);
		driver.findElement(By.name("depositAmount")).sendKeys(amount);
		driver.findElement(By.name("depositCheckImage")).sendKeys(image.getAbsolutePath());

		ignoreErrors(() -> driver.findElement(By.name("deposit")).submit());
	}

	@Test(groups = "web")
	public void testMakeDepositForXSS() {
		for (String template : new XssCheatSheet(true)) {
			goToPage("/");

			try {
				String depositAccountNumberXss = String.format(template, "depositAccountNumber");
				String depositCheckNumberXss = String.format(template, "depositCheckNumber");
				String depositAmountXss = String.format(template, "depositAmount");

				makeDeposit(depositAccountNumberXss, depositCheckNumberXss, depositAmountXss,
						new File("src/test/resources/check.png"));

				Alert alert = switchToAlertEventually(driver, 2000);
				Assert.fail(getTextThenDismiss(alert));
			} catch (NoAlertPresentException e) {
				// awesome!
			}
		}
	}


	protected byte[] attemptMaliciousUpload(String maliciousCheckNumber, File maliciousImage) throws IOException {
		byte[] source = java.nio.file.Files.readAllBytes(Paths.get(maliciousImage.toURI()));

		String depositAccountNumber = "987654321";
		String depositCheckNumber = maliciousCheckNumber;
		String depositAmount = "450.00";

		makeDeposit(depositAccountNumber, depositCheckNumber, depositAmount, maliciousImage);

		return source;
	}
	
	protected byte[] attemptTraversedLookup(String maliciousCheckNumber) throws IOException {
		String checkLookupNumber = maliciousCheckNumber;

		try ( CloseableHttpResponse response =
					this.honest.post("/checkLookup", new BasicNameValuePair("checkLookupNumber", checkLookupNumber)) ) {
			ByteArrayOutputStream destination = new ByteArrayOutputStream();
			IOUtils.copy(response.getEntity().getContent(), destination);
	
			return destination.toByteArray();
		}
	}

	protected boolean attemptRoundTrip(String maliciousCheckNumber, File maliciousImage) throws IOException {
		byte[] source = attemptMaliciousUpload(maliciousCheckNumber, maliciousImage);
		byte[] destination = attemptTraversedLookup(maliciousCheckNumber);

		return Arrays.equals(destination, source);
	}
	
	protected void removeIfPresent(Path path) {
		File uploaded = path.toFile();
		if (uploaded.exists()) {
			uploaded.delete();
		}
	}

	private Path evil = Paths.get("evil");
	private Path notAnImage = Paths.get("evil/notAnImage.txt");
	private Path stillNotAnImage = Paths.get("evil/notAnImage.jpg");
	private Path reallyBigImage = Paths.get("evil/reallyBig.jpg");
	private Path withVirus = Paths.get("evil/withVirus.jpg");
	private Path real = Paths.get("evil/realImage.jpg");
	
	private Path uploadedCheck1 = Paths.get("images/checks/620");
	private Path uploadedCheck2 = Paths.get("images/checks/621");
	private Path uploadedCheck3 = Paths.get("images/checks/622");
	private Path uploadedCheck4 = Paths.get("images/checks/623");
	private Path uploadedDoubleExtension = Paths.get("images/checks/index.jsp;.jpg");
	private Path uploadedSchema = Paths.get("target/classes/evil.data.sql");

	@BeforeMethod(alwaysRun = true)
	public void mkdirs() {
		this.evil.toFile().mkdirs();
	}

	@AfterMethod(alwaysRun = true)
	public void removeFiles() {
		removeIfPresent(this.notAnImage);
		removeIfPresent(this.stillNotAnImage);
		removeIfPresent(this.reallyBigImage);
		removeIfPresent(this.withVirus);
		removeIfPresent(this.uploadedCheck1);
		removeIfPresent(this.uploadedCheck2);
		removeIfPresent(this.uploadedCheck3);
		removeIfPresent(this.uploadedCheck4);
		removeIfPresent(this.uploadedDoubleExtension);
		removeIfPresent(this.uploadedSchema);
		removeIfPresent(this.evil);
	}

	@Test(groups="filesystem")
	public void testMakeDepositWithNonImage() throws IOException {
		goToPage("/");

		File toUpload = Files.write(this.notAnImage, "random content".getBytes()).toFile();
		Assert.assertFalse(attemptRoundTrip("620", toUpload), toUpload.getName() + " was uploaded. :(");
	}
	
	@Test(groups="filesystem")
	public void testMakeDepositWithMasqueradingImage() throws IOException {
		goToPage("/");
		File toUpload = Files.write(this.stillNotAnImage, "random content".getBytes()).toFile();
		Assert.assertFalse(attemptRoundTrip("621", toUpload), toUpload.getName() + " was uploaded. :(");
	}
	
	@Test(groups="filesystem")
	public void testMakeDepositWithTooBigImage() throws IOException {
		goToPage("/");
		Random rnd = new Random();
		BufferedImage img = new BufferedImage(5000, 5000, BufferedImage.TYPE_INT_ARGB);
		for ( int i = 0; i < img.getWidth(); i++ ) {
			for ( int j = 0; j < img.getHeight(); j++ ) {
				img.setRGB(i, j, rnd.nextInt(256));
			}
		}
		File toUpload = this.reallyBigImage.toFile();
		ImageIO.write(img, "jpg", toUpload);
		Assert.assertFalse(attemptRoundTrip("622", toUpload), toUpload.getName() + " was uploaded. :(");
	}
	
	@Test(groups="filesystem")
	public void testMakeDepositWithVirus() throws IOException {
		goToPage("/");
	
		File toUpload = this.withVirus.toFile();
		try ( PrintWriter pw = new PrintWriter(new FileWriter(toUpload)) ) {
			pw.println("X5O!P%@AP[4\\PZX54(P^)7CC)7}" + "$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*");
		}
		Assert.assertFalse(attemptRoundTrip("623", toUpload), toUpload.getName() + " was uploaded. :(");
	}
	
	@Test(groups="filesystem")
	public void testMakeDepositWithDoubleExtension() throws IOException {
		goToPage("/");
		
		BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		File toUpload = this.real.toFile();
		ImageIO.write(img, "jpg", toUpload);
		Assert.assertFalse(attemptRoundTrip("index.jsp;.jpg", toUpload), toUpload.getName() + " was uploaded as index.jsp;.jpg. :(");
	}
	
	@Test(groups="filesystem")
	public void testMakeDepositToProtectedDirectory() throws IOException {
		goToPage("/");
		
		File toUpload = Files.write(this.notAnImage,
				("INSERT INTO user (id, name, email, username, password, is_admin) "
				+ "VALUES (12345, 'Evil Doer', 'evil@doer.com', 'evil.user', 'alwaysbeevil', true)").getBytes()).toFile();
		Assert.assertFalse(attemptRoundTrip("../../target/classes/evil.data.sql",
				toUpload), "data.sql was uploaded. :(");
	}
}
