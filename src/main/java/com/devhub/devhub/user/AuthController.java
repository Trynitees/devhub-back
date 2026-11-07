package com.devhub.devhub.user;

import com.devhub.devhub.user.dto.AuthResponse;
import com.devhub.devhub.user.dto.LoginRequest;
import com.devhub.devhub.user.dto.RegisterRequest;
import com.devhub.devhub.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/")
public class AuthController {

    private final UserService userService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/csrf/")
    public Map<String, Object> csrf(){
        return Map.of("ok", true);
    }

    @PostMapping("/register/")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) {
        User user = userService.register(req);
        signIn(user, request, response);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(true, UserResponse.from(user)));
    }

    @PostMapping("/login/")
    public AuthResponse login(@Valid @RequestBody LoginRequest req,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        User user = userService.login(req);
        signIn(user, request, response);
        return new AuthResponse(true, UserResponse.from(user));
    }

    @PostMapping("/logout/")
    public Map<String, Object> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return Map.of("ok", true);
    }

    @GetMapping("/me/")
    public AuthResponse me(Principal principal) {
        if (principal == null) {
            return new AuthResponse(true, null);
        }
        User user = userService.getByUsername(principal.getName());
        return new AuthResponse(true, UserResponse.from(user));
    }

    private void signIn(User user, HttpServletRequest request, HttpServletResponse response) {
        String role = user.isStaff() ? "ROLE_ADMIN" : "ROLE_USER";
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

}
