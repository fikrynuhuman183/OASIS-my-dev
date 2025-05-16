package com.oasis.ocrspring.annotations;

import com.oasis.ocrspring.model.Role;
import com.oasis.ocrspring.model.User;
import com.oasis.ocrspring.repository.RefreshtokenRepsitory;
import com.oasis.ocrspring.service.RoleService;
import com.oasis.ocrspring.service.UserService;
import com.oasis.ocrspring.service.auth.AuthenticationToken;
import com.oasis.ocrspring.service.auth.TokenService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;

@Component
public class Authenticator implements HandlerInterceptor
{

    private final TokenService tokenService;
    private final UserService userService;
    private final RoleService roleService;
    private final RefreshtokenRepsitory refreshTokenRepository;

    public static final String USER_NOT_FOUND = "User not found";

    @Autowired
    public Authenticator(TokenService tokenService, UserService userService,
                         RoleService roleService,
                         RefreshtokenRepsitory refreshTokenRepository)
    {
        this.tokenService = tokenService;
        this.userService = userService;
        this.roleService = roleService;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler)
    {
        // Check if the handler is a method
        if (handler instanceof HandlerMethod handlerMethod)
        {
            Method method = handlerMethod.getMethod();

            // Check if the method has the @Protect annotation
            Protected annotatedProtected = method.getAnnotation(Protected.class);
            if (annotatedProtected != null)
            {
                String token = extractToken(request.getHeader("Authorization"));
                String email = request.getHeader("email");

                if (validateTokenAndEmail(token, email) &&
                        isAccessGrantedAndSetAttribute(request, token, email))
                {
                    return true;
                } else
                {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return false;
                }
            }
        }

        return true;  // If no @Protect annotation, continue with the request
    }

    private static String extractToken(String authorizationHeader)
    {
       if (authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
       {
           return authorizationHeader.substring("Bearer ".length());
       }
       return null;
    }


    private boolean validateTokenAndEmail(String token, String email)
    {
        return (token != null && email != null);
    }

    private boolean isAccessGrantedAndSetAttribute(HttpServletRequest request,
                                                   String token, String email)
    {
        try
        {
            Map<String, Object> tokenBody = tokenService.decodeAccessToken(token);
            Optional<User> user = userService.getUserByEmail(getEmail(tokenBody));

            if(user.isEmpty())
            {
                return false;
            }
            String userEmail = user.get().getEmail();
            String userRole = user.get().getRole();
            if (Objects.equals(userEmail, email) && Objects.equals(userRole,
                    getRole(tokenBody)))
            {
                setRequestAttributes(request, user, tokenBody);
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    private void setRequestAttributes(HttpServletRequest request, Optional<User> user,
                                      Map<String, Object> tokenBody)
            throws NoSuchElementException
    {
        if (user.isEmpty())
        {
            throw new NoSuchElementException(USER_NOT_FOUND);
        }
        Optional<Role> role = roleService.getRoleByrole(user.get().getRole());

        request.setAttribute("email", getEmail(tokenBody));
        request.setAttribute("role", getRole(tokenBody));
        request.setAttribute("_id", getId(user));
        request.setAttribute("permissions", getPermission(role));
        request.setAttribute("ownsToken",
                (AuthenticationToken.TokenOwner) tokenToCheck -> refreshTokenRepository.findByUser(
                                getId(user)).stream()
                        .anyMatch(rt -> rt.getToken().equals(tokenToCheck)));

    }


    private List<String> getPermission(Optional<Role> role)
    {
        return role.map(value -> value.getPermissions().stream()
                .map(Object::toString)
                .toList()).orElseGet(List::of);
    }

    private static ObjectId getId(Optional<User> user) throws NullPointerException
    {
        if (user.isPresent())
        {
            return user.get().getId();
        }
        throw new NoSuchElementException(USER_NOT_FOUND);
    }

    private static String getRole(Map<String, Object> tokenBody)
    {
        return tokenBody.get("role").toString();
    }

    private static String getEmail(Map<String, Object> tokenBody)
    {
        return tokenBody.get("sub").toString();
    }
}
