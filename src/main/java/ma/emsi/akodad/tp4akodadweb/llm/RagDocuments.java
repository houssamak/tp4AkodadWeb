package ma.emsi.akodad.tp4akodadweb.llm;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class RagDocuments {

    private RetrievalAugmentor retrievalAugmentor;

    @PostConstruct
    void initialiser() {
        var parser = new ApacheTikaDocumentParser();
        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        ContentRetriever cours = indexer("rag.pdf", parser, embeddingModel);
        ContentRetriever recettes = indexer("autre.pdf", parser, embeddingModel);

        retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(new DefaultQueryRouter(cours, recettes))
                .build();
    }

    public RetrievalAugmentor retrievalAugmentor() {
        return retrievalAugmentor;
    }

    private ContentRetriever indexer(String fichier, ApacheTikaDocumentParser parser,
                                     EmbeddingModel embeddingModel) {
        Document document = ClassPathDocumentLoader.loadDocument(fichier, parser);
        List<TextSegment> segments = DocumentSplitters.recursive(600, 100).split(document);

        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        store.addAll(embeddingModel.embedAll(segments).content(), segments);

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(4)
                .minScore(0.5)
                .build();
    }
}