package com.devhub.devhub.user;

import com.devhub.devhub.common.ApiException;
import com.devhub.devhub.common.FileStorageService;
import com.devhub.devhub.user.dto.LoginRequest;
import com.devhub.devhub.user.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileStorageService fileStorageService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    public User register(RegisterRequest req){
        if (userRepository.existsByUsername(req.username())){
            throw new ApiException(HttpStatus.BAD_REQUEST, "username", "Пользователь с таким именем уже существует");
        }
        if (userRepository.existsByEmailIgnoreCase(req.email())){
            throw new ApiException(HttpStatus.BAD_REQUEST, "email", "Этот email уже используется");
        }
        if (!req.password().equals(req.password2())){
            throw new ApiException(HttpStatus.BAD_REQUEST, "password2", "Пароль не совпадает");
        }

        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        return userRepository.save(user);
    }

    public User login(LoginRequest req){
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "default", "Неверное имя пользователя или пароль"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())){
            throw new ApiException(HttpStatus.BAD_REQUEST, "detail", "Неверное имя пользователя или пароль");
        }
        return user;
    }

    public User getByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(()-> new ApiException(HttpStatus.UNAUTHORIZED, "detail", "Нужно войти"));
    }

    public User updateProfile(String username, String firstname, String lastname, String patronymic, String email, String bio, MultipartFile avatar){
        User user = getByUsername(username);

        if (!email.isBlank() && !email.equalsIgnoreCase(user.getEmail())){
            if (!email.contains("@")){
                throw new ApiException(HttpStatus.BAD_REQUEST, "email", "Некорректный email");
            }
            if (userRepository.existsByEmailIgnoreCase(email)){
                throw new ApiException(HttpStatus.BAD_REQUEST, "email", "Email уже используется");
            }
            user.setEmail(email);
        }
        if (bio.length() > 300){
            throw new ApiException(HttpStatus.BAD_REQUEST, "bio", "Не более 300 символов");
        }

        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setPatronymic(patronymic);
        user.setBio(bio);

        if (avatar != null && !avatar.isEmpty()){
            user.setAvatar(fileStorageService.save(avatar, "avatars"));
        }

        return userRepository.save(user);
    }
}
