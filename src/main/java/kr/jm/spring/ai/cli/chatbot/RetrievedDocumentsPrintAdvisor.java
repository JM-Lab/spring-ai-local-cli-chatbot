package kr.jm.spring.ai.cli.chatbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.document.Document;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

public class RetrievedDocumentsPrintAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(RetrievedDocumentsPrintAdvisor.class);
    private final int order;

    public RetrievedDocumentsPrintAdvisor() {
        this(0);
    }

    public RetrievedDocumentsPrintAdvisor(int order) {this.order = order;}

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        loggingRetrievedDocuments(advisedRequest);
        return chain.nextAroundCall(advisedRequest);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        loggingRetrievedDocuments(advisedRequest);
        return chain.nextAroundStream(advisedRequest);
    }

    private static void loggingRetrievedDocuments(AdvisedRequest advisedRequest) {
        printSearchResults(
                Optional.ofNullable(advisedRequest.adviseContext().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS))
                        .stream().map(documents -> (List<Document>) documents).flatMap(List::stream).toList());
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
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}