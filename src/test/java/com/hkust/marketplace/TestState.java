package com.hkust.marketplace;

import com.hkust.marketplace.data.PersistenceService;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;

/**
 * resets the in-memory store and the session before each test
 */
public final class TestState {

    private TestState() {
    }

    public static void reset() {
        PersistenceService.setEnabled(false);
        SessionManager.logout();
        DummyDataService.getInstance().resetForTests();
    }
}
