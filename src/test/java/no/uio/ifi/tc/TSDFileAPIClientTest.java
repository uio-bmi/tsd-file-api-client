package no.uio.ifi.tc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Client tests.
 */
public class TSDFileAPIClientTest {

    @Test
    public void test() {
        TSDFileAPIClient tsdFileAPIClient = new TSDFileAPIClient.Builder().build();
        Assert.assertNotNull(tsdFileAPIClient);
    }

}
