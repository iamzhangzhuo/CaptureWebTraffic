import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.*;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Har {
    public static void main(String[] args) throws InterruptedException {
        System.setProperty("webdriver.gecko.driver", "D:\\temp\\geckodriver.exe");
        System.setProperty("webdriver.chrome.driver", "D:\\temp\\chromedriver.exe");
        try {
            TestcaseFileParser.parse("");
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        for (TestCase testCase : TestcaseFileParser.testCases) {
            if (testCase.steps.size() < 1) {
                continue;
            }
            // start the proxy
            BrowserMobProxy server = new BrowserMobProxyServer();
            server.start(0);
            // enable more detailed HAR capture, if desired (see CaptureType for the complete list)
            server.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
            server.newHar("www.abc.com");

            // get the Selenium proxy object
            Proxy seleniumProxy = ClientUtil.createSeleniumProxy(server);
            // configure it as a desired capability
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

            // start the browser up
            WebDriver driver = new ChromeDriver(capabilities);


            for (Step step : testCase.steps) {
                if (step.url.length() > 0) {
                    driver.get(step.url);
                    waitAfterAction(step,15);
                } else if (step.action.equalsIgnoreCase("click")) {
                    if (step.xPath.length() > 0) {
                        WebElement element = driver.findElement(By.xpath(step.xPath));
                        element.click();
                        waitAfterAction(step,10);
                    }
                } else if (step.action.equalsIgnoreCase("input")) {
                    if (step.xPath.length() > 0) {
                        WebElement element = driver.findElement(By.xpath(step.xPath));
                        element.sendKeys(step.value);
                        waitAfterAction(step,2);
                    }
                } else if (step.action.equalsIgnoreCase("executeScript")) {
                    if (driver instanceof JavascriptExecutor) {
                        ((JavascriptExecutor) driver).executeScript(step.value);
                    }
                    waitAfterAction(step,15);
                } else {
                    System.out.println("Step is ignored! stepId: " + step.id);
                }
            }


            // get the HAR data
            net.lightbody.bmp.core.har.Har har = server.getHar();

            saveHar(har, "d:\\temp\\web_traffic_log\\web_traffic.log");
            server.stop();
            driver.quit();
        }
    }

    private static void waitAfterAction(Step step, int seconds) throws InterruptedException {
        if (step.waitSecondsAfterAction < 1) {
            Thread.sleep(seconds * 1000);
        } else {
            Thread.sleep(step.waitSecondsAfterAction * 1000);
        }
    }

    public static void saveHar(net.lightbody.bmp.core.har.Har har, String fileName) {
        HarLog harLog = har.getLog();
        List<HarEntry> harEntryList = harLog.getEntries();
        StringBuilder logItems = new StringBuilder();
        for (HarEntry harEntry : harEntryList) {
            HarRequest harRequest = harEntry.getRequest();
            HarResponse harResponse = harEntry.getResponse();
            String url = harRequest.getUrl();
            long time = harEntry.getTime();
            //logItems.append("----- :  -----");
            logItems.append(System.getProperty("line.separator"));
            logItems.append("Url: " + url);
            logItems.append(System.getProperty("line.separator"));
            logItems.append("Status: " + harResponse.getStatus() + "  " + harResponse.getStatusText()
                    + "    Method: " + harRequest.getMethod() + "    Type:" + harResponse.getContent().getMimeType()
                    + "    Time: " + harEntry.getTime());
            if (harRequest.getMethod().equalsIgnoreCase("POST")) {
                logItems.append(System.getProperty("line.separator"));
                logItems.append("Post Data: " + harRequest.getPostData().getText());
                logItems.append(System.getProperty("line.separator"));
                logItems.append("Post Data Parameters: " + harRequest.getPostData().getParams());
                logItems.append("    Post Data MimeType: " + harRequest.getPostData().getMimeType());
                logItems.append(System.getProperty("line.separator"));
            }
            logItems.append(System.getProperty("line.separator"));
            logItems.append(System.getProperty("line.separator"));
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(logItems.toString());

        } catch (IOException e) {
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
            }
        }
    }
}
