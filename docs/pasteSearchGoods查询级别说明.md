# pasteSearchGoods 接口查询级别详细说明

## 概述

`pasteSearchGoods` 接口是一个智能商品匹配接口，用于处理批量粘贴的商品订单。该接口采用**多级匹配策略**，按照优先级从高到低依次尝试匹配商品，确保商品匹配的准确性和灵活性。

**接口路径**: `/api/nxdepartmentorders/pasteSearchGoods`  
**请求方式**: `POST`  
**请求参数**: `List<NxDepartmentOrdersEntity>` - 订单列表

---

## 查询级别快速参考

| 级别 | 简称 | 说明 | 匹配规则 | 数据源 |
|------|------|------|---------|--------|
| **级别0** | 部门历史完全匹配 | 优先匹配，部门商品历史记录（名称+规格） | 完全匹配（名称+规格） | `nx_department_dis_goods` |
| **级别1** | 分销商完全匹配 | 一级匹配，分销商商品（名称+规格） | 完全匹配（名称+规格） | `nx_distributer_goods` |
| **级别2** | 分销商名称匹配 | 二级匹配，分销商商品（仅名称） | 部分匹配（仅名称） | `nx_distributer_goods` |
| **级别3** | 拼音匹配 | 三级匹配，拼音匹配（3个子级别） | 拼音完全/模糊匹配 | `nx_distributer_goods` |
| **级别4** | 别名匹配 | 四级匹配，别名匹配（2个子级别） | 别名完全/模糊匹配 | `nx_distributer_goods` + `nx_distributer_alias` |
| **级别5** | 部门历史名称匹配 | 兜底匹配，部门商品历史记录（仅名称） | 部分匹配（仅名称） | `nx_department_dis_goods` |
| **级别6** | 临时订单创建 | 最终处理，创建临时订单+查询系统商品 | 创建临时订单 | `nx_goods` |

**说明**：
- 级别0-5为商品匹配级别，按优先级从高到低依次尝试
- 级别6为最终处理，当所有匹配都失败时创建临时订单
- 如果某个级别找到唯一匹配，直接保存订单并跳过后续匹配

---

## 查询级别架构

### 整体流程图

```
┌─────────────────────────────────────────────────────────────┐
│           级别0：部门历史完全匹配（优先匹配）                   │
│         部门商品历史记录：名称 + 规格（完全匹配）              │
└───────────────────────┬─────────────────────────────────────┘
                        │ 未找到
                        ▼
┌─────────────────────────────────────────────────────────────┐
│           级别1：分销商完全匹配（一级匹配）                     │
│         分销商商品：名称 + 规格（完全匹配）                   │
└───────────────────────┬─────────────────────────────────────┘
                        │ 未找到
                        ▼
┌─────────────────────────────────────────────────────────────┐
│           级别2：分销商名称匹配（二级匹配）                     │
│           分销商商品：仅名称（不含规格）                      │
└───────────────────────┬─────────────────────────────────────┘
                        │ 未找到
                        ▼
┌─────────────────────────────────────────────────────────────┐
│        二级匹配后处理流程                                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 2.1 别名完全匹配（优先级提前）                        │   │
│  │ 2.2 商品名称模糊搜索（优先级靠后）                    │   │
│  └──────────────────────────────────────────────────────┘   │
└───────────────────────┬─────────────────────────────────────┘
                        │ 未找到
                        ▼
┌─────────────────────────────────────────────────────────────┐
│           级别3：拼音匹配（三级匹配）                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 3.1 拼音 + 规格（完全匹配）                           │   │
│  │ 3.2 仅拼音（完全匹配）                                │   │
│  │ 3.3 拼音模糊匹配（LIKE查询）                          │   │
│  └──────────────────────────────────────────────────────┘   │
└───────────────────────┬─────────────────────────────────────┘
                        │ 未找到
                        ▼
┌─────────────────────────────────────────────────────────────┐
│           级别4：别名匹配（四级匹配）                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 4.1 别名模糊匹配（LIKE查询）                          │   │
│  │ 注意：别名完全匹配已提前到二级匹配后执行                │   │
│  └──────────────────────────────────────────────────────┘   │
└───────────────────────┬─────────────────────────────────────┘
                        │ 未找到
                        ▼
┌─────────────────────────────────────────────────────────────┐
│           级别5：部门历史名称匹配（兜底匹配）                   │
│       部门商品历史记录：仅名称（不含规格）                    │
└───────────────────────┬─────────────────────────────────────┘
                        │ 未找到
                        ▼
┌─────────────────────────────────────────────────────────────┐
│           级别6：临时订单创建（最终处理）                      │
│        状态设为-2（待确认），查询nx_goods作为候选              │
└─────────────────────────────────────────────────────────────┘
```

