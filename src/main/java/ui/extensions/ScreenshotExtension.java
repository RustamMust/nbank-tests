package ui.extensions;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotExtension implements TestWatcher {

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String testName = context.getDisplayName().replace("()", ""); // убираем скобки
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String screenshotName = testName + "_" + timestamp;

        try {
            // Делаем скриншот через Selenide
            String screenshotPath = Selenide.screenshot(screenshotName);
            System.out.println("Screenshot saved: " + screenshotPath);
        } catch (Exception e) {
            System.err.println("Failed to made screenshot for test " + testName);
            e.printStackTrace();
        }
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
    }
}

