CREATE DATABASE IF NOT EXISTS `delivo`;
USE `delivo`;

-- Table structure for address_book
DROP TABLE IF EXISTS `address_book`;
CREATE TABLE `address_book` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `consignee` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT 'Consignee Name',
  `sex` varchar(2) COLLATE utf8_bin DEFAULT NULL COMMENT 'Gender',
  `phone` varchar(11) COLLATE utf8_bin NOT NULL COMMENT 'Phone Number',
  `province_code` varchar(12) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'Province Code',
  `province_name` varchar(32) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'Province Name',
  `city_code` varchar(12) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'City Code',
  `city_name` varchar(32) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'City Name',
  `district_code` varchar(12) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'District Code',
  `district_name` varchar(32) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'District Name',
  `detail` varchar(200) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'Detailed Address',
  `label` varchar(100) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'Label (e.g., Home, Company)',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Is Default: 0 No, 1 Yes',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='Address Book';


-- Table structure for category
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `type` int DEFAULT NULL COMMENT 'Type: 1 Dish Category, 2 Set Meal Category',
  `name` varchar(32) COLLATE utf8_bin NOT NULL COMMENT 'Category Name',
  `sort` int NOT NULL DEFAULT '0' COMMENT 'Sort Order',
  `status` int DEFAULT NULL COMMENT 'Status: 0 Disabled, 1 Enabled',
  `create_time` datetime DEFAULT NULL COMMENT 'Create Time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update Time',
  `create_user` bigint DEFAULT NULL COMMENT 'Creator ID',
  `update_user` bigint DEFAULT NULL COMMENT 'Modifier ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_category_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='Dish and Set Meal Categories';

INSERT INTO category VALUES
(201,1,'Italian Cuisine',1,1,NOW(),NOW(),1,1),
(202,1,'Japanese Cuisine',2,1,NOW(),NOW(),1,1),
(203,1,'Korean Cuisine',3,1,NOW(),NOW(),1,1),
(204,1,'American Grill',4,1,NOW(),NOW(),1,1),
(205,1,'Healthy Salads',5,1,NOW(),NOW(),1,1),
(206,1,'Desserts',6,1,NOW(),NOW(),1,1),
(207,1,'Coffee & Drinks',7,1,NOW(),NOW(),1,1),
(208,1,'Seafood Specials',8,1,NOW(),NOW(),1,1),
(301,2,'Lunch Combos',9,1,NOW(),NOW(),1,1),
(302,2,'Value Meals',10,1,NOW(),NOW(),1,1);

-- Table structure for dish
DROP TABLE IF EXISTS `dish`;
CREATE TABLE `dish` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `name` varchar(32) COLLATE utf8_bin NOT NULL COMMENT 'Dish Name',
  `category_id` bigint NOT NULL COMMENT 'Category ID',
  `price` decimal(10,2) DEFAULT NULL COMMENT 'Price',
  `image` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT 'Image URL',
  `description` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT 'Description',
  `status` int DEFAULT '1' COMMENT 'Status: 0 Stop Selling, 1 On Sale',
  `create_time` datetime DEFAULT NULL COMMENT 'Create Time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update Time',
  `create_user` bigint DEFAULT NULL COMMENT 'Creator ID',
  `update_user` bigint DEFAULT NULL COMMENT 'Modifier ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_dish_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='Dishes';

INSERT INTO dish VALUES
(301,'Margherita Pizza',201,12.5,'https://images.unsplash.com/photo-1600891964599-f61ba0e24092','Wood-fired pizza with mozzarella, tomato sauce, and fresh basil',1,NOW(),NOW(),1,1),
(302,'Spaghetti Carbonara',201,13.8,'https://images.unsplash.com/photo-1588013273468-315fd88ea34c','Creamy spaghetti with crispy bacon, egg yolk, and parmesan',1,NOW(),NOW(),1,1),
(303,'Lasagna Bolognese',201,14.5,'https://images.unsplash.com/photo-1612874742237-6526221588e3','Layered pasta with seasoned ground beef, bechamel, and mozzarella',1,NOW(),NOW(),1,1),
(304,'Seafood Risotto',201,16.9,'https://images.unsplash.com/photo-1546549032-9571cd6b27df','Creamy arborio rice with shrimp, mussels, and squid in white wine',1,NOW(),NOW(),1,1),
(305,'Pesto Pasta',201,12.9,'https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9','Pasta tossed in fresh basil pesto with pine nuts and parmesan',1,NOW(),NOW(),1,1),
(306,'Tiramisu',201,8.9,'https://images.unsplash.com/photo-1571877227200-a0d98ea607e9','Classic Italian layered dessert with mascarpone, espresso, and cocoa',1,NOW(),NOW(),1,1);

