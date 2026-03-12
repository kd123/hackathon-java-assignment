package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LegacyStoreManagerGatewayTest {

    @Test
    void testCreateStoreOnLegacySystem() {

        LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();

        Store store = new Store();
        store.name = "STORE-LEGACY-1";
        store.quantityProductsInStock = 20;

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    }

    @Test
    void testUpdateStoreOnLegacySystem() {

        LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();

        Store store = new Store();
        store.name = "STORE-LEGACY-2";
        store.quantityProductsInStock = 50;

        assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
    }
}