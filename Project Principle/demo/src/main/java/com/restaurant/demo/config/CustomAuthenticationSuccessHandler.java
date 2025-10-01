package com.restaurant.demo.config;

import com.restaurant.demo.model.Customer;
import com.restaurant.demo.service.CustomerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * Custom authentication success handler that redirects users to their customer-specific page
 * after successful login. This handler fetches the customer ID from the database based on
 * the authenticated username and redirects to /customer/{customerId}.
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final CustomerService customerService;

    public CustomAuthenticationSuccessHandler(@Lazy CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        // Get the authenticated username
        String username = authentication.getName();
        
        // Fetch customer from database by username
        Optional<Customer> customerOpt = customerService.findCustomerByUsernameOrEmail(username);
        
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            Long customerId = customer.getId();
            
            // Store customer ID in HTTP session for server-side validation
            HttpSession session = request.getSession();
            session.setAttribute("customerId", customerId);
            session.setAttribute("username", customer.getUsername());
            
            // Redirect to customer-specific page
            response.sendRedirect(request.getContextPath() + "/customer/" + customerId);
        } else {
            // Fallback: redirect to login with error if customer not found
            response.sendRedirect(request.getContextPath() + "/login?error=notfound");
        }
    }
}
