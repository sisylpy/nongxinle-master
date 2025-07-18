#!/bin/bash

# 修复重复的isEmpty判断

echo "修复重复的isEmpty判断..."

# 修复NxDepartmentOrdersController.java中的重复判断
echo "修复 NxDepartmentOrdersController.java..."

# 修复第2752行的重复判断
sed -i '' 's/getNxDoPrice() != null && !getNxDoPrice().trim().isEmpty() && !oldOrderEntity.getNxDoPrice().trim().isEmpty()/oldOrderEntity.getNxDoPrice() != null \&\& !oldOrderEntity.getNxDoPrice().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/NxDepartmentOrdersController.java

# 修复第2781行的重复判断
sed -i '' 's/getNxDoPrice() != null && !getNxDoPrice().trim().isEmpty()/ordersEntity.getNxDoPrice() != null \&\& !ordersEntity.getNxDoPrice().trim().isEmpty()/g' src/main/java/com/nongxinle/controller/NxDepartmentOrdersController.java

# 修复其他重复的判断
sed -i '' 's/getNxDoPrice() != null && !getNxDoPrice().trim().isEmpty() && new BigDecimal(ordersEntity.getNxDoPrice()).compareTo(BigDecimal.ZERO) > 0/ordersEntity.getNxDoPrice() != null \&\& !ordersEntity.getNxDoPrice().trim().isEmpty() \&\& new BigDecimal(ordersEntity.getNxDoPrice()).compareTo(BigDecimal.ZERO) > 0/g' src/main/java/com/nongxinle/controller/NxDepartmentOrdersController.java

echo "修复完成！" 