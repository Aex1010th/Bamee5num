
let employeeModal;
let employeeForm;
let employeeModalTitle;
let employeeNameInput;
let employeePositionInput;

export async function setupManagerDashboard() {
    const user = JSON.parse(localStorage.getItem("currentUser"));
    if (!user || user.role !== "manager") {
        alert("กรุณาเข้าสู่ระบบ!");
        window.location.href = "/";
        return;
    }

    document.getElementById("welcomeText").textContent = `สวัสดี, ${user.fullName}`;
    document.getElementById("userNav").classList.remove("hidden");
    document.getElementById("navButtons").classList.add("hidden");

    document.getElementById('addMenuBtn').addEventListener('click', showAddMenuModal);

    const addEmployeeBtn = document.getElementById('addEmployeeBtn');
    if (addEmployeeBtn) {
        addEmployeeBtn.addEventListener('click', () => openEmployeeDialog());
    }

    document.getElementById("logoutBtn").addEventListener("click", () => {
        localStorage.removeItem("currentUser");
        window.location.href = "/";
    });

    await loadMenuItems();
    await loadEmployeeManagement();
    await updateManagerStats();
    initManagerTabs();
    hideAddMenuModal();
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById('cancelAddMenu').addEventListener('click', hideAddMenuModal);
    document.getElementById('addMenuForm').addEventListener('submit', handleAddMenu);

    employeeModal = document.getElementById('employeeModal');
    employeeForm = document.getElementById('employeeForm');
    employeeModalTitle = document.getElementById('employeeModalTitle');
    employeeNameInput = document.getElementById('employeeName');
    employeePositionInput = document.getElementById('employeePosition');
    const cancelEmployeeBtn = document.getElementById('cancelEmployee');

    if (employeeForm) {
        employeeForm.addEventListener('submit', handleEmployeeSubmit);
    }
    if (cancelEmployeeBtn) {
        cancelEmployeeBtn.addEventListener('click', hideEmployeeModal);
    }

    setupManagerDashboard();
});

async function updateManagerStats() {
    try {
        const resp = await fetch('/api/reports/sales');
        if (!resp.ok) {
            throw new Error('ไม่สามารถโหลดรายงานยอดขาย');
        }
        const report = await resp.json();
        const ordersEl = document.getElementById('todayOrders');
        const revenueEl = document.getElementById('todayRevenue');
        if (ordersEl) {
            ordersEl.textContent = report.orderCount ?? 0;
        }
        if (revenueEl) {
            const revenueValue = typeof report.revenue === 'number' ? report.revenue.toFixed(2) : '0.00';
            revenueEl.textContent = `฿${revenueValue}`;
        }
    } catch (err) {
        console.error(err);
    }
}

async function loadMenuItems() {
    const menuList = document.getElementById("menuManagementList");
    if (!menuList) {
        return;
    }
    menuList.innerHTML = "";
    let menuItems = [];
    try {
        const resp = await fetch('/api/menuItems');
        if (resp.ok) {
            menuItems = await resp.json();
        }
    } catch (e) {
        alert('โหลดเมนูไม่สำเร็จ');
    }
    menuItems.forEach(item => {
        const div = document.createElement("div");
        div.className = "flex justify-between bg-gray-100 p-4 rounded-lg";
        div.innerHTML = `
            <span>${item.name} - ฿${item.price}</span>
            <div class="flex space-x-2">
                <select onchange="updateMenuStatus(${item.id}, this.value)" 
                        class="text-sm border rounded px-2 py-1">
                    <option value="available" ${item.status === 'available' ? 'selected' : ''}>
                        พร้อมขาย
                    </option>
                    <option value="unavailable" ${item.status === 'unavailable' ? 'selected' : ''}>
                        ไม่พร้อมขาย
                    </option>
                </select>
                <button class="bg-yellow-500 text-white px-3 py-1 rounded edit-menu" 
                    data-id="${item.id}">แก้ไข</button>
                <button class="bg-red-500 text-white px-3 py-1 rounded delete-menu" 
                    data-id="${item.id}">ลบ</button>
            </div>
        `;
        menuList.appendChild(div);
        console.log(div);
    });

    document.querySelectorAll(".delete-menu").forEach(btn => {
        btn.addEventListener("click", async () => {
            const id = parseInt(btn.dataset.id, 10);
            const resp = await fetch(`/api/menuItems/${id}`, { method: 'DELETE' });
            if (resp.ok || resp.status === 204) {
                await loadMenuItems();
            } else {
                alert('ลบเมนูไม่สำเร็จ');
            }
        });
    });

    document.querySelectorAll(".edit-menu").forEach(btn => {
    btn.addEventListener("click", async () => {
        const id = parseInt(btn.dataset.id, 10);
        const item = menuItems.find(m => m.id === id);

        if (!item) return;

        // ตัวอย่างง่าย ๆ: ใช้ prompt ให้แก้ชื่อและราคา
        const newName = prompt("ชื่อเมนูใหม่:", item.name);
        const newPrice = prompt("ราคามีใหม่:", item.price);

        if (newName && newPrice) {
            const resp = await fetch(`/api/menuItems/${id}`, {
                method: 'PUT',
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ 
                    name: newName, 
                    price: parseInt(newPrice, 10), 
                    status: item.status 
                })
            });

            if (resp.ok) {
                await loadMenuItems();
                alert("แก้ไขเมนูสำเร็จ!");
            } else {
                alert("แก้ไขเมนูไม่สำเร็จ");
            }
        }
    });
});

}

async function updateMenuStatus(id, newStatus) {
    try {
        const resp = await fetch(`/api/menuItems/${id}/status`, {
            method: 'PUT',
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ status: newStatus })
        });

        if (resp.ok) {
            await loadMenuItems(); // รีโหลด list หลังอัปเดตสำเร็จ
        } else {
            alert('อัปเดตสถานะไม่สำเร็จ');
        }
    } catch (err) {
        console.error(err);
        alert('เกิดข้อผิดพลาด');
    }
}


function showAddMenuModal() {
    document.getElementById('addMenuModal').classList.remove('hidden');
}

function hideAddMenuModal() {
    document.getElementById('addMenuModal').classList.add('hidden');
}

async function handleAddMenu(event) {
    event.preventDefault();
    const menuData = {
        name: document.getElementById('menuName').value,
        price: parseFloat(document.getElementById('menuPrice').value),
        category: document.getElementById('menuCategory').value,
        description: document.getElementById('menuDescription').value
    };

    const resp = await fetch('/api/menuItems', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(menuData)
    });
    if (resp.ok) {
        alert('เพิ่มเมนูใหม่สำเร็จ!');
        await loadMenuItems();
        hideAddMenuModal();
        event.target.reset();
    } else {
        alert('เกิดข้อผิดพลาดในการเพิ่มเมนู');
    }
}

function initManagerTabs() {
    document.querySelectorAll('.manager-tab').forEach(tab => {
        tab.addEventListener('click', event => {
            const tabName = tab.dataset.tab;
            switchManagerTab(event, tabName);
        });
    });
}

function switchManagerTab(event, tabName) {
    document.querySelectorAll('.manager-tab').forEach(tab => {
        tab.classList.remove('border-orange-500', 'text-orange-600');
        tab.classList.add('text-gray-500');
    });
    event.currentTarget.classList.add('border-orange-500', 'text-orange-600');
    event.currentTarget.classList.remove('text-gray-500');

    document.querySelectorAll('.manager-content').forEach(content => {
        content.classList.add('hidden');
    });

    const target = document.getElementById(`${tabName}Management`);
    if (target) {
        target.classList.remove('hidden');
    }
}

async function loadEmployeeManagement() {
    const employeeList = document.getElementById('employeeList');
    if (!employeeList) {
        return;
    }
    employeeList.innerHTML = '';

    let employees = [];
    try {
        const resp = await fetch('/api/employees');
        if (resp.ok) {
            employees = await resp.json();
        }
    } catch (err) {
        console.error('โหลดข้อมูลพนักงานไม่สำเร็จ', err);
    }

    if (employees.length === 0) {
        employeeList.innerHTML = '<p class="text-gray-500">ยังไม่มีพนักงาน</p>';
        return;
    }

    employees.forEach(employee => {
        const employeeItem = document.createElement('div');
        employeeItem.className = 'flex items-center justify-between p-4 bg-gray-50 rounded-lg';
        employeeItem.innerHTML = `
            <div>
                <h5 class="font-semibold">${employee.name}</h5>
                <p class="text-sm text-gray-600">${employee.position || ''}</p>
            </div>
            <div class="flex items-center space-x-2">
                <button class="text-sm text-blue-600 hover:text-blue-800 edit-employee">แก้ไข</button>
                <button class="text-sm text-red-600 hover:text-red-800 delete-employee">ลบ</button>
            </div>
        `;

        employeeItem.querySelector('.edit-employee').addEventListener('click', () => {
            openEmployeeDialog(employee);
        });

        employeeItem.querySelector('.delete-employee').addEventListener('click', async () => {
            const confirmDelete = confirm(`ต้องการลบ ${employee.name} หรือไม่?`);
            if (!confirmDelete) {
                return;
            }
            const resp = await fetch(`/api/employees/${employee.id}`, {
                method: 'DELETE'
            });
            if (resp.ok || resp.status === 204) {
                await loadEmployeeManagement();
            } else {
                alert('ลบพนักงานไม่สำเร็จ');
            }
        });

        employeeList.appendChild(employeeItem);
    });
}

function openEmployeeDialog(employee) {
    if (!employeeModal || !employeeForm || !employeeModalTitle || !employeeNameInput || !employeePositionInput) {
        return;
    }

    if (employee) {
        employeeForm.dataset.mode = 'edit';
        employeeForm.dataset.id = String(employee.id);
        employeeModalTitle.textContent = 'แก้ไขพนักงาน';
        employeeNameInput.value = employee.name || '';
        employeePositionInput.value = employee.position || '';
    } else {
        employeeForm.dataset.mode = 'create';
        delete employeeForm.dataset.id;
        employeeModalTitle.textContent = 'เพิ่มพนักงาน';
        employeeForm.reset();
    }

    showEmployeeModal();
}

function showEmployeeModal() {
    if (employeeModal) {
        employeeModal.classList.remove('hidden');
    }
}

function hideEmployeeModal() {
    if (employeeModal) {
        employeeModal.classList.add('hidden');
    }
    if (employeeForm) {
        employeeForm.reset();
        delete employeeForm.dataset.mode;
        delete employeeForm.dataset.id;
    }
}

async function handleEmployeeSubmit(event) {
    event.preventDefault();
    if (!employeeForm) {
        return;
    }

    const payload = {
        name: employeeNameInput.value.trim(),
        position: employeePositionInput.value.trim()
    };

    if (!payload.name) {
        alert('กรุณาระบุชื่อพนักงาน');
        return;
    }

    const mode = employeeForm.dataset.mode;
    try {
        let resp;
        if (mode === 'edit' && employeeForm.dataset.id) {
            resp = await fetch(`/api/employees/${employeeForm.dataset.id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        } else {
            resp = await fetch('/api/employees', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        }

        if (!resp.ok) {
            throw new Error('Request failed');
        }

        await loadEmployeeManagement();
        hideEmployeeModal();
    } catch (err) {
        console.error(err);
        alert('บันทึกข้อมูลพนักงานไม่สำเร็จ');
    }
}

export async function fetchCartForUser(userId) {
    const resp = await fetch(`/api/carts/${userId}`);
    if (!resp.ok) {
        throw new Error('ไม่พบตะกร้าของผู้ใช้');
    }
    return resp.json();
}

window.fetchCartForUser = fetchCartForUser;