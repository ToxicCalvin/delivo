package com.delivo.ai.retrieval;

import com.delivo.entity.Dish;
import com.delivo.mapper.DishMapper;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;


@Service
@Slf4j
public class DishSearchService {

    
    private static final int MAX_RESULTS = 5;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private DishMapper dishMapper;

    
    /**
     * Executes a vector similarity search to find dishes that match the user's query.
     * Uses the Langchain4j EmbeddingStore to retrieve relevant Dish metadata
     * and filters results based on price and category constraints.
     * @param query The search query containing keywords and filters
     * @return The formatted search result containing matching dishes
     */
    public DishSearchResult search(DishSearchQuery query) {
        long startTime = System.currentTimeMillis();

        String keyword = (query.getKeyword() == null || query.getKeyword().isBlank())
                ? "popular food dish meal drink dessert" : query.getKeyword().trim();

        log.info("DishSearchService: keyword='{}', maxPrice={}, categoryId={}",
                keyword, query.getMaxPrice(), query.getCategoryId());

        
        Filter filter = buildMetadataFilter(query.getMaxPrice(), query.getCategoryId(), query.getExcludeCategoryIds());
        EmbeddingStoreContentRetriever.EmbeddingStoreContentRetrieverBuilder retrieverBuilder =
                EmbeddingStoreContentRetriever.builder()
                        .embeddingStore(embeddingStore)
                        .embeddingModel(embeddingModel)
                        .minScore(0.3)
                        .maxResults(MAX_RESULTS);
        if (filter != null) retrieverBuilder.filter(filter);

        
        List<Content> results = retrieverBuilder.build().retrieve(Query.from(keyword));

        long elapsed = System.currentTimeMillis() - startTime;

        if (results.isEmpty()) {
            log.info("DishSearchService: no results. keyword='{}', elapsed={}ms", keyword, elapsed);
            return DishSearchResult.empty();
        }

        
        List<Long> topIds = results.stream()
                .map(c -> c.textSegment().metadata().getLong("dishId"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Dish> dishes = resolveDishes(topIds);

        log.info("DishSearchService done: keyword='{}' → resolved {} dishes (elapsed={}ms)",
                keyword, dishes.size(), elapsed);

        return new DishSearchResult(dishes);
    }

    
    private List<Dish> resolveDishes(List<Long> orderedIds) {
        if (orderedIds.isEmpty()) return Collections.emptyList();
        Dish query = new Dish();
        query.setStatus(1);
        List<Dish> all = dishMapper.list(query);
        Map<Long, Dish> byId = new HashMap<>();
        for (Dish d : all) byId.put(d.getId(), d);

        List<Dish> ordered = new ArrayList<>(orderedIds.size());
        for (Long id : orderedIds) {
            Dish d = byId.get(id);
            if (d != null) ordered.add(d);
        }
        return ordered;
    }

    private Filter buildMetadataFilter(Double maxPrice, Long categoryId, java.util.List<Long> excludeCategoryIds) {
        Filter filter = null;
        if (maxPrice != null) {
            filter = metadataKey("price").isLessThanOrEqualTo(maxPrice);
        }
        if (categoryId != null) {
            Filter f = metadataKey("categoryId").isEqualTo(categoryId);
            filter = (filter == null) ? f : filter.and(f);
        }
        if (excludeCategoryIds != null) {
            for (Long excludeId : excludeCategoryIds) {
                Filter f = metadataKey("categoryId").isNotEqualTo(excludeId);
                filter = (filter == null) ? f : filter.and(f);
            }
        }
        return filter;
    }
}
