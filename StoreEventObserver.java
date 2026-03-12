package com.fulfilment.application.monolith.stores;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class StoreEventObserver {

    private static final Logger LOG = Logger.getLogger(StoreEventObserver.class);

    @Inject
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    public void onStoreChange(@Observes(during = TransactionPhase.AFTER_SUCCESS) Store store) {
        LOG.infov("Store created event received, syncing with legacy system: {0}", store.id);
        try {
            legacyStoreManagerGateway.createStoreOnLegacySystem(store);
        } catch (Exception e) {
            LOG.errorv(e, "Failed to sync created store {0} with legacy system", store.id);
        }
    }
}