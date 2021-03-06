package io.fabric8.launcher.service.openshift.impl;

import java.io.File;

import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftEnvironment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class OpenShiftClusterRegistryTest {

    private OpenShiftClusterRegistry registry;

    @Rule
    public final ProvideSystemProperty properties =
            new ProvideSystemProperty(OpenShiftEnvironment.LAUNCHER_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE.propertyKey(),
                                      new File("src/test/resources/openshift-clusters.yaml").getAbsolutePath());

    @Rule
    public final ClearSystemProperties clearSystemProperties =
            new ClearSystemProperties(OpenShiftEnvironment.LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL.propertyKey(),
                                      OpenShiftEnvironment.LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL.propertyKey());

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Before
    public void setUp() {
        environmentVariables.clear(OpenShiftEnvironment.LAUNCHER_MISSIONCONTROL_OPENSHIFT_API_URL.propertyKey(),
                                   OpenShiftEnvironment.LAUNCHER_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL.propertyKey());
        registry = new OpenShiftClusterRegistryImpl();
    }

    @Test
    public void testDefaultClusterNeverNull() {
        assertThat(registry.getDefault()).isNotNull();
    }

    @Test
    public void testGetClustersIncludeDefaultCluster() {
        assertThat(registry.getClusters()).contains(registry.getDefault());
    }

    @Test
    public void testGetClustersHasAtLeastTwoItems() {
        System.out.println(registry.getClusters());
        assertThat(registry.getClusters().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void testFindNullReturnsDefault() {
        assertThat(registry.findClusterById(null).get()).isSameAs(registry.getDefault());
    }

    @Test
    public void testFindOpenshiftOnlineInt() {
        assertThat(registry.findClusterById("openshift-online-int").get())
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", "openshift-online-int")
                .hasFieldOrPropertyWithValue("name", "openshift-online-int")
                .hasFieldOrPropertyWithValue("apiUrl", "https://api.online-int.openshift.com/")
                .hasFieldOrPropertyWithValue("consoleUrl", "https://console.online-int.openshift.com/console");
    }
}