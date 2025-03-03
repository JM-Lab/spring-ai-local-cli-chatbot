package kr.jm.spring.ai.cli.chatbot;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
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

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootApplication
public class SpringAiCliChtbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiCliChtbotApplication.class, args);
    }

    @Bean
    public CommandLineRunner cli(
            @Value("${spring.application.cli-chatbot.documents-location-pattern}") String documentsLocationPattern,
            ChatClient.Builder chatClientBuilder, VectorStore vectorStore, TokenTextSplitter tokenTextSplitter,
            @Value("${spring.application.name}") String applicationName) {
        return args -> {

            // 1. Load the hurricane documents in vector store
            Arrays.stream(new PathMatchingResourcePatternResolver().getResources(documentsLocationPattern))
                    .map(TikaDocumentReader::new).map(DocumentReader::read).map(tokenTextSplitter::split)
                    .forEach(vectorStore::add);

            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.getLogger("ROOT").detachAppender("CONSOLE");

            // 2. Create the ChatClient with chat memory and RAG support
            var chatClient = chatClientBuilder
                    .defaultSystem("You are useful assistant, expert in hurricanes.") // Set the system prompt
                    .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory())) // Enable chat memory
                    .defaultAdvisors(new RetrievedDocumentsPrintAdvisor())
                    .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore)) // Enable RAG
                    .defaultAdvisors(new SimpleLoggerAdvisor())
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
    @ConditionalOnMissingBean(TokenTextSplitter.class)
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

}
