package com.delivo.service.impl;

import com.delivo.constant.StatusConstant;
import com.delivo.dto.CategoryDTO;
import com.delivo.entity.Category;
import com.delivo.exception.DeletionNotAllowedException;
import com.delivo.mapper.CategoryMapper;
import com.delivo.mapper.DishMapper;
import com.delivo.mapper.SetmealMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private SetmealMapper setmealMapper;

    /**
     * User Story 4.1 - AC 1: Delete an empty category.
     * TC-CAT-01: Call deleteById. Mock DB returning 0 for both dishMapper.count and
     * setmealMapper.count.
     */
    @Test
    public void testDeleteById_NoLinkedItems_DeletesSuccessfully() {
        // [Given]
        Long categoryId = 1L;
        // Simulate a scenario where there are neither dishes nor set meals under the current category.
        when(dishMapper.countByCategoryId(categoryId)).thenReturn(0);
        when(setmealMapper.countByCategoryId(categoryId)).thenReturn(0);

        // [When]
        categoryService.deleteById(categoryId);

        // [Then]
        verify(categoryMapper, times(1)).deleteById(categoryId);
    }

    /**
     * User Story 4.1 - AC 2: Delete a category linked to dishes.
     * TC-CAT-02: Call deleteById. Mock DB returning count > 0 for
     * dishMapper.countByCategoryId.
     */
    @Test
    public void testDeleteById_LinkedToDishes_ThrowsException() {
        // [Given]
        Long categoryId = 1L;
        when(dishMapper.countByCategoryId(categoryId)).thenReturn(2);

        // [When & Then]
        DeletionNotAllowedException exception = assertThrows(DeletionNotAllowedException.class, () -> {
            categoryService.deleteById(categoryId);
        });

        verify(categoryMapper, never()).deleteById(anyLong());
    }

    /**
     * User Story 4.1 - AC 3: Delete a category linked to set meals.
     * TC-CAT-03: Call deleteById. Mock DB returning 0 for dish, but count > 0 for
     * setmealMapper.count.
     */
    @Test
    public void testDeleteById_LinkedToSetmeals_ThrowsException() {
        // [Given]
        Long categoryId = 1L;
        when(dishMapper.countByCategoryId(categoryId)).thenReturn(0);
        // However, it is associated with 1 setmeal.
        when(setmealMapper.countByCategoryId(categoryId)).thenReturn(1);

        // [When & Then]
        assertThrows(DeletionNotAllowedException.class, () -> {
            categoryService.deleteById(categoryId);
        });

        verify(categoryMapper, never()).deleteById(anyLong());
    }

    /**
     * User Story 4.2 - AC 1: Add a new category defaults to status 0 (Disabled).
     * TC-CAT-04: Call save with a new CategoryDTO. Assert mapped entity has status
     * set to 0.
     */
    @Test
    public void testSave_NewCategory_StatusIsDisabled() {
        // [Given]
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Burger");
        dto.setType(1);
        dto.setSort(10);

        // [When]
        categoryService.save(dto);

        // [Then]
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryMapper, times(1)).insert(categoryCaptor.capture());

        Category savedCategory = categoryCaptor.getValue();
        // 验证保存的分类名和DTO一致
        assertEquals("Burger", savedCategory.getName());
        // 验证状态是否正确设置为 0 (DISABLE)
        assertEquals(StatusConstant.DISABLE, savedCategory.getStatus());
    }
}
