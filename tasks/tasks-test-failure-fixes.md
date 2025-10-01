# Test Failure Fixes Task List

## Relevant Files

- `src/main/java/com/restaurant/demo/exception/GlobalExceptionHandler.java` - Currently handles some exceptions but missing critical handlers for RuntimeException and validation issues
- `src/main/java/com/restaurant/demo/controller/CartController.java` - Has validation annotations but validation is not working properly for edge cases
- `src/main/java/com/restaurant/demo/controller/CustomerController.java` - Missing proper validation for path parameters and some endpoint validations
- `src/main/java/com/restaurant/demo/service/CartService.java` - Throwing RuntimeExceptions that need proper handling
- `src/main/java/com/restaurant/demo/service/CustomerService.java` - Service layer exception handling needs improvement
- `src/main/java/com/restaurant/demo/dto/CustomerRegistrationDto.java` - Phone validation pattern needs adjustment
- `src/main/java/com/restaurant/demo/config/ValidationConfig.java` - May need validation configuration updates
- `src/test/java/com/restaurant/demo/controller/CartControllerTest.java` - Test cases that need to pass
- `src/test/java/com/restaurant/demo/controller/CustomerControllerTest.java` - Test cases that need to pass
- `src/test/java/com/restaurant/demo/service/CartServiceTest.java` - Service layer tests
- `src/test/java/com/restaurant/demo/service/CustomerServiceTest.java` - Service layer tests

### Notes

- Tests are expecting proper HTTP status codes (400 for bad requests, 404 for not found, etc.) but getting different responses
- RuntimeExceptions are not being caught and converted to proper HTTP responses
- Phone validation pattern is too restrictive for test data
- Path parameter validation is not working for negative/zero values

## Tasks

- [ ] 1.0 Fix Global Exception Handling
  - [ ] 1.1 Add handler for generic RuntimeException to return HTTP 500 (Internal Server Error)
  - [ ] 1.2 Add handler for IllegalArgumentException to return HTTP 400 (Bad Request)
  - [ ] 1.3 Add handler for MethodArgumentNotValidException to return HTTP 400 with validation details
  - [ ] 1.4 Add handler for MissingServletRequestParameterException to return HTTP 400
  - [ ] 1.5 Add handler for HttpMessageNotReadableException to return HTTP 400
  - [ ] 1.6 Update existing exception handlers to use consistent error response format

- [ ] 2.0 Fix Controller Input Validation
  - [ ] 2.1 Add validation configuration to enable method-level validation
  - [ ] 2.2 Fix CartController to properly validate negative customer IDs (should return 400, not 201)
  - [ ] 2.3 Fix CartController to validate empty/null item names (should return 400, not 201)
  - [ ] 2.4 Fix CartController to validate invalid item prices (should return 400, not 201)
  - [ ] 2.5 Fix CartController to validate invalid quantities (should return 400, not 201)
  - [ ] 2.6 Fix CartController to validate long item names (should return 400, not 201)
  - [ ] 2.7 Add proper validation for path parameters that should not accept negative/zero values

- [ ] 3.0 Fix Service Layer Exception Handling
  - [ ] 3.1 Replace generic RuntimeException throws with specific custom exceptions in CartService
  - [ ] 3.2 Replace generic RuntimeException throws with specific custom exceptions in CustomerService
  - [ ] 3.3 Create CartItemNotFoundException for cart item not found scenarios
  - [ ] 3.4 Create CustomerNotFoundException for customer not found scenarios
  - [ ] 3.5 Create UnauthorizedAccessException for authorization failures
  - [ ] 3.6 Update service methods to throw appropriate specific exceptions instead of RuntimeException

- [ ] 4.0 Fix Phone Number Validation
  - [ ] 4.1 Update CustomerRegistrationDto phone validation pattern to accept international formats
  - [ ] 4.2 Change phone pattern from Thai-only to support both Thai and international formats
  - [ ] 4.3 Update validation message to reflect new accepted formats
  - [ ] 4.4 Ensure test data compatibility with new phone validation pattern

- [ ] 5.0 Add Missing Exception Handlers
  - [ ] 5.1 Add handler for CustomerNotFoundException to return HTTP 404
  - [ ] 5.2 Add handler for CartItemNotFoundException to return HTTP 404
  - [ ] 5.3 Add handler for UnauthorizedCartAccessException to return HTTP 403
  - [ ] 5.4 Add handler for InvalidCartOperationException to return HTTP 400
  - [ ] 5.5 Add handler for ConstraintViolationException for method parameter validation
  - [ ] 5.6 Ensure all handlers return consistent error response format

- [ ] 6.0 Fix Path Parameter Validation
  - [ ] 6.1 Add @Valid annotation to CustomerController path parameters where needed
  - [ ] 6.2 Add @Min(1) validation to customer ID path parameters to prevent negative/zero values
  - [ ] 6.3 Add @Min(1) validation to cart item ID path parameters to prevent negative/zero values
  - [ ] 6.4 Update CustomerController getCustomerProfile methods to return 400 for invalid IDs
  - [ ] 6.5 Update CustomerController validatePassword to properly validate empty passwords
  - [ ] 6.6 Ensure path parameter validation triggers BadRequest (400) responses, not NotFound (404)

- [ ] 7.0 Verify and Run All Tests
  - [ ] 7.1 Run CartControllerTest and verify all 33 tests pass
  - [ ] 7.2 Run CustomerControllerTest and verify all 27 tests pass
  - [ ] 7.3 Run CartServiceTest and verify all 25 tests pass
  - [ ] 7.4 Run CustomerServiceTest and verify all 14 tests pass
  - [ ] 7.5 Run DemoApplicationTests and verify integration test passes
  - [ ] 7.6 Run full test suite and ensure 100% success rate (100 tests passing)
  - [ ] 7.7 Fix any remaining test failures by updating implementation or test data as needed
