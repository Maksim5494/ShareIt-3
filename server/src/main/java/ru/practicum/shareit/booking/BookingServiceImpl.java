package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto create(Long userId, BookingRequestDto bookingRequestDto) {
        validate(userId, bookingRequestDto);
        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("ItemId = " + bookingRequestDto.getItemId() + " not found!"));
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User id = " + userId + " not found!"));
        return BookingMapper.toBookingDto(bookingRepository.save(
                Booking.builder()
                        .start(bookingRequestDto.getStart())
                        .end(bookingRequestDto.getEnd())
                        .item(item)
                        .booker(booker)
                        .status(BookingStatus.WAITING)
                        .build()
        ));
    }

    @Override
    public BookingDto setApproved(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking id = " + bookingId + " not found!"));
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("setApproved: User id = " + userId + " not found!");
        }

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only owner can set approved!");
        }
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new ValidationException("Status is APPROVED!");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto findById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking id = " + bookingId + " not found!"));
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only owner or booker can get Booking!");
        }
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User id = " + userId + " not found!");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public Collection<BookingDto> findAllByBookerAndStatus(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User id = " + userId + " not found!");
        }
        return switch (state) {
            case "ALL" -> bookingRepository.findAllByBookerIdOrderByStartDesc(userId).stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
            case "CURRENT" ->
                    bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now()).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            case "PAST" ->
                    bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now()).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            case "FUTURE" ->
                    bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now()).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            case "WAITING", "REJECTED" ->
                    bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.valueOf(state)).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            default -> throw new RuntimeException("Unknown state: " + state);
        };
    }

    @Override
    public Collection<BookingDto> findAllByOwnerAndStatus(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User id = " + userId + " not found!");
        }
        return switch (state) {
            case "ALL" -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId).stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
            case "CURRENT" ->
                    bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now()).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            case "PAST" ->
                    bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now()).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            case "FUTURE" ->
                    bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now()).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            case "WAITING", "REJECTED" ->
                    bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.valueOf(state)).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            default -> throw new RuntimeException("Unknown state: " + state);
        };
    }

    private void validate(Long userId, BookingRequestDto bookingRequestDto) {
        if (bookingRequestDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("End date before now!");
        }
        if (bookingRequestDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Start date before now!");
        }
        if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart())) {
            throw new ValidationException("End date before Start date!");
        }
        if (bookingRequestDto.getEnd().equals(bookingRequestDto.getStart())) {
            throw new ValidationException("End date equals Start date!");
        }
        Item item = itemRepository.findById(bookingRequestDto.getItemId()).orElseThrow(
                () -> new NotFoundException("Validation: Item id = " + bookingRequestDto.getItemId() + " not found!"));
        if (Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("Item is already booked!");
        }
        if (Boolean.FALSE.equals(item.getAvailable())) {
            throw new RuntimeException("Available is not true!");
        }
        if (bookingRepository.findAllByItemId(item.getId()).stream()
                .anyMatch(booking -> (booking.getStart().isAfter(bookingRequestDto.getStart())
                        && booking.getStart().isBefore(bookingRequestDto.getEnd()))
                        || (booking.getEnd().isAfter(bookingRequestDto.getStart())
                        && booking.getEnd().isBefore(bookingRequestDto.getEnd())))) {
            throw new ValidationException("Crossing dates!");
        }
    }
}