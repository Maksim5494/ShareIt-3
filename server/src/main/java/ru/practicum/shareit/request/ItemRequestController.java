package ru.practicum.shareit.request;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody ItemRequestRequestDto itemRequestRequestDto) {
        itemRequestRequestDto.setRequestorId(userId);
        log.info("Получен HTTP-запрос по адресу /requests (метод POST). "
                + "Вызван метод create(itemRequestRequestDto)");
        return itemRequestService.create(itemRequestRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> findAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен HTTP-запрос по адресу /requests (метод GET). "
                + "Вызван метод findAllByUserId(userId)");
        return itemRequestService.findAllByUserId(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto findItemRequestById(@PathVariable(value = "requestId") Long requestId) {
        log.info("Получен HTTP-запрос по адресу /requests/{requestId} (метод GET). "
                + "Вызван метод findItemRequestById(requestId)");
        return itemRequestService.findItemRequestById(requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAllUsersItemRequest(
            @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
            @RequestParam(defaultValue = "10", required = false) @Min(1) int size) {
        Pageable pageable = PageRequest.of(from, size, Sort.by("created").descending());
        log.info("Получен HTTP-запрос по адресу /requests/all (метод GET). "
                + "Вызван метод findAllUsersItemRequest(pageable)");
        return itemRequestService.findAllUsersItemRequest(pageable);
    }
}