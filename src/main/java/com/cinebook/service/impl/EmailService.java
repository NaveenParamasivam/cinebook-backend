package com.cinebook.service.impl;

import com.cinebook.entity.Booking;
import com.cinebook.entity.ShowSeat;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("🎬 Your CineBook Tickets — " + booking.getBookingReference());
            helper.setText(buildTicketHtml(booking), true);
            mailSender.send(message);
            log.info("Booking confirmation sent to {}", booking.getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send booking confirmation: {}", e.getMessage());
        }
    }

    @Async
    public void sendCancellationEmail(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("❌ Booking Cancelled — " + booking.getBookingReference());
            helper.setText(buildCancellationHtml(booking), true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send cancellation email: {}", e.getMessage());
        }
    }

    private String buildTicketHtml(Booking booking) {
        String seats = booking.getSeats().stream()
                .map(ShowSeat::getSeatLabel)
                .collect(Collectors.joining(", "));

        String showDate = booking.getShow().getShowDate()
                .format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
        String showTime = booking.getShow().getShowTime()
                .format(DateTimeFormatter.ofPattern("hh:mm a"));

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <title>Your CineBook Ticket</title>
          <link href="https://fonts.googleapis.com/css2?family=Syne:wght@400;700;800&family=DM+Sans:wght@400;500;600&display=swap" rel="stylesheet"/>
        </head>
        <body style="margin:0;padding:0;background:#0a0a0f;font-family:'DM Sans',sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0a0a0f;padding:40px 20px;">
            <tr><td align="center">
              <!-- Header -->
              <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;">
                <tr>
                  <td style="padding:0 0 32px 0;text-align:center;">
                    <div style="display:inline-block;background:linear-gradient(135deg,#f97316,#ef4444);padding:12px 28px;border-radius:50px;">
                      <span style="font-family:'Syne',sans-serif;font-size:22px;font-weight:800;color:#fff;letter-spacing:2px;">CINEBOOK</span>
                    </div>
                    <p style="color:#6b7280;margin:12px 0 0;font-size:14px;">Your movie experience awaits</p>
                  </td>
                </tr>

                <!-- Main Ticket Card -->
                <tr>
                  <td>
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background:#131319;border-radius:24px;overflow:hidden;border:1px solid #1f1f2e;">
                      <!-- Movie banner top -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#1a0533 0%%,#0f1a3d 50%%,#1a1005 100%%);padding:36px 40px;">
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                              <td>
                                <p style="margin:0 0 6px;font-size:11px;letter-spacing:3px;color:#f97316;font-weight:600;text-transform:uppercase;">Booking Confirmed</p>
                                <h1 style="margin:0;font-family:'Syne',sans-serif;font-size:32px;font-weight:800;color:#ffffff;line-height:1.2;">
                                  %s
                                </h1>
                                <p style="margin:10px 0 0;font-size:14px;color:#9ca3af;">
                                  %s &nbsp;·&nbsp; %s &nbsp;·&nbsp; %s min
                                </p>
                              </td>
                              <td width="80" style="text-align:right;vertical-align:top;">
                                <div style="background:rgba(249,115,22,0.15);border:1px solid rgba(249,115,22,0.3);border-radius:12px;padding:8px 14px;display:inline-block;">
                                  <span style="font-family:'Syne',sans-serif;font-size:13px;font-weight:700;color:#f97316;">%s</span>
                                </div>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>

                      <!-- Perforated divider -->
                      <tr>
                        <td style="padding:0;position:relative;">
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                              <td width="30" style="background:#0a0a0f;border-radius:0 50px 50px 0;height:30px;"></td>
                              <td style="border-top:2px dashed #2a2a3e;height:1px;"></td>
                              <td width="30" style="background:#0a0a0f;border-radius:50px 0 0 50px;height:30px;"></td>
                            </tr>
                          </table>
                        </td>
                      </tr>

                      <!-- Ticket Details -->
                      <tr>
                        <td style="padding:32px 40px;">
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                              <!-- Date & Time -->
                              <td width="50%%" style="padding-right:20px;border-right:1px solid #1f1f2e;">
                                <p style="margin:0 0 4px;font-size:11px;letter-spacing:2px;color:#6b7280;text-transform:uppercase;">Date & Time</p>
                                <p style="margin:0;font-family:'Syne',sans-serif;font-size:16px;font-weight:700;color:#f1f5f9;">%s</p>
                                <p style="margin:4px 0 0;font-size:20px;font-weight:600;color:#f97316;">%s</p>
                              </td>
                              <!-- Theater -->
                              <td width="50%%" style="padding-left:20px;">
                                <p style="margin:0 0 4px;font-size:11px;letter-spacing:2px;color:#6b7280;text-transform:uppercase;">Theater</p>
                                <p style="margin:0;font-family:'Syne',sans-serif;font-size:16px;font-weight:700;color:#f1f5f9;">%s</p>
                                <p style="margin:4px 0 0;font-size:13px;color:#9ca3af;">%s</p>
                              </td>
                            </tr>
                          </table>

                          <!-- Divider -->
                          <div style="border-top:1px solid #1f1f2e;margin:24px 0;"></div>

                          <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                              <!-- Seats -->
                              <td width="50%%" style="padding-right:20px;border-right:1px solid #1f1f2e;">
                                <p style="margin:0 0 4px;font-size:11px;letter-spacing:2px;color:#6b7280;text-transform:uppercase;">Seats</p>
                                <p style="margin:0;font-family:'Syne',sans-serif;font-size:18px;font-weight:700;color:#f1f5f9;">%s</p>
                                <p style="margin:4px 0 0;font-size:13px;color:#9ca3af;">%d seat(s)</p>
                              </td>
                              <!-- Amount -->
                              <td width="50%%" style="padding-left:20px;">
                                <p style="margin:0 0 4px;font-size:11px;letter-spacing:2px;color:#6b7280;text-transform:uppercase;">Total Paid</p>
                                <p style="margin:0;font-family:'Syne',sans-serif;font-size:26px;font-weight:800;color:#10b981;">₹%s</p>
                                <p style="margin:4px 0 0;font-size:13px;color:#6b7280;">Payment Successful</p>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>

                      <!-- Booking Reference -->
                      <tr>
                        <td style="padding:0 40px 32px;">
                          <div style="background:#0f0f17;border:1px solid #2a2a3e;border-radius:14px;padding:20px;text-align:center;">
                            <p style="margin:0 0 8px;font-size:11px;letter-spacing:3px;color:#6b7280;text-transform:uppercase;">Booking Reference</p>
                            <p style="margin:0;font-family:'Syne',sans-serif;font-size:28px;font-weight:800;color:#f97316;letter-spacing:4px;">%s</p>
                            <p style="margin:8px 0 0;font-size:12px;color:#6b7280;">Show this at the theater entrance</p>
                          </div>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>

                <!-- Footer -->
                <tr>
                  <td style="padding:32px 0 0;text-align:center;">
                    <p style="margin:0;font-size:13px;color:#4b5563;">Enjoy the show! 🍿</p>
                    <p style="margin:8px 0 0;font-size:12px;color:#374151;">
                      Questions? Contact us at <a href="mailto:support@cinebook.com" style="color:#f97316;text-decoration:none;">support@cinebook.com</a>
                    </p>
                    <p style="margin:16px 0 0;font-size:11px;color:#374151;">
                      © 2024 CineBook. All rights reserved.
                    </p>
                  </td>
                </tr>

              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.formatted(
                booking.getShow().getMovie().getTitle(),
                booking.getShow().getMovie().getGenre(),
                booking.getShow().getMovie().getLanguage() != null ? booking.getShow().getMovie().getLanguage() : "English",
                booking.getShow().getMovie().getDurationMinutes() != null ? booking.getShow().getMovie().getDurationMinutes() : "N/A",
                booking.getShow().getMovie().getRating() != null ? booking.getShow().getMovie().getRating() : "U/A",
                showDate,
                showTime,
                booking.getShow().getTheater().getName(),
                booking.getShow().getTheater().getCity(),
                seats,
                booking.getSeatCount(),
                booking.getTotalAmount().toPlainString(),
                booking.getBookingReference()
        );
    }

    private String buildCancellationHtml(Booking booking) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
          <link href="https://fonts.googleapis.com/css2?family=Syne:wght@400;700;800&family=DM+Sans:wght@400;500&display=swap" rel="stylesheet"/>
        </head>
        <body style="margin:0;padding:0;background:#0a0a0f;font-family:'DM Sans',sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0a0a0f;padding:40px 20px;">
            <tr><td align="center">
              <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px;background:#131319;border-radius:24px;overflow:hidden;border:1px solid #1f1f2e;">
                <tr>
                  <td style="background:linear-gradient(135deg,#3b0a0a,#1a0a0a);padding:40px;text-align:center;">
                    <div style="font-size:48px;margin-bottom:16px;">❌</div>
                    <h1 style="margin:0;font-family:'Syne',sans-serif;font-size:28px;color:#f1f5f9;">Booking Cancelled</h1>
                    <p style="margin:12px 0 0;color:#9ca3af;">Your booking has been successfully cancelled</p>
                  </td>
                </tr>
                <tr>
                  <td style="padding:32px 40px;text-align:center;">
                    <p style="color:#6b7280;font-size:13px;letter-spacing:2px;text-transform:uppercase;">Booking Reference</p>
                    <p style="font-family:'Syne',sans-serif;font-size:24px;color:#ef4444;font-weight:800;letter-spacing:3px;margin:8px 0;">%s</p>
                    <p style="color:#9ca3af;font-size:14px;margin:0;">%s</p>
                    <div style="margin-top:24px;padding:20px;background:#0f0f17;border-radius:14px;border:1px solid #2a2a3e;">
                      <p style="color:#9ca3af;font-size:14px;margin:0;">If you paid for this booking, a refund will be processed within 5–7 business days.</p>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td style="padding:0 40px 32px;text-align:center;">
                    <p style="color:#6b7280;font-size:12px;">© 2024 CineBook &nbsp;·&nbsp;
                      <a href="mailto:support@cinebook.com" style="color:#f97316;text-decoration:none;">support@cinebook.com</a>
                    </p>
                  </td>
                </tr>
              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.formatted(booking.getBookingReference(), booking.getShow().getMovie().getTitle());
    }
}
