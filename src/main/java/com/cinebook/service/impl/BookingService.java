package com.cinebook.service.impl;

import com.cinebook.dto.BookingDto;
import com.cinebook.dto.ShowSeatDto;
import com.cinebook.entity.*;
import com.cinebook.enums.BookingStatus;
import com.cinebook.enums.PaymentStatus;
import com.cinebook.enums.SeatStatus;
import com.cinebook.exception.BadRequestException;
import com.cinebook.exception.ResourceNotFoundException;
import com.cinebook.repository.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ShowService showService;
    private final SeatService seatService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.currency}")
    private String currency;

    // ── Step 1: Lock seats + create pending booking ──────────────────────────
    @Transactional
    public BookingDto.RazorpayOrderResponse initiateBooking(BookingDto.CreateBookingRequest request,
                                                             String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show", request.getShowId()));

        // Lock seats — throws BadRequestException if any seat is unavailable
        ShowSeatDto.LockSeatsResponse lockResponse = seatService.lockSeats(
                show.getId(), request.getSeatIds(), userEmail);

        // Validate that all requested seats were actually locked
        if (lockResponse.getLockedSeats().size() != request.getSeatIds().size()) {
            throw new BadRequestException("Could not lock all requested seats. Please try again.");
        }

        // Fetch locked seat entities
        List<ShowSeat> seats = showSeatRepository.findAllById(request.getSeatIds());

        // Calculate total
        BigDecimal total = show.getTicketPrice()
                .multiply(BigDecimal.valueOf(seats.size()));

        // Create pending booking
        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .seatCount(seats.size())
                .totalAmount(total)
                .bookingStatus(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        booking = bookingRepository.save(booking);

        // Link seats to booking
        final Booking finalBooking = booking;
        seats.forEach(seat -> seat.setBooking(finalBooking));
        showSeatRepository.saveAll(seats);

        // Create Razorpay order
        String razorpayOrderId = createRazorpayOrder(booking);
        booking.setRazorpayOrderId(razorpayOrderId);
        bookingRepository.save(booking);

        BookingDto.RazorpayOrderResponse response = new BookingDto.RazorpayOrderResponse();
        response.setOrderId(razorpayOrderId);
        response.setAmount(total);
        response.setCurrency(currency);
        response.setBookingReference(booking.getBookingReference());
        response.setBookingId(booking.getId());
        response.setKeyId(razorpayKeyId);
        return response;
    }

    // ── Step 2: Verify payment signature ─────────────────────────────────────
    @Transactional
    public BookingDto.BookingResponse verifyPaymentAndConfirm(BookingDto.PaymentVerificationRequest request,
                                                               String userEmail) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", request.getBookingId()));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new BadRequestException("Unauthorized access to booking");
        }

        // Verify Razorpay signature
        if (!verifySignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId(),
                request.getRazorpaySignature())) {
            booking.setPaymentStatus(PaymentStatus.FAILED);
            bookingRepository.save(booking);
            throw new BadRequestException("Payment verification failed. Invalid signature.");
        }

        // Confirm booking
        booking.setRazorpayPaymentId(request.getRazorpayPaymentId());
        booking.setRazorpaySignature(request.getRazorpaySignature());
        booking.setPaymentStatus(PaymentStatus.SUCCESS);
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        // Mark seats as BOOKED
        booking.getSeats().forEach(seat -> {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setLockedUntil(null);
        });
        showSeatRepository.saveAll(booking.getSeats());
        bookingRepository.save(booking);

        // Send confirmation email async
        emailService.sendBookingConfirmation(booking);

        return toResponse(booking);
    }

    // ── Cancel Booking ────────────────────────────────────────────────────────
    @Transactional
    public BookingDto.BookingResponse cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new BadRequestException("Unauthorized");
        }
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking already cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);

        // Release seats
        booking.getSeats().forEach(seat -> {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedUntil(null);
            seat.setBooking(null);
        });
        showSeatRepository.saveAll(booking.getSeats());
        bookingRepository.save(booking);

        emailService.sendCancellationEmail(booking);
        return toResponse(booking);
    }

    // ── Get user bookings ─────────────────────────────────────────────────────
    @Transactional
    public List<BookingDto.BookingResponse> getUserBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
//                .stream().map(this::toResponse).toList();
        return bookingRepository.findByUserIdWithDetails(user.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public BookingDto.BookingResponse getBookingById(Long id, String userEmail) {
//        Booking booking = bookingRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        Booking booking = bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new BadRequestException("Unauthorized");
        }
        return toResponse(booking);
    }

    // ── Razorpay helpers ──────────────────────────────────────────────────────
    private String createRazorpayOrder(Booking booking) {
        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject options = new JSONObject();
            // Razorpay amount is in paise (smallest currency unit)
            options.put("amount", booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
            options.put("currency", currency);
            options.put("receipt", booking.getBookingReference());
            options.put("payment_capture", 1);
            Order order = client.orders.create(options);
            return order.get("id");
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new BadRequestException("Payment gateway error. Please try again.");
        }
    }

    private boolean verifySignature(String orderId, String paymentId, String razorpaySignature) {
        try {
            // Razorpay signature = HMAC-SHA256(orderId + "|" + paymentId, keySecret)
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generated = HexFormat.of().formatHex(hash);
            // Constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(
                    generated.getBytes(StandardCharsets.UTF_8),
                    razorpaySignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private BookingDto.BookingResponse toResponse(Booking b) {
        BookingDto.BookingResponse r = new BookingDto.BookingResponse();
        r.setId(b.getId());
        r.setBookingReference(b.getBookingReference());
        r.setShow(showService.toResponse(b.getShow()));
        r.setSeatCount(b.getSeatCount());
        r.setTotalAmount(b.getTotalAmount());
        r.setBookingStatus(b.getBookingStatus());
        r.setPaymentStatus(b.getPaymentStatus());
        r.setRazorpayOrderId(b.getRazorpayOrderId());
        r.setCreatedAt(b.getCreatedAt());
        if (b.getSeats() != null) {
            r.setSeats(b.getSeats().stream().map(seatService::toResponse).toList());
        }
        return r;
    }
}
