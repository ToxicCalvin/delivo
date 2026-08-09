package com.delivo.service.impl;

import com.delivo.context.BaseContext;
import com.delivo.dto.ShoppingCartDTO;
import com.delivo.entity.Dish;
import com.delivo.entity.Setmeal;
import com.delivo.entity.ShoppingCart;
import com.delivo.mapper.DishMapper;
import com.delivo.mapper.SetmealMapper;
import com.delivo.mapper.ShoppingCartMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingCartServiceImplTest {

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    @Mock
    private ShoppingCartMapper shoppingCartMapper;
    @Mock
    private DishMapper dishMapper;
    @Mock
    private SetmealMapper setmealMapper;

    @BeforeEach
    public void setUp() {
        // 模拟当前登录用户 userId = 101
        BaseContext.setCurrentId(101L);
    }

    @AfterEach
    public void tearDown() {
        // 测试结束后清理 ThreadLocal 防止内存泄漏及对其他测试的影响
        BaseContext.removeCurrentId();
    }

    /**
     * Test Part B, Module 3, US 1 - AC 2: Existing Item (Increment)
     * TC-CRT-02: Call addShoppingCart with a dish ID already in cart.
     * Expected: shoppingCartMapper.updateNumberById() increments quantity,
     * no new row inserted.
     */
    @Test
    public void testAddShoppingCart_ItemAlreadyExists_IncreaseNumber() {
        // [Given] 预设条件
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(10L); // 用户点击了ID为10的菜品

        List<ShoppingCart> mockCartList = new ArrayList<>();
        ShoppingCart cartInDb = new ShoppingCart();
        cartInDb.setId(100L); // 数据库中该条购物车记录的主键
        cartInDb.setDishId(10L);
        cartInDb.setNumber(1); // 数据库中原本就有一份了
        mockCartList.add(cartInDb);

        // 模拟：当由 Service 尝试查询这个商品在不在购物车里时，返回在数据库的模拟记录
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(mockCartList);

        // [When] 执行方法
        shoppingCartService.addShoppingCart(dto);

        // [Then] 验证结果
        // 1. 验证数量被修改：验证 mapper 是否成功调用了 updateNumberById
        verify(shoppingCartMapper, times(1)).updateNumberById(any(ShoppingCart.class));
        // 2. 验证：由于已经存在，所以不可能调用 insert 插入
        verify(shoppingCartMapper, never()).insert(any(ShoppingCart.class));
    }

    /**
     * Test Part B, Module 3, US 1 - AC 1: New Item (Dish)
     * TC-CRT-01: Call addShoppingCart with a dish ID, cart is empty.
     * Expected: dishMapper.getById() hydrates the entry, shoppingCartMapper.insert()
     * creates a new row with quantity 1.
     */
    @Test
    public void testAddShoppingCart_NewDish_InsertRecord() {
        // [Given] 预设条件
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(20L); // 传入了菜品的 ID，没有传套餐 ID

        // 模拟：本来购物车里是没有这件商品的 (查询返回空列表)
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(new ArrayList<>());

        // 模拟：查出这个菜品的详细信息，用于填构购物车实体 (单价，图片，名字)
        Dish mockDish = new Dish();
        mockDish.setName("麻婆豆腐");
        mockDish.setImage("mapo.png");
        mockDish.setPrice(BigDecimal.valueOf(25.5));
        when(dishMapper.getById(20L)).thenReturn(mockDish);

        // [When] 执行方法
        shoppingCartService.addShoppingCart(dto);

        // [Then] 验证逻辑结果
        // 1. 验证：正确地构建了一条新记录并且向 Mapper 发起了 insert(插入) 请求
        verify(shoppingCartMapper, times(1)).insert(any(ShoppingCart.class));
        // 2. 验证：由于是加入新菜品，不可能去碰套餐 (Setmeal) 的接口
        verify(setmealMapper, never()).getById(anyLong());
        // 3. 验证：不需要调用 update 修改现有数量
        verify(shoppingCartMapper, never()).updateNumberById(any(ShoppingCart.class));
    }

    /**
     * 测试 User Story: Customer - Add items to cart (Setmeal)
     * AC: 当用户首次点击一个“新套餐”加入购物车时，会建立一条全新的购物车记录。
     */
    @Test
    public void testAddShoppingCart_NewSetmeal_InsertRecord() {
        // [Given] 预设条件：只传套餐ID
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setSetmealId(30L);

        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(new ArrayList<>());

        // 模拟套餐查询返回
        Setmeal mockSetmeal = new Setmeal();
        mockSetmeal.setName("超值单人套餐");
        mockSetmeal.setImage("setmeal.png");
        mockSetmeal.setPrice(BigDecimal.valueOf(50.0));
        when(setmealMapper.getById(30L)).thenReturn(mockSetmeal);

        // [When]
        shoppingCartService.addShoppingCart(dto);

        // [Then]
        verify(shoppingCartMapper, times(1)).insert(any(ShoppingCart.class));
        verify(dishMapper, never()).getById(anyLong()); // 验证不要去查菜品表
    }

    /**
     * 测试 User Story: Customer - View dish lists in cart
     * AC: 显示当前登录用户的所有购物车商品。
     */
    @Test
    public void testShowShoppingCart() {
        // [Given]
        List<ShoppingCart> mockList = new ArrayList<>();
        mockList.add(new ShoppingCart());
        mockList.add(new ShoppingCart()); // 购物车里有2件商品
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(mockList);

        // [When]
        List<ShoppingCart> result = shoppingCartService.showShoppingCart();

        // [Then]
        assert result.size() == 2;
        verify(shoppingCartMapper, times(1)).list(any(ShoppingCart.class));
    }

    /**
     * 测试 User Story: Customer - Clear the shopping cart
     * AC: 点击清空购物车，该用户的所有购物车数据被清空。
     */
    @Test
    public void testCleanShoppingCart() {
        // [When]
        shoppingCartService.cleanShoppingCart();

        // [Then] 验证调用了Mapper的根据userId删除的方法
        verify(shoppingCartMapper, times(1)).deleteByUserId(101L); // userId在setUp中固定为101L
    }

    /**
     * 测试 User Story: Customer - Update cart quantities (Reduce)
     * AC: 当商品在购物车里的份数 > 1 时，点击减号，数量减 1。
     */
    @Test
    public void testSubShoppingCart_NumberGreaterThanOne_ReduceNumber() {
        // [Given]
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(10L);

        // 模拟当前购物车中该商品有2份
        List<ShoppingCart> mockCartList = new ArrayList<>();
        ShoppingCart cartInDb = new ShoppingCart();
        cartInDb.setId(100L);
        cartInDb.setNumber(2); // 数据库里目前是2份
        mockCartList.add(cartInDb);

        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(mockCartList);

        // [When] 进行减库存操作
        shoppingCartService.subShoppingCart(dto);

        // [Then] 验证：调用了 update 更新操作，并且数量变成了 1 (2 - 1)
        verify(shoppingCartMapper, times(1)).updateNumberById(any(ShoppingCart.class));
        assert cartInDb.getNumber() == 1; // 验证实体对象的number属性是否已经被修改成了1
        verify(shoppingCartMapper, never()).deleteById(anyLong()); // 不应该直接删除
    }

    /**
     * 测试 User Story: Customer - Update cart quantities (Delete)
     * AC: 当商品在购物车里的份数 = 1 时，点击减号，直接删除该条记录。
     */
    @Test
    public void testSubShoppingCart_NumberIsOne_DeleteRecord() {
        // [Given]
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(10L);

        // 模拟购物车里刚好只有 1 份的情况
        List<ShoppingCart> mockCartList = new ArrayList<>();
        ShoppingCart cartInDb = new ShoppingCart();
        cartInDb.setId(100L); // 这是主键ID
        cartInDb.setNumber(1); // 数量为1
        mockCartList.add(cartInDb);

        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(mockCartList);

        // [When] 进行减库存操作
        shoppingCartService.subShoppingCart(dto);

        // [Then] 验证：由于已经只有1份了，需要调用 deleteById 按照主键把它删掉
        verify(shoppingCartMapper, times(1)).deleteById(100L);
        verify(shoppingCartMapper, never()).updateNumberById(any(ShoppingCart.class)); // 绝对不应该走更新分支
    }

}
