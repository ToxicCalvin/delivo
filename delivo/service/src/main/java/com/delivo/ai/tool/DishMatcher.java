package com.delivo.ai.tool;

import com.delivo.entity.Dish;
import com.delivo.mapper.DishMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class DishMatcher implements ApplicationRunner {

    @Autowired
    private DishMapper dishMapper;

    private volatile List<Dish> cachedDishes = new ArrayList<>();

    @Override
    public void run(ApplicationArguments args) {
        refresh();
    }

    public void refresh() {
        try {
            Dish query = new Dish();
            query.setStatus(1);
            cachedDishes = dishMapper.list(query);
            log.info("DishMatcher: loaded {} active dishes into cache", cachedDishes.size());
        } catch (Exception e) {
            log.error("DishMatcher: failed to load dish cache", e);
        }
    }

    public List<Dish> getAllDishes() {
        return cachedDishes;
    }

    
    public Dish findBestMatch(String dishName) {
        if (dishName == null || dishName.trim().isEmpty()) {
            return null;
        }

        String searchUpper = dishName.trim().toUpperCase();

        
        for (Dish d : cachedDishes) {
            if (d.getName().toUpperCase().equals(searchUpper)) {
                return d;
            }
        }

        
        Dish bestMatch = null;
        double bestScore = 0.0;

        for (Dish d : cachedDishes) {
            String dbNameUpper = d.getName().toUpperCase();
            double score = overlapScore(searchUpper, dbNameUpper);
            if (score > bestScore && score >= 0.5) {
                bestScore = score;
                bestMatch = d;
            }
        }

        return bestMatch;
    }

    
    private double overlapScore(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) return 0.0;

        
        if (b.contains(a)) return (double) a.length() / b.length() + 0.3; 
        if (a.contains(b)) return (double) b.length() / a.length() + 0.3;

        
        String[] wordsA = a.split("\\s+");
        String[] wordsB = b.split("\\s+");
        int matches = 0;
        for (String wa : wordsA) {
            for (String wb : wordsB) {
                if (wa.equals(wb)) {
                    matches++;
                    break;
                }
            }
        }
        int union = wordsA.length + wordsB.length - matches;
        return union > 0 ? (double) matches / union : 0.0;
    }
}
