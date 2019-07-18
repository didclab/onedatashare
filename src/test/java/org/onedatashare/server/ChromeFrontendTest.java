package org.onedatashare.server;

import org.junit.After;
import org.onedatashare.server.model.core.User;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.context.annotation.DependsOn;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**  This test assumes
 *  you have vanditsa@buffalo.edu
 *  with password asdasd as your login account.
 *
 *
 */
public class ChromeFrontendTest {
    private String baseUrl;
    private int msWaitShort = 1000;
    private int msWaitLong = 3000;

    @Test
    public void setUp() throws Exception {
        WebDriver driver = new ChromeDriver();
        baseUrl = "localhost:8080";
        driver.get(baseUrl);
        assertEquals(driver.getTitle(), "OneDataShare - Home");
        driver.quit();
    }

    @Test(dependsOnMethods = {"setUp"})
    public void LoginTest() throws Exception {
        WebDriver driver = new ChromeDriver();
        driver.get(baseUrl);
        driver.findElement(By.linkText("Sign in")).click();
        Thread.sleep(msWaitShort);
        assertEquals(driver.getCurrentUrl(), "http://localhost:8080/account/signIn");
        driver.findElement(By.name("email")).click();
        driver.findElement(By.name("email")).clear();
        driver.findElement(By.name("email")).sendKeys("vanditsa@buffalo.edu");
        driver.findElement(By.name("email")).sendKeys(Keys.ENTER);
        Thread.sleep(msWaitShort);
        driver.findElement(By.id("Password")).click();
        driver.findElement(By.id("Password")).clear();
        driver.findElement(By.id("Password")).sendKeys("asdasd");
        driver.findElement(By.id("Password")).sendKeys(Keys.ENTER);
        Thread.sleep(msWaitShort);
        assertEquals(driver.getTitle(), "OneDataShare - Transfer");
        assertEquals(driver.getCurrentUrl(), "http://localhost:8080/transfer");
        driver.findElement(By.linkText("Queue")).click();
        assertEquals(driver.getCurrentUrl(), "http://localhost:8080/queue");
        assertEquals(driver.getTitle(), "OneDataShare - Home");
        driver.findElement(By.linkText("Transfer")).click();
        assertEquals(driver.getCurrentUrl(), "http://localhost:8080/transfer");
        assertEquals(driver.getTitle(), "OneDataShare - Transfer");
        driver.findElement(By.linkText("vanditsa@buffalo.edu")).click();
        assertEquals(driver.getCurrentUrl(), "http://localhost:8080/user");
        assertEquals(driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Log out'])[1]/following::span[1]")).getText(), "Email");
        assertEquals(driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Email'])[1]/following::span[1]")).getText(), "First Name");
        assertEquals(driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='First Name'])[1]/following::span[1]")).getText(), "Last Name");
        assertEquals(driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Last Name'])[1]/following::span[1]")).getText(), "Organization");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='vanditsa@buffalo.edu'])[1]/following::span[1]")).click();
        Thread.sleep(msWaitShort);
        assertTrue(driver.getCurrentUrl().equals( "http://localhost:8080/") || driver.getCurrentUrl().equals( "http://localhost:8080"));
        driver.quit();
        throw new Exception();
    }

    @Test(dependsOnMethods = {"LoginTest"})
    public void SortingTestLeft() throws Exception {
        WebDriver driver = new ChromeDriver();
        driver.get(baseUrl);
        driver.findElement(By.linkText("Sign in")).click();
        Thread.sleep(msWaitShort);
        assertEquals(driver.getCurrentUrl(), "http://localhost:8080/account/signIn");
        driver.findElement(By.name("email")).click();
        driver.findElement(By.name("email")).clear();
        driver.findElement(By.name("email")).sendKeys("vanditsa@buffalo.edu");
        driver.findElement(By.name("email")).sendKeys(Keys.ENTER);
        Thread.sleep(msWaitShort);
        driver.findElement(By.id("Password")).click();
        driver.findElement(By.id("Password")).clear();
        driver.findElement(By.id("Password")).sendKeys("asdasd");
        driver.findElement(By.id("Password")).sendKeys(Keys.ENTER);
        Thread.sleep(msWaitShort);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Grid FTP'])[1]/following::span[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Back'])[1]/following::div[3]")).click();
        driver.findElement(By.id("outlined-name")).click();
        driver.findElement(By.id("outlined-name")).clear();
        driver.findElement(By.id("outlined-name")).sendKeys("speedtest.tele2.net");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='​'])[2]/following::span[1]")).click();
        Thread.sleep(msWaitLong);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='*.'])[1]/following::span[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='*.'])[1]/following::span[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='*.'])[1]/following::span[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='File Name'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='File Name'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='File Name'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Date'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Date'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Date'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Permission'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Permission'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Permission'])[1]/following::span[1]")).click();
        driver.findElement(By.linkText("Queue")).click();
        Thread.sleep(msWaitShort);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Log out'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Job ID'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Progress'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Average Speed'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Log out'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Log out'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Job ID'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Job ID'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Progress'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Progress'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Average Speed'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Average Speed'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Log out'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Log out'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Job ID'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Job ID'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Progress'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Progress'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Average Speed'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Average Speed'])[1]/following::span[1]")).click();
        driver.quit();
    }

    @Test(dependsOnMethods = {"LoginTest"})
    public void SortingTestRight() throws Exception {
        WebDriver driver = new ChromeDriver();
        driver.get(baseUrl);
        driver.findElement(By.linkText("Sign in")).click();
        Thread.sleep(msWaitShort);
        assertEquals(driver.getCurrentUrl(), "http://localhost:8080/account/signIn");
        driver.findElement(By.name("email")).click();
        driver.findElement(By.name("email")).clear();
        driver.findElement(By.name("email")).sendKeys("vanditsa@buffalo.edu");
        driver.findElement(By.name("email")).sendKeys(Keys.ENTER);
        Thread.sleep(msWaitShort);
        driver.findElement(By.id("Password")).click();
        driver.findElement(By.id("Password")).clear();
        driver.findElement(By.id("Password")).sendKeys("asdasd");
        driver.findElement(By.id("Password")).sendKeys(Keys.ENTER);
        Thread.sleep(msWaitShort);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Grid FTP'])[2]/following::span[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Back'])[1]/following::div[3]")).click();
        driver.findElement(By.id("outlined-name")).click();
        driver.findElement(By.id("outlined-name")).clear();
        driver.findElement(By.id("outlined-name")).sendKeys("speedtest.tele2.net");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='​'])[2]/following::span[1]")).click();
        Thread.sleep(msWaitLong);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='*.'])[1]/following::span[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='*.'])[1]/following::span[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='*.'])[1]/following::span[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='File Name'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='File Name'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='File Name'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Date'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Date'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Date'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Permission'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Permission'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Permission'])[1]/following::span[1]")).click();
        driver.findElement(By.cssSelector("button.btn.btn-primary > svg.MuiSvgIcon-root")).click();
        driver.findElement(By.linkText("Queue")).click();
        Thread.sleep(msWaitShort);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Log out'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Job ID'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Progress'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Average Speed'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Log out'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Log out'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Job ID'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Job ID'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Progress'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Progress'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Average Speed'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Average Speed'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Log out'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Log out'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Job ID'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Job ID'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Progress'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Progress'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Average Speed'])[1]/following::span[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Average Speed'])[1]/following::span[1]")).click();
        driver.quit();
    }

    @Test(dependsOnMethods = {"LoginTest"})
    public void SearchingTestLeft() throws Exception {
        WebDriver driver = new ChromeDriver();
        driver.get(baseUrl);
        driver.findElement(By.linkText("Sign in")).click();
        Thread.sleep(msWaitShort);
        assertEquals(driver.getCurrentUrl(), "http://localhost:8080/account/signIn");
        driver.findElement(By.name("email")).click();
        driver.findElement(By.name("email")).clear();
        driver.findElement(By.name("email")).sendKeys("vanditsa@buffalo.edu");
        driver.findElement(By.name("email")).sendKeys(Keys.ENTER);
        Thread.sleep(msWaitShort);
        driver.findElement(By.id("Password")).click();
        driver.findElement(By.id("Password")).clear();
        driver.findElement(By.id("Password")).sendKeys("asdasd");
        driver.findElement(By.id("Password")).sendKeys(Keys.ENTER);
        Thread.sleep(msWaitShort);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Grid FTP'])[1]/following::span[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Back'])[1]/following::div[3]")).click();
        driver.findElement(By.id("outlined-name")).click();
        driver.findElement(By.id("outlined-name")).clear();
        driver.findElement(By.id("outlined-name")).sendKeys("speedtest.tele2.net");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='​'])[2]/following::span[1]")).click();
        Thread.sleep(msWaitLong);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("1000");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys(Keys.ENTER);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("1000gb");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys(Keys.ENTER);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("1000");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::span[4]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("1000gb");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys(Keys.ENTER);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("10*");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys(Keys.ENTER);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Aa'])[1]/following::b[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("1000*");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys(Keys.ENTER);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("1000gb.zipa*");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys(Keys.ENTER);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("");
        driver.findElement(By.cssSelector("button.btn.btn-primary > svg.MuiSvgIcon-root")).click();
        driver.quit();
    }

    @Test(dependsOnMethods = {"LoginTest"})
    public void SearchingTestRight() throws Exception {
        WebDriver driver = new ChromeDriver();
        driver.get(baseUrl);
        driver.findElement(By.linkText("Sign in")).click();
        Thread.sleep(msWaitShort);
        assertEquals(driver.getCurrentUrl(), "http://localhost:8080/account/signIn");
        driver.findElement(By.name("email")).click();
        driver.findElement(By.name("email")).clear();
        driver.findElement(By.name("email")).sendKeys("vanditsa@buffalo.edu");
        driver.findElement(By.name("email")).sendKeys(Keys.ENTER);
        Thread.sleep(msWaitShort);
        driver.findElement(By.id("Password")).click();
        driver.findElement(By.id("Password")).clear();
        driver.findElement(By.id("Password")).sendKeys("asdasd");
        driver.findElement(By.id("Password")).sendKeys(Keys.ENTER);
        Thread.sleep(msWaitShort);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Grid FTP'])[1]/following::span[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Back'])[1]/following::div[3]")).click();
        driver.findElement(By.id("outlined-name")).click();
        driver.findElement(By.id("outlined-name")).clear();
        driver.findElement(By.id("outlined-name")).sendKeys("speedtest.tele2.net");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='​'])[2]/following::span[1]")).click();
        Thread.sleep(msWaitLong);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("1000");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys(Keys.ENTER);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("1000gb");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys(Keys.ENTER);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("1000");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::span[4]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("1000gb");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys(Keys.ENTER);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("10*");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys(Keys.ENTER);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Aa'])[1]/following::b[1]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).click();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("1000*");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys(Keys.ENTER);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("1000gb.zipa*");
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys(Keys.ENTER);
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).clear();
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='ftp://speedtest.tele2.net'])[1]/following::input[2]")).sendKeys("");
        driver.findElement(By.cssSelector("button.btn.btn-primary > svg.MuiSvgIcon-root")).click();
        driver.quit();
    }

    class Task implements Runnable
    {
        private String name;

        public Task(String s)
        {
            name = s;
        }

        // Prints task name and sleeps for 1s
        // This Whole process is repeated 5 times
        public void run() throws RuntimeException
        {
            try {
                WebDriver driver = new ChromeDriver();
                Thread.sleep(msWaitShort);
                driver.get("https://www.onedatashare.org");
                driver.findElement(By.linkText("Sign in")).click();
                Thread.sleep(msWaitLong);
                driver.findElement(By.name("email")).click();
                driver.findElement(By.name("email")).clear();
                driver.findElement(By.name("email")).sendKeys("yifuyin@buffalo.edu");
                driver.findElement(By.name("email")).sendKeys(Keys.ENTER);
                Thread.sleep(msWaitLong);
                driver.findElement(By.id("Password")).click();
                driver.findElement(By.id("Password")).clear();
                driver.findElement(By.id("Password")).sendKeys("asdasd");
                driver.findElement(By.id("Password")).sendKeys(Keys.ENTER);
                Thread.sleep(msWaitLong);
                assertEquals(driver.getTitle(), "OneDataShare - Transfer");
                driver.findElement(By.linkText("Queue")).click();
                assertEquals(driver.getTitle(), "OneDataShare - Home");
                driver.findElement(By.linkText("Transfer")).click();
                assertEquals(driver.getTitle(), "OneDataShare - Transfer");
                driver.findElement(By.linkText("yifuyin@buffalo.edu")).click();
                assertEquals(driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Log out'])[1]/following::span[1]")).getText(), "Email");
                assertEquals(driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Email'])[1]/following::span[1]")).getText(), "First Name");
                assertEquals(driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='First Name'])[1]/following::span[1]")).getText(), "Last Name");
                assertEquals(driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Last Name'])[1]/following::span[1]")).getText(), "Organization");
                driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='yifuyin@buffalo.edu'])[1]/following::span[1]")).click();
                Thread.sleep(msWaitLong);
                driver.quit();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    static final int MAX_T = 20;
    //@Test
    public void MultipleUser() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_T);
        for(int i =0; i < MAX_T; i++){
            // creates five tasks
            Runnable r1 = new Task("task"+i);
            pool.execute(r1);
        }
        pool.awaitTermination(20, TimeUnit.MINUTES);
        pool.shutdown();
    }

}