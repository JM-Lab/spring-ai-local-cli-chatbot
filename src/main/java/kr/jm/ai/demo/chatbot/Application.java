package kr.jm.ai.demo.chatbot;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import java.util.Scanner;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner cli(@Value("classpath:wikipedia-hurricane-milton-page.pdf") Resource hurricaneDocs,
            ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {

        return args -> {

            // 1. Load the hurricane documents in vector store
            vectorStore.add(new TokenTextSplitter().split(new PagePdfDocumentReader(hurricaneDocs).read()));

            // 2. Create the ChatClient with chat memory and RAG support
            var chatClient = chatClientBuilder
                    .defaultSystem("You are useful assistant, expert in hurricanes.") // Set the system prompt
                    .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory())) // Enable chat memory
                    .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore)) // Enable RAG
                    .build();

            // 3. Start the chat loop
            System.out.println("\nI am your Hurricane Milton assistant.\n");
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("\nUSER: ");
                    String userMessage = scanner.nextLine();
                    System.out.print("\nASSISTANT: ");
                    chatClient.prompt(userMessage).stream().content().toStream().forEach(System.out::print);
                    System.out.println();
                }
            }
        };
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel){
        return new SimpleVectorStore(embeddingModel);
    }

}
