package com.company.claimx.context;

/**
 * ThreadLocal storage for authenticated user information.
 * Each HTTP request runs in its own thread, so this is thread-safe.
 * The AuthenticationAspect populates this context before each method,
 * and controllers can retrieve user info from here.
 * Usage in controllers:
 * String email = AuthenticationContext.getUserEmail();
 * String role = AuthenticationContext.getUserRole();
 */
public class AuthenticationContext {

    private static final ThreadLocal<String> userEmail = new ThreadLocal<>();
    private static final ThreadLocal<String> userRole = new ThreadLocal<>();
    private static final ThreadLocal<Long> userId = new ThreadLocal<>();

    /**
     * Set the authenticated user's email
     */
    public static void setUserEmail(String email) {
        userEmail.set(email);
    }

    /**
     * Get the authenticated user's email
     */
    public static String getUserEmail() {
        return userEmail.get();
    }

    /**
     * Set the authenticated user's role
     */
    public static void setUserRole(String role) {
        userRole.set(role);
    }

    /**
     * Get the authenticated user's role
     */
    public static String getUserRole() {
        return userRole.get();
    }

    /**
     * Set the authenticated user's ID
     */
    public static void setUserId(Long id) {
        userId.set(id);
    }

    /**
     * Get the authenticated user's ID
     */
    public static Long getUserId() {
        return userId.get();
    }

    /**
     * Clear all context data (called after request completes)
     */
    public static void clear() {
        userEmail.remove();
        userRole.remove();
        userId.remove();
    }

}
