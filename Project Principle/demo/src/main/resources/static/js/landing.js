// landing.js

export function openAuthModal() {
    document.getElementById("authModal").classList.remove("hidden");
}

export function closeAuthModal() {
    document.getElementById("authModal").classList.add("hidden");
}

export function setupLanding() {
    const loginBtn = document.getElementById("loginBtn");
    const closeAuth = document.getElementById("closeAuth");
    const loginTab = document.getElementById("loginTab");
    const registerTab = document.getElementById("registerTab");
    const registerFields = document.getElementById("registerFields");
    const roleSelect = document.getElementById("userRole"); // <-- ตรงนี้แก้
    const authButtonText = document.getElementById("authButtonText");
    const authForm = document.getElementById("authForm");

    // เปิด/ปิด modal
    loginBtn.addEventListener("click", openAuthModal);
    closeAuth.addEventListener("click", closeAuthModal);

    // Tab Login
    loginTab.addEventListener("click", () => {
        registerFields.classList.add("hidden");
        roleSelect.classList.add("hidden");
        loginTab.classList.add("bg-orange-500", "text-white");
        loginTab.classList.remove("text-gray-600");
        registerTab.classList.remove("bg-orange-500", "text-white");
        registerTab.classList.add("text-gray-600");
        authButtonText.textContent = "เข้าสู่ระบบ";
    });

    // Tab Register
    registerTab.addEventListener("click", () => {
        registerFields.classList.remove("hidden");
        roleSelect.classList.remove("hidden");
        registerTab.classList.add("bg-orange-500", "text-white");
        registerTab.classList.remove("text-gray-600");
        loginTab.classList.remove("bg-orange-500", "text-white");
        loginTab.classList.add("text-gray-600");
        authButtonText.textContent = "สมัครสมาชิก";
    });

    // Submit form
    authForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const username = document.getElementById("username").value.trim();
        const password = document.getElementById("password").value.trim();

        if (!registerFields.classList.contains("hidden")) {
            // Register - Call API endpoint
            const fullName = document.getElementById("fullName").value.trim();
            const email = document.getElementById("email")?.value.trim() || "";
            const phone = document.getElementById("phone")?.value.trim() || "";
            
            registerCustomer(username, password, fullName, email, phone);
        } else {
            // Login - Call API endpoint
            loginCustomer(username, password);
        }
    });
}

// ======== API Login Function ========
async function loginCustomer(username, password) {
    try {
        const response = await fetch("/api/customers/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include", // Include cookies/session in request
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        if (!response.ok) {
            if (response.status === 401) {
                alert("ผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
            } else {
                alert("เกิดข้อผิดพลาดในการเข้าสู่ระบบ");
            }
            return;
        }

        const authResponse = await response.json();
        
        console.log("Login response:", authResponse);
        
        // Store session data in localStorage including customer ID, username, and token
        const sessionData = {
            token: authResponse.token,
            customerId: authResponse.customerId,
            username: authResponse.username,
            email: authResponse.email,
            loginTime: authResponse.loginTime
        };
        
        console.log("Storing session data:", sessionData);
        
        // Set localStorage synchronously
        localStorage.setItem("currentUser", JSON.stringify(sessionData));
        
        // Verify it was stored
        const storedData = localStorage.getItem("currentUser");
        console.log("Verified stored data:", storedData);
        
        console.log("About to redirect to:", `/customer/${authResponse.customerId}`);
        
        // Small delay to ensure localStorage is persisted before redirect
        setTimeout(() => {
            window.location.href = `/customer/${authResponse.customerId}`;
        }, 100);
        
    } catch (error) {
        console.error("Login error:", error);
        alert("เกิดข้อผิดพลาดในการเข้าสู่ระบบ");
    }
}

// ======== API Register Function ========
async function registerCustomer(username, password, fullName, email, phone) {
    try {
        const response = await fetch("/api/customers/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include", // Include cookies/session in request
            body: JSON.stringify({
                username: username,
                password: password,
                name: fullName,
                email: email,
                phone: phone
            })
        });

        if (!response.ok) {
            if (response.status === 409) {
                alert("มีชื่อผู้ใช้นี้อยู่แล้ว!");
            } else {
                const errorText = await response.text();
                alert("เกิดข้อผิดพลาดในการสมัครสมาชิก: " + errorText);
            }
            return;
        }

        const authResponse = await response.json();
        
        // Store session data in localStorage
        const sessionData = {
            token: authResponse.token,
            customerId: authResponse.customerId,
            username: authResponse.username,
            email: authResponse.email,
            loginTime: authResponse.loginTime
        };
        
        // Set localStorage synchronously
        localStorage.setItem("currentUser", JSON.stringify(sessionData));
        
        alert("สมัครสมาชิกเรียบร้อย!");
        closeAuthModal();
        
        // Small delay to ensure localStorage is persisted before redirect
        setTimeout(() => {
            window.location.href = `/customer/${authResponse.customerId}`;
        }, 100);
        
    } catch (error) {
        console.error("Registration error:", error);
        alert("เกิดข้อผิดพลาดในการสมัครสมาชิก");
    }
}
