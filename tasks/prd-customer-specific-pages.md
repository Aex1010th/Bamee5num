# Product Requirements Document: Customer-Specific Pages with Session Management

## 1. Introduction/Overview

### Problem Statement
Currently, the Bamee 5 Num restaurant application displays a hardcoded customer name on the customer page, regardless of which user is logged in. When multiple customers log in simultaneously using different browser tabs or windows, they all see the same hardcoded name instead of their actual personalized information. This creates a poor user experience and makes it impossible to properly test or use multi-user scenarios.

### Goal
Implement customer-specific pages where each logged-in customer sees their own name and personal information. The system must support multiple concurrent sessions, allowing different customers to be logged in simultaneously across different browser tabs/windows, each seeing their own personalized content.

## 2. Goals

1. **Display personalized customer name** on the customer page based on the logged-in user's session
2. **Support concurrent multi-user sessions** where different customers can be logged in simultaneously in different tabs/windows
3. **Implement URL-based customer identification** using `/customer/{customerId}` pattern
4. **Ensure session isolation** so that Customer A in Tab 1 and Customer B in Tab 2 see different information
5. **Maintain backward compatibility** with existing cart functionality and authentication flow
6. **Prevent unauthorized access** to other customers' pages
7. **Create comprehensive tests** for multi-session scenarios using H2 test database

## 3. User Stories

### US-1: Display Customer Name After Login
**As a** customer  
**I want to** see my own name displayed on the customer page after logging in  
**So that** I know the system recognizes me and I'm viewing my personal account

**Acceptance Criteria:**
- When I log in as "John Doe", I see "Welcome, John Doe" (or similar) on the customer page
- The displayed name matches my customer record in the database
- No hardcoded names are displayed

### US-2: Multi-Tab Session Independence
**As a** customer  
**I want to** log in with my account in one browser tab while another customer logs in with their account in a different tab  
**So that** we can both use the system simultaneously without interfering with each other

**Acceptance Criteria:**
- Customer A logs in on Tab 1, sees their name
- Customer B logs in on Tab 2, sees their name (different from Customer A)
- Tab 1 still shows Customer A's name without changing
- Both customers can use features independently

### US-3: Direct Customer Page Access
**As a** customer  
**I want to** access my customer page via a unique URL like `/customer/123`  
**So that** I can bookmark or directly navigate to my account page

**Acceptance Criteria:**
- After login, I am redirected to `/customer/{my-customer-id}`
- The URL clearly shows my customer ID
- I can bookmark this URL for future use

### US-4: Shared Cart Across Sessions
**As a** customer  
**I want to** see the same cart contents when I log in from different tabs or devices  
**So that** my shopping cart is consistent across all my sessions

**Acceptance Criteria:**
- When I add items to cart in Tab 1, they appear in Tab 2 when I refresh
- Cart is tied to my customer ID, not the browser session
- Cart persists across login/logout cycles

### US-5: Security - Prevent Unauthorized Access
**As a** customer  
**I want to** be prevented from accessing other customers' pages  
**So that** my personal information remains private and secure

**Acceptance Criteria:**
- If I try to access `/customer/999` (not my ID), I get redirected or see an error
- The system validates that my session matches the requested customer ID
- I can only view and modify my own data

## 4. Functional Requirements

### FR-1: Session Token Enhancement
- **FR-1.1**: Upon successful login, generate a unique session token containing customer ID
- **FR-1.2**: Store session token in browser's `localStorage` or `sessionStorage`
- **FR-1.3**: Session token must include: `token`, `customerId`, `username`, `timestamp`
- **FR-1.4**: Token format should be: `session-{customerId}-{randomString}`

### FR-2: Customer Page Routing
- **FR-2.1**: Implement controller endpoint `GET /customer/{customerId}` 
- **FR-2.2**: After successful login, redirect user to `/customer/{customerId}`
- **FR-2.3**: Page must accept customer ID as path variable
- **FR-2.4**: Retrieve customer data from database using the path variable customer ID