INSERT INTO dish VALUES
(307,'Salmon Sushi',202,11,'https://images.unsplash.com/photo-1553621042-f6e147245754','Hand-pressed fresh Atlantic salmon nigiri on seasoned sushi rice',1,NOW(),NOW(),1,1),
(308,'Tuna Sashimi',202,15,'https://images.unsplash.com/photo-1617196034183-421b4917c92d','Premium sliced raw tuna served with wasabi and soy sauce',1,NOW(),NOW(),1,1),
(309,'Chicken Teriyaki',202,13.5,'https://images.unsplash.com/photo-1604908176997-125f25cc6f3d','Grilled chicken thigh glazed in sweet soy teriyaki sauce with sesame',1,NOW(),NOW(),1,1),
(310,'Ramen',202,12,'https://images.unsplash.com/photo-1557872943-16a5ac26437e','Rich pork bone broth with wheat noodles, chashu pork, and soft egg',1,NOW(),NOW(),1,1),
(311,'Shrimp Tempura',202,14.2,'https://images.unsplash.com/photo-1604908554007-1f99cfa0c63b','Large shrimp in light crispy tempura batter with dipping sauce',1,NOW(),NOW(),1,1),
(312,'Matcha Ice Cream',202,7.5,'https://images.unsplash.com/photo-1563805042-7684c019e1cb','Creamy Japanese green tea matcha ice cream, smooth and aromatic',1,NOW(),NOW(),1,1);

INSERT INTO dish VALUES
(313,'Bibimbap',203,12.8,'https://images.unsplash.com/photo-1590301157890-4810ed352733','Korean mixed rice with assorted vegetables, gochujang, and fried egg',1,NOW(),NOW(),1,1),
(314,'Kimchi Fried Rice',203,11.5,'https://images.unsplash.com/photo-1604908177522-4297b19b3fd3','Stir-fried rice with aged kimchi, chili flakes, and a fried egg',1,NOW(),NOW(),1,1),
(315,'Korean BBQ Beef',203,16,'https://images.unsplash.com/photo-1544025162-d76694265947','Marinated bulgogi beef grilled with soy, garlic, and sesame',1,NOW(),NOW(),1,1),
(316,'Tteokbokki',203,9.9,'https://images.unsplash.com/photo-1604908176997-125f25cc6f3d','Chewy Korean rice cakes in sweet and spicy gochujang sauce',1,NOW(),NOW(),1,1),
(317,'Korean Fried Chicken',203,13.9,'https://images.unsplash.com/photo-1562967916-eb82221dfb36','Double-fried crispy chicken with sweet-spicy or soy garlic glaze',1,NOW(),NOW(),1,1),
(318,'Seaweed Soup',203,8,'https://images.unsplash.com/photo-1604909052743-94e838986d24','Light Korean seaweed soup with beef broth, garlic, and sesame oil',1,NOW(),NOW(),1,1);

INSERT INTO dish VALUES
(319,'Cheeseburger',204,11.9,'https://images.unsplash.com/photo-1550547660-d9450f859349','Grilled beef patty with melted cheddar, lettuce, tomato, and pickles',1,NOW(),NOW(),1,1),
(320,'BBQ Ribs',204,18.5,'https://images.unsplash.com/photo-1600891963935-c8e23b7e2b5f','Slow-smoked pork ribs glazed with sweet and tangy BBQ sauce',1,NOW(),NOW(),1,1),
(321,'Grilled Steak',204,22,'https://images.unsplash.com/photo-1544025162-d76694265947','Prime beef steak grilled to order with garlic butter and rosemary',1,NOW(),NOW(),1,1),
(322,'Hot Dog',204,7.5,'https://images.unsplash.com/photo-1551782450-a2132b4ba21d','Classic beef sausage in a soft bun with ketchup and mustard',1,NOW(),NOW(),1,1),
(323,'Buffalo Wings',204,12.3,'https://images.unsplash.com/photo-1608039755401-742074f0548d','Crispy chicken wings tossed in spicy buffalo hot sauce with blue cheese',1,NOW(),NOW(),1,1),
(324,'French Fries',204,5.9,'https://images.unsplash.com/photo-1541592106381-b31e9677c0e5','Golden crispy potato fries seasoned with sea salt',1,NOW(),NOW(),1,1);

