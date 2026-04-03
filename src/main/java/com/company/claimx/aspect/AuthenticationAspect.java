package com.company.claimx.aspect;

import com.company.claimx.annotation.Authenticated;
import com.company.claimx.constants.ErrorMessageConstants;
import com.company.claimx.context.AuthenticationContext;
import com.company.claimx.entity.User;
import com.company.claimx.exception.UnauthorizedAccessException;
import com.company.claimx.exception.UserNotAuthenticated;
import com.company.claimx.exception.UserNotFoundException;
import com.company.claimx.repository.UserRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class AuthenticationAspect {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationAspect.class);

    @Autowired
    private UserRepository userRepository;

    @Around("@annotation(com.company.claimx.annotation.Authenticated)")
    public Object authenticate(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Authenticated authenticated = signature.getMethod().getAnnotation(Authenticated.class);

        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {

                throw new UserNotAuthenticated(ErrorMessageConstants.USER_NOT_AUTHENTICATED);
            }

            String email = authentication.getName();
            if (email == null || email.isEmpty()) {
                throw new UserNotAuthenticated(ErrorMessageConstants.USER_NOT_AUTHENTICATED_NO_EMAIL_FOUND);
            }

            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse(ErrorMessageConstants.UNKNOWN);

            if (role.startsWith(ErrorMessageConstants.ROLE)) {
                role = role.substring(5);
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL + email));

            AuthenticationContext.setUserEmail(email);
            AuthenticationContext.setUserRole(role);
            AuthenticationContext.setUserId(user.getId());

            if (authenticated.roles().length > 0) {
                boolean hasRequiredRole = Arrays.asList(authenticated.roles()).contains(role);

                if (!hasRequiredRole) {
                    logger.error(ErrorMessageConstants.ROLES_REQUIRED_ACCESS_DENIED, Arrays.toString(authenticated.roles()));
                    throw new UnauthorizedAccessException(ErrorMessageConstants.UNAUTHORIZED_ACCESS);
                }


            }

            Object result = joinPoint.proceed();
            return result;


        }catch (Exception e) {
            logger.error(ErrorMessageConstants.AUTHENTICATION_FAILED, e.getMessage());
            throw e;
        } finally {
            AuthenticationContext.clear();

        }
    }

}
