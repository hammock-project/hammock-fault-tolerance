package ws.ament.hammock.ft;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.junit.Assert.*;

public class RetryTest extends Arquillian {
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addPackages(true,"ws.ament.hammock.ft")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    @Inject
    private RetryBean retryBean;

    @Test
    public void shouldRetryMultipleTimes() {
        String msg = retryBean.doRetries();
        assertEquals("success 4", msg);
    }

    @Test
    public void shouldRunFallbackOnAbort() {
        try {
            retryBean.doRetryWithAbort();
            fail("Exception expected");
        }
        catch (RuntimeException e) {
            assertEquals("Failed 0",e.getMessage());
        }
    }
}