INSERT INTO dish VALUES
(325,'Caesar Salad',205,9.8,'https://images.unsplash.com/photo-1551248429-40975aa4de74','Crisp romaine with parmesan, croutons, and creamy Caesar dressing',1,NOW(),NOW(),1,1),
(326,'Greek Salad',205,9.5,'https://images.unsplash.com/photo-1568605114967-8130f3a36994','Fresh cucumber, tomato, feta cheese, and olives with olive oil',1,NOW(),NOW(),1,1),
(327,'Avocado Salad',205,10.5,'https://images.unsplash.com/photo-1512621776951-a57141f2eefd','Ripe avocado on mixed greens with cherry tomatoes and lime dressing',1,NOW(),NOW(),1,1),
(328,'Quinoa Bowl',205,11.2,'https://images.unsplash.com/photo-1546069901-ba9599a7e63c','Nutrient-rich quinoa with roasted vegetables, chickpeas, and tahini',1,NOW(),NOW(),1,1),
(329,'Chicken Salad',205,12,'https://images.unsplash.com/photo-1604908177253-7462950a6a0a','Grilled chicken breast on mixed greens with light vinaigrette',1,NOW(),NOW(),1,1),
(330,'Fruit Salad',205,8.5,'https://images.unsplash.com/photo-1567306226416-28f0efdc88ce','Colorful mix of seasonal fresh fruits with a light honey drizzle',1,NOW(),NOW(),1,1);

INSERT INTO dish VALUES
(331,'Chocolate Cake',206,7.9,'https://images.unsplash.com/photo-1578985545062-69928b1d9587','Rich dark chocolate layer cake with velvety chocolate ganache',1,NOW(),NOW(),1,1),
(332,'Cheesecake',206,8.5,'https://images.unsplash.com/photo-1505253716362-afaea6fc7f7f','Smooth New York-style cream cheesecake on a graham cracker crust',1,NOW(),NOW(),1,1),
(333,'Macarons',206,6.8,'https://images.unsplash.com/photo-1569864358642-9d1684040f43','Delicate French almond macarons in assorted flavors and colors',1,NOW(),NOW(),1,1),
(334,'Ice Cream Sundae',206,7.2,'https://images.unsplash.com/photo-1563805042-7684c019e1cb','Vanilla ice cream with chocolate sauce, whipped cream, and cherry',1,NOW(),NOW(),1,1),
(335,'Pancakes',206,9,'https://images.unsplash.com/photo-1528207776546-365bb710ee93','Fluffy buttermilk pancakes with maple syrup and fresh berries',1,NOW(),NOW(),1,1),
(336,'Apple Pie',206,8.7,'https://images.unsplash.com/photo-1562007908-4a8d1c6d8c74','Warm baked apple pie with cinnamon, nutmeg, and flaky butter crust',1,NOW(),NOW(),1,1);

INSERT INTO dish VALUES
(337,'Espresso',207,3.5,'https://images.unsplash.com/photo-1511920170033-f8396924c348','Concentrated shot of rich Italian-style espresso coffee',1,NOW(),NOW(),1,1),
(338,'Cappuccino',207,4.5,'https://images.unsplash.com/photo-1509042239860-f550ce710b93','Espresso with steamed milk and a thick layer of velvety foam',1,NOW(),NOW(),1,1),
(339,'Latte',207,4.8,'https://images.unsplash.com/photo-1498804103079-a6351b050096','Espresso blended with smooth steamed milk, creamy and mild',1,NOW(),NOW(),1,1),
(340,'Iced Americano',207,4.2,'https://images.unsplash.com/photo-1517701604599-bb29b565090c','Chilled double espresso over ice, bold and refreshing',1,NOW(),NOW(),1,1),
(341,'Mango Smoothie',207,5.5,'https://images.unsplash.com/photo-1553530666-ba11a7da3888','Tropical mango blended with yogurt and ice, thick and refreshing',1,NOW(),NOW(),1,1),
(342,'Lemon Tea',207,4,'https://images.unsplash.com/photo-1497534446932-c925b458314e','Freshly brewed black tea with sliced lemon, served hot or iced',1,NOW(),NOW(),1,1);

