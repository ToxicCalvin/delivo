package com.delivo.ai.retrieval;

import com.delivo.entity.Dish;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;


@Getter
@AllArgsConstructor
public class DishSearchResult {

    
    private final List<Dish> dishes;

    public static DishSearchResult empty() {
        return new DishSearchResult(Collections.emptyList());
    }

    public boolean isEmpty() {
        return dishes == null || dishes.isEmpty();
    }
}
