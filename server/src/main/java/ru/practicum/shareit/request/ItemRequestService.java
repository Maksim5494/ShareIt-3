package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

import java.util.List;

public interface ItemRequestService {
    List<ItemRequestDto> findAllByUserId(Long userId);

    ItemRequestDto create(ItemRequestRequestDto itemRequestRequestDto);

    ItemRequestDto findItemRequestById(Long itemRequestId);

    List<ItemRequestDto> findAllUsersItemRequest(Pageable pageable);
}