package kr.jm.spring.ai.cli.chatbot;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Optional;

import static org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT;

public class RetrievedDocumentsCliPrintAdvisor implements BaseAdvisor {

    private final int order;

    public RetrievedDocumentsCliPrintAdvisor() {
        // Executes after RetrievalAugmentationAdvisor and before ChatModelCallAdvisor and ChatModelStreamAdvisor (LOWEST_PRECEDENCE).
        this.order = Ordered.LOWEST_PRECEDENCE - 1;
    }

    private static ChatClientRequest loggingRetrievedDocuments(ChatClientRequest chatClientRequest) {
        printSearchResults(
                Optional.ofNullable(chatClientRequest.context().get(DOCUMENT_CONTEXT))
                        .stream().map(documents -> (List<Document>) documents).flatMap(List::stream).toList());
        return chatClientRequest;
    }

    public static void printSearchResults(List<Document> results) {
        System.out.println("\n[ Search Results ]");
        System.out.println("===============================================");

        if (results == null || results.isEmpty()) {
            System.out.println("  No search results found.");
            System.out.println("===============================================");
            return;
        }

        for (int i = 0; i < results.size(); i++) {
            Document document = results.get(i);
            System.out.printf("▶ %d Document, Score: %.2f%n", i + 1, document.getScore());
            System.out.println("-----------------------------------------------");

            String[] lines = document.getText().split("\n");
            for (String line : lines) {
                System.out.printf("%s%n", line);
            }
            System.out.println("===============================================");
        }
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        return loggingRetrievedDocuments(chatClientRequest);
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }
}