### FR-3: Customer Data Display
- **FR-3.1**: Display customer's name on the customer page header
- **FR-3.2**: Replace hardcoded "สวัสดี, [ชื่อ]" with dynamic customer name from database
- **FR-3.3**: If customer record not found, display appropriate error message
- **FR-3.4**: Name should be fetched from `Customer.name` field in database

### FR-4: Session Validation
- **FR-4.1**: Before loading customer page, validate that session token matches requested customer ID
- **FR-4.2**: If customer ID in URL doesn't match session token, redirect to login page
- **FR-4.3**: If no valid session exists, redirect to login page (`/login`)
- **FR-4.4**: Validation must occur on server-side (backend controller)

### FR-5: Login Flow Modification
- **FR-5.1**: Keep existing Thymeleaf login form at `/login`
- **FR-5.2**: On successful authentication, create session with customer details
- **FR-5.3**: Redirect authenticated user to `/customer/{customerId}` instead of generic `/customer-page`
- **FR-5.4**: Store customer ID in session/token for subsequent validation

### FR-6: Logout Functionality
- **FR-6.1**: Implement logout button on customer page
- **FR-6.2**: Clear browser's `localStorage` session data
- **FR-6.3**: Redirect to `/` (index.html) after logout
- **FR-6.4**: Display logout confirmation (optional)

### FR-7: Multi-Session Support
- **FR-7.1**: Multiple customers can be logged in simultaneously across different tabs/windows
- **FR-7.2**: Each tab maintains its own session token in `localStorage`
- **FR-7.3**: Sessions are isolated - changes in one tab don't affect other tabs' sessions
- **FR-7.4**: Same customer logged in multiple tabs shares cart data (server-side association)

### FR-8: Cart Integration
- **FR-8.1**: Cart remains associated with customer ID (existing functionality)
- **FR-8.2**: Cart data is loaded based on customer ID from URL path variable
- **FR-8.3**: Cart operations use customer ID from session validation
- **FR-8.4**: Cart API endpoints validate customer ID matches session

### FR-9: Backend Controller Changes
- **FR-9.1**: Update `PageController` to add `@GetMapping("/customer/{customerId}")`
- **FR-9.2**: Controller method signature: `public String customerPage(@PathVariable Long customerId, Model model, HttpSession session)`
- **FR-9.3**: Fetch customer from database using `customerService.findCustomerById(customerId)`
- **FR-9.4**: Add customer data to model: `model.addAttribute("customer", customer)`
- **FR-9.5**: Return `customer.html` view

### FR-10: Frontend JavaScript Changes
- **FR-10.1**: Update `landing.js` to redirect to `/customer/{customerId}` after successful login
- **FR-10.2**: Modify `customer.js` to read customer ID from URL instead of hardcoded value
- **FR-10.3**: Extract customer ID from `window.location.pathname`
- **FR-10.4**: Use customer ID to fetch cart and profile data

### FR-11: Template Changes
- **FR-11.1**: Update `customer.html` to use Thymeleaf variable for customer name
- **FR-11.2**: Replace hardcoded welcome text with: `<span th:text="${customer.name}"></span>`
- **FR-11.3**: Add hidden input or data attribute with customer ID for JavaScript access
- **FR-11.4**: Ensure template gracefully handles missing customer data

## 5. Non-Goals (Out of Scope)

1. **Spring Security Integration** - Will use enhanced client-side tokens with server validation, not full Spring Security framework
2. **JWT Implementation** - Not implementing stateless JWT tokens in this phase
3. **Redis Session Store** - Sessions will be managed via browser storage and server-side validation
4. **Password Reset Functionality** - Authentication improvements only, not password management
5. **Role-Based Access Control (RBAC)** - Focus is on customer pages only, not employee/manager roles
6. **Session Timeout** - No automatic session expiration in this phase
7. **Remember Me Functionality** - Simple session management only
8. **OAuth/Social Login** - Username/password authentication only
9. **Account Settings Page** - Display name only, not full profile editing
10. **Audit Logging** - No tracking of login/logout events

## 6. Design Considerations

### UI/UX Requirements

1. **Customer Page Header**
   ```
   สวัสดี, [Customer Name]     🛒 ตะกร้า (0)  [Logout]
   ```
   - Dynamic customer name from database
   - Existing cart and logout buttons remain

2. **URL Display**
   - User sees: `http://localhost:8080/customer/1` in address bar
   - Provides visual confirmation of their customer ID

3. **Error Handling**
   - If customer not found: Show error page or redirect to login with message
   - If unauthorized access: Redirect to login with "Unauthorized access" message
   - If session expired: Redirect to login with "Session expired" message

### Component Modifications

1. **customer.html** - Add Thymeleaf attributes for dynamic name
2. **PageController.java** - Add new endpoint with path variable
3. **landing.js** - Modify redirect after login
4. **customer.js** - Extract customer ID from URL, remove hardcoded values
5. **login.html** - No changes required (existing form works)

## 7. Technical Considerations

### Technology Stack
- **Framework**: Spring Boot 3.5.5
- **Database**: MySQL (production), H2 (testing)
- **Template Engine**: Thymeleaf
- **Frontend**: Vanilla JavaScript (no frameworks)

### Implementation Approach

1. **Backend (Spring Boot)**
   - Use `@PathVariable` to capture customer ID from URL
   - Use `HttpSession` or custom session validation
   - Query database for customer data using `CustomerService`
   - Pass customer object to Thymeleaf template via `Model`

2. **Frontend (JavaScript)**
   - Parse URL to extract customer ID: `const customerId = window.location.pathname.split('/')[2]`
   - Store session data with customer ID in `localStorage`
   - Update API calls to use dynamic customer ID

3. **Session Management**
   - Client-side: Store session token in `localStorage`
   - Server-side: Validate customer ID from URL matches session/token
   - No database-backed sessions (stateless approach with validation)