---

## 详细查询级别说明

### 📌 部门商品历史记录匹配级别说明

部门商品历史记录查询有**2个独立的匹配级别**：

#### 部门商品历史记录级别1（优先匹配）
- **查询条件**：部门ID + 商品名称 + 规格
- **匹配规则**：完全匹配（名称+规格）
- **位置**：优先匹配（最高优先级，级别0）
- **查询方法**：`queryDepartmentGoods()`
- **代码位置**：1419-1437行

#### 部门商品历史记录级别2（兜底匹配）
- **查询条件**：部门ID + 商品名称（不含规格）
- **匹配规则**：部分匹配（仅名称）
- **位置**：兜底匹配（最后机会，级别5）
- **查询方法**：`queryDepartmentGoods()`
- **代码位置**：1492-1517行

#### 部门商品历史记录辅助查询（非独立匹配级别）

部门商品历史记录还会在以下场景中作为**辅助决策**使用（不是独立匹配级别）：

1. **一级匹配辅助查询**（1646-1658行）
   - 触发条件：一级匹配找到多个结果时
   - 查询条件：部门ID + 商品名称（不含规格）
   - 查询方法：`queryDepDisGoodsByParams()`
   - 用途：辅助判断应该选择哪个商品

2. **二级匹配辅助查询**（1586-1601行、1606-1630行）
   - 触发条件：二级匹配找到结果时（无论1个还是多个）
   - 查询条件：部门ID + 商品名称（不含规格）
   - 查询方法：`queryDepartmentGoods()`
   - 用途：辅助判断应该选择哪个商品

**总结**：部门商品历史记录有**2个独立匹配级别**（级别1和级别2），另外还有**2个辅助查询场景**（用于辅助决策，不是独立匹配级别）。

---

## 详细查询级别说明

### 🔴 级别0：部门历史完全匹配（优先匹配）

**简称**: 部门历史完全匹配  
**级别**: 0（最高优先级）  
**数据源**: `nx_department_dis_goods`（部门商品历史记录表）  
**查询方法**: `nxDepartmentDisGoodsService.queryDepartmentGoods()`

#### 查询条件
```java
{
    "depId": 部门ID,
    "name": 商品名称,
    "standard": 规格
}
```

#### 匹配规则
- **完全匹配**：商品名称 + 规格必须完全一致
- **优先级最高**：如果匹配成功，直接保存订单，**跳过所有后续匹配**
- **部门商品历史记录级别1**：名称+规格完全匹配

#### 结果处理

| 匹配结果 | 处理方式 | 说明 |
|---------|---------|------|
| **找到1个** | ✅ 直接保存订单 | 调用 `saveOneOrder()`，订单状态设为0（正常），**跳过后续所有匹配** |
| **找到0个** | ⬇️ 继续一级匹配 | 进入下一级匹配流程 |

#### 代码位置
```java
// 行号: 1419-1437
// 优先匹配：首先查询部门商品历史记录，如果订货商品名称和规格完全匹配，则直接推断为该商品
Map<String, Object> depGoodsMap = new HashMap<>();
depGoodsMap.put("depId", ordersEntity.getNxDoDepartmentId());
depGoodsMap.put("name", goodsName);
depGoodsMap.put("standard", ordersEntity.getNxDoStandard());
List<NxDepartmentDisGoodsEntity> departmentDisGoodsList = 
    nxDepartmentDisGoodsService.queryDepartmentGoods(depGoodsMap);

if (departmentDisGoodsList.size() == 1) {
    // 直接保存订单，跳过后续匹配
    returnList.add(saveOneOrder(ordersEntity, distributerGoodsEntity));
    continue;
}
```