INSERT INTO dish VALUES
(343,'Grilled Salmon',208,18.9,'https://images.unsplash.com/photo-1467003909585-2f8a72700288','Omega-3 rich salmon fillet grilled with lemon, dill, and olive oil',1,NOW(),NOW(),1,1),
(344,'Lobster Tail',208,29,'https://images.unsplash.com/photo-1559847844-5315695dadae','Succulent lobster tail baked with garlic butter, lemon, and parsley',1,NOW(),NOW(),1,1),
(345,'Garlic Butter Shrimp',208,17.5,'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38','Jumbo shrimp sauteed in garlic butter with white wine and parsley',1,NOW(),NOW(),1,1),
(346,'Seafood Paella',208,21,'https://images.unsplash.com/photo-1504674900247-0877df9cc836','Spanish saffron rice with shrimp, mussels, clams, and squid',1,NOW(),NOW(),1,1),
(347,'Fried Calamari',208,13.9,'https://images.unsplash.com/photo-1562967916-eb82221dfb36','Crispy fried squid rings served with lemon and marinara sauce',1,NOW(),NOW(),1,1),
(348,'Oyster Platter',208,24.5,'https://images.unsplash.com/photo-1559847844-5315695dadae','Fresh raw oysters on ice with lemon, mignonette, and cocktail sauce',1,NOW(),NOW(),1,1);

-- Table structure for dish_flavor
DROP TABLE IF EXISTS `dish_flavor`;
CREATE TABLE `dish_flavor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `dish_id` bigint NOT NULL COMMENT 'Dish ID',
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT 'Flavor Name',
  `value` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT 'Flavor Data List (JSON)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='Dish Flavors';

INSERT INTO dish_flavor VALUES
(301,301,'Spiciness','["Not Spicy","Mild","Medium","Hot"]'),
(302,307,'Wasabi Level','["None","Light","Normal","Extra"]'),
(303,313,'Spiciness','["Mild","Medium","Hot"]'),
(304,319,'Add-ons','["Extra Cheese","Bacon","Double Patty"]'),
(305,325,'Dressing','["Caesar","Olive Oil","Vinaigrette"]'),
(306,331,'Sweetness','["Low Sugar","Normal","Extra Sweet"]'),
(307,337,'Sugar Level','["No Sugar","Half Sugar","Normal"]'),
(308,343,'Cooking Level','["Rare","Medium","Well Done"]');

-- Table structure for employee
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `name` varchar(32) COLLATE utf8_bin NOT NULL COMMENT 'Name',
  `username` varchar(32) COLLATE utf8_bin NOT NULL COMMENT 'Username',
  `password` varchar(64) COLLATE utf8_bin NOT NULL COMMENT 'Password',
  `phone` varchar(11) COLLATE utf8_bin NOT NULL COMMENT 'Phone',
  `sex` varchar(2) COLLATE utf8_bin NOT NULL COMMENT 'Gender',
  `id_number` varchar(18) COLLATE utf8_bin NOT NULL COMMENT 'ID Card Number',
  `status` int NOT NULL DEFAULT '1' COMMENT 'Status: 0 Disabled, 1 Enabled',
  `create_time` datetime DEFAULT NULL COMMENT 'Create Time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update Time',
  `create_user` bigint DEFAULT NULL COMMENT 'Creator ID',
  `update_user` bigint DEFAULT NULL COMMENT 'Modifier ID',
  `role` int NOT NULL DEFAULT '0' COMMENT 'Role: 0 Employee, 1 Admin',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='Employee Information';

-- Translate Employee Data
INSERT INTO `employee` VALUES (1,'Administrator','admin','e10adc3949ba59abbe56e057f20f883e','13812312312','1','110101199001010047',1,'2022-02-15 15:51:20','2022-02-17 09:16:20',10,1,1);

-- Table structure for order_detail
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT 'Item Name',
  `image` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT 'Image',
  `order_id` bigint NOT NULL COMMENT 'Order ID',
  `dish_id` bigint DEFAULT NULL COMMENT 'Dish ID',
  `setmeal_id` bigint DEFAULT NULL COMMENT 'Set Meal ID',
  `dish_flavor` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT 'Flavor',
  `number` int NOT NULL DEFAULT '1' COMMENT 'Quantity',
  `amount` decimal(10,2) NOT NULL COMMENT 'Amount',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='Order Detail';

-- Table structure for orders
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `number` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT 'Order Number',
  `status` int NOT NULL DEFAULT '1' COMMENT 'Status: 1 Pending Payment, 2 Awaiting Acceptance, 3 Accepted, 4 Delivering, 5 Completed, 6 Cancelled, 7 Refunded',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `address_book_id` bigint NOT NULL COMMENT 'Address ID',
  `order_time` datetime NOT NULL COMMENT 'Order Time',
  `checkout_time` datetime DEFAULT NULL COMMENT 'Checkout Time',
  `pay_method` int NOT NULL DEFAULT '1' COMMENT '',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT 'Pay Status: 0 Unpaid, 1 Paid, 2 Refunded',
  `amount` decimal(10,2) NOT NULL COMMENT 'Total Amount',
  `remark` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT 'Remark',
  `phone` varchar(11) COLLATE utf8_bin DEFAULT NULL COMMENT 'Phone',
  `address` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT 'Address',
  `user_name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT 'User Name',
  `consignee` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT 'Consignee',
  `cancel_reason` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT 'Cancel Reason',
  `rejection_reason` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT 'Rejection Reason',
  `cancel_time` datetime DEFAULT NULL COMMENT 'Cancel Time',
  `estimated_delivery_time` datetime DEFAULT NULL COMMENT 'Estimated Delivery Time',
  `delivery_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Delivery Status: 1 Immediate, 0 Scheduled',
  `delivery_time` datetime DEFAULT NULL COMMENT 'Delivery Time',
  `pack_amount` int DEFAULT NULL COMMENT 'Packing Fee',
  `tableware_number` int DEFAULT NULL COMMENT 'Tableware Count',
  `tableware_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Tableware Status: 1 By Meal Quantity, 0 Specific Quantity',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='Orders';

