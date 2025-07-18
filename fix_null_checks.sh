#!/bin/bash

# 批量修改Controller文件中的null判断
# 为价格、重量、日期、标准相关字段添加isEmpty判断

echo "开始批量修改Controller文件中的null判断..."

# 修改NxDepartmentOrdersController.java
echo "修改 NxDepartmentOrdersController.java..."

# 价格相关字段
sed -i '' 's/getNxDoPrice() != null/getNxDoPrice() != null \&\& !getNxDoPrice().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/NxDepartmentOrdersController.java
sed -i '' 's/getNxDoExpectPrice() != null/getNxDoExpectPrice() != null \&\& !getNxDoExpectPrice().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/NxDepartmentOrdersController.java
sed -i '' 's/getNxDoCostPrice() != null/getNxDoCostPrice() != null \&\& !getNxDoCostPrice().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/NxDepartmentOrdersController.java

# 重量相关字段
sed -i '' 's/getNxDoWeight() != null/getNxDoWeight() != null \&\& !getNxDoWeight().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/NxDepartmentOrdersController.java
sed -i '' 's/getNxDoQuantity() != null/getNxDoQuantity() != null \&\& !getNxDoQuantity().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/NxDepartmentOrdersController.java

# 标准相关字段
sed -i '' 's/getNxDoStandard() != null/getNxDoStandard() != null \&\& !getNxDoStandard().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/NxDepartmentOrdersController.java
sed -i '' 's/getNxDoWeightId() != null/getNxDoWeightId() != null \&\& !getNxDoWeightId().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/NxDepartmentOrdersController.java

# 修改Gb开头的Controller文件
echo "修改 Gb 开头的 Controller 文件..."

# GbDistributerGoodsController.java
if [ -f "src/main/java/com/nongxinle/controller/GbDistributerGoodsController.java" ]; then
    echo "修改 GbDistributerGoodsController.java..."
    sed -i '' 's/getGbDgPrice() != null/getGbDgPrice() != null \&\& !getGbDgPrice().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/GbDistributerGoodsController.java
    sed -i '' 's/getGbDgWeight() != null/getGbDgWeight() != null \&\& !getGbDgWeight().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/GbDistributerGoodsController.java
    sed -i '' 's/getGbDgStandard() != null/getGbDgStandard() != null \&\& !getGbDgStandard().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/GbDistributerGoodsController.java
fi

# GbDistributerPurchaseGoodsController.java
if [ -f "src/main/java/com/nongxinle/controller/GbDistributerPurchaseGoodsController.java" ]; then
    echo "修改 GbDistributerPurchaseGoodsController.java..."
    sed -i '' 's/getGbDpgPrice() != null/getGbDpgPrice() != null \&\& !getGbDpgPrice().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/GbDistributerPurchaseGoodsController.java
    sed -i '' 's/getGbDpgWeight() != null/getGbDpgWeight() != null \&\& !getGbDpgWeight().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/GbDistributerPurchaseGoodsController.java
    sed -i '' 's/getGbDpgStandard() != null/getGbDpgStandard() != null \&\& !getGbDpgStandard().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/GbDistributerPurchaseGoodsController.java
fi

# GbDepartmentGoodsStockController.java
if [ -f "src/main/java/com/nongxinle/controller/GbDepartmentGoodsStockController.java" ]; then
    echo "修改 GbDepartmentGoodsStockController.java..."
    sed -i '' 's/getGbDgsPrice() != null/getGbDgsPrice() != null \&\& !getGbDgsPrice().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/GbDepartmentGoodsStockController.java
    sed -i '' 's/getGbDgsWeight() != null/getGbDgsWeight() != null \&\& !getGbDgsWeight().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/GbDepartmentGoodsStockController.java
    sed -i '' 's/getGbDgsStandard() != null/getGbDgsStandard() != null \&\& !getGbDgsStandard().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/GbDepartmentGoodsStockController.java
fi

echo "批量修改完成！"
echo "请检查修改结果，确保语法正确。" 