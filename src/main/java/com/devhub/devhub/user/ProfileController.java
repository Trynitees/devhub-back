package com.devhub.devhub.user;

import com.devhub.devhub.user.dto.AuthResponse;
import com.devhub.devhub.user.dto.UserResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService){
        this.userService = userService;
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AuthResponse update(
            Principal principal,
            @RequestParam(name = "first_name", defaultValue = "") String firstName,
            @RequestParam(name = "last_name", defaultValue = "") String lastName,
            @RequestParam(name = "patronymic", defaultValue = "") String patronymic,
            @RequestParam(name = "email", defaultValue = "") String email,
            @RequestParam(name = "bio", defaultValue = "") String bio,
            @RequestParam(name = "avatar", required = false) MultipartFile avatar
    ){
        User user = userService.updateProfile(principal.getName(), firstName, lastName, patronymic, email, bio, avatar);
        return new AuthResponse(true, UserResponse.from(user));
    }

}
