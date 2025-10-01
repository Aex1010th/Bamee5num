# Task List: Customer-Specific Pages with Session Management

## Relevant Files

- `Project Principle/demo/src/main/java/com/restaurant/demo/controller/PageController.java` - Main controller for page navigation; needs new endpoint for `/customer/{customerId}` with session validation
- `Project Principle/demo/src/test/java/com/restaurant/demo/controller/PageControllerTest.java` - Unit tests for PageController (to be created)
- `Project Principle/demo/src/main/java/com/restaurant/demo/service/CustomerService.java` - Customer business logic; already has `findCustomerById()` method that can be leveraged
- `Project Principle/demo/src/test/java/com/restaurant/demo/service/CustomerServiceTest.java` - Unit tests for CustomerService (already exists)
- `Project Principle/demo/src/main/resources/templates/customer.html` - Customer page template; needs Thymeleaf variables for dynamic customer name display
- `Project Principle/demo/src/main/resources/static/js/customer.js` - Customer page JavaScript; needs to extract customer ID from URL path
- `Project Principle/demo/src/main/resources/static/js/landing.js` - Login/register JavaScript; needs to redirect to `/customer/{customerId}` after successful login
- `Project Principle/demo/src/test/java/com/restaurant/demo/integration/MultiSessionIntegrationTest.java` - Integration tests for multi-session scenarios (to be created)
- `Project Principle/demo/src/test/resources/application-test.properties` - Test configuration (already exists)
- `Project Principle/demo/src/test/resources/test-data.sql` - Test fixtures for H2 database with multiple customers (to be created)

### Notes

- The project uses Spring Boot 3.5.5 with Thymeleaf for server-side rendering
- Session management will use `HttpSession` for server-side validation combined with client-side `localStorage` for token storage
- Tests should use H2 in-memory database with pre-populated test fixtures
- Existing `CustomerService` already has the `findCustomerById()` method needed for fetching customer data
- Current login flow in `landing.js` redirects to `/customer-page` (static endpoint) - this needs to change to `/customer/{customerId}`
- Cart functionality already exists and is tied to customer ID, so it should continue working with the new URL pattern

## Tasks

- [x] 1.0 Implement Backend Session Management and Customer Page Endpoint
  - [x] 1.1 Add new `@GetMapping("/customer/{customerId}")` endpoint in `PageController.java`
  - [x] 1.2 Implement method signature: `public String customerPage(@PathVariable Long customerId, Model model, HttpSession session)`
  - [x] 1.3 Store customer ID in `HttpSession` upon successful login (modify existing login POST handler if needed)
  - [x] 1.4 Fetch customer data using `customerService.findCustomerById(customerId)`
  - [x] 1.5 Add customer object to model: `model.addAttribute("customer", customer)`
  - [x] 1.6 Handle case when customer is not found (return appropriate error view or redirect)
  - [x] 1.7 Return "customer" view name for Thymeleaf rendering

- [x] 2.0 Update Frontend for Dynamic Customer ID Routing
  - [x] 2.1 Modify `landing.js` login success handler to redirect to `/customer/{customerId}` instead of `/customer-page`
  - [x] 2.2 Update login handler to extract customer ID from API response (`AuthResponseDto.customerId`)
  - [x] 2.3 Store session data in `localStorage` including customer ID, username, and token
  - [x] 2.4 Modify `customer.js` to extract customer ID from URL path using `window.location.pathname.split('/')`
  - [x] 2.5 Remove hardcoded customer ID references in `customer.js`
  - [x] 2.6 Update cart API calls to use dynamic customer ID extracted from URL
  - [x] 2.7 Update profile data fetching to use dynamic customer ID

- [x] 3.0 Modify Customer Page Template for Personalized Display
  - [x] 3.1 Replace hardcoded welcome text in `customer.html` with Thymeleaf expression: `th:text="'สวัสดี, ' + ${customer.name}"`
  - [x] 3.2 Add hidden input or data attribute for customer ID: `<input type="hidden" id="customerId" th:value="${customer.id}">`
  - [x] 3.3 Add null check for customer object in template (use `th:if="${customer != null}"`)
  - [x] 3.4 Add error message display when customer is null or invalid
  - [x] 3.5 Test template rendering with both valid and invalid customer data

- [x] 4.0 Implement Session Validation and Security
  - [x] 4.1 Add session validation logic at the start of `customerPage()` method
  - [x] 4.2 Retrieve customer ID from session: `Long sessionCustomerId = (Long) session.getAttribute("customerId")`
  - [x] 4.3 Compare session customer ID with URL path variable customer ID
  - [x] 4.4 If session is null or IDs don't match, redirect to `/login?error=unauthorized`
  - [x] 4.5 Add logout endpoint that clears session and redirects to index
  - [x] 4.6 Update logout button in `customer.js` to call logout endpoint and clear `localStorage`
  - [x] 4.7 Add CSRF protection considerations for logout action
  - [x] 4.8 Test unauthorized access attempts (manual URL manipulation)


- [ ] 5.0 Create Comprehensive Test Suite
  - [ ] 5.1 Create `PageControllerTest.java` unit test class with `@WebMvcTest(PageController.class)` annotation
  - [ ] 5.2 Write test: `testCustomerPage_WithValidSessionAndCustomerId_ReturnsCustomerView()`
  - [ ] 5.3 Write test: `testCustomerPage_WithInvalidCustomerId_RedirectsToLogin()`
  - [ ] 5.4 Write test: `testCustomerPage_WithMismatchedSessionCustomerId_RedirectsToLogin()`
  - [ ] 5.5 Write test: `testCustomerPage_WithNoSession_RedirectsToLogin()`
  - [ ] 5.6 Write test: `testCustomerPage_AddsCustomerToModel()`
  - [ ] 5.7 Create `test-data.sql` with 3+ test customers for H2 database
  - [ ] 5.8 Update existing `CustomerServiceTest.java` if needed for new functionality
  - [ ] 5.9 Verify all tests pass with `mvn test`

- [ ] 6.0 Integration Testing and Bug Fixes
  - [ ] 6.1 Create `MultiSessionIntegrationTest.java` for multi-session scenarios
  - [ ] 6.2 Write integration test: `testMultipleCustomerSessions_ShowDifferentNames()`
  - [ ] 6.3 Write integration test: `testUnauthorizedCustomerAccess_RedirectsToLogin()`
  - [ ] 6.4 Write integration test: `testSameCustomerMultipleTabs_SharedCart()`
  - [ ] 6.5 Write integration test: `testLogoutFlow_ClearsSessionAndRedirects()`
  - [ ] 6.6 Perform manual testing: Single user login with correct name display
  - [ ] 6.7 Perform manual testing: Multi-tab different users with isolated sessions
  - [ ] 6.8 Perform manual testing: Security - URL manipulation attempt blocked
  - [ ] 6.9 Perform manual testing: Logout flow clears session and redirects
  - [ ] 6.10 Fix any bugs discovered during testing
  - [ ] 6.11 Verify no hardcoded customer references remain in codebase (grep search)
  - [ ] 6.12 Run full test suite and ensure all tests pass
