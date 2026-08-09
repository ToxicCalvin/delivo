package com.delivo.service.impl;

import com.delivo.constant.StatusConstant;
import com.delivo.context.BaseContext;
import com.delivo.dto.OrdersSubmitDTO;
import com.delivo.entity.*;
import com.delivo.exception.AddressBookBusinessException;
import com.delivo.exception.OrderBusinessException;
import com.delivo.exception.ShoppingCartBusinessException;
import com.delivo.mapper.*;
import com.delivo.utils.WeChatPayUtil;
import com.delivo.vo.OrderSubmitVO;
import com.delivo.websocket.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderDetailMapper orderDetailMapper;

    @Mock
    private AddressBookMapper addressBookMapper;

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private SetmealMapper setmealMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private WeChatPayUtil weChatPayUtil;

    @Mock
    private WebSocketServer webSocketServer;

    @BeforeEach
    public void setUp() {
        BaseContext.setCurrentId(1L);
    }

    @AfterEach
    public void tearDown() {
        BaseContext.removeCurrentId();
    }

    /**
     * Test US 5.1 - AC 1: Cancel Unpaid Order
     * TC-ORD-01: Call userCancelById (status=1). orderMapper.update() called. No
     * refund called.
     */
    @Test
    public void testUserCancelById_PendingPaymentStatus_CancelsWithoutRefund() throws Exception {
        // [Given]
        Long orderId = 100L;
        Orders mockOrder = new Orders();
        mockOrder.setId(orderId);
        mockOrder.setStatus(Orders.PENDING_PAYMENT); // status = 1
        when(orderMapper.getById(orderId)).thenReturn(mockOrder);

        // [When]
        orderService.userCancelById(orderId);

        // [Then]
        // Verify refund is never called
        verify(weChatPayUtil, never()).refund(anyString(), anyString(), any(BigDecimal.class), any(BigDecimal.class));

        // Verify update is called with CANCELLED status
        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(orderMapper, times(1)).update(orderCaptor.capture());
        assertEquals(Orders.CANCELLED, orderCaptor.getValue().getStatus());
    }

    /**
     * Test US 5.1 - AC 2: Cancel Paid Order
     * TC-ORD-02: Call userCancelById (status=2). orderMapper.update() called.
     * refund IS called.
     */
    @Test
    public void testUserCancelById_ToBeConfirmedStatus_CancelsWithRefund() throws Exception {
        // [Given]
        Long orderId = 101L;
        Orders mockOrder = new Orders();
        mockOrder.setId(orderId);
        mockOrder.setStatus(Orders.TO_BE_CONFIRMED); // status = 2
        mockOrder.setNumber("ORD12345");
        when(orderMapper.getById(orderId)).thenReturn(mockOrder);

        // Mock refund call
        when(weChatPayUtil.refund(anyString(), anyString(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn("SUCCESS");

        // [When]
        orderService.userCancelById(orderId);

        // [Then]
        // Verify refund is called
        verify(weChatPayUtil, times(1)).refund(
                eq("ORD12345"), eq("ORD12345"), any(BigDecimal.class), any(BigDecimal.class));

        // Verify update is called with CANCELLED status and REFUND payStatus
        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(orderMapper, times(1)).update(orderCaptor.capture());
        assertEquals(Orders.CANCELLED, orderCaptor.getValue().getStatus());
        assertEquals(Orders.REFUND, orderCaptor.getValue().getPayStatus());
    }

    /**
     * Test US 5.1 - AC 3: Cannot Cancel In-Progress
     * TC-ORD-03: Call userCancelById (status=3). Throws OrderBusinessException.
     */
    @Test
    public void testUserCancelById_ConfirmedStatus_ThrowsException() throws Exception {
        // [Given]
        Long orderId = 102L;
        Orders mockOrder = new Orders();
        mockOrder.setId(orderId);
        mockOrder.setStatus(Orders.CONFIRMED); // status = 3
        when(orderMapper.getById(orderId)).thenReturn(mockOrder);

        // [When & Then]
        assertThrows(OrderBusinessException.class, () -> {
            orderService.userCancelById(orderId);
        });

        verify(orderMapper, never()).update(any(Orders.class));
    }

    /**
     * Test US 5.2 - AC 1: Valid Dispatch
     * TC-ORD-04: Call delivery (status=3). orderMapper.update() called with
     * status=4.
     */
    @Test
    public void testDelivery_ConfirmedStatus_UpdatesToDeliveryInProgress() {
        // [Given]
        Long orderId = 103L;
        Orders mockOrder = new Orders();
        mockOrder.setId(orderId);
        mockOrder.setStatus(Orders.CONFIRMED); // status = 3
        when(orderMapper.getById(orderId)).thenReturn(mockOrder);

        // [When]
        orderService.delivery(orderId);

        // [Then]
        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(orderMapper, times(1)).update(orderCaptor.capture());
        assertEquals(Orders.DELIVERY_IN_PROGRESS, orderCaptor.getValue().getStatus());
    }

    /**
     * Test US 5.2 - AC 2: Invalid Dispatch
     * TC-ORD-05: Call delivery (status=4). Throws OrderBusinessException.
     */
    @Test
    public void testDelivery_DeliveryInProgressStatus_ThrowsException() {
        // [Given]
        Long orderId = 104L;
        Orders mockOrder = new Orders();
        mockOrder.setId(orderId);
        mockOrder.setStatus(Orders.DELIVERY_IN_PROGRESS); // status = 4
        when(orderMapper.getById(orderId)).thenReturn(mockOrder);

        // [When & Then]
        assertThrows(OrderBusinessException.class, () -> {
            orderService.delivery(orderId);
        });

        verify(orderMapper, never()).update(any(Orders.class));
    }

    // ==================== Submit Order Tests ====================

    /**
     * Test Part B, Module 3, US 6 - AC 1: Successful Submission
     * TC-SUB-01: Call submitOrder with valid address and non-empty cart.
     * Expected: order created, details inserted, cart cleared, VO returned.
     */
    @Test
    public void testSubmitOrder_ValidAddressAndCart_CreatesOrderSuccessfully() {
        // [Given]
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(1L);
        dto.setAmount(BigDecimal.valueOf(88.00));
        dto.setPayMethod(1);
        dto.setPackAmount(1);
        dto.setTablewareNumber(1);
        dto.setTablewareStatus(1);
        dto.setDeliveryStatus(1);
        dto.setEstimatedDeliveryTime(java.time.LocalDateTime.now().plusHours(1));

        // Mock: valid address exists
        AddressBook mockAddress = new AddressBook();
        mockAddress.setId(1L);
        mockAddress.setDetail("123 Main Street");
        mockAddress.setPhone("1234567890");
        mockAddress.setConsignee("Test User");
        when(addressBookMapper.getById(1L)).thenReturn(mockAddress);

        // Mock: cart has one enabled dish
        List<ShoppingCart> cartItems = new ArrayList<>();
        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setDishId(10L);
        cartItem.setName("Test Dish");
        cartItem.setNumber(1);
        cartItem.setAmount(BigDecimal.valueOf(88.00));
        cartItems.add(cartItem);
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(cartItems);

        // Mock: the dish is currently enabled (on sale)
        Dish enabledDish = new Dish();
        enabledDish.setId(10L);
        enabledDish.setStatus(StatusConstant.ENABLE);
        when(dishMapper.getById(10L)).thenReturn(enabledDish);

        // [When]
        OrderSubmitVO result = orderService.submitOrder(dto);

        // [Then]
        assertNotNull(result);
        verify(orderMapper, times(1)).insert(any(Orders.class));
        verify(orderDetailMapper, times(1)).insertBatch(anyList());
        verify(shoppingCartMapper, times(1)).deleteByUserId(1L);
    }

    /**
     * Test Part B, Module 3, US 6 - AC 2: No Delivery Address
     * TC-SUB-02: Call submitOrder with invalid address ID.
     * Expected: throws AddressBookBusinessException, no order created.
     */
    @Test
    public void testSubmitOrder_InvalidAddress_ThrowsAddressBookException() {
        // [Given]
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(999L);

        // Mock: address does not exist
        when(addressBookMapper.getById(999L)).thenReturn(null);

        // [When & Then]
        assertThrows(AddressBookBusinessException.class, () -> {
            orderService.submitOrder(dto);
        });

        // Verify no order was created
        verify(orderMapper, never()).insert(any(Orders.class));
        verify(orderDetailMapper, never()).insertBatch(anyList());
    }

    /**
     * Test Part B, Module 3, US 6 - AC 3: Empty Shopping Cart
     * TC-SUB-03: Call submitOrder with valid address but empty cart.
     * Expected: throws ShoppingCartBusinessException, no order created.
     */
    @Test
    public void testSubmitOrder_EmptyCart_ThrowsShoppingCartException() {
        // [Given]
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(1L);

        // Mock: valid address exists
        AddressBook mockAddress = new AddressBook();
        mockAddress.setId(1L);
        mockAddress.setDetail("123 Main Street");
        when(addressBookMapper.getById(1L)).thenReturn(mockAddress);

        // Mock: shopping cart is empty
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());

        // [When & Then]
        assertThrows(ShoppingCartBusinessException.class, () -> {
            orderService.submitOrder(dto);
        });

        // Verify no order was created
        verify(orderMapper, never()).insert(any(Orders.class));
        verify(orderDetailMapper, never()).insertBatch(anyList());
    }
}