---

### 🟠 级别1：分销商完全匹配（一级匹配）

**简称**: 分销商完全匹配  
**级别**: 1  
**数据源**: `nx_distributer_goods`（分销商商品表）  
**查询方法**: `nxDistributerGoodsService.queryDisGoodsByName()`

#### 查询条件
```java
{
    "disId": 分销商ID,
    "searchStr": 商品名称,
    "standard": 规格
}
```

#### 匹配规则
- **完全匹配**：商品名称 + 规格必须完全一致
- **数据源**：分销商商品主表

#### 结果处理

| 匹配结果 | 处理方式 | 说明 |
|---------|---------|------|
| **找到1个** | ✅ 直接保存订单 | 调用 `saveOneOrder()`，订单状态设为0（正常） |
| **找到多个** | 🔍 查询部门历史记录辅助判断 | 如果部门历史记录中有1个匹配，则保存订单；否则列为候选商品列表 |
| **找到0个** | ⬇️ 继续二级匹配 | 进入二级匹配流程 |

#### 代码位置
```java
// 行号: 1439-1642
Map<String, Object> mapZero = new HashMap<>();
mapZero.put("disId", ordersEntity.getNxDoDistributerId());
mapZero.put("searchStr", goodsName);
mapZero.put("standard", ordersEntity.getNxDoStandard());
List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = 
    nxDistributerGoodsService.queryDisGoodsByName(mapZero);

if (distributerGoodsEntitiesZero.size() == 1) {
    // 直接保存订单
    returnList.add(saveOneOrder(ordersEntity, disGoodsEntity));
} else if (distributerGoodsEntitiesZero.size() > 1) {
    // 查询部门历史记录辅助判断
    // 如果部门历史记录中有1个匹配，则保存订单；否则列为候选商品列表
}
```

---

### 🟡 级别2：分销商名称匹配（二级匹配）

**简称**: 分销商名称匹配  
**级别**: 2  
**数据源**: `nx_distributer_goods`（分销商商品表）  
**查询方法**: `nxDistributerGoodsService.queryDisGoodsByName()`

#### 查询条件
```java
{
    "disId": 分销商ID,
    "searchStr": 商品名称
    // 注意：不包含规格
}
```

#### 匹配规则
- **部分匹配**：仅商品名称匹配，**不要求规格匹配**
- **数据源**：分销商商品主表

#### 结果处理

| 匹配结果 | 处理方式 | 说明 |
|---------|---------|------|
| **找到1个** | 🔍 查询部门历史记录辅助判断 | 如果部门历史记录中有1个匹配，则保存订单；否则列为候选商品列表 |
| **找到多个** | 🔍 查询部门历史记录辅助判断 | 如果部门历史记录中有1个匹配，则保存订单；否则列为候选商品列表 |
| **找到0个** | ⬇️ 进入二级匹配后处理流程 | 先查询别名完全匹配，无结果后再查询商品名称模糊搜索 |

#### 二级匹配未找到时的处理流程

当二级匹配未找到商品时，会按以下顺序继续匹配：

**2.1 别名完全匹配（优先级提前）**

**查询条件**:
```java
{
    "disId": 分销商ID,
    "alias": 商品名称
}
```

**匹配规则**:
- 别名完全匹配（等值查询，性能更好）
- 通过别名表关联查询分销商商品

**结果处理**:
- **找到1个** → ✅ 直接保存订单
- **找到多个** → 📋 列为候选商品列表
- **找到0个** → ⬇️ 进入2.2

**2.2 商品名称模糊搜索（优先级靠后）**

**查询条件**:
```java
{
    "disId": 分销商ID,
    "depId": 部门ID,
    "searchStr": 商品名称
}
```

**SQL查询**: `WHERE nx_dg_goods_name LIKE CONCAT('%', ?, '%')`

**匹配规则**:
- 商品名称模糊匹配（LIKE查询）
- 作为兜底方案，优先级低于别名完全匹配

