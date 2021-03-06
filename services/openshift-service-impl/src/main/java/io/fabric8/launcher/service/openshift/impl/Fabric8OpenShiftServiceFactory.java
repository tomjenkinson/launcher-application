package io.fabric8.launcher.service.openshift.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.ImmutableUserPasswordIdentity;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.service.openshift.api.ImmutableParameters;
import io.fabric8.launcher.service.openshift.api.OpenShiftClusterRegistry;
import io.fabric8.launcher.service.openshift.api.OpenShiftService;
import io.fabric8.launcher.service.openshift.api.OpenShiftServiceFactory;

import static io.fabric8.launcher.service.openshift.api.OpenShiftEnvironment.LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD;
import static io.fabric8.launcher.service.openshift.api.OpenShiftEnvironment.LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN;
import static io.fabric8.launcher.service.openshift.api.OpenShiftEnvironment.LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME;

/**
 * {@link OpenShiftServiceFactory} implementation
 *
 * @author <a href="mailto:xcoulon@redhat.com">Xavier Coulon</a>
 */
@ApplicationScoped
public class Fabric8OpenShiftServiceFactory implements OpenShiftServiceFactory {

    /**
     * Needed for proxying
     */
    @Deprecated
    Fabric8OpenShiftServiceFactory() {
        this.clusterRegistry = null;
    }

    @Inject
    public Fabric8OpenShiftServiceFactory(OpenShiftClusterRegistry clusterRegistry) {
        this.clusterRegistry = clusterRegistry;
    }

    private final OpenShiftClusterRegistry clusterRegistry;

    @Override
    public OpenShiftService create() {
        Parameters parameters = ImmutableParameters.builder()
                .cluster(clusterRegistry.getDefault())
                .identity(getDefaultIdentity().
                        orElseThrow(() -> new IllegalStateException("OpenShift Credentials not found. Are the required environment variables set?")))
                .build();
        return create(parameters);
    }

    @Override
    public Fabric8OpenShiftServiceImpl create(Parameters parameters) {
        return new Fabric8OpenShiftServiceImpl(parameters);
    }

    @Override
    public Optional<Identity> getDefaultIdentity() {
        if (!isDefaultIdentitySet()) {
            return Optional.empty();
        }
        // Read from the ENV variables
        String token = LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN.value();
        if (token != null) {
            return Optional.of(TokenIdentity.of(token));
        } else {
            String user = LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME.valueRequired();
            String password = LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD.valueRequired();
            return Optional.of(ImmutableUserPasswordIdentity.of(user, password));
        }
    }

    private boolean isDefaultIdentitySet() {
        String user = LAUNCHER_MISSIONCONTROL_OPENSHIFT_USERNAME.value();
        String password = LAUNCHER_MISSIONCONTROL_OPENSHIFT_PASSWORD.value();
        String token = LAUNCHER_MISSIONCONTROL_OPENSHIFT_TOKEN.value();

        return ((user != null && password != null) || token != null);
    }
}