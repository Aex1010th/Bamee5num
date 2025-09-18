// customer.js
import { menuItems, cart, currentUser } from "./db.js";

document.addEventListener("DOMContentLoaded", () => {
    setupCustomerDashboard();
});

export function setupCustomerDashboard() {
    const user = JSON.parse(localStorage.getItem("currentUser")) || currentUser;

    if (!user || user.role !== "customer") {
        alert("กรุณาเข้าสู่ระบบ!");
        window.location.href = "/";
        return;
    }

    // Navbar
    document.getElementById("welcomeText").textContent = `สวัสดี, ${user.fullName}`;
    document.getElementById("userNav").classList.remove("hidden");
    document.getElementById("navButtons").classList.add("hidden");

    // สร้างเมนูอาหาร
    const menuGrid = document.getElementById("menuGrid");
    menuGrid.innerHTML = "";
    menuItems.forEach(item => {
        const div = document.createElement("div");
        div.className = "bg-white p-6 rounded-xl shadow-lg";
        div.innerHTML = `
            <h3 class="text-xl font-semibold mb-2">${item.name}</h3>
            <p class="text-gray-600">${item.description}</p>
            <p class="text-orange-600 font-bold mt-2">฿${item.price}</p>
            <button class="bg-orange-500 text-white px-4 py-2 rounded mt-2 add-to-cart" data-id="${item.id}">ใส่ตะกร้า</button>
        `;
        menuGrid.appendChild(div);
    });

    // Event ใส่ตะกร้า
    document.querySelectorAll(".add-to-cart").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = parseInt(btn.dataset.id);
            const item = menuItems.find(i => i.id === id);
            if (item) addToCart(item);
        });
    });

    // ตะกร้า sidebar
    const cartBtn = document.getElementById("cartBtn");
    const cartSidebar = document.getElementById("cartSidebar");
    const closeCart = document.getElementById("closeCart");

    cartBtn.addEventListener("click", () => cartSidebar.classList.remove("translate-x-full"));
    closeCart.addEventListener("click", () => cartSidebar.classList.add("translate-x-full"));

    // Logout
    document.getElementById("logoutBtn").addEventListener("click", () => {
        localStorage.removeItem("currentUser");
        window.location.href = "/";
    });

    // โหลดตะกร้าจาก localStorage
    const storedCart = JSON.parse(localStorage.getItem("cart")) || [];
    storedCart.forEach(item => cart.push(item));
    updateCartUI();
}

function addToCart(item) {
    cart.push(item);
    localStorage.setItem("cart", JSON.stringify(cart));
    updateCartUI();
    showNotification(`${item.name} ถูกเพิ่มในตะกร้า`);
}

function updateCartUI() {
    const cartCount = document.getElementById("cartCount");
    const cartItemsDiv = document.getElementById("cartItems");
    const emptyCart = document.getElementById("emptyCart");
    const cartFooter = document.getElementById("cartFooter");
    const cartTotal = document.getElementById("cartTotal");

    cartItemsDiv.innerHTML = "";

    if (cart.length > 0) {
        cartCount.textContent = cart.length;
        cartCount.classList.remove("hidden");
    } else {
        cartCount.classList.add("hidden");
    }

    if (cart.length === 0) {
        emptyCart.classList.remove("hidden");
        cartFooter.classList.add("hidden");
    } else {
        emptyCart.classList.add("hidden");
        cartFooter.classList.remove("hidden");
        let total = 0;
        cart.forEach((item, index) => {
            total += item.price;
            const div = document.createElement("div");
            div.className = "flex justify-between items-center border-b pb-2";
            div.innerHTML = `
                <span>${item.name}</span>
                <div class="flex items-center space-x-2">
                    <span>฿${item.price}</span>
                    <button class="text-red-500 remove-item" data-index="${index}">ลบ</button>
                </div>
            `;
            cartItemsDiv.appendChild(div);
        });
        cartTotal.textContent = `฿${total}`;

        // ลบสินค้า
        document.querySelectorAll(".remove-item").forEach(btn => {
            btn.addEventListener("click", () => {
                const index = parseInt(btn.dataset.index);
                cart.splice(index, 1);
                localStorage.setItem("cart", JSON.stringify(cart));
                updateCartUI();
            });
        });
    }
}

function showNotification(message) {
    const notifications = document.getElementById("notifications");
    const div = document.createElement("div");
    div.textContent = message;
    div.className = "bg-orange-500 text-white p-2 rounded shadow";
    notifications.appendChild(div);
    setTimeout(() => div.remove(), 3000);
}
