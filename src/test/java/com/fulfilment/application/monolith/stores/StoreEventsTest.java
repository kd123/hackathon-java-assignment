package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StoreEventsTest {

    @Test
    public void testCreatedEvent_getStore() {
        Store s = new Store();
        s.name = "E1";
        StoreCreatedEvent e = new StoreCreatedEvent(s);
        assertSame(s, e.getStore());
    }

    @Test
    public void testUpdatedEvent_getStore() {
        Store s = new Store();
        s.name = "E2";
        StoreUpdatedEvent e = new StoreUpdatedEvent(s);
        assertSame(s, e.getStore());
    }
}

