package com.cinebook.service;

import com.cinebook.dto.BookingDto;
import com.cinebook.dto.ShowSeatDto;
import com.cinebook.entity.Booking;
import com.cinebook.entity.Movie;
import com.cinebook.entity.Show;
import com.cinebook.entity.ShowSeat;
import com.cinebook.entity.Theater;
import com.cinebook.entity.User;
import com.cinebook.enums.BookingStatus;
import com.cinebook.enums.Genre;
import com.cinebook.enums.PaymentStatus;
import com.cinebook.enums.Role;
import com.cinebook.enums.SeatStatus;
import com.cinebook.exception.BadRequestException;
import com.cinebook.exception.ResourceNotFoundException;
import com.cinebook.repository.BookingRepository;
import com.cinebook.repository.ShowRepository;
import com.cinebook.repository.ShowSeatRepository;
import com.cinebook.repository.UserRepository;
import com.cinebook.service.impl.BookingService;
import com.cinebook.service.impl.EmailService;
import com.cinebook.service.impl.SeatService;
import com.cinebook.service.impl.ShowService;
import com.razorpay.OrderClient;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Tests")
class BookingServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock ShowRepository showRepository;
    @Mock ShowSeatRepository showSeatRepository;
    @Mock UserRepository userRepository;
    @Mock EmailService emailService;
    @Mock ShowService showService;
    @Mock SeatService seatService;

    @InjectMocks BookingService bookingService;

    private User user;
    private Show show;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(bookingService, "razorpayKeyId", "rzp_test_key");
        ReflectionTestUtils.setField(bookingService, "razorpayKeySecret", "rzp_test_secret");
        ReflectionTestUtils.setField(bookingService, "currency", "INR");

        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .password("x")
                .firstName("F")
                .lastName("L")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        Movie movie = Movie.builder().id(2L).title("M").genre(Genre.ACTION).active(true).build();
        Theater theater = Theater.builder().id(3L).name("T").city("C").totalSeats(100).build();
        show = Show.builder()
                .id(10L)
                .movie(movie)
                .theater(theater)
                .showDate(LocalDate.now())
                .showTime(LocalTime.NOON)
                .ticketPrice(new BigDecimal("100.00"))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("initiateBooking locks seats, creates pending booking, and creates Razorpay order")
    void initiateBooking_happyPath() throws Exception {
        BookingDto.CreateBookingRequest req = new BookingDto.CreateBookingRequest();
        req.setShowId(10L);
        req.setSeatIds(List.of(1L, 2L));

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(showRepository.findById(10L)).thenReturn(Optional.of(show));

        ShowSeatDto.LockSeatsResponse lockResp = new ShowSeatDto.LockSeatsResponse();
        ShowSeatDto.SeatResponse lr1 = new ShowSeatDto.SeatResponse(); lr1.setId(1L);
        ShowSeatDto.SeatResponse lr2 = new ShowSeatDto.SeatResponse(); lr2.setId(2L);
        lockResp.setLockedSeats(List.of(lr1, lr2));
        when(seatService.lockSeats(eq(10L), eq(List.of(1L, 2L)), eq("user@example.com"))).thenReturn(lockResp);

        ShowSeat seat1 = ShowSeat.builder().id(1L).show(show).rowLabel("A").seatNumber(1).status(SeatStatus.LOCKED).build();
        ShowSeat seat2 = ShowSeat.builder().id(2L).show(show).rowLabel("A").seatNumber(2).status(SeatStatus.LOCKED).build();
        when(showSeatRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(seat1, seat2));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b.getId() == null) b.setId(99L);
            return b;
        });

        // Mock RazorpayClient construction and its orders.create call
        Order order = mock(Order.class);
        when(order.get("id")).thenReturn("order_123");

        try (MockedConstruction<RazorpayClient> ignored = mockConstruction(RazorpayClient.class,
                (mock, context) -> {
                    OrderClient orderClient = mock(OrderClient.class);
                    when(orderClient.create(any(JSONObject.class))).thenReturn(order);
                    // RazorpayClient exposes `orders` as a field, not an injected dependency.
                    // We set it directly so BookingService can call `client.orders.create(...)`.
                    RazorpayClient.class.getField("orders").set(mock, orderClient);
                })) {
            BookingDto.RazorpayOrderResponse out = bookingService.initiateBooking(req, "user@example.com");

            assertThat(out.getOrderId()).isEqualTo("order_123");
            assertThat(out.getBookingId()).isEqualTo(99L);
            assertThat(out.getAmount()).isEqualByComparingTo("200.00");
            assertThat(out.getCurrency()).isEqualTo("INR");
            assertThat(out.getKeyId()).isEqualTo("rzp_test_key");
        }

        verify(showSeatRepository).saveAll(argThat(seats ->
                StreamSupport.stream(seats.spliterator(), false).count() == 2
                        && StreamSupport.stream(seats.spliterator(), false).allMatch(s -> s.getBooking() != null)
        ));
    }

    @Test
    @DisplayName("initiateBooking throws when user not found")
    void initiateBooking_userNotFound() {
        BookingDto.CreateBookingRequest req = new BookingDto.CreateBookingRequest();
        req.setShowId(10L);
        req.setSeatIds(List.of(1L));

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.initiateBooking(req, "user@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("verifyPaymentAndConfirm rejects access if booking doesn't belong to user")
    void verifyPaymentAndConfirm_unauthorized() {
        Booking booking = Booking.builder().id(1L).user(User.builder().email("other@example.com").build()).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingDto.PaymentVerificationRequest req = new BookingDto.PaymentVerificationRequest();
        req.setBookingId(1L);
        req.setRazorpayOrderId("o");
        req.setRazorpayPaymentId("p");
        req.setRazorpaySignature("s");

        assertThatThrownBy(() -> bookingService.verifyPaymentAndConfirm(req, "user@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    @DisplayName("verifyPaymentAndConfirm marks FAILED when signature is invalid")
    void verifyPaymentAndConfirm_invalidSignature() {
        Booking booking = Booking.builder()
                .id(1L)
                .user(user)
                .paymentStatus(PaymentStatus.PENDING)
                .bookingStatus(BookingStatus.PENDING)
                .build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto.PaymentVerificationRequest req = new BookingDto.PaymentVerificationRequest();
        req.setBookingId(1L);
        req.setRazorpayOrderId("order");
        req.setRazorpayPaymentId("pay");
        req.setRazorpaySignature("definitely-wrong");

        assertThatThrownBy(() -> bookingService.verifyPaymentAndConfirm(req, "user@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid signature");

        assertThat(booking.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(emailService, never()).sendBookingConfirmation(any());
    }
}