**结果处理**:
- **找到1个或多个** → 📋 列为候选商品列表（不直接保存订单）
- **找到0个** → ⬇️ 继续三级匹配（拼音匹配）

#### 代码位置
```java
// 行号: 1454-1510
// 二级匹配
Map<String, Object> mapOne = new HashMap<>();
mapOne.put("disId", ordersEntity.getNxDoDistributerId());
mapOne.put("searchStr", goodsName);
List<NxDistributerGoodsEntity> distributerGoodsEntitiesOne = 
    nxDistributerGoodsService.queryDisGoodsByName(mapOne);

// 二级匹配未找到时，先查询别名完全匹配
if (distributerGoodsEntitiesOne.size() == 0) {
    Map<String, Object> mapA = new HashMap<>();
    mapA.put("disId", ordersEntity.getNxDoDistributerId());
    mapA.put("alias", goodsName);
    List<NxDistributerGoodsEntity> distributerGoodsEntitiesA = 
        nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
    
    // 别名完全匹配无结果后，查询商品名称模糊搜索
    mapOne.put("depId", ordersEntity.getNxDoDepartmentId());
    List<NxDistributerGoodsEntity> distributerGoodsEntitiesLikeName = 
        nxDistributerGoodsService.queryDisGoodsByLikeName(mapOne);
}
```

---

### 🟢 级别3：拼音匹配（三级匹配）

**简称**: 拼音匹配  
**级别**: 3  
**数据源**: `nx_distributer_goods`（分销商商品表）  
**查询方法**: `nxDistributerGoodsService.queryDisGoodsByNamePinyin()` / `queryDisGoodsByNameLikePinyin()`

#### 3.1 拼音 + 规格匹配

**查询条件**:
```java
{
    "disId": 分销商ID,
    "searchPinyin": 商品名称的拼音,
    "standard": 规格
}
```

**匹配规则**:
- 拼音完全匹配 + 规格完全匹配
- 拼音转换：如果商品名称包含汉字，则转换为拼音；否则使用原名

**结果处理**:
- **找到1个** → ✅ 直接保存订单
- **找到多个** → 📋 列为候选商品列表
- **找到0个** → ⬇️ 进入3.2

#### 3.2 仅拼音匹配

**查询条件**:
```java
{
    "disId": 分销商ID,
    "searchPinyin": 商品名称的拼音
    // 注意：不包含规格
}
```

**匹配规则**:
- 拼音完全匹配，不要求规格匹配

**结果处理**:
- **找到1个或多个** → 📋 列为候选商品列表
- **找到0个** → ⬇️ 进入3.3

#### 3.3 拼音模糊匹配

**查询条件**:
```java
{
    "disId": 分销商ID,
    "searchPinyin": 商品名称的拼音
}
```

**SQL查询**: `WHERE nx_dg_goods_pinyin LIKE CONCAT('%', ?, '%')`

**匹配规则**:
- 拼音模糊匹配（LIKE查询）
- 最宽松的拼音匹配方式

**结果处理**:
- **找到1个或多个** → 📋 列为候选商品列表
- **找到0个** → ⬇️ 进入四级匹配

#### 代码位置
```java
// 行号: 1455-1580
// 3.1 拼音+规格匹配
Map<String, Object> mapTwo = new HashMap<>();
mapTwo.put("disId", ordersEntity.getNxDoDistributerId());
mapTwo.put("searchPinyin", pinyinString);
mapTwo.put("standard", ordersEntity.getNxDoStandard());
List<NxDistributerGoodsEntity> disGoodsByNamePinyin = 
    nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);

// 3.2 仅拼音匹配
mapTwo.put("standard", null);
List<NxDistributerGoodsEntity> disGoodsByNamePinyinJust = 
    nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);

// 3.3 拼音模糊匹配
List<NxDistributerGoodsEntity> disGoodsByNamePinyinLike = 
    nxDistributerGoodsService.queryDisGoodsByNameLikePinyin(mapTwo);
```

---

### 🔵 级别4：别名匹配（四级匹配）

