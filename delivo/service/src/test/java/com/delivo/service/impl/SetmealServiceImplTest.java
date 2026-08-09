package com.delivo.service.impl;

import com.delivo.constant.StatusConstant;
import com.delivo.entity.Dish;
import com.delivo.entity.Setmeal;
import com.delivo.exception.DeletionNotAllowedException;
import com.delivo.exception.SetmealEnableFailedException;
import com.delivo.mapper.DishMapper;
import com.delivo.mapper.SetmealDishMapper;
import com.delivo.mapper.SetmealMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SetmealServiceImplTest {

    @InjectMocks
    private SetmealServiceImpl setmealService;

    @Mock
    private SetmealMapper setmealMapper;

    @Mock
    private SetmealDishMapper setmealDishMapper;

    @Mock
    private DishMapper dishMapper;

    /**
     * Test US 7.1 - AC 1: Successful Enable
     * TC-SET-01: Call startOrStop(1). Mock dishMapper returning only ENABLED
     * dishes.
     */
    @Test
    public void testStartOrStop_EnableSetmeal_AllDishesEnabled_Success() {
        // [Given]
        Long setmealId = 1L;
        Integer status = StatusConstant.ENABLE;

        // Mock 2 dishes in the set meal, both are ENABLED
        Dish dish1 = new Dish();
        dish1.setStatus(StatusConstant.ENABLE);
        Dish dish2 = new Dish();
        dish2.setStatus(StatusConstant.ENABLE);

        when(dishMapper.getBySetmealId(setmealId)).thenReturn(Arrays.asList(dish1, dish2));

        // [When]
        setmealService.startOrStop(status, setmealId);

        // [Then]
        // Verify setmealMapper.update was called to change the status
        verify(setmealMapper, times(1)).update(any(Setmeal.class));
    }

    /**
     * Test US 7.1 - AC 2: Cannot Enable if Dish Disabled
     * TC-SET-02: Call startOrStop(1). Mock dishMapper returning a list containing a
     * DISABLED dish.
     */
    @Test
    public void testStartOrStop_EnableSetmeal_ContainsDisabledDish_ThrowsException() {
        // [Given]
        Long setmealId = 2L;
        Integer status = StatusConstant.ENABLE;

        // Mock 2 dishes, one is ENABLED, one is DISABLED
        Dish dish1 = new Dish();
        dish1.setStatus(StatusConstant.ENABLE);
        Dish dish2 = new Dish();
        dish2.setStatus(StatusConstant.DISABLE); // This should trigger the exception

        when(dishMapper.getBySetmealId(setmealId)).thenReturn(Arrays.asList(dish1, dish2));

        // [When & Then]
        assertThrows(SetmealEnableFailedException.class, () -> {
            setmealService.startOrStop(status, setmealId);
        });

        // Verify the setmeal was NOT updated
        verify(setmealMapper, never()).update(any(Setmeal.class));
    }

    /**
     * Test US 7.2 - AC 1: Valid Deletion
     * TC-SET-03: Call deleteBatch on a DISABLED set meal.
     */
    @Test
    public void testDeleteBatch_DisabledSetmeals_DeletesSuccessfully() {
        // [Given]
        List<Long> ids = Arrays.asList(3L, 4L);

        Setmeal disabledSetmeal1 = new Setmeal();
        disabledSetmeal1.setStatus(StatusConstant.DISABLE);

        Setmeal disabledSetmeal2 = new Setmeal();
        disabledSetmeal2.setStatus(StatusConstant.DISABLE);

        when(setmealMapper.getById(3L)).thenReturn(disabledSetmeal1);
        when(setmealMapper.getById(4L)).thenReturn(disabledSetmeal2);

        // [When]
        setmealService.deleteBatch(ids);

        // [Then]
        // Verify tables are cleared for both IDs
        verify(setmealMapper, times(1)).deleteById(3L);
        verify(setmealDishMapper, times(1)).deleteBySetmealId(3L);

        verify(setmealMapper, times(1)).deleteById(4L);
        verify(setmealDishMapper, times(1)).deleteBySetmealId(4L);
    }

    /**
     * Test US 7.2 - AC 2: Cannot Delete Enabled Set Meal
     * TC-SET-04: Call deleteBatch on an ENABLED set meal.
     */
    @Test
    public void testDeleteBatch_EnabledSetmeal_ThrowsException() {
        // [Given]
        List<Long> ids = Arrays.asList(5L);

        Setmeal enabledSetmeal = new Setmeal();
        enabledSetmeal.setStatus(StatusConstant.ENABLE); // On sale

        when(setmealMapper.getById(5L)).thenReturn(enabledSetmeal);

        // [When & Then]
        assertThrows(DeletionNotAllowedException.class, () -> {
            setmealService.deleteBatch(ids);
        });

        // Verify deletion was aborted
        verify(setmealMapper, never()).deleteById(anyLong());
        verify(setmealDishMapper, never()).deleteBySetmealId(anyLong());
    }
}