### Database Schema
No changes required to existing schema. Will use existing `Customer` entity.

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/customer/{customerId}` | Display customer-specific page |
| POST | `/login` | Authenticate and create session (existing, modified redirect) |
| POST | `/api/customers/login` | API login endpoint (existing) |
| GET | `/api/cart/customer/{customerId}` | Get customer's cart (existing) |

### Security Considerations

1. **Session Validation**: Always validate customer ID in URL matches authenticated session
2. **SQL Injection**: Use parameterized queries (JPA handles this)
3. **XSS Protection**: Thymeleaf escapes variables by default
4. **CSRF**: Keep Spring's CSRF protection enabled
5. **Authorization**: Prevent customers from accessing others' pages via ID manipulation

### Error Handling

```java
@GetMapping("/customer/{customerId}")
public String customerPage(@PathVariable Long customerId, Model model, HttpSession session) {
    // Validate session matches customer ID
    Long sessionCustomerId = (Long) session.getAttribute("customerId");
    if (sessionCustomerId == null || !sessionCustomerId.equals(customerId)) {
        return "redirect:/login?error=unauthorized";
    }
    
    // Fetch customer
    Optional<Customer> customer = customerService.findCustomerById(customerId);
    if (customer.isEmpty()) {
        return "redirect:/login?error=notfound";
    }
    
    model.addAttribute("customer", customer.get());
    return "customer";
}
```

## 8. Success Metrics

1. **Functional Success**
   - ✅ Customer name displays correctly after login (100% of cases)
   - ✅ Different customers in different tabs see different names (100% of cases)
   - ✅ Unauthorized access attempts are blocked (100% rejection rate)
   - ✅ Cart data loads correctly based on customer ID

2. **User Experience**
   - ✅ Login-to-page load time < 2 seconds
   - ✅ No visual glitches or hardcoded names appearing
   - ✅ URL clearly shows customer ID for user awareness

3. **Testing Coverage**
   - ✅ Unit tests for session validation logic
   - ✅ Integration tests for multi-session scenarios
   - ✅ E2E test: Login two customers in different browsers, verify isolation
   - ✅ Test unauthorized access attempt (different customer ID in URL)

4. **Code Quality**
   - ✅ No hardcoded customer references in code
   - ✅ Proper error handling for all edge cases
   - ✅ Clean separation of concerns (controller, service, view)

## 9. Testing Requirements

### Unit Tests

1. **CustomerControllerTest**
   - Test `GET /customer/{customerId}` returns correct customer
   - Test `GET /customer/{customerId}` with invalid ID returns error
   - Test unauthorized access (session customer ID ≠ URL customer ID)

2. **PageControllerTest**
   - Test customer page endpoint with valid session
   - Test redirect to login when session missing
   - Test model contains customer object

### Integration Tests

1. **Multi-Session Test**
   ```java
   @Test
   void testMultipleCustomerSessions() {
       // Login as customer1 in session1
       // Login as customer2 in session2
       // Verify session1 shows customer1 name
       // Verify session2 shows customer2 name
       // Verify sessions are isolated
   }
   ```

2. **Session Validation Test**
   ```java
   @Test
   void testUnauthorizedCustomerAccess() {
       // Login as customer1 (ID=1)
       // Attempt to access /customer/2
       // Verify redirect to login with error
   }
   ```

3. **Cart Isolation Test**
   ```java
   @Test
   void testCartIsolationBetweenCustomers() {
       // Customer1 adds item to cart
       // Customer2 logs in different session
       // Verify customer2 cart is empty
       // Verify customer1 cart still has item
   }
   ```

### Test Data (H2 Database)

Create test fixtures with multiple customers:

```sql
INSERT INTO customers (id, name, username, email, phone, password_hash, created_at, updated_at)
VALUES 
  (1, 'John Doe', 'john_doe', 'john@example.com', '0812345678', '$2a$10$hashedpassword1', NOW(), NOW()),
  (2, 'Jane Smith', 'jane_smith', 'jane@example.com', '0823456789', '$2a$10$hashedpassword2', NOW(), NOW()),
  (3, 'Bob Wilson', 'bob_wilson', 'bob@example.com', '0834567890', '$2a$10$hashedpassword3', NOW(), NOW());
