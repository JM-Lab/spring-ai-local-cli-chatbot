# Spring AI Powered Local CLI Chat Bot

This example is a chatbot you use through the Command Line Interface (CLI). It's inspired by the [spring-ai-cli-chatbot](https://github.com/tzolov/spring-ai-cli-chatbot) project, but unlike the original, it focuses on full local execution and offers AI-driven conversations without requiring external AI services.

## Demo
```
spring-ai-local-cli-chatbot

USER: Tell me about Hurricane Milton.

[ Search Results ]
===============================================
▶ 1 Document, Score: 0.74
-----------------------------------------------
Tropical Depression Fourteen Discussion Number 1 (https://
www.nhc.noaa.gov/archive/2024/al14/al142024.discus.001.shtml?) (Report). Miami, Florida:
National Hurricane Center. Archived (https://web.archive.org/web/20241005151826/https://ww
w.nhc.noaa.gov/archive/2024/al14/al142024.discus.001.shtml) from the original on October 5,
2024. Retrieved October 5, 2024.

===============================================
▶ 2 Document, Score: 0.74
-----------------------------------------------
Brown, Daniel; Blake, Eric (October 7, 2024). Hurricane Milton Update Statement (https://www.nhc.noaa.gov
...
===============================================

ASSISTANT: Hurricane Milton was a powerful Category 5 storm that formed in the Atlantic in 2023. It caused significant damage along the Gulf Coast, particularly in Florida, where it made landfall near Tampa. The storm surge and high winds led to widespread flooding and power outages. Emergency services were overwhelmed, and recovery efforts took several weeks.
```

## Quick Start

Build and run the app:
```
./mvnw clean install
./mvnw spring-boot:run
```

## Auto-configurations

### Configuration Options
The following options have been added to application.properties:
```
# Sets the name of the Spring Boot application.
spring.application.name=spring-ai-local-cli-chatbot
# Specifies the location pattern for RAG (Retrieval-Augmented Generation) documents to be embedded.
spring.application.cli-chatbot.documents-location-pattern=classpath:wikipedia-hurricane-milton-page.pdf
```
#### RAG Document Path Pattern
The project uses PathMatchingResourcePatternResolver to generate the path pattern for RAG documents. This Spring utility allows flexible specification of document locations, enabling the application to dynamically load and embed documents for RAG-based conversations. The patterns can target resources in the classpath or the file system.

#### Pattern Examples
- Classpath Patterns:
classpath:wikipedia-hurricane-milton-page.pdf: Loads a specific PDF file from the classpath.
classpath*:*.pdf: Loads all PDF files from the classpath.
- File System Patterns:
file:/path/to/docs/*.pdf: Loads all PDF files from the specified directory on the file system.

These patterns provide flexibility in defining which documents are used for embedding and retrieval, making the chatbot adaptable to various document sources.

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
5. **Dynamic RAG Document Loading**: Uses PathMatchingResourcePatternResolver for flexible document retrieval.

