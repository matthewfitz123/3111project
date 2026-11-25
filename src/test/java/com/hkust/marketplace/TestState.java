package com.hkust.marketplace;

import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;

/**
 * resets the in-memory store and the session before each test
 */
public final class TestState {

    private TestState() {
    }

    public static void reset() {
        SessionManager.logout();
        DummyDataService.getInstance().resetForTests();
    }
}
