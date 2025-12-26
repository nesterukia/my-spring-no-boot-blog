# Бэкенд приложения-блога с использованием Spring Boot

Полнофункциональное REST API приложение для управления блогом, написанное на Java с использованием Spring Boot.

### Основной функционал

- **Управление постами** - создание, чтение, обновление и удаление постов
- **Система комментариев** - добавление, редактирование и удаление комментариев к постам
- **Теги** - управление тегами и привязка их к постам
- **Загрузка изображений** - загрузка и получение изображений для постов
- **Поиск по текстам** - полнотекстовый поиск по содержимому постов с поддержкой пагинации

### Установка и запуск

1. **Клонируйте репозиторий**
2. **Отредактируйте файл `application.yml`** 

   Пример конфигурации `src/main/resources/application.yml`:
   ```yaml
   blog:
     uploads:
       images:
         directory: /app/storage/images/
   spring:
     datasource:
       url: jdbc:postgresql://postgres-db:5432/blog_db
       username: ${DB_USER}
       password: ${DB_PASSWORD}
   logging:
     level:
       root: INFO
   server:
     port: 8080
   ```

3. **Отредактируйте файл `docker-compose.yml`**

   Пример конфигурации `docker-compose.yml`:
```yaml
services:
   spring-backend:
      container_name: blog-backend
      build: .
      ports:
         - "8080:8080"
      restart: unless-stopped
      depends_on:
         - postgres-db
      networks:
         - blog-network
      volumes:
         - image-storage:/app/storage/images
      environment:
         - DB_USER=
         - DB_PASSWORD=

   postgres-db:
      image: 'postgres:13.1-alpine'
      container_name: blog-database
      environment:
         - POSTGRES_USER=
         - POSTGRES_PASSWORD=
         - POSTGRES_DB=blog_db
      volumes:
         - ./src/main/resources/db/postgre/db_init:/docker-entrypoint-initdb.d/
         - postgres-data:/var/lib/postgres/data
      ports:
         - "5432:5432"
      networks:
         - blog-network

volumes:
   image-storage:
      name: "blog-image-storage"
   postgres-data:
      name: "blog-database"

networks:
   blog-network:
      driver: bridge
```

4. **Запустите приложение**
   ```bash
   docker-compose up --build -d
   ```

   Приложение будет доступно по адресу `http://localhost:8080`

   База данных PostgreSQL будет доступна по адресу `localhost:5432`

5. **Запуск без Docker**

   ```bash
   # Собрать бэкенд (создать JAR файл)
   ./gradlew clean build -x test
   
   # Запустить тесты
   ./gradlew test
   
   # Запустить бэкенд
   ./gradlew bootRun
   ```
### API Endpoints

### Посты
- `GET /api/posts` - получить все посты (с поддержкой поиска и пагинации)
- `GET /api/posts/{postId}` - получить пост по ID
- `POST /api/posts` - создать новый пост
- `PUT /api/posts/{postId}` - обновить пост
- `DELETE /api/posts/{postId}` - удалить пост
- `POST /api/posts/{postId}/likes` - увеличить счетчик лайков
- `PUT /api/posts/{postId}/image` - загрузить изображение для поста
- `GET /api/posts/{postId}/image` - получить изображение поста

### Комментарии
- `GET /api/posts/{postId}/comments` - получить все комментарии поста
- `GET /api/posts/{postId}/comments/{commentId}` - получить комментарий
- `POST /api/posts/{postId}/comments` - создать комментарий
- `PUT /api/posts/{postId}/comments/{commentId}` - обновить комментарий
- `DELETE /api/posts/{postId}/comments/{commentId}` - удалить комментарий

### Остановка и удаление контейнеров

```bash
# Остановить контейнеры
docker-compose down

# Остановить и удалить все данные (включая базу данных)
docker-compose down -v
```
