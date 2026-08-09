package com.delivo.ai.retrieval;

import com.delivo.entity.Category;
import com.delivo.entity.Dish;
import com.delivo.entity.DishFlavor;
import com.delivo.mapper.CategoryMapper;
import com.delivo.mapper.DishFlavorMapper;
import com.delivo.mapper.DishMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class DishVectorStoreLoader implements ApplicationRunner {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private EmbeddingModel embeddingModel;

    private final InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    private static final Path CACHE_PATH = Path.of("data/dish-embeddings.json");

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return embeddingStore;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("AI: Loading all dishes into InMemoryEmbeddingStore...");

        Dish query = new Dish();
        query.setStatus(1);
        List<Dish> dishes = dishMapper.list(query);

        if (dishes == null || dishes.isEmpty()) {
            log.warn("AI: No active dishes found in database.");
            return;
        }

        Map<Long, String> categoryNameMap = loadCategoryNames();
        Map<Long, List<DishFlavor>> flavorsByDishId = loadFlavors(dishes);

        List<TextSegment> segments = new ArrayList<>();
        for (Dish dish : dishes) {
            segments.add(TextSegment.from(
                    buildText(dish, categoryNameMap, flavorsByDishId),
                    buildMetadata(dish, categoryNameMap)));
        }

        if (loadFromCache(segments.size())) {
            return;
        }

        try {
            Response<List<Embedding>> batchResponse = embeddingModel.embedAll(segments);
            embeddingStore.addAll(batchResponse.content(), segments);
            log.info("AI: Successfully batch-embedded {} dishes into Vector Store.", segments.size());
        } catch (Exception e) {
            log.error("AI: Batch embedding failed, falling back to one-by-one embedding", e);
            int loadedCount = 0;
            for (TextSegment segment : segments) {
                try {
                    embeddingStore.add(embeddingModel.embed(segment).content(), segment);
                    loadedCount++;
                } catch (Exception ex) {
                    log.error("Failed to embed segment: {}",
                            segment.text().substring(0, Math.min(50, segment.text().length())), ex);
                }
            }
            log.info("AI: Fallback embedded {} / {} dishes.", loadedCount, segments.size());
        }

        saveToCache();
    }

    
    private String buildText(Dish dish,
                             Map<Long, String> categoryNameMap,
                             Map<Long, List<DishFlavor>> flavorsByDishId) {
        StringBuilder content = new StringBuilder();
        content.append(dish.getName());

        if (dish.getDescription() != null && !dish.getDescription().isBlank()) {
            content.append(". ").append(dish.getDescription());
        }

        List<DishFlavor> flavors = flavorsByDishId.get(dish.getId());
        if (flavors != null && !flavors.isEmpty()) {
            content.append(". Flavors: ");
            flavors.forEach(f ->
                    content.append(f.getName()).append("-").append(f.getValue()).append(", "));
        }

        return content.toString();
    }

    private Metadata buildMetadata(Dish dish, Map<Long, String> categoryNameMap) {
        String categoryName = categoryNameMap.getOrDefault(dish.getCategoryId(), "General");

        Metadata metadata = Metadata.from("dishId", dish.getId())
                .put("dishName", dish.getName())
                .put("price", dish.getPrice().doubleValue())
                .put("categoryName", categoryName);

        if (dish.getCategoryId() != null) {
            metadata.put("categoryId", dish.getCategoryId());
        }
        return metadata;
    }

    
    public void refreshIndices() {
        invalidateCache();
        log.info("AI: Embedding cache cleared. Restart the server to refresh vector search.");
    }

    private Map<Long, String> loadCategoryNames() {
        Map<Long, String> map = new HashMap<>();
        List<Category> categories = categoryMapper.list(null);
        if (categories != null) {
            for (Category cat : categories) map.put(cat.getId(), cat.getName());
        }
        return map;
    }

    private Map<Long, List<DishFlavor>> loadFlavors(List<Dish> dishes) {
        Map<Long, List<DishFlavor>> map = new HashMap<>();
        for (Dish dish : dishes) {
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(dish.getId());
            if (flavors != null && !flavors.isEmpty()) map.put(dish.getId(), flavors);
        }
        return map;
    }

    

    private boolean loadFromCache(int expectedSegmentCount) {
        if (!Files.exists(CACHE_PATH)) {
            return false;
        }
        try {
            String json = Files.readString(CACHE_PATH);
            InMemoryEmbeddingStore<TextSegment> cached = InMemoryEmbeddingStore.fromJson(json);

            int cachedSize = cached.findRelevant(
                    embeddingModel.embed("test").content(), expectedSegmentCount + 10, 0.0
            ).size();

            if (cachedSize != expectedSegmentCount) {
                log.info("AI: Cache stale (cached={}, expected={}). Re-embedding.",
                        cachedSize, expectedSegmentCount);
                Files.deleteIfExists(CACHE_PATH);
                return false;
            }

            var entries = cached.findRelevant(
                    embeddingModel.embed("test").content(), expectedSegmentCount, 0.0);
            for (var entry : entries) {
                embeddingStore.add(entry.embedding(), entry.embedded());
            }

            log.info("AI: Loaded {} dishes from embedding cache.", cachedSize);
            return true;
        } catch (Exception e) {
            log.warn("AI: Failed to load embedding cache, will re-embed.", e);
            try { Files.deleteIfExists(CACHE_PATH); } catch (IOException ignored) {}
            return false;
        }
    }

    private void saveToCache() {
        try {
            Files.createDirectories(CACHE_PATH.getParent());
            Files.writeString(CACHE_PATH, embeddingStore.serializeToJson());
            log.info("AI: Cached embedding store to {}", CACHE_PATH);
        } catch (IOException e) {
            log.warn("AI: Failed to cache embedding store.", e);
        }
    }

    
    public static void invalidateCache() {
        try {
            Files.deleteIfExists(CACHE_PATH);
        } catch (IOException e) {
            
        }
    }
}
