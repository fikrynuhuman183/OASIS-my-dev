package com.oasis.ocrspring.service.auth;

import com.oasis.ocrspring.model.Role;
import com.oasis.ocrspring.model.User;
import com.oasis.ocrspring.repository.RefreshtokenRepsitory;
import com.oasis.ocrspring.service.ResponseMessages.SendErrorResponse;
import com.oasis.ocrspring.service.RoleService;
import com.oasis.ocrspring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthenticationToken {

    private final TokenService tokenService;
    private final UserService userservice;
    private final RoleService roleService;
    private final RefreshtokenRepsitory refreshTokenRepository;

    @Autowired
    public AuthenticationToken(TokenService tokenService,
                               UserService userservice,
                               RoleService roleService,
                               RefreshtokenRepsitory refreshTokenRepository) {
        this.tokenService = tokenService;
        this.userservice = userservice;
        this.roleService = roleService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /*
      * @deprecated (use Protected annotation instead , this method will be removed in future)
     */
    @Deprecated(since="under next issue", forRemoval=true)
    public void authenticateRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader != null ? authHeader.split(" ")[1] : null;
        String email = request.getHeader("email");
        SendErrorResponse sendErrorResponse = new SendErrorResponse();

        if (token == null || email == null) {

            sendErrorResponse.setErrorResponse(response, false, "Unauthorized Access");
            return;
        }

        Map<String, Object> tokenBody = tokenService.decodeAccessToken(token);
        Optional<User> user = userservice.getUserByEmail(tokenBody.get("sub").toString());

        if (user.isEmpty() || !Objects.equals(user.get().getEmail(), email) || !Objects.equals(user.get().getRole(), tokenBody.get("role").toString())) {
            sendErrorResponse.setErrorResponse(response, false, "Unauthorized Access");
            return;
        }

        Optional<Role> role = roleService.getRoleByrole(user.get().getRole());

        request.setAttribute("email", tokenBody.get("sub").toString());
        request.setAttribute("role", tokenBody.get("role").toString());
        request.setAttribute("_id", user.get().getId());
        List<String> permissions = role.get().getPermissions().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        request.setAttribute("permissions", permissions);
        request.setAttribute("ownsToken", (TokenOwner) tokenToCheck -> refreshTokenRepository.findByUser(user.get().getId()).stream().anyMatch(rt -> rt.getToken().equals(tokenToCheck))

        );
    }

    @FunctionalInterface
    public interface TokenOwner {
        boolean ownsToken(String tokenToCheck);
    }
}
