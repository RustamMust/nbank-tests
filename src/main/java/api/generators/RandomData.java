package api.generators;

import org.apache.commons.lang3.RandomStringUtils;
import java.util.concurrent.ThreadLocalRandom;

public class RandomData {
    private RandomData() {
    }

    public static String getUsername() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static String getPassword() {
        return RandomStringUtils.randomAlphabetic(3).toUpperCase()
                + RandomStringUtils.randomAlphabetic(5).toLowerCase()
                + RandomStringUtils.randomNumeric(3).toLowerCase()
                + "%$#";
    }

    public static String getInvalidNameSingleWord() {
        return RandomStringUtils.randomAlphabetic(6, 10);
    }

    public static String getValidName() {
        return RandomStringUtils.randomAlphabetic(5, 8) + " " + RandomStringUtils.randomAlphabetic(5, 8);
    }

    public static int getRandomBalance() {
        return ThreadLocalRandom.current().nextInt(1, 5000);
    }

    public static int getNonExistingAccountId() {
        return ThreadLocalRandom.current().nextInt(9000, 100000);
    }
}
