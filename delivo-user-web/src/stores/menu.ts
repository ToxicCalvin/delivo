import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Category, Dish, Combo } from '@/types';
import { menuService } from '@/services/menu.service';

export const useMenuStore = defineStore('menu', () => {

    const categories = ref<Category[]>([]);
    const dishes = ref<Dish[]>([]);
    const combos = ref<Combo[]>([]);
    const currentCategoryId = ref<string | null>(null);
    const loading = ref<boolean>(false);
    const error = ref<string | null>(null);


    const dishCategories = computed(() => categories.value.filter(c => c.type === 'dish'));
    const comboCategories = computed(() => categories.value.filter(c => c.type === 'combo'));



    const currentCategoryItems = computed(() => {
        if (!currentCategoryId.value) return [];

        const category = categories.value.find(c => c.id === currentCategoryId.value);
        if (!category) return [];

        if (category.type === 'dish') {
            return dishes.value.filter(d => d.categoryId === currentCategoryId.value);
        } else {
            return combos.value.filter(c => c.categoryId === currentCategoryId.value);
        }
    });



    async function fetchCategories() {
        loading.value = true;
        error.value = null;
        try {
            const data: any[] = await menuService.getCategories();


            categories.value = data.map(c => ({
                ...c,
                id: String(c.id),
                type: c.type === 1 ? 'dish' : 'combo'
            }));


            if (!currentCategoryId.value && categories.value.length > 0) {

                currentCategoryId.value = categories.value[0]?.id || null;

            }
        } catch (e: any) {
            error.value = e.message || 'Failed to load categories';
            throw e;
        } finally {
            loading.value = false;
        }
    }

    async function fetchItemsByCategory(categoryId: string) {
        loading.value = true;
        error.value = null;
        try {
            const category = categories.value.find(c => c.id === categoryId);
            if (!category) {
                return;
            }

            if (category.type === 'dish') {
                const data: any[] = await menuService.getDishesByCategory(categoryId);


                const newDishes = data.map(d => ({
                    ...d,
                    id: String(d.id),
                    categoryId: String(d.categoryId),
                    price: Number(d.price),
                    status: d.status === 1 ? 'available' : 'unavailable'
                }));


                dishes.value = [
                    ...dishes.value.filter(d => d.categoryId !== categoryId),
                    ...newDishes
                ];
            } else {
                const data: any[] = await menuService.getCombosByCategory(categoryId);


                const newCombos = data.map(c => ({
                    ...c,
                    id: String(c.id),
                    categoryId: String(c.categoryId),
                    price: Number(c.price),
                    status: c.status === 1 ? 'available' : 'unavailable'
                }));

                combos.value = [
                    ...combos.value.filter(c => c.categoryId !== categoryId),
                    ...newCombos
                ];
            }
        } catch (e: any) {
            error.value = e.message || 'Failed to load items';
            throw e;
        } finally {
            loading.value = false;
        }
    }

    async function setCurrentCategory(categoryId: string) {
        currentCategoryId.value = categoryId;
        await fetchItemsByCategory(categoryId);
    }

    return {
        categories,
        dishes,
        combos,
        currentCategoryId,
        loading,
        error,
        dishCategories,
        comboCategories,
        currentCategoryItems,
        fetchCategories,
        fetchItemsByCategory,
        setCurrentCategory
    };
});
