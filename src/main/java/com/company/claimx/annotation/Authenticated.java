package com.company.claimx.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that require authentication.
 *
 * When applied to a controller method, the AuthenticationAspect will:
 * 1. Extract user information from Spring Security Context
 * 2. Store user info in ThreadLocal (AuthenticationContext)
 * 3. Check role requirements (if specified)
 * 4. Allow method execution if all checks pass
 *
 * Usage:
 * - Simple auth: @Authenticated
 * - Role check: @Authenticated(roles = {"MANAGER"})
 * - Multiple roles: @Authenticated(roles = {"MANAGER", "ADMIN"})
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticated {

    /**
     * Optional: Required roles for this endpoint.
     * If empty, any authenticated user can access.
     * If specified, user must have one of these roles.
     */
    String[] roles() default {};

}
