# Hotel User Service

🔐 **Микросервис управления пользователями для отельной системы с JWT аутентификацией**

## 📋 Описание

Основной микросервис для управления пользователями в отельной системе. Обеспечивает регистрацию, авторизацию, управление профилями и административные функции с использованием JWT токенов и Spring Security.

## ⚡ Основной функционал

- 🔐 **Регистрация и авторизация** пользователей
- 🎫 **JWT токены** (Access + Refresh tokens)
- 👤 **Управление профилями** пользователей
- 👥 **Административная панель** для управления пользователями
- 🔒 **Разграничение ролей**: USER, HOTEL_OWNER, ADMIN
- 📄 **Пагинация** списков пользователей

## 🏗 Архитектура

- **Controller** → **Service** → **Repository** → **Database**
- **JWT фильтры** для аутентификации
- **BCrypt** хеширование паролей
- **PostgreSQL** база данных
- **RESTful API** с JSON ответами

## 🛠 Технологии

- **Backend**: Spring Boot 3.3, Spring Security, Spring Data JPA
- **Database**: PostgreSQL
- **Auth**: JWT (jjwt 0.12.5), BCrypt
- **Validation**: Bean Validation, Custom validators
- **Build**: Maven, Java 17
- **Tools**: Lombok, Actuator

## 🚀 Запуск

```bash
# Клонировать репозиторий
git clone https://github.com/VadimKharovyuk/HotelUserServce.git

# Перейти в директорию
cd HotelUserServce

# Настроить PostgreSQL базу данных
createdb hotelUserService

# Настроить переменные окружения (опционально)
export JWT_SECRET=your-secret-key
export DB_PASSWORD=your-db-password

# Запустить приложение
mvn spring-boot:run
```

Сервис будет доступен по адресу: `http://localhost:1511`

## 📡 API Endpoints

### Публичные (без токена):
- `POST /api/auth/register` - Регистрация пользователя
- `POST /api/auth/login` - Авторизация
- `POST /api/auth/refresh` - Обновление токена
- `POST /api/auth/logout` - Выход из системы

### Пользовательские (требуют токен):
- `GET /api/user/profile` - Получить профиль
- `PUT /api/user/profile` - Обновить профиль

### Административные (только ADMIN):
- `GET /api/admin/users` - Список всех пользователей (с пагинацией)
- `PUT /api/admin/users/{id}/role` - Изменить роль пользователя
- `DELETE /api/admin/users/{id}` - Удалить пользователя

## 🗄️ Структура БД

### Users Table:
```sql
- id (BIGINT, PK)
- username (VARCHAR, UNIQUE)
- email (VARCHAR, UNIQUE)
- password (VARCHAR, BCrypt hash)
- first_name, last_name, phone
- role (ENUM: USER, HOTEL_OWNER, ADMIN)
- email_verified, account_locked (BOOLEAN)
- created_at, updated_at, last_login (TIMESTAMP)
```

### Refresh Tokens Table:
```sql
- id (BIGINT, PK)
- token (VARCHAR, UNIQUE)
- user_id (BIGINT, FK)
- expires_at (TIMESTAMP)
- revoked (BOOLEAN)
```

## 🔧 Конфигурация

### application.properties:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/hotelUserService
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}

# JWT
jwt.secret=${JWT_SECRET}
jwt.access-token.expiration=1800000  # 30 минут
jwt.refresh-token.expiration=604800000  # 7 дней

# Server
server.port=1511
```

## 🔗 Связанные проекты

- [Hotel UI Service](https://github.com/VadimKharovyuk/HotelUiService) - Веб-интерфейс для системы

## 📊 Роли пользователей

- **USER** - Обычные клиенты отеля (бронирование, профиль)
- **HOTEL_OWNER** - Владельцы отелей (управление отелями)
- **ADMIN** - Администраторы системы (полный доступ)

## 🧪 Тестирование

```bash
# Запуск тестов
mvn test

# Тестовые данные
POST /api/auth/register
{
  "username": "test_user",
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User"
}
```

---

*Основа для микросервисной архитектуры отельной системы управления*
