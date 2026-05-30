# 项目名称（例如：User Management System）

> 一句话描述项目核心价值：一个基于 SpringBoot 的员工管理系统，提供 JWT 鉴权、RBAC 权限管理和 Excel 导入导出功能。

## ✨ 技术栈

- **核心框架**: Spring Boot 2.7.x / 3.x
- **项目管理**: Maven / Gradle
- **数据库**: MySQL 8.0 + MyBatis-Plus / Spring Data JPA
- **缓存**: Redis (用于 token 存储/分布式锁)
- **工具**: Lombok, Hutool, Swagger/Knife4j (API 文档)

## 🚀 快速启动

### 前置要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis (可选)

### 克隆与配置

```bash
# 1. 克隆项目
git clone https://github.com/your-username/your-project.git

# 2. 进入目录
cd your-project

# 3. 修改数据库配置 (application-dev.yml)
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_db?useSSL=false
    username: root
    password: your_password

# 4. 创建数据库并执行 SQL 脚本 (位于 /docs/schema.sql)
