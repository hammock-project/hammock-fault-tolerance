package ws.ament.hammock.ft;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

public class CircuitBreakerTest extends Arquillian{
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addPackages(true,"ws.ament.hammock.ft")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private CircuitBreakerBean circuitBreakerBean;

    @Inject
    private CircuitFallback circuitFallback;

    @Test
    public void shouldBeTolerant() throws Exception{
        run10Iterations();
        // we called it 10 times, however only 5 should have been called to the bean
        assertEquals(5, circuitBreakerBean.getInvocations());
        assertEquals(10, circuitFallback.getInvocations());

        // let the circuit reset
        Thread.sleep(5000l);

        run10Iterations();

        // the next 10 should have all succeeded and gone to the actual bean
        assertEquals(15, circuitBreakerBean.getInvocations());
        // we never failed
        assertEquals(10, circuitFallback.getInvocations());
    }

    private void run10Iterations() {
        for(int i = 0;i<10;i++) {
            try {
                circuitBreakerBean.doCircuits();
            }
            catch (Exception e) {

            }
        }
    }
}
