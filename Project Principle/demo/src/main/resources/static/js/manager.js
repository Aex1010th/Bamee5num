import { users, menuItems } from "./db.js";

export function setupManagerDashboard() {
    const user = JSON.parse(localStorage.getItem("currentUser"));
    if (!user || user.role !== "manager") {
        alert("กรุณาเข้าสู่ระบบ!");
        window.location.href = "/";
        return;
    }

    document.getElementById("welcomeText").textContent = `สวัสดี, ${user.fullName}`;
    document.getElementById("userNav").classList.remove("hidden");
    document.getElementById("navButtons").classList.add("hidden");

    const menuList = document.getElementById("menuManagementList");
    menuList.innerHTML = "";
    menuItems.forEach(item => {
        const div = document.createElement("div");
        div.className = "flex justify-between bg-gray-100 p-4 rounded-lg";
        div.innerHTML = `
            <span>${item.name} - ฿${item.price}</span>
            <button class="bg-red-500 text-white px-3 py-1 rounded delete-menu" data-id="${item.id}">ลบ</button>
        `;
        menuList.appendChild(div);
    });

    document.querySelectorAll(".delete-menu").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = parseInt(btn.dataset.id);
            const idx = menuItems.findIndex(i => i.id === id);
            if(idx !== -1) menuItems.splice(idx,1);
            setupManagerDashboard(); // Refresh
        });
    });
}

document.addEventListener("DOMContentLoaded", () => {
    setupManagerDashboard();
});