```

### Manual Testing Scenarios

1. **Scenario 1: Single User Login**
   - Open browser, go to `/login`
   - Login as customer1
   - Verify redirect to `/customer/1`
   - Verify header shows "สวัสดี, John Doe"

2. **Scenario 2: Multi-Tab Different Users**
   - Tab 1: Login as customer1 (John Doe)
   - Tab 2: Login as customer2 (Jane Smith)
   - Verify Tab 1 shows "John Doe"
   - Verify Tab 2 shows "Jane Smith"
   - Refresh both tabs, verify names persist

3. **Scenario 3: Security - URL Manipulation**
   - Login as customer1 (redirected to `/customer/1`)
   - Manually change URL to `/customer/2`
   - Verify redirect to login with error message

4. **Scenario 4: Logout Flow**
   - Login as customer1
   - Click logout button
   - Verify redirect to `/` (index.html)
   - Verify localStorage is cleared
   - Attempt to access `/customer/1` directly
   - Verify redirect to login

5. **Scenario 5: Same Customer Multiple Tabs**
   - Tab 1: Login as customer1
   - Tab 2: Login as customer1 (same user)
   - Add item to cart in Tab 1
   - Refresh Tab 2
   - Verify cart item appears in Tab 2 (shared cart)

## 10. Implementation Checklist

### Phase 1: Backend Changes
- [ ] Add `GET /customer/{customerId}` endpoint in `PageController`
- [ ] Implement session validation logic
- [ ] Add customer data to model for Thymeleaf
- [ ] Update login redirect to use customer ID
- [ ] Add error handling for invalid customer IDs

### Phase 2: Frontend Changes
- [ ] Update `customer.html` with Thymeleaf variables
- [ ] Modify `landing.js` redirect logic
- [ ] Update `customer.js` to parse customer ID from URL
- [ ] Remove all hardcoded customer references
- [ ] Update logout to redirect to `/`

### Phase 3: Testing
- [ ] Create H2 test fixtures with 3+ customers
- [ ] Write unit tests for session validation
- [ ] Write integration tests for multi-session scenarios
- [ ] Write test for unauthorized access prevention
- [ ] Write test for cart isolation
- [ ] Perform manual testing of all scenarios

### Phase 4: Code Review & Documentation
- [ ] Review all code changes
- [ ] Update README with new URL pattern
- [ ] Document session management approach
- [ ] Add inline code comments for complex logic

## 11. Open Questions

1. **Session Storage Duration**: Should sessions persist after browser close, or only for current browser session?
   - **Recommendation**: Use `localStorage` for persistence across browser restarts

2. **Multiple Device Login**: If same customer logs in from phone and desktop, should sessions be independent or synchronized?
   - **Recommendation**: Independent sessions but shared cart (current approach)

3. **Session Conflict Notification**: Should we notify users if they're logged in elsewhere?
   - **Recommendation**: No notification needed (out of scope for this phase)

4. **Customer ID Visibility**: Is it okay for customers to see their ID in the URL?
   - **Recommendation**: Yes, provides transparency and bookmarkability

5. **Error Page Design**: Should we create a custom error page or use simple redirects?
   - **Recommendation**: Simple redirect to login with error query parameter

## 12. Dependencies

- **Existing Code**: Builds on top of existing `Customer`, `CustomerService`, `CustomerController`
- **Database**: Requires existing `customers` table (already exists)
- **Frontend Libraries**: No new dependencies required
- **Spring Boot**: No additional Spring dependencies needed

## 13. Timeline Estimate

- **Backend Implementation**: 2-3 hours
- **Frontend Implementation**: 2-3 hours
- **Testing (Unit + Integration)**: 3-4 hours
- **Manual Testing & Bug Fixes**: 1-2 hours
- **Code Review & Documentation**: 1 hour

**Total Estimated Time**: 9-13 hours

---

## Appendix A: Code Snippets

### Backend Example
```java
@Controller
public class PageController {
    
    @Autowired
    private CustomerService customerService;
    
    @GetMapping("/customer/{customerId}")
    public String customerPage(@PathVariable Long customerId, 
                               Model model, 
                               HttpSession session) {
        // Validate session
        Long sessionCustomerId = (Long) session.getAttribute("customerId");
        if (sessionCustomerId == null || !sessionCustomerId.equals(customerId)) {
            return "redirect:/login?error=unauthorized";
        }
        
        // Fetch customer
        Optional<Customer> customerOpt = customerService.findCustomerById(customerId);
        if (customerOpt.isEmpty()) {
            return "redirect:/login?error=notfound";
        }
        
        model.addAttribute("customer", customerOpt.get());
        return "customer";
    }
}
```

### Frontend Example (customer.js)
```javascript
// Extract customer ID from URL
const pathParts = window.location.pathname.split('/');
const customerId = pathParts[pathParts.length - 1];

// Use customer ID to fetch data
async function loadCustomerData() {
    const response = await fetch(`/api/customers/${customerId}`);
    const customer = await response.json();
    document.getElementById("welcomeText").textContent = `สวัสดี, ${customer.name}`;
}
```

### Template Example (customer.html)
```html
<span id="welcomeText" th:text="'สวัสดี, ' + ${customer.name}">สวัสดี, ผู้ใช้</span>
<input type="hidden" id="customerId" th:value="${customer.id}">
```

---

**Document Version**: 1.0  
**Last Updated**: September 30, 2025  
**Author**: GitHub Copilot  
**Status**: Ready for Implementation
