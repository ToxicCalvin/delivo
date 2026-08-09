export interface User {
    id: string;
    username: string;
    phone: string;
    avatar?: string;
}


export interface AuthResponse {
    token: string;
    user: User;
}


export interface DishFlavor {
    name: string;
    value: string;
}


export interface Category {
    id: string;
    name: string;
    type: 'dish' | 'combo';
    sort: number;
}


export interface Dish {
    id: string;
    name: string;
    categoryId: string;
    price: number;
    description: string;
    image: string;
    status: 'available' | 'unavailable';
    flavors?: DishFlavor[];
}


export interface Combo {
    id: string;
    name: string;
    categoryId: string;
    price: number;
    description: string;
    image: string;
    dishes: Dish[];
    status: 'available' | 'unavailable';
}


export interface CartItem {
    id: string;
    type: 'dish' | 'combo';
    item: Dish | Combo;
    quantity: number;
    dishFlavor?: string;
}


export interface Address {
    id: string;
    consignee: string;
    phone: string;
    sex: string;
    provinceCode: string;
    provinceName: string;
    cityCode: string;
    cityName: string;
    districtCode: string;
    districtName: string;
    detail: string;
    label: string;
    isDefault: number;
}



export type OrderStatus = 1 | 2 | 3 | 4 | 5 | 6;


export interface OrderDetail {
    id: string;
    name: string;
    orderId: string;
    dishId?: string;
    setmealId?: string;
    dishFlavor?: string;
    number: number;
    amount: number;
    image: string;
}


export interface Order {
    id: string;

    number: string;

    status: OrderStatus;

    userId?: string;

    addressBookId?: string;

    orderTime: string;

    checkoutTime?: string;

    payMethod: number;

    payStatus?: number;

    amount: number;

    remark?: string;

    userName?: string;

    phone: string;

    address: string;

    consignee: string;

    cancelReason?: string;

    rejectionReason?: string;

    estimatedDeliveryTime?: string;

    deliveryStatus?: number;

    deliveryTime?: string;

    packAmount?: number;

    tablewareNumber?: number;

    tablewareStatus?: number;

    orderDishes?: string;

    orderDetailList?: OrderDetail[];
}


export interface ApiResponse<T> {
    code: number;
    message: string;
    data: T;
}