**简称**: 别名匹配  
**级别**: 4  
**数据源**: `nx_distributer_goods` + `nx_distributer_alias`（分销商商品别名表）  
**查询方法**: `nxDistributerGoodsService.queryDisGoodsByAliasLike()`

**注意**：别名完全匹配已提前到**二级匹配后**执行（见级别2的2.1），这里只执行别名模糊匹配。

#### 4.1 别名模糊匹配（原4.2）

**查询条件**:
```java
{
    "disId": 分销商ID,
    "alias": 商品名称
}
```

**SQL查询**: `WHERE nx_DA_alias_name LIKE CONCAT('%', ?, '%')`

**匹配规则**:
- 别名模糊匹配（LIKE查询）

**结果处理**:
- **找到1个或多个** → 📋 列为候选商品列表
- **找到0个** → ⬇️ 进入兜底匹配

#### 代码位置
```java
// 行号: 1531-1577
// 别名完全匹配已在二级匹配后执行（见级别2的2.1）
// 这里只执行别名模糊匹配
List<NxDistributerGoodsEntity> distributerGoodsEntitiesALike = 
    nxDistributerGoodsService.queryDisGoodsByAliasLike(mapTwo);
```

---

### ⚪ 级别5：部门历史名称匹配（兜底匹配）

**简称**: 部门历史名称匹配  
**级别**: 5  
**数据源**: `nx_department_dis_goods`（部门商品历史记录表）  
**查询方法**: `nxDepartmentDisGoodsService.queryDepartmentGoods()`

#### 查询条件
```java
{
    "depId": 部门ID,
    "name": 商品名称
    // 注意：不包含规格
}
```

#### 匹配规则
- **部分匹配**：仅商品名称匹配，不要求规格匹配
- **最后机会**：在所有分销商商品匹配都失败后的兜底方案
- **部门商品历史记录级别2**：仅名称匹配（不含规格）

#### 结果处理

| 匹配结果 | 处理方式 | 说明 |
|---------|---------|------|
| **找到1个** | ✅ 直接保存订单 | 调用 `saveOneOrder()` |
| **找到多个** | 📋 列为候选商品列表 | 调用 `aaaTemp()` |
| **找到0个** | 📋 创建临时订单 | 调用 `aaaTemp()`，状态设为-2（待确认） |

#### 代码位置
```java
// 行号: 1492-1517
Map<String, Object> mapDep = new HashMap<>();
mapDep.put("depId", ordersEntity.getNxDoDepartmentId());
mapDep.put("name", goodsName);
List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntityList = 
    nxDepartmentDisGoodsService.queryDepartmentGoods(mapDep);
```

---

### ⚫ 级别6：临时订单创建（最终处理）

**简称**: 临时订单创建  
**级别**: 6（最终）  
**数据源**: `nx_goods`（系统商品表）  
**处理方法**: `aaaTemp()`

#### 触发条件
- 所有匹配级别都未找到商品
- 或者找到多个候选商品需要用户选择

#### 处理逻辑

1. **创建临时订单**
   - 订单状态设为 `-2`（待确认状态）
   - 保存到数据库

2. **查询系统商品**
   - 查询条件：商品名称、别名、拼音
   - 数据源：`nx_goods` 表
   - **重要规则**：如果已有分销商商品候选列表，**不查询系统商品**

3. **设置候选商品列表**
   - 分销商商品候选列表：`nxDistributerGoodsEntityList`
   - 系统商品候选列表：`nxGoodsEntities`（仅在无分销商商品时设置）

#### 代码位置
```java
// 行号: 3998-4041
private NxDepartmentOrdersEntity aaaTemp(NxDepartmentOrdersEntity order) {
    // 查询系统商品
    // 如果已有分销商商品候选列表，不设置系统商品列表
    if (order.getNxDistributerGoodsEntityList() != null && 
        order.getNxDistributerGoodsEntityList().size() > 0) {
        // 不设置系统商品列表，优先显示分销商商品
    } else {
        // 设置系统商品列表
        order.setNxGoodsEntities(all);
    }
    
    // 设置订单状态为-2（待确认）
    order.setNxDoStatus(-2);
    // 保存订单
    nxDepartmentOrdersService.save(order);
    return order;
}
```

