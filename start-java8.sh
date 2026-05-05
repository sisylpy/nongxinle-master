#!/bin/bash

# nongxinle老项目 - Java 1.8启动脚本
# 使用方法: source start-java8.sh

echo "🔧 为nongxinle老项目配置Java 1.8环境..."

# 设置Java 1.8环境
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "✅ Java版本已切换:"
java -version

echo ""
echo "📁 当前目录: $(pwd)"
echo "🔗 JAVA_HOME: $JAVA_HOME"
echo ""
echo "💡 提示: 现在可以运行 mvn clean compile 等命令"
echo "💡 要切换回Java 17，请运行: source ../ai-marketing-server/start-java17.sh"