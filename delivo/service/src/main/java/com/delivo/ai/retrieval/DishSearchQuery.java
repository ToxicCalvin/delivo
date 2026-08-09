package com.delivo.ai.retrieval;

import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@Builder
public class DishSearchQuery {

    
    private final String keyword;

    
    private final Double maxPrice;

    
    private final Long categoryId;

    
    private final List<Long> excludeCategoryIds;
}
