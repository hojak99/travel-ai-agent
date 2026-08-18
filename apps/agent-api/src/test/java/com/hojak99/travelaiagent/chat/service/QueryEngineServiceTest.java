package com.hojak99.travelaiagent.chat.service;

import com.hojak99.travelaiagent.chat.domain.ConversationMessage;
import com.hojak99.travelaiagent.chat.domain.QueryCommand;
import com.hojak99.travelaiagent.chat.domain.QueryResult;
import com.hojak99.travelaiagent.chat.repository.ConversationStateRepository;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryEngineServiceTest {

    private final ConversationStateRepository repository = new ConversationStateRepository();
    private final RuntimeCancellationRegistry cancellationRegistry = new RuntimeCancellationRegistry();
    private final QueryEngineService queryEngine = new QueryEngineService(
            repository,
            new QueryRuntimeService((instructions, messages) -> "여행 날짜와 예산을 알려주세요."),
            cancellationRegistry);

    @Test
    void keepsConversationStateForSameSession() {
        queryEngine.submit(new QueryCommand("study-1", "여행 일정 추천해줘"));
        QueryResult secondResult = queryEngine.submit(new QueryCommand("study-1", "오사카로 가고 싶어"));

        var state = repository.loadOrCreate("study-1");
        assertThat(secondResult.status()).isEqualTo("NEED_USER_INPUT");
        assertThat(state.getMessages()).extracting(ConversationMessage::role)
                .containsExactly(ConversationMessage.Role.USER, ConversationMessage.Role.ASSISTANT,
                        ConversationMessage.Role.USER, ConversationMessage.Role.ASSISTANT);
    }

    @Test
    void createsSeparateStateForDifferentSessions() {
        queryEngine.submit(new QueryCommand("study-1", "오사카"));
        queryEngine.submit(new QueryCommand("study-2", "도쿄"));

        assertThat(repository.loadOrCreate("study-1").getMessages()).hasSize(2);
        assertThat(repository.loadOrCreate("study-2").getMessages()).hasSize(2);
    }

    @Test
    void getsCurrentStateWithoutCreatingUnknownSession() {
        queryEngine.submit(new QueryCommand("study-1", "여행 일정을 추천해줘"));

        var state = queryEngine.getState("study-1");

        assertThat(state.sessionId()).isEqualTo("study-1");
        assertThat(state.messages()).hasSize(2);
        assertThat(state.pendingQuestion()).isNotBlank();
        assertThat(state.iteration()).isEqualTo(1);
        assertThatThrownBy(() -> queryEngine.getState("unknown"))
                .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    void returnsErrorWhenLlmCallFails() {
        QueryEngineService failingEngine = new QueryEngineService(
                new ConversationStateRepository(),
                new QueryRuntimeService((instructions, messages) -> {
                    throw new IllegalStateException("LLM unavailable");
                }),
                new RuntimeCancellationRegistry()
        );

        QueryResult result = failingEngine.submit(new QueryCommand("error-session", "여행 일정 추천해줘"));

        assertThat(result.status()).isEqualTo("ERROR");
        assertThat(result.message()).doesNotContain("LLM unavailable");
    }

    @Test
    void stopsAfterMaximumIterationsForBlankLlmResponses() {
        AtomicInteger calls = new AtomicInteger();
        ConversationStateRepository blankRepository = new ConversationStateRepository();
        QueryEngineService blankEngine = new QueryEngineService(
                blankRepository,
                new QueryRuntimeService((instructions, messages) -> {
                    calls.incrementAndGet();
                    return " ";
                }),
                new RuntimeCancellationRegistry()
        );

        QueryResult result = blankEngine.submit(new QueryCommand("blank-session", "여행 일정 추천해줘"));

        assertThat(result.status()).isEqualTo("MAX_ITERATIONS");
        assertThat(calls).hasValue(3);
        assertThat(blankRepository.loadOrCreate("blank-session").getIteration()).isEqualTo(3);
    }

    @Test
    void cancelsAnActiveRunAndIgnoresItsLateLlmResult() throws Exception {
        CountDownLatch llmStarted = new CountDownLatch(1);
        CountDownLatch releaseLlm = new CountDownLatch(1);
        RuntimeCancellationRegistry registry = new RuntimeCancellationRegistry();
        QueryEngineService cancellableEngine = new QueryEngineService(
                new ConversationStateRepository(),
                new QueryRuntimeService((instructions, messages) -> {
                    llmStarted.countDown();
                    await(releaseLlm);
                    return "이 결과는 취소 후 State에 반영되면 안 됩니다.";
                }),
                registry
        );

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<QueryResult> future = executor.submit(() ->
                    cancellableEngine.submit(new QueryCommand("cancel-session", "여행 일정 추천해줘")));

            assertThat(llmStarted.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(cancellableEngine.cancel("cancel-session")).isTrue();
            assertThat(cancellableEngine.cancel("cancel-session")).isFalse();
            releaseLlm.countDown();

            QueryResult result = future.get(1, TimeUnit.SECONDS);
            assertThat(result.status()).isEqualTo("CANCELLED");
            assertThat(result.message()).doesNotContain("State에 반영");
            assertThat(cancellableEngine.cancel("cancel-session")).isFalse();
        }
    }

    @Test
    void serializesConcurrentRunsForTheSameSession() throws Exception {
        CountDownLatch firstLlmStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstLlm = new CountDownLatch(1);
        CountDownLatch secondLlmStarted = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        AtomicInteger activeCalls = new AtomicInteger();
        AtomicInteger maxActiveCalls = new AtomicInteger();

        QueryEngineService concurrentEngine = new QueryEngineService(
                new ConversationStateRepository(),
                new QueryRuntimeService((instructions, messages) -> {
                    int call = calls.incrementAndGet();
                    int active = activeCalls.incrementAndGet();
                    maxActiveCalls.accumulateAndGet(active, Math::max);
                    try {
                        if (call == 1) {
                            firstLlmStarted.countDown();
                            await(releaseFirstLlm);
                        } else {
                            secondLlmStarted.countDown();
                        }
                        return "추가 질문 " + call;
                    } finally {
                        activeCalls.decrementAndGet();
                    }
                }),
                new RuntimeCancellationRegistry()
        );

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<QueryResult> first = executor.submit(() ->
                    concurrentEngine.submit(new QueryCommand("same-session", "첫 번째 요청")));
            assertThat(firstLlmStarted.await(1, TimeUnit.SECONDS)).isTrue();

            Future<QueryResult> second = executor.submit(() ->
                    concurrentEngine.submit(new QueryCommand("same-session", "두 번째 요청")));
            assertThat(secondLlmStarted.await(200, TimeUnit.MILLISECONDS)).isFalse();

            releaseFirstLlm.countDown();
            assertThat(first.get(1, TimeUnit.SECONDS).status()).isEqualTo("NEED_USER_INPUT");
            assertThat(second.get(1, TimeUnit.SECONDS).status()).isEqualTo("NEED_USER_INPUT");
            assertThat(maxActiveCalls).hasValue(1);
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out while waiting in test");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting in test", exception);
        }
    }
}
