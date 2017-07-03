package ws.ament.hammock.ft.extension;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import javax.enterprise.inject.spi.Extension;

public class ArchiveAppender implements ApplicationArchiveProcessor {
    private static final StringAsset BEANS_XML = new StringAsset("<beans version=\"1.1\" bean-discovery-mode=\"annotated\"/>");
    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        archive.as(JavaArchive.class)
                .addPackages(true, "ws.ament.hammock.ft", "org.eclipse.microprofile.fault.tolerance.tck.retry.clientserver")
                .addAsServiceProvider(Extension.class, FaultToleranceExtension.class)
                .addAsManifestResource(BEANS_XML, "beans.xml");
    }
}
