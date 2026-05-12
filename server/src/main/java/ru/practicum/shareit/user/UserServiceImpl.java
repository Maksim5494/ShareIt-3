package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public UserDto create(UserDto user) {
        if (repository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User with e-mail = " + user.getEmail() + " exist!");
        }
        log.info("Начало процесса создания пользователя");
        User createdUser = repository.save(UserMapper.toUser(user));
        log.info("Пользователь создан");
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto getById(Long userId) {
        log.info("Начало процесса получения пользователя по id = {}", userId);
        log.info("Пользователь получен");
        return UserMapper.toUserDto(repository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден")));
    }

    @Override
    public List<UserDto> findAll() {
        log.info("Начало процесса получения всех пользователей");
        List<User> users = repository.findAll();
        log.info("Список всех пользователей получен");
        return users.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        log.info("Начало процесса обновления пользователя");
        User user = repository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("User (id = " + userId + ") not found!");
        });
        if (userDto.getEmail() != null
                && !userDto.getEmail().isBlank()) {
            if (repository.existsByEmail(userDto.getEmail())) {
                throw new DuplicatedDataException("User with e-mail = " + userDto.getEmail() + "exist!");
            }
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null
                && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        log.info("Пользователь обновлен");
        return UserMapper.toUserDto(repository.save(user));
    }

    @Override
    public void delete(Long userId) {
        log.info("Начало процесса удаления пользователя");
        repository.deleteById(userId);
        log.info("Пользователь удален");
    }
}
