## Delivo - A food delivery platform with AI Assitance



### Project Overview

Delivo is a full-stack food delivery platform and business solution that I designed and implemented during my undergraduate studies in Computer Science at Eötvös Loránd University. 

The project mainly explores an **LLM + Java** approach to improve the reliability of an AI assistant when handling the strict deterministic business logic requirements of the food service industry, with the goal of minimizing LLM hallucinations as much as possible.

The core architecture of the project was designed and implemented independently by me, while certain components were developed with reference to and inspiration from existing open-source projects.

The project consists of two main components:

**Customer Application:** Allows customers to browse menus, search for dishes, manage their shopping carts&address, place and pay for orders, and interact with an AI assistant to achieve some task.

<img src="./docs/imgs/user-main.png" style="zoom:30%;" />

**Admin Dashboard:** Enables restaurant administrators to manage dishes and meal packages, process orders, manage employees, and view business and operational statistics.

<img src="./docs/imgs/admin-mian.png" style="zoom:50%;" />

### Key Features

**AI Assistant can do**

#### Dish Recommendation. 

- By keyword or cuisine 

<img src="./docs/imgs/ai_1.png" style="zoom:70%;" />

- By taste preference and occasion.

<img src="./docs/imgs/ai_2.png" style="zoom:70%;" />

- By budget
- Combo recommendation.

<img src="./docs/imgs/ai_3.png" style="zoom:70%;" />

#### Place order (step by step)

<img src="./docs/imgs/ai_4.png" style="zoom:70%;" />

#### Single-message ordering (One step)

<img src="./docs/imgs/ai_5.png" style="zoom:30%;" />

- Other Supported Operations

  - Clear cart, Reorder, Check order status, Cancel order etc..

  <img src="./docs/imgs/ai_6.png" style="zoom:70%;" />

  <img src="./docs/imgs/ai_7.png" style="zoom:70%;" />

### Technology Stack

Springboot2.7

LangChain4j

MySQL, MyBatis, Redis

websocket

etc.

### Core System Architecture

#### System Architecture

Delivo follows a classical three-tier physical deployment (presentation/application/data), with the entire backend packaged as a single Spring Boot monolith. Logically, the codebase adopts a layered architecture (Controller → Service → Mapper) as its backbone, while the AI module introduces an orchestration layer that sits alongside, rather than above. The system serves two client applications — an admin and a customer-facing ordering interface — both implemented as Vue 3 single-page applications. 

![](./docs/imgs/sys_arch.png)

To be noticed, the AI assistant in this project is not an “Agent” in the strict sense. Instead, the LLM is constrained to handle only two tasks that are difficult to implement reliably with traditional code:

1. **Convert users’ natural-language requests into structured data**
    *(Role 1: Natural Language → JSON Intent)*
2. **Convert structured results into natural-language responses that users can easily understand**
    *(Role 2: Structured Result → Natural-Language Response)*

Overall, this trade-off sacrifices some of the “intelligence” of the AI assistant in exchange for greater **engineering robustness, predictability, and user experience**.

### Limitation and onging improvement



### 

### Running the Project





