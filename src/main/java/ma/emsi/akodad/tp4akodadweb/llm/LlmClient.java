package ma.emsi.akodad.tp4akodadweb.llm;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.io.Serializable;

@Dependent
public class LlmClient implements Serializable {

    private Assistant assistant;
    private ChatMemory chatMemory;

    @Inject
    private RagDocuments ragDocuments;

    @PostConstruct
    void initialiser() {
        String apiKey = System.getenv("GEMINI_KEY");

        chatMemory = MessageWindowChatMemory.withMaxMessages(20);

        ChatModel model = GoogleGenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3.1-flash-lite")
                .build();

        assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .retrievalAugmentor(ragDocuments.retrievalAugmentor(model))
                .build();
    }

    public void setSystemRole(String systemRole) {
        chatMemory.clear();

        if (systemRole != null && !systemRole.isBlank()) {
            chatMemory.add(SystemMessage.from(systemRole));
        }
    }

    public String chat(String prompt) {
        return assistant.chat(prompt);
    }
}