---

## 重要规则和特性

### 1. 优先级规则

- **优先匹配 > 一级 > 二级 > 三级 > 四级 > 兜底**
- 一旦在某个级别找到唯一匹配，立即保存订单，**跳过后续所有匹配**
- 优先匹配成功时，使用 `continue` 语句跳过后续逻辑

### 2. 匹配精度规则

匹配精度从高到低：
1. **完全匹配**：名称 + 规格
2. **部分匹配**：仅名称
3. **别名完全匹配**：别名完全匹配（等值查询，性能更好，优先级提前到二级匹配后）
4. **商品名称模糊搜索**：商品名称模糊匹配（LIKE查询，优先级靠后）
5. **拼音匹配**：拼音 + 规格 → 仅拼音 → 拼音模糊
6. **别名模糊匹配**：别名模糊匹配（LIKE查询）
7. **兜底匹配**：部门历史记录（仅名称）

### 3. 部门历史记录辅助决策

在以下场景中，会查询部门商品历史记录**辅助决策**（不是独立匹配级别）：
- 一级匹配找到多个结果时
- 二级匹配找到结果时（无论1个还是多个）

**辅助决策查询条件**：
```java
{
    "depId": 部门ID,
    "name": 商品名称
    // 注意：不包含规格（仅名称匹配）
}
```

**辅助决策逻辑**：
- 如果部门历史记录中有1个匹配 → 直接保存订单
- 如果部门历史记录中有多个匹配 → 列为候选商品列表
- 如果部门历史记录中无匹配 → 列为候选商品列表

**注意**：辅助决策查询使用的是**部门商品历史记录级别2**（仅名称匹配），用于辅助判断，不是独立的匹配级别。

### 4. 候选商品列表规则

**触发条件**：
- 任何级别找到多个匹配结果
- 所有级别都未找到商品（创建临时订单）

**候选商品类型**：
1. **分销商商品候选列表**（`nxDistributerGoodsEntityList`）
   - 优先级：高
   - 如果存在，**不显示系统商品候选列表**

2. **系统商品候选列表**（`nxGoodsEntities`）
   - 优先级：低
   - 仅在**没有分销商商品候选列表**时显示

**订单状态**：
- 候选商品列表的订单状态设为 `-2`（待确认）
- 需要用户从候选列表中选择具体商品

### 5. 数据返回优化

接口返回使用 `PasteSearchGoodsResponseDTO`，只包含前端需要的字段：
- 订单基本信息（ID、名称、数量、规格等）
- 候选商品列表（简化的DTO对象）
- 原始订单JSON（`rawText`字段）

---

## 查询性能优化建议

### 1. 索引优化

建议在以下字段上创建索引：
- `nx_department_dis_goods`: `(nx_DDG_department_id, nx_DDG_order_goods_name, nx_DDG_order_standard)`
- `nx_distributer_goods`: `(nx_dg_distributer_id, nx_dg_goods_name, nx_dg_goods_standardname)`
- `nx_distributer_goods`: `(nx_dg_distributer_id, nx_dg_goods_pinyin)`
- `nx_distributer_alias`: `(nx_DA_dis_goods_id, nx_DA_alias_name)`

### 2. 查询优化

- 优先匹配使用完全匹配，性能最好
- 拼音模糊匹配和别名模糊匹配使用LIKE查询，性能相对较低
- 建议限制模糊匹配的结果数量

### 3. 缓存策略

- 部门商品历史记录查询结果可以缓存（按部门ID）
- 分销商商品查询结果可以缓存（按分销商ID）

---

## 示例场景

### 场景1：级别0匹配成功

**输入**: 商品名称="甘蓝", 规格="斤"

**匹配过程**:
1. ✅ **级别0：部门历史完全匹配** → **找到1个** → 直接保存订单

**结果**: 订单状态=0（正常），直接保存

---

### 场景2：级别1匹配成功

**输入**: 商品名称="甘蓝", 规格="斤"

**匹配过程**:
1. ❌ 级别0：部门历史完全匹配 → 未找到
2. ✅ **级别1：分销商完全匹配** → **找到1个** → 直接保存订单