-- Table structure for setmeal
DROP TABLE IF EXISTS `setmeal`;
CREATE TABLE `setmeal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `category_id` bigint NOT NULL COMMENT 'Category ID',
  `name` varchar(32) COLLATE utf8_bin NOT NULL COMMENT 'Set Meal Name',
  `price` decimal(10,2) NOT NULL COMMENT 'Price',
  `status` int DEFAULT '1' COMMENT 'Status: 0 Stop Selling, 1 On Sale',
  `description` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT 'Description',
  `image` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT 'Image',
  `create_time` datetime DEFAULT NULL COMMENT 'Create Time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update Time',
  `create_user` bigint DEFAULT NULL COMMENT 'Creator ID',
  `update_user` bigint DEFAULT NULL COMMENT 'Modifier ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_setmeal_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='Set Meal';

-- Table structure for setmeal_dish
DROP TABLE IF EXISTS `setmeal_dish`;
CREATE TABLE `setmeal_dish` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `setmeal_id` bigint DEFAULT NULL COMMENT 'Set Meal ID',
  `dish_id` bigint DEFAULT NULL COMMENT 'Dish ID',
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT 'Dish Name (Redundant)',
  `price` decimal(10,2) DEFAULT NULL COMMENT 'Price (Redundant)',
  `copies` int DEFAULT NULL COMMENT 'Quantity',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='Set Meal Dish Relation';

-- Table structure for shopping_cart
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT 'Item Name',
  `image` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT 'Image',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `dish_id` bigint DEFAULT NULL COMMENT 'Dish ID',
  `setmeal_id` bigint DEFAULT NULL COMMENT 'Set Meal ID',
  `dish_flavor` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT 'Flavor',
  `number` int NOT NULL DEFAULT '1' COMMENT 'Quantity',
  `amount` decimal(10,2) NOT NULL COMMENT 'Amount',
  `create_time` datetime DEFAULT NULL COMMENT 'Create Time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='Shopping Cart';

-- Table structure for user
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `openid` varchar(45) COLLATE utf8_bin DEFAULT NULL COMMENT 'WeChat OpenID',
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT 'Name',
  `phone` varchar(11) COLLATE utf8_bin DEFAULT NULL COMMENT 'Phone',
  `sex` varchar(2) COLLATE utf8_bin DEFAULT NULL COMMENT 'Gender',
  `id_number` varchar(18) COLLATE utf8_bin DEFAULT NULL COMMENT 'ID Number',
  `avatar` varchar(500) COLLATE utf8_bin DEFAULT NULL COMMENT 'Avatar',
  `create_time` datetime DEFAULT NULL,
  `username` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT 'Username',
  `password` varchar(64) COLLATE utf8_bin DEFAULT NULL COMMENT 'Password',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin COMMENT='User Information';