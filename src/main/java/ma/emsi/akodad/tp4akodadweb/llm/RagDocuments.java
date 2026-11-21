package ma.emsi.akodad.tp4akodadweb.llm;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class RagDocuments {

    private static final Logger LOGGER = Logger.getLogger(RagDocuments.class.getName());

    private ContentRetriever cours;
    private ContentRetriever recettes;

    @PostConstruct
    void initialiser() {
        DocumentParser parser = new ApacheTikaDocumentParser();
        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        cours = indexer("rag.pdf", parser, embeddingModel);
        recettes = indexer("autre.pdf", parser, embeddingModel);
    }

    public RetrievalAugmentor retrievalAugmentor(ChatModel model) {
        Map<ContentRetriever, String> descriptions = new LinkedHashMap<>();
        descriptions.put(cours,
                "Support de cours sur l'intelligence artificielle : RAG, fine-tuning, "
                        + "embeddings et modèles de langage.");
        descriptions.put(recettes,
                "Livre de recettes NEFF : plats, ingrédients, quantités, préparation "
                        + "et cuisson. Il contient notamment la recette des croquettes de poulet.");

        return DefaultRetrievalAugmentor.builder()
                .queryRouter(new LanguageModelQueryRouter(model, descriptions))
                .build();
    }

    private ContentRetriever indexer(String fichier, DocumentParser parser,
                                     EmbeddingModel embeddingModel) {
        Document document = ClassPathDocumentLoader.loadDocument(fichier, parser);
        List<TextSegment> segments = DocumentSplitters.recursive(600, 100).split(document);

        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        store.addAll(embeddingModel.embedAll(segments).content(), segments);

        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(4)
                .minScore(0.5)
                .build();

        return query -> {
            LOGGER.info(() -> "RAG - PDF consulte : " + fichier);
            return retriever.retrieve(query);
        };
    }
}