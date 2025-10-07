// customer-orders.js
// Handles displaying customer orders and status updates

const ORDER_POLL_INTERVAL_MS = 8000;
let orderPollTimerId = null;
let lastRenderedOrderSignature = null;
let pollCleanupRegistered = false;

document.addEventListener("DOMContentLoaded", () => {
    setupCustomerOrdersPage();
});

async function setupCustomerOrdersPage() {
    // Read customer ID from DOM (set by Thymeleaf server-side rendering)
    const customerIdElement = document.getElementById("customerId");
    const customerId = customerIdElement ? parseInt(customerIdElement.value) : null;

    // If customer ID is not present in DOM, server didn't render it (no valid session)
    if (!customerId || isNaN(customerId)) {
        console.warn("No customer ID found in DOM - server session validation failed");
        showNotification("ไม่พบข้อมูลผู้ใช้ กรุณาเข้าสู่ระบบอีกครั้ง", "error");

        const loadingIndicator = document.getElementById("loadingIndicator");
        const emptyOrders = document.getElementById("emptyOrders");
        if (loadingIndicator) {
            loadingIndicator.classList.add("hidden");
        }
        if (emptyOrders) {
            emptyOrders.classList.remove("hidden");
        }
        return;
    }

    // Logout button
    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", handleLogout);
    }

    // Load pending orders
    await loadCustomerOrders(customerId);
    startOrderPolling(customerId);
}

function startOrderPolling(customerId) {
    if (orderPollTimerId) {
        clearInterval(orderPollTimerId);
    }

    orderPollTimerId = setInterval(() => {
        loadCustomerOrders(customerId, { skipSpinner: true });
    }, ORDER_POLL_INTERVAL_MS);

    if (!pollCleanupRegistered) {
        pollCleanupRegistered = true;
        window.addEventListener("beforeunload", () => {
            if (orderPollTimerId) {
                clearInterval(orderPollTimerId);
            }
        });
    }
}

// ======== Load Customer Orders ========
async function loadCustomerOrders(customerId, options = {}) {
    const { skipSpinner = false } = options;

    const loadingIndicator = document.getElementById("loadingIndicator");
    const emptyOrders = document.getElementById("emptyOrders");
    const ordersContainer = document.getElementById("ordersContainer");

    try {
        if (!skipSpinner && loadingIndicator) {
            loadingIndicator.classList.remove("hidden");
        }
        if (emptyOrders) {
            emptyOrders.classList.add("hidden");
        }
        if (ordersContainer) {
            ordersContainer.classList.add("hidden");
        }

        const response = await fetch(`/api/orders/customers/${customerId}/pending-orders`, {
            cache: "no-store",
            credentials: "same-origin"
        });
        
        if (!response.ok) {
            throw new Error(`Failed to fetch orders: ${response.status} ${response.statusText}`);
        }

        const orderData = await response.json();
        
        if (loadingIndicator) {
            loadingIndicator.classList.add("hidden");
        }

        // Backend returns a single OrderResponseDto object, not an array
        // Convert to array format expected by displayOrders function
        const orders = (orderData && orderData.items && orderData.items.length > 0) ? [orderData] : [];

        if (orders.length === 0) {
            if (emptyOrders) {
                const emptyText = emptyOrders.querySelector("p");
                if (emptyText) {
                    emptyText.textContent = "ยังไม่มีคำสั่งซื้อในระบบ";
                }
                emptyOrders.classList.remove("hidden");
            }
            lastRenderedOrderSignature = null;
            return;
        }

        // Display orders
        displayOrders(orders);
        if (ordersContainer) {
            ordersContainer.classList.remove("hidden");
        }

    } catch (error) {
        console.error("Error loading customer orders:", error);
        if (loadingIndicator) {
            loadingIndicator.classList.add("hidden");
        }
        showNotification("เกิดข้อผิดพลาดในการโหลดคำสั่งซื้อ", "error");
        if (emptyOrders) {
            emptyOrders.classList.remove("hidden");
        }
    }
}

// ======== Display Orders ========
function displayOrders(orders) {
    const ordersContainer = document.getElementById("ordersContainer");
    if (!ordersContainer) {
        return;
    }

    const snapshotSignature = JSON.stringify(orders);
    if (snapshotSignature === lastRenderedOrderSignature) {
        return;
    }
    lastRenderedOrderSignature = snapshotSignature;

    ordersContainer.innerHTML = "";

    orders.forEach(order => {
        const orderCard = createOrderCard(order);
        ordersContainer.appendChild(orderCard);
    });
}

// ======== Create Order Card ========
function createOrderCard(order) {
    const card = document.createElement("div");
    card.className = "bg-white rounded-lg shadow-lg p-6";

    const orderDate = formatDate(order.createdAt);
    const updatedDate = formatDate(order.updatedAt);
    const orderTimeSeed = (() => {
        const updated = parseDate(order.updatedAt);
        return updated ? updated.getTime() : Date.now();
    })();

    // Get status badge
    const statusBadge = getStatusBadge(order.status);

    // Create items list HTML
    let itemsHTML = "";
    if (order.items && order.items.length > 0) {
        itemsHTML = order.items.map(item => `
            <div class="flex justify-between items-center py-2 border-b border-gray-100">
                <div class="flex-1">
                    <span class="font-medium">${item.itemName}</span>
                    <span class="text-gray-500 ml-2">x${item.quantity}</span>
                </div>
                <div class="text-right">
                    <div class="text-sm text-gray-500">฿${formatCurrency(item.itemPrice)} × ${item.quantity}</div>
                    <div class="font-semibold text-orange-600">฿${formatCurrency(item.subtotal ?? item.itemPrice * item.quantity)}</div>
                </div>
            </div>
        `).join("");
    } else {
        itemsHTML = '<p class="text-gray-500 py-2">ไม่มีรายการสินค้า</p>';
    }

    card.innerHTML = `
        <div class="flex justify-between items-start mb-4">
            <div>
                <h3 class="text-xl font-bold text-gray-800">คำสั่งซื้อ #${order.customerId}-${orderTimeSeed}</h3>
                <p class="text-sm text-gray-500 mt-1">${orderDate}</p>
                <p class="text-xs text-gray-400">อัปเดตล่าสุด: ${updatedDate}</p>
            </div>
            ${statusBadge}
        </div>

        <div class="mb-4">
            <h4 class="font-semibold text-gray-700 mb-2">รายการสินค้า:</h4>
            <div class="space-y-1">
                ${itemsHTML}
            </div>
        </div>

        <div class="flex justify-between items-center pt-4 border-t-2 border-gray-200">
            <span class="text-lg font-bold text-gray-800">รวมทั้งหมด:</span>
            <span class="text-2xl font-bold text-orange-600">฿${formatCurrency(order.totalPrice)}</span>
        </div>
    `;

    return card;
}

function parseDate(isoDateString) {
    if (!isoDateString) {
        return null;
    }
    const parsed = new Date(isoDateString);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
}

function formatDate(isoDateString) {
    const parsed = parseDate(isoDateString);
    if (!parsed) {
        return "ไม่ระบุวันที่";
    }

    return parsed.toLocaleString('th-TH', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatCurrency(value) {
    const numericValue = Number(value ?? 0);
    return numericValue.toLocaleString('th-TH', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

// ======== Get Status Badge ========
function getStatusBadge(status) {
    const statusConfig = {
        "Pending": {
            color: "bg-yellow-100 text-yellow-800 border-yellow-300",
            icon: "⏳",
            text: "รอดำเนินการ"
        },
        "In Progress": {
            color: "bg-blue-100 text-blue-800 border-blue-300",
            icon: "🔄",
            text: "กำลังดำเนินการ"
        },
        "Finish": {
            color: "bg-green-100 text-green-800 border-green-300",
            icon: "✅",
            text: "เสร็จสิ้น"
        },
        "Cancelled": {
            color: "bg-red-100 text-red-800 border-red-300",
            icon: "❌",
            text: "ยกเลิก"
        }
    };

    const config = statusConfig[status] || statusConfig["Pending"];
    
    return `
        <div class="flex items-center space-x-2 px-3 py-1 border-2 rounded-full ${config.color}">
            <span>${config.icon}</span>
            <span class="font-semibold text-sm">${config.text}</span>
        </div>
    `;
}

// ======== Logout Handler ========
async function handleLogout() {
    try {
        // Get CSRF token from meta tags
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        const formData = new FormData();

        // Send POST request to logout endpoint with CSRF token
        const response = await fetch('/logout', {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken
            },
            body: formData
        });

        // Clear localStorage
        localStorage.removeItem("currentUser");

        // Redirect to index page
        window.location.href = "/";
    } catch (error) {
        console.error("Logout error:", error);
        localStorage.removeItem("currentUser");
        window.location.href = "/";
    }
}

// ======== Notification ========
function showNotification(message, type = "success") {
    const notifications = document.getElementById("notifications");
    const div = document.createElement("div");
    div.textContent = message;
    
    const bgColor = type === "error" ? "bg-red-500" : "bg-orange-500";
    div.className = `${bgColor} text-white p-3 rounded shadow-lg mb-2`;
    
    notifications.appendChild(div);
    setTimeout(() => div.remove(), 3000);
}
