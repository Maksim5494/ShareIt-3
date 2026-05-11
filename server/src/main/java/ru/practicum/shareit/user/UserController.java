package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("Получен HTTP-запрос по адресу /users (метод POST). "
                + "Вызван метод create(@Valid @RequestBody UserDto userDto)");
        return userService.create(userDto);
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Long userId) {
        log.info("Получен HTTP-запрос по адресу /users/{userId} (метод GET). " +
                " Вызван метод getById(@PathVariable int userId)");
        return userService.getById(userId);
    }

    @GetMapping
    public List<UserDto> findAll() {
        log.info("Получен HTTP-запрос по адресу /users (метод GET). Вызван метод getAll()");
        return userService.findAll();
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Получен HTTP-запрос по адресу /users (метод PATCH). "
                + "Вызван метод update()");
        return userService.update(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        log.info("Получен HTTP-запрос по адресу /users/{userId} (метод DELETE). " +
                " Вызван метод delete(@PathVariable int userId)");
        userService.delete(userId);
    }

}
