# Spring AI Powered Local CLI Chat Bot

This example is a chatbot you use through the Command Line Interface (CLI). It's inspired by the [spring-ai-cli-chatbot](https://github.com/tzolov/spring-ai-cli-chatbot) project, but unlike the original, it focuses on full local execution and offers AI-driven conversations without requiring external AI services.

## Quick Start

Build and run the app:
```
./mvnw clean install
./mvnw spring-boot:run
```

## Auto-configurations

### AI Model

Spring AI works with **Ollama** for local LLM and embedding models. No API keys are required.
To enable this, simply add the following dependency:
```xml
<dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
</dependency>
```
> **Before using Ollama with Spring AI**, ensure that Ollama is installed and running on your system, refer to the [Spring AI Ollama Chat Prerequisites](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html#_prerequisites).

To use different models for local LLM and embedding in your Spring Boot application with Ollama, you can specify them using command-line arguments. Here’s how to do it:
```
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.ai.ollama.chat.options.model=llama3.2:1b --spring.ai.ollama.embedding.options.model=bge-m3"
```
#### Tips

- For additional configuration options in **Spring AI with Ollama**:
    - **LLM Settings**: [LLM Properties Documentation](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html#_chat_properties)
    - **Embedding Settings**: [Embedding Properties Documentation](https://docs.spring.io/spring-ai/reference/api/embeddings/ollama-embeddings.html#_embedding_properties)

- For a list of models supported by **Ollama**:
    - [LLM Models](https://github.com/ollama/ollama?tab=readme-ov-file#model-library)
    - [Embedding Models](https://ollama.com/search?c=embedding)

### Vector Store
```java
@Bean
public VectorStore vectorStore(EmbeddingModel embeddingModel){
    return new SimpleVectorStore(embeddingModel);
}
```
The project uses Spring AI's SimpleVectorStore, eliminating the need for external vector databases.

### PDF Document Processing

PDF document reading capability is enabled through the `spring-ai-pdf-document-reader` dependency.

## Implementation Details
### Vector Store Loading
```java
vectorStore.add(new TokenTextSplitter().split(new PagePdfDocumentReader(localDocs).read()));
```
### Chat Loop
```java
try (Scanner scanner = new Scanner(System.in)) {
    while (true) {
        System.out.print("\nUSER: ");
        String userMessage = scanner.nextLine();
        System.out.print("\nASSISTANT: ");
        chatClient.prompt(userMessage).stream().content().toStream().forEach(System.out::print);
        System.out.println();
    }
}
```
Creates an interactive loop for chatbot interaction, **streaming the assistant’s response in real-time**.

## Key Features

1. **Fully Local Execution**: All components run on your local machine, ensuring privacy and offline capability.
2. **Ollama Integration**: Uses Ollama for both LLM and embedding models, offering high-performance local AI processing.
3. **Spring AI's SimpleVectorStore**: Implements a lightweight vector store for efficient information retrieval without external dependencies.
4. **Interactive Console Interface**: Provides a streamlined console-based interface for real-time chatbot interaction.

