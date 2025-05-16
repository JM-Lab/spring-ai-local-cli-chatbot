package kr.jm.spring.ai.cli.chatbot;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootApplication
public class SpringAiCliChatbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiCliChatbotApplication.class, args);
    }

    @Bean
    public CommandLineRunner cli(ChatClient.Builder chatClientBuilder, VectorStore vectorStore,
            DocumentReader[] documentReaders, TextSplitter textSplitter,
            RetrievalAugmentationAdvisor retrievalAugmentationAdvisor, Advisor[] advisors,
            @Value("${spring.application.name}") String applicationName,
            @Value("${spring.application.cli-chatbot.system-prompt}") String systemPrompt) {
        return args -> {

            // 1. Load the documents in vector store
            Arrays.stream(documentReaders).map(DocumentReader::read).map(textSplitter::split).forEach(vectorStore::add);

            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.getLogger("ROOT").detachAppender("CONSOLE");

            // 2. Create the ChatClient with chat memory and RAG support
            var chatClient = chatClientBuilder
                    .defaultSystem(systemPrompt) // Set the system prompt
                    .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder()
                            .maxMessages(10).build()).build()) // Enable chat memory
                    .defaultAdvisors(retrievalAugmentationAdvisor) // Enable RAG
                    .defaultAdvisors(advisors)
                    .build();

            // 3. Start the chat loop
            System.out.println("\n" + applicationName);
            AtomicBoolean isFirst = new AtomicBoolean(true);
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("\nUSER: ");
                    String userMessage = scanner.nextLine();
                    chatClient.prompt(userMessage).stream().content().toStream().forEach(s -> {
                        if (isFirst.get()) {
                            isFirst.set(false);
                            System.out.print("\nASSISTANT: ");
                        }
                        System.out.print(s);
                    });
                    System.out.println();
                    isFirst.set(true);
                }
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    @ConditionalOnMissingBean(DocumentReader.class)
    public DocumentReader[] documentReaders(
            @Value("${spring.application.cli-chatbot.documents-location-pattern}") String documentsLocationPattern) throws
            IOException {
        return Arrays.stream(new PathMatchingResourcePatternResolver().getResources(documentsLocationPattern))
                .map(TikaDocumentReader::new).toArray(DocumentReader[]::new);
    }

    @Bean
    @ConditionalOnMissingBean(TextSplitter.class)
    public TextSplitter textSplitter() {
        return new TokenTextSplitter();
    }

    @Bean
    @ConditionalOnMissingBean(RetrievalAugmentationAdvisor.class)
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(VectorStore vectorStore) {
        return RetrievalAugmentationAdvisor.builder().documentRetriever(
                VectorStoreDocumentRetriever.builder().similarityThreshold(0.50).vectorStore(vectorStore)
                        .build()).build();
    }

    @Bean
    public Advisor[] advisors() {
        return new Advisor[]{new RetrievedDocumentsCliPrintAdvisor(), new SimpleLoggerAdvisor()};
    }

}
