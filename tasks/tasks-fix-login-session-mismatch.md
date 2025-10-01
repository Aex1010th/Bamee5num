# Task List: Fix Login Session and localStorage Mismatch

## Problem Summary

The application has two incompatible login systems:
1. **Spring Security Form Login** (`/login`) - Creates server session but doesn't set localStorage
2. **API Login** (`/api/customers/login`) - Creates server session AND sets localStorage

When users log in via Spring Security form, they are redirected to `/customer/{customerId}`, but the `customer.js` JavaScript expects localStorage data that was never set, causing the "กรุณาเข้าสู่ระบบ!" alert even though the server session is valid.

## Solution Approach

Remove the client-side localStorage dependency and rely solely on server-side session validation with Thymeleaf-rendered data. This approach is more secure and aligns with Spring Security's session management.

## Relevant Files

### Modified Files (Task 1.0 & 2.0 Completed)
- `src/main/resources/templates/customer.html` - ✅ Added hidden input for customerUsername and data attributes on body tag for JavaScript access
- `src/main/resources/static/js/customer.js` - ✅ Removed localStorage dependency for session validation; now reads customer ID from DOM; keeps localStorage clear on logout only

### Pending Files
- `src/main/java/com/restaurant/demo/config/CustomAuthenticationSuccessHandler.java` - Already sets session correctly; may need minor enhancements
- `src/main/java/com/restaurant/demo/controller/PageController.java` - Already validates session correctly; reference for session handling
- `src/main/java/com/restaurant/demo/controller/CustomerController.java` - API login endpoint; needs to align with form login behavior
- `src/main/resources/static/js/landing.js` - Contains API login function; needs to be updated or deprecated
- `src/main/resources/templates/login.html` - Spring Security login form page
- `src/main/resources/templates/index.html` - Landing page; may need login modal or redirect to login page

## Tasks

- [x] 1.0 Remove localStorage dependency from customer.js and use server-rendered data
  - [x] 1.1 Modify `setupCustomerDashboard()` function to read customer ID from a hidden input field or data attribute in the DOM instead of localStorage
  - [x] 1.2 Remove the localStorage validation check that causes the "กรุณาเข้าสู่ระบบ!" alert (lines 19-33 in customer.js)
  - [x] 1.3 Update `loadCustomerProfile()` function to work without localStorage - get customerId from DOM or URL only
  - [x] 1.4 Keep localStorage usage ONLY for logout functionality to clear any residual data
  - [x] 1.5 Update the welcome text logic to use data from Thymeleaf-rendered customer object instead of fetching from API

- [x] 2.0 Ensure customer.html passes necessary data to JavaScript via data attributes
  - [x] 2.1 Verify the existing hidden input `<input type="hidden" id="customerId" th:value="${customer != null ? customer.id : ''}" />` is present (line 32)
  - [x] 2.2 Add a hidden input or data attribute for customer username: `<input type="hidden" id="customerUsername" th:value="${customer != null ? customer.username : ''}" />`
  - [x] 2.3 Optionally add customer name to the welcome text directly via Thymeleaf (already exists at line 19)
  - [x] 2.4 Ensure the customer object is properly passed from PageController to the template (already implemented)
  - [x] 2.5 Add data attributes to the body or main container for easy JavaScript access: `<body data-customer-id="${customer?.id}" data-customer-username="${customer?.username}">`

- [ ] 3.0 Update or unify the two login flows (API vs Form-based)
  - [ ] 3.1 **Decision Point:** Choose one of the following approaches:
    - **Option A (Recommended):** Keep Spring Security form login as primary, deprecate API login for regular users
    - **Option B:** Make API login the primary method and remove Spring Security form-based login
    - **Option C:** Keep both but ensure they both set localStorage consistently
  - [ ] 3.2 **If Option A chosen:** Update `landing.js` to redirect to `/login` page instead of using API login
  - [ ] 3.3 **If Option A chosen:** Add JavaScript to `login.html` to set localStorage after successful Spring Security login (using redirect with query params or session data endpoint)
  - [ ] 3.4 **If Option B chosen:** Remove Spring Security form login configuration and update SecurityConfig to allow API-only authentication
  - [ ] 3.5 **If Option B chosen:** Create a proper login page with modal matching the `landing.js` expectations (authModal, loginBtn, etc.)
  - [ ] 3.6 **If Option C chosen:** Create a new endpoint `/api/auth/session-data` that returns current session information to be stored in localStorage after form login
  - [ ] 3.7 Update `CustomAuthenticationSuccessHandler` to redirect to an intermediate page that sets localStorage before final redirect (if Option A or C)
  - [ ] 3.8 Ensure both login methods create identical session attributes (customerId, username, email, etc.)

