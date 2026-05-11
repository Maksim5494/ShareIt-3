package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.BookingDateInfoDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    public static Item toItem(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public static ItemInfoDto toItemInfoDto(Item item,
                                            BookingDateInfoDto lastBooking,
                                            BookingDateInfoDto nextBooking,
                                            Collection<CommentDto> commentDtos) {
        if (item == null) return null;
        return ItemInfoDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .request(item.getRequest() != null ? item.getRequest().getId() : null)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(commentDtos)
                .build();
    }

    public static List<ItemDto> toItemsDtoCollection(Collection<Item> items) {
        return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }
}
