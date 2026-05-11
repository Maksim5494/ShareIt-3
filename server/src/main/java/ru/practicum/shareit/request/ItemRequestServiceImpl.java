package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemRequestDto> findAllByUserId(Long userId) {
        return ItemRequestMapper.toItemRequestDtoList(itemRequestRepository.findAllByRequestorId(userId));
    }

    @Override
    public ItemRequestDto create(ItemRequestRequestDto itemRequestRequestDto) {
        User requestor = userRepository.findById(itemRequestRequestDto.getRequestorId()).orElseThrow(() -> {
            throw new NotFoundException("User id = " + itemRequestRequestDto.getRequestorId() + " not found!");
        });


        ItemRequest itemRequest = itemRequestRepository.save(
                ItemRequest.builder()
                        .description(itemRequestRequestDto.getDescription())
                        .requestor(requestor)
                        .created(LocalDateTime.now())
                        //.items(itemRepository.findAll())
                        .build()
        );
        log.info("Запрос создан");
        return ItemRequestMapper.toItemRequestDto(itemRequest);


    }

    @Override
    public ItemRequestDto findItemRequestById(Long itemRequestId) {
        return ItemRequestMapper.toItemRequestDto(
                itemRequestRepository.findByIdOrderByCreatedAsc(itemRequestId).orElseThrow(() -> {
                    throw new NotFoundException("ItemRequest id = " + itemRequestId + " not found!");
                }));
    }

    @Override
    public List<ItemRequestDto> findAllUsersItemRequest(Pageable pageable) {
        return itemRequestRepository.findAll(pageable).get()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }
}