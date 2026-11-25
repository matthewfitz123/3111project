package com.hkust.marketplace.utils;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.ChatMessage;
import com.hkust.marketplace.models.ChatThread;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatStoreTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
    }

    @Test
    void threadIsReusedForTheSameListingAndBuyer() {
        ChatThread first = store.getOrCreateThread("L100", "alice");
        ChatThread second = store.getOrCreateThread("L100", "alice");

        assertSame(first, second);
        assertEquals("charlie", first.getSellerUsername());
        assertEquals("Calculus Textbook (Stewart)", first.getListingTitle());
        assertNotNull(first.getId());
    }

    @Test
    void messagesAreStoredAndSortedOldestFirst() {
        ChatThread thread = store.getOrCreateThread("L100", "alice");

        ChatMessage older = new ChatMessage();
        older.setThreadId(thread.getId());
        older.setSenderUsername("alice");
        older.setContent("is this still available");
        older.setSentAt(LocalDateTime.now().minusMinutes(2));
        store.addChatMessage(older);

        ChatMessage newer = new ChatMessage();
        newer.setThreadId(thread.getId());
        newer.setSenderUsername("charlie");
        newer.setContent("yes it is");
        newer.setSentAt(LocalDateTime.now());
        store.addChatMessage(newer);

        List<ChatMessage> messages = store.getMessagesForThread(thread.getId());
        assertEquals(2, messages.size());
        assertEquals("is this still available", messages.get(0).getContent());
        assertEquals("yes it is", messages.get(1).getContent());
        assertNotNull(older.getId());
        assertNotNull(newer.getId());
    }

    @Test
    void chatSearchIgnoresCaseAndBlankInput() {
        ChatThread thread = store.getOrCreateThread("L100", "alice");
        ChatMessage message = new ChatMessage();
        message.setThreadId(thread.getId());
        message.setSenderUsername("alice");
        message.setContent("About the TEXTBOOK pickup");
        store.addChatMessage(message);

        assertEquals(1, store.searchMessages("textbook").size());
        assertTrue(store.searchMessages("   ").isEmpty());
        assertTrue(store.searchMessages("nothing here").isEmpty());
    }

    @Test
    void threadsAreFoundForBothSides() {
        store.getOrCreateThread("L100", "alice");

        assertEquals(1, store.getThreadsForUser("alice").size());
        assertEquals(1, store.getThreadsForUser("charlie").size());
        assertTrue(store.getThreadsForUser("bob").isEmpty());
    }

    @Test
    void threadForUnknownListingIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> store.getOrCreateThread("missing", "alice"));
    }
}
