# 📋 Hướng dẫn Test API Groups trên Postman

## 🚀 Cách sử dụng

### 1. Import Collection vào Postman
1. Mở Postman
2. Click "Import" 
3. Chọn file `Postman_Group_API_Collection.json`
4. Collection sẽ được import với tên "WorkManager - Group API Tests"

### 2. Thiết lập Environment Variables
Trước khi test, bạn cần cập nhật các biến trong collection:

| Variable | Giá trị mặc định | Mô tả |
|----------|------------------|-------|
| `baseUrl` | `http://localhost:8080` | URL của server |
| `authToken` | (để trống) | Token JWT (sẽ được set tự động sau khi login) |
| `testUserId` | `1` | ID của user test |
| `testBoardId` | `1` | ID của board test (sẽ được set tự động) |
| `testGroupId` | `1` | ID của group test (sẽ được set tự động) |

### 3. Thứ tự Test

#### 🔐 Bước 1: Authentication
1. **Login để lấy token**
   - Method: `POST`
   - URL: `{{baseUrl}}/api/auth/login`
   - Body: 
   ```json
   {
     "email": "admin@example.com",
     "password": "password123"
   }
   ```
   - Token sẽ được tự động lưu vào variable `authToken`

2. **Test Authentication**
   - Method: `GET`
   - URL: `{{baseUrl}}/api/auth/test-auth`
   - Kiểm tra xem user đã được authenticate chưa

#### 📋 Bước 2: Setup Board
1. **Create Test Board**
   - Method: `POST`
   - URL: `{{baseUrl}}/api/boards`
   - Body:
   ```json
   {
     "name": "Test Board for Groups",
     "description": "Board để test Groups API"
   }
   ```

#### 📁 Bước 3: Test Groups API

##### ✅ **Create Group Tests**
1. **Create Group - Success**
   - Method: `POST`
   - URL: `{{baseUrl}}/api/groups`
   - Body:
   ```json
   {
     "name": "Development Team",
     "boardId": {{testBoardId}}
   }
   ```

2. **Create Group - Missing Name**
   - Method: `POST`
   - URL: `{{baseUrl}}/api/groups`
   - Body:
   ```json
   {
     "boardId": {{testBoardId}}
   }
   ```
   - Expected: 400 Bad Request

3. **Create Group - Missing BoardId**
   - Method: `POST`
   - URL: `{{baseUrl}}/api/groups`
   - Body:
   ```json
   {
     "name": "Test Group"
   }
   ```
   - Expected: 400 Bad Request

4. **Create Group - Invalid BoardId**
   - Method: `POST`
   - URL: `{{baseUrl}}/api/groups`
   - Body:
   ```json
   {
     "name": "Test Group",
     "boardId": "invalid"
   }
   ```
   - Expected: 400 Bad Request

##### 📖 **Read Group Tests**
1. **Get All Groups**
   - Method: `GET`
   - URL: `{{baseUrl}}/api/groups`
   - Expected: 200 OK với array các groups

2. **Get Group By ID - Success**
   - Method: `GET`
   - URL: `{{baseUrl}}/api/groups/{{testGroupId}}`
   - Expected: 200 OK với group data

3. **Get Group By ID - Not Found**
   - Method: `GET`
   - URL: `{{baseUrl}}/api/groups/99999`
   - Expected: 404 Not Found

##### ✏️ **Update Group Tests**
1. **Update Group - Success**
   - Method: `PUT`
   - URL: `{{baseUrl}}/api/groups/{{testGroupId}}`
   - Body:
   ```json
   {
     "name": "Updated Development Team"
   }
   ```
   - Expected: 200 OK với updated data

2. **Update Group - Missing Name**
   - Method: `PUT`
   - URL: `{{baseUrl}}/api/groups/{{testGroupId}}`
   - Body: `{}`
   - Expected: 400 Bad Request

3. **Update Group - Not Found**
   - Method: `PUT`
   - URL: `{{baseUrl}}/api/groups/99999`
   - Body:
   ```json
   {
     "name": "Updated Name"
   }
   ```
   - Expected: 404 Not Found

##### 🗑️ **Delete Group Tests**
1. **Delete Group - Success**
   - Method: `DELETE`
   - URL: `{{baseUrl}}/api/groups/{{testGroupId}}`
   - Expected: 200 OK

2. **Delete Group - Not Found**
   - Method: `DELETE`
   - URL: `{{baseUrl}}/api/groups/99999`
   - Expected: 404 Not Found

##### 📋 **Get Tasks Tests**
1. **Get Tasks By Group ID**
   - Method: `GET`
   - URL: `{{baseUrl}}/api/groups/{{testGroupId}}/tasks`
   - Expected: 200 OK với array tasks

2. **Get Tasks By Group ID - Not Found**
   - Method: `GET`
   - URL: `{{baseUrl}}/api/groups/99999/tasks`
   - Expected: 404 Not Found

## 🧪 Test Cases được cover

### ✅ **Happy Path Tests**
- ✅ Tạo group thành công
- ✅ Lấy danh sách groups
- ✅ Lấy group theo ID
- ✅ Cập nhật group
- ✅ Xóa group
- ✅ Lấy tasks trong group

### ❌ **Error Handling Tests**
- ❌ Missing required fields (name, boardId)
- ❌ Invalid data types
- ❌ Not found scenarios
- ❌ Permission denied scenarios

### 🔐 **Security Tests**
- 🔐 Authentication required
- 🔐 Authorization based on board permissions
- 🔐 JWT token validation

## 📊 Expected Response Formats

### ✅ Success Response (200)
```json
{
  "id": 1,
  "name": "Development Team",
  "boardId": 1,
  "boardName": "Test Board for Groups"
}
```

### ❌ Error Response (400)
```json
"Missing 'name' or 'boardId'"
```

### ❌ Not Found Response (404)
```json
{}
```

### ❌ Forbidden Response (403)
```json
"Bạn không có quyền tạo group trên board này"
```

## 🚨 Lưu ý quan trọng

1. **Authentication**: Tất cả API đều yêu cầu JWT token
2. **Permissions**: User phải có quyền trên board để thao tác với groups
3. **Data Dependencies**: Groups phải thuộc về một board
4. **Cascade Delete**: Khi xóa board, tất cả groups sẽ bị xóa theo

## 🔧 Troubleshooting

### Lỗi thường gặp:
1. **401 Unauthorized**: Chưa login hoặc token hết hạn
2. **403 Forbidden**: Không có quyền trên board
3. **404 Not Found**: Board/Group không tồn tại
4. **400 Bad Request**: Dữ liệu đầu vào không hợp lệ

### Cách fix:
1. Chạy lại request login để lấy token mới
2. Kiểm tra quyền của user trên board
3. Kiểm tra ID của board/group có tồn tại không
4. Kiểm tra format dữ liệu gửi lên 