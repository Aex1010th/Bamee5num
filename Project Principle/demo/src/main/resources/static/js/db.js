// db.js

// ผู้ใช้งาน
export let users = [
    { id: 1, username: "customer1", password: "1234", role: "customer", fullName: "สมชาย ใจดี" },
    { id: 2, username: "employee1", password: "1234", role: "employee", fullName: "มานะ แสนดี" },
    { id: 3, username: "manager1", password: "1234", role: "manager", fullName: "สมศักดิ์ จิตดี" }
];

// เมนูอาหาร
export let menuItems = [
    { id: 1, name: "บะหมี่หมูแดง", price: 50, category: "noodles", status: 'available', description: "เส้นเหนียวนุ่ม หมูแดงย่าง" },
    { id: 2, name: "น้ำอัดลม", price: 20, category: "drinks", status: 'available',  description: "น้ำอัดลมเย็น ๆ" },
    { id: 3, name: "ขนมหวาน", price: 30, category: "desserts", status: 'available',  description: "หวานอร่อย สดใหม่" }
];

// ตะกร้าสินค้า (เฉพาะ customer)
export let cart = [];

// ข้อมูลผู้ใช้งานปัจจุบัน
export let currentUser = null;

// ออเดอร์ (ใช้ได้กับ Employee / Manager)
export let orders = [
    { id: 1, customerId: 1, items: [{ id: 1, name: "บะหมี่หมูแดง", quantity: 2, price: 50 }], total: 100, status: "pending" }
];

// การจอง (ใช้ได้กับ Employee / Manager)
export let reservations = [
    { id: 1, customerId: 1, name: "สมชาย ใจดี", date: "2025-09-12", people: 2, status: "confirmed" }
];

// ฟังก์ชันช่วยเหลือ เพิ่ม order ใหม่
export function addOrder(customerId, items) {
    const newOrder = {
        id: orders.length + 1,
        customerId,
        items,
        total: items.reduce((sum, i) => sum + i.price * i.quantity, 0),
        status: "pending"
    };
    orders.push(newOrder);
    return newOrder;
}

// ฟังก์ชันช่วยเหลือ เพิ่ม reservation ใหม่
export function addReservation(customerId, name, date, people) {
    const newRes = {
        id: reservations.length + 1,
        customerId,
        name,
        date,
        people,
        status: "pending"
    };
    reservations.push(newRes);
    return newRes;
}

// ฟังก์ชัน login
export function login(username, password) {
    const user = users.find(u => u.username === username && u.password === password);
    if(user) currentUser = user;
    return user;
}

// ฟังก์ชัน logout
export function logout() {
    currentUser = null;
    cart = [];
}
