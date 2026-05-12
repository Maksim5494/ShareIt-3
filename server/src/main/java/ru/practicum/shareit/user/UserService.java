package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto user);

    UserDto getById(Long userId);

    List<UserDto> findAll();

    UserDto update(Long userId, UserDto user);

    void delete(Long userId);
}
