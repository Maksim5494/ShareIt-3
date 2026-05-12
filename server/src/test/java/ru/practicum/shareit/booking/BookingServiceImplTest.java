package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        owner = User.builder().id(2L).build();

        item = Item.builder()
                .id(10L)
                .owner(owner)
                .available(true)
                .build();

        booking = Booking.builder()
                .id(100L)
                .item(item)
                .booker(user)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        requestDto = new BookingRequestDto();
        requestDto.setItemId(10L);
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    void create_ShouldReturnBookingDto() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            saved.setId(100L);
            saved.setStatus(BookingStatus.WAITING);
            return saved;
        });

        BookingDto result = bookingService.create(1L, requestDto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("WAITING", result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void create_WhenEndBeforeNow_ShouldThrowValidationException() {
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().minusDays(1));

        assertThrows(ValidationException.class, () -> bookingService.create(1L, requestDto));
    }

    @Test
    void create_WhenStartBeforeNow_ShouldThrowValidationException() {
        requestDto.setStart(LocalDateTime.now().minusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class, () -> bookingService.create(1L, requestDto));
    }

    @Test
    void create_WhenEndEqualsStart_ShouldThrowValidationException() {
        LocalDateTime t = LocalDateTime.now().plusDays(1);
        requestDto.setStart(t);
        requestDto.setEnd(t);

        assertThrows(ValidationException.class, () -> bookingService.create(1L, requestDto));
    }

    @Test
    void create_WhenItemNotFound_ShouldThrowNotFoundException() {
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(1L, requestDto));
    }

    @Test
    void create_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(1L, requestDto));
    }

    @Test
    void setApproved_ShouldReturnApprovedBookingDto() {
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingDto result = bookingService.setApproved(2L, 100L, true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED.name(), result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void setApproved_WhenNotOwner_ShouldThrowNotFoundException() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(NotFoundException.class, () -> bookingService.setApproved(1L, 100L, true));
    }

    @Test
    void setApproved_WhenAlreadyApproved_ShouldThrowValidationException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userRepository.existsById(2L)).thenReturn(true);

        assertThrows(ValidationException.class, () -> bookingService.setApproved(2L, 100L, true));
    }

    @Test
    void findById_ShouldReturnBooking() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userRepository.existsById(1L)).thenReturn(true);

        BookingDto result = bookingService.findById(100L, 1L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    void findById_WhenBookingNotFound_ShouldThrowNotFoundException() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.findById(100L, 1L));
    }

    @Test
    void findAllByBookerAndStatus_All_ShouldReturnBookings() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(1L)).thenReturn(List.of(booking));

        Collection<BookingDto> result = bookingService.findAllByBookerAndStatus(1L, "ALL");

        assertEquals(1, result.size());
    }

    @Test
    void findAllByBookerAndStatus_UnknownState_ShouldThrowRuntimeException() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> bookingService.findAllByBookerAndStatus(1L, "UNSUPPORTED"));
    }

    @Test
    void findAllByBookerAndStatus_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.findAllByBookerAndStatus(1L, "ALL"));
    }

    @Test
    void findAllByOwnerAndStatus_All_ShouldReturnBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(2L)).thenReturn(List.of(booking));

        Collection<BookingDto> result = bookingService.findAllByOwnerAndStatus(2L, "ALL");

        assertEquals(1, result.size());
    }

    @Test
    void findAllByOwnerAndStatus_UnknownState_ShouldThrowRuntimeException() {
        when(userRepository.existsById(2L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> bookingService.findAllByOwnerAndStatus(2L, "UNSUPPORTED"));
    }

    @Test
    void findAllByOwnerAndStatus_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.existsById(2L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.findAllByOwnerAndStatus(2L, "ALL"));
    }
}