# 🛡️ System Admin Toàn Quyền (Super Admin)

## 1️⃣ **Migration MySQL**

```sql
ALTER TABLE user ADD COLUMN is_system_admin BOOLEAN DEFAULT FALSE;
```

## 2️⃣ **Gán 1 user là system admin**

### **Cách 1: SQL trực tiếp**
```sql
UPDATE user SET is_system_admin = TRUE WHERE id = 9;
```

### **Cách 2: API test**
```http
POST /api/roles/test-make-system-admin
Content-Type: application/json

{
  "userId": 9
}
```

**Response:**
```json
{
  "message": "Đã gán quyền SYSTEM ADMIN cho user!",
  "userId": 9,
  "userEmail": "admin@example.com",
  "isSystemAdmin": true
}
```

## 3️⃣ **Logic quyền mới**
- Nếu user là system admin (`isSystemAdmin = true`) thì luôn có toàn quyền trên hệ thống:
  - Tạo/sửa/xóa mọi board
  - Quản lý mọi user
  - Phân quyền trên mọi board
  - Không bị giới hạn bởi role trên từng board
- Các user khác chỉ có quyền theo từng board (manager/member/viewer)

## 4️⃣ **Test lại**
- Login với user admin
- Gọi API `/api/auth/my-permissions` → sẽ thấy `isSystemAdmin: true`
- Thử mọi thao tác (tạo/xóa board, phân quyền...) đều thành công

## 5️⃣ **Lưu ý**
- Chỉ nên có 1 system admin duy nhất
- Không cho phép user thường tự gán quyền này qua UI
- API test chỉ dùng cho dev/test, không public cho end-user

---

## 📝 **Bảng phân quyền chuẩn**

| Vai trò      | Áp dụng         | Quyền hạn                                                        |
|--------------|-----------------|-------------------------------------------------------------------|
| ADMIN        | Toàn hệ thống   | Tất cả các quyền của hệ thống                                     |
| MANAGER      | Trong từng board| Gán user vào board, tạo/sửa/xóa group và task                     |
| MEMBER       | Trong từng board| Xem và cập nhật task được gán                                     |
| VIEWER       | Trong từng board| Chỉ xem nội dung, không sửa                                       | 