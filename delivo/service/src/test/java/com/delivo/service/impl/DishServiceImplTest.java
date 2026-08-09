package com.delivo.service.impl;

import com.delivo.constant.StatusConstant;
import com.delivo.entity.Dish;
import com.delivo.exception.DeletionNotAllowedException;
import com.delivo.mapper.DishFlavorMapper;
import com.delivo.mapper.DishMapper;
import com.delivo.mapper.SetmealDishMapper;
import com.delivo.mapper.SetmealMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DishServiceImplTest {

    @InjectMocks
    private DishServiceImpl dishService;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private DishFlavorMapper dishFlavorMapper;

    @Mock
    private SetmealDishMapper setmealDishMapper;

    @Mock
    private SetmealMapper setmealMapper;

    /**
     * Test US 6.1 - AC 1: Valid Deletion
     * TC-DSH-01: Call deleteBatch with valid disabled dish IDs.
     */
    @Test
    public void testDeleteBatch_ValidDisabledDishes_DeletesSuccessfully() {
        // [Given]
        List<Long> ids = Arrays.asList(1L, 2L);

        Dish disabledDish1 = new Dish();
        disabledDish1.setId(1L);
        disabledDish1.setStatus(StatusConstant.DISABLE);

        Dish disabledDish2 = new Dish();
        disabledDish2.setId(2L);
        disabledDish2.setStatus(StatusConstant.DISABLE);

        when(dishMapper.getById(1L)).thenReturn(disabledDish1);
        when(dishMapper.getById(2L)).thenReturn(disabledDish2);

        // Mock that these dishes are not linked to any setmeals
        when(setmealDishMapper.getSetmealIdsByDishIds(ids)).thenReturn(new ArrayList<>());

        // [When]
        dishService.deleteBatch(ids);

        // [Then]
        // Verify delete is called for both the dish and its flavors
        verify(dishMapper, times(1)).deleteById(1L);
        verify(dishFlavorMapper, times(1)).deleteByDishId(1L);

        verify(dishMapper, times(1)).deleteById(2L);
        verify(dishFlavorMapper, times(1)).deleteByDishId(2L);
    }

    /**
     * Test US 6.1 - AC 2: Cannot Delete Enabled Dish
     * TC-DSH-02: Call deleteBatch on an ENABLED dish. Throws
     * DeletionNotAllowedException.
     */
    @Test
    public void testDeleteBatch_EnabledDish_ThrowsException() {
        // [Given]
        List<Long> ids = Arrays.asList(3L);

        Dish enabledDish = new Dish();
        enabledDish.setId(3L);
        enabledDish.setStatus(StatusConstant.ENABLE); // On sale

        when(dishMapper.getById(3L)).thenReturn(enabledDish);

        // [When & Then]
        assertThrows(DeletionNotAllowedException.class, () -> {
            dishService.deleteBatch(ids);
        });

        // Verify delete was completely aborted
        verify(dishMapper, never()).deleteById(anyLong());
        verify(dishFlavorMapper, never()).deleteByDishId(anyLong());
    }

    /**
     * Test US 6.1 - AC 3: Cannot Delete Linked Dish
     * TC-DSH-03: Call deleteBatch on a dish linked to setmeal. Throws
     * DeletionNotAllowedException.
     */
    @Test
    public void testDeleteBatch_LinkedToSetmeal_ThrowsException() {
        // [Given]
        List<Long> ids = Arrays.asList(4L);

        Dish disabledDish = new Dish();
        disabledDish.setId(4L);
        disabledDish.setStatus(StatusConstant.DISABLE);

        when(dishMapper.getById(4L)).thenReturn(disabledDish);

        // Mock that this dish IS linked to a setmeal
        when(setmealDishMapper.getSetmealIdsByDishIds(ids)).thenReturn(Arrays.asList(99L));

        // [When & Then]
        assertThrows(DeletionNotAllowedException.class, () -> {
            dishService.deleteBatch(ids);
        });

        // Verify delete was completely aborted
        verify(dishMapper, never()).deleteById(anyLong());
        verify(dishFlavorMapper, never()).deleteByDishId(anyLong());
    }

    /**
     * Test US 6.2 - AC 1: Cascade Disable to Set Meals
     * TC-DSH-04: Call startOrStop with STATUS=DISABLE. verify setmeal status is
     * also updated.
     */
    @Test
    public void testStartOrStop_DisableDish_CascadesToSetmeal() {
        // [Given]
        Long dishId = 5L;
        Integer newStatus = StatusConstant.DISABLE;

        // Mock that this dish is linked to setmeal ID 100 and 101
        List<Long> dishIds = Arrays.asList(dishId);
        List<Long> setmealIds = Arrays.asList(100L, 101L);
        when(setmealDishMapper.getSetmealIdsByDishIds(dishIds)).thenReturn(setmealIds);

        // [When]
        dishService.startOrStop(newStatus, dishId);

        // [Then]
        // 1. Verify dish status gets explicitly updated
        verify(dishMapper, times(1)).update(any(Dish.class));

        // 2. Verify setmeal status gets updated for EACH linked setmeal (cascading
        // effect)
        verify(setmealMapper, times(2)).update(any());
    }
}