**结果**: 订单状态=0（正常），直接保存

---

### 场景3：级别3匹配成功（多候选商品）

**输入**: 商品名称="小米辣", 规格="斤"

**匹配过程**:
1. ❌ 级别0：部门历史完全匹配 → 未找到
2. ❌ 级别1：分销商完全匹配 → 未找到
3. ❌ 级别2：分销商名称匹配 → 未找到
4. ❌ 级别3.1：拼音+规格匹配 → 未找到
5. ❌ 级别3.2：仅拼音匹配 → 未找到
6. ✅ **级别3.3：拼音模糊匹配** → **找到5个** → 列为候选商品列表

**结果**: 
- 订单状态=-2（待确认）
- `nxDistributerGoodsEntityList`: 5个分销商商品候选
- `nxGoodsEntities`: null（因为已有分销商商品候选列表）

---

### 场景4：级别6最终处理（创建临时订单）

**输入**: 商品名称="新商品", 规格="斤"

**匹配过程**:
1. ❌ 级别0：部门历史完全匹配 → 未找到
2. ❌ 级别1：分销商完全匹配 → 未找到
3. ❌ 级别2：分销商名称匹配 → 未找到
4. ❌ 级别3：拼音匹配 → 未找到
5. ❌ 级别4：别名匹配 → 未找到
6. ❌ 级别5：部门历史名称匹配 → 未找到
7. 📋 **级别6：临时订单创建** → 创建临时订单 + 查询系统商品

**结果**:
- 订单状态=-2（待确认）
- `nxDistributerGoodsEntityList`: null
- `nxGoodsEntities`: 系统商品候选列表（如果有）

---

## 代码关键位置

| 级别 | 简称 | 代码位置 | 说明 |
|------|------|---------|------|
| 级别0 | 部门历史完全匹配 | 1419-1437行 | 部门商品历史记录查询（名称+规格） |
| 级别1 | 分销商完全匹配 | 1439-1642行 | 分销商商品（名称+规格） |
| 级别2 | 分销商名称匹配 | 1454-1510行 | 分销商商品（仅名称）+ 别名完全匹配 + 商品名称模糊搜索 |
| 级别3 | 拼音匹配 | 1511-1580行 | 拼音匹配（3个子级别） |
| 级别4 | 别名匹配 | 1531-1577行 | 别名模糊匹配（别名完全匹配已提前到二级匹配后） |
| 级别5 | 部门历史名称匹配 | 1492-1517行 | 部门历史记录（仅名称） |
| 级别6 | 临时订单创建 | 3998-4041行 | `aaaTemp()` 方法 |
| - | DTO转换 | 4681-4755行 | `convertToResponseDTO()` 方法 |

---

## 注意事项

1. **订单状态说明**:
   - `0`: 正常订单（已匹配到商品）
   - `-2`: 待确认订单（需要用户选择商品）

2. **候选商品列表优先级**:
   - 分销商商品候选列表优先显示
   - 只有在没有分销商商品候选列表时，才显示系统商品候选列表

3. **日志记录**:
   - 每个匹配级别都有详细的日志记录
   - 日志格式：`[pasteSearchGoods] 级别-描述，查询结果数量: {}`

4. **错误处理**:
   - 所有查询都有异常处理
   - 如果查询失败，会继续下一级匹配

---

## 更新日志

- **2025-11-30**: 
  - 优化返回数据结构，使用DTO减少数据传输量
  - 修复候选商品列表逻辑：如果已有分销商商品候选列表，不显示系统商品候选列表
  - 添加订单ID字段到返回数据
  - 添加详细的日志记录
  - **调整匹配优先级**：
    - 别名完全匹配提前到二级匹配后执行（优先级提前，性能更好）
    - 商品名称模糊搜索保留但优先级靠后（作为兜底方案）
    - 移除了三级匹配后重复的别名完全匹配逻辑

---

## 相关文档

- [API接口文档](./api-docs.md)
- [数据库设计文档](./database-design.md)
- [DTO设计文档](./dto-design.md)

