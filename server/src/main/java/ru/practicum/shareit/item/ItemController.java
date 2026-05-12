package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentRequestDto;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;
    private static final String REQUEST_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto create(@RequestHeader(REQUEST_HEADER) Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("Получен HTTP-запрос по адресу /items (метод POST). "
                + "Вызван метод create()");
        return service.create(userId, itemDto);
    }

    @GetMapping("{itemId}")
    public ItemInfoDto getById(@RequestHeader(REQUEST_HEADER) Long userId,
                               @PathVariable Long itemId) {
        ItemInfoDto item = service.findItemById(userId, itemId);
        log.info("Получен HTTP-запрос по адресу /items/{itemId} (метод GET). "
                + "Вызван метод findItemById(userId, itemId);");
        return item;
    }

    @GetMapping
    public List<ItemInfoDto> getAllItems(@RequestHeader(REQUEST_HEADER) Long id) {
        List<ItemInfoDto> items = service.findItemsByUserId(id);
        log.info("Получен HTTP-запрос по адресу /items (метод GET). "
                + "Вызван метод findItemsByUserId(id)");
        return items;
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(REQUEST_HEADER) Long userId,
                              @RequestBody ItemDto itemDto,
                              @PathVariable Long itemId) {
        log.info("Получен HTTP-запрос по адресу /{itemId} (метод PATCH). "
                + "Вызван метод update(userId, itemDto, itemId)");
        return service.update(userId, itemDto, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchByNameOrDesc(@RequestParam("text") String text) {
        log.info("Получен HTTP-запрос по адресу /search (метод GET). "
                + "Вызван метод findItemsByText(text)");
        return service.findItemsByText(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long itemId,
                                 @RequestBody CommentRequestDto commentRequestDto) {
        log.info("Получен HTTP-запрос по адресу /{itemId}/comment (метод POST). "
                + "Вызван метод addComment(userId, itemId, commentRequestDto)");
        return service.addComment(userId, itemId, commentRequestDto);
    }
}