- [ ] 4.0 Test the complete login flow end-to-end
  - [ ] 4.1 **Test Case 1:** Clear browser cookies and localStorage, navigate to `/login`, login with valid credentials, verify redirect to `/customer/{id}` works without alerts
  - [ ] 4.2 **Test Case 2:** After logging in, refresh the `/customer/{id}` page and verify no "please login" alert appears
  - [ ] 4.3 **Test Case 3:** Try to access `/customer/3` without logging in first - verify proper redirect to login page
  - [ ] 4.4 **Test Case 4:** Try to access `/customer/3` while logged in as customer ID 5 - verify proper error handling (unauthorized or redirect)
  - [ ] 4.5 **Test Case 5:** Login, then logout, verify session is cleared and localStorage is cleared
  - [ ] 4.6 **Test Case 6:** Test cart functionality after login to ensure customer-specific cart operations work correctly
  - [ ] 4.7 **Test Case 7:** Check browser console for any JavaScript errors during login flow
  - [ ] 4.8 **Test Case 8:** Test with different browsers (Chrome, Firefox, Edge) to ensure localStorage/session compatibility

- [ ] 5.0 Clean up unused code and add documentation
  - [ ] 5.1 If API login is deprecated, add `@Deprecated` annotation to `/api/customers/login` endpoint with comment explaining to use form login
  - [ ] 5.2 If form login is deprecated, remove or comment out the `login.html` template and update SecurityConfig
  - [ ] 5.3 Remove any unused imports in `customer.js` and `landing.js`
  - [ ] 5.4 Add JSDoc comments to modified JavaScript functions explaining the new data flow
  - [ ] 5.5 Update `README.md` or create `docs/LOGIN_FLOW.md` documenting the chosen login approach and session management
  - [ ] 5.6 Add inline comments in `customer.js` explaining why localStorage is no longer used for session validation
  - [ ] 5.7 Review and remove any console.log statements used for debugging (or convert to proper logging)
  - [ ] 5.8 Ensure consistent error messages in Thai language across all login-related alerts and UI messages

## Implementation Notes

### Recommended Approach (Option A - Form Login Primary)

The **recommended solution** is to keep Spring Security form-based login and remove the localStorage dependency entirely:

**Pros:**
- More secure (server-side session management)
- Aligns with Spring Security best practices
- No client-side storage of sensitive session data
- Works even if JavaScript is disabled or localStorage is blocked

**Implementation Steps:**
1. Modify `customer.js` to read customer data from DOM (hidden inputs with Thymeleaf values)
2. Remove localStorage checks that cause false "please login" alerts
3. Keep the API login endpoint for potential mobile app use but document it's not for web UI
4. The server-side session validation in `PageController` is already working correctly

### Alternative: Add localStorage Population After Form Login (Option C)

If you need localStorage for other features (like offline cart), add an intermediate page:

**Implementation:**
1. Create `login-success.html` that reads session data and populates localStorage via JavaScript
2. Update `CustomAuthenticationSuccessHandler` to redirect to `/login-success?customerId=X`
3. JavaScript on login-success page stores data in localStorage then redirects to `/customer/{id}`

### Testing Checklist

- [ ] Session persists across page refreshes
- [ ] Session expires after configured timeout (30 minutes per `application.properties`)
- [ ] Logout properly clears both server session and localStorage
- [ ] Cannot access another customer's page (e.g., logged in as ID 3 cannot access `/customer/5`)
- [ ] Cart operations work correctly with customer-specific data

## Security Considerations

- Server-side session validation is the primary security mechanism (already implemented in `PageController`)
- Client-side localStorage should NEVER be trusted for authentication/authorization
- Always validate session on the server before rendering sensitive data
- CSRF token is already configured in `customer.html` for logout operations

## Files Modified Summary

**JavaScript Files:**
- `customer.js` - Remove localStorage dependency, read from DOM instead
- `landing.js` - Optionally update or deprecate based on chosen approach

**HTML Templates:**
- `customer.html` - Add data attributes for JavaScript access (minor changes)
- `login.html` - Potentially add JavaScript for localStorage population

**Java Files:**
- Minimal or no changes needed (session management already working correctly)
- Possibly update `CustomAuthenticationSuccessHandler` for Option C approach

**Configuration:**
- `application.properties` - Already correctly configured for sessions
- `SecurityConfig.java` - Already correctly configured
