package ru.practicum.shareit.item;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentRequestDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> findAll();

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, ItemDto itemDto, Long itemId);

    CommentDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto);

    ItemInfoDto findItemById(Long userId, Long itemId);

    List<ItemInfoDto> findItemsByUserId(Long userId);

    void delete(Long itemId);

    List<ItemDto> findItemsByText(String text);
}
