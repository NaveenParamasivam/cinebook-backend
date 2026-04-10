# 🎬 CineBook — Backend

> **Spring Boot REST API** for the CineBook Movie Ticket Booking System.  
> Handles authentication, movie management, seat booking, Razorpay payments, and email notifications.

---

## 🔗 Important Links

| Resource | URL |
|----------|-----|
| 🌐 **Live API (Base URL)** | `https://your-backend-url.onrender.com/api` |
| 🗄️ **Frontend Repo** | [cinebook-frontend](https://github.com/NaveenParamasivam/cinebook-frontend) |
| 🌍 **Frontend Live** | `https://your-frontend-url.vercel.app` |
| 📋 **Test Report** | [View on GitHub Pages](https://naveenparamasivam.github.io/cinebook-backend/) |

---

## 📸 Demo Screenshots

### API Health Check
> _Add a screenshot of `GET /api/movies` returning data_

<!-- ![API Response](docs/screenshots/api-health.png) -->

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.2.5 |
| Language | Java 17 |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security + JWT (JJWT 0.12.5) |
| Payment | Razorpay Java SDK |
| Email | Gmail SMTP via Spring Mail |
| Build | Maven |
| Testing | JUnit 5 + Mockito + AssertJ |
| Deployment | Render / Railway / AWS EC2 |

---

## 📁 Project Structure

```
cinebook-backend/
├── src/
│   ├── main/
│   │   ├── java/com/cinebook/
│   │   │   ├── config/          # SecurityConfig, DataInitializer (auto-creates admin)
│   │   │   ├── controller/      # REST controllers (7 controllers)
│   │   │   ├── dto/             # Request/Response DTOs
│   │   │   ├── entity/          # JPA Entities
│   │   │   ├── enums/           # Role, Genre, SeatStatus, BookingStatus, PaymentStatus
│   │   │   ├── exception/       # GlobalExceptionHandler + custom exceptions
│   │   │   ├── repository/      # Spring Data JPA Repositories
│   │   │   ├── security/        # JWT Filter, TokenProvider, UserDetailsService
│   │   │   └── service/impl/    # Business logic (7 services)
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/cinebook/
│           ├── service/
│           │   ├── MovieServiceTest.java   # 8 unit tests
│           │   └── SeatServiceTest.java    # 9 unit tests
│           └── controller/                # (integration tests)
├── .env.example
└── pom.xml
```

---

## 🔌 API Endpoints

> **Base URL:** `https://your-backend-url.onrender.com/api`  
> 🔓 = Public &nbsp;&nbsp; 🔑 = Requires JWT &nbsp;&nbsp; 🛡️ = Admin only

### Auth — `/auth`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | 🔓 | Register new user |
| POST | `/auth/login` | 🔓 | Login, returns JWT |
| POST | `/auth/register-admin` | 🔓 | Create admin (requires secret key) |

### Users — `/users`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/users/me` | 🔑 | Get logged-in user profile |
| PUT | `/users/me` | 🔑 | Update profile |
| PUT | `/users/me/password` | 🔑 | Change password |

### Movies — `/movies`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/movies` | 🔓 | Get all active movies |
| GET | `/movies/{id}` | 🔓 | Get movie by ID |
| GET | `/movies/genre/{genre}` | 🔓 | Filter by genre |
| GET | `/movies/search?q=` | 🔓 | Search movies |
| POST | `/movies` | 🛡️ | Create movie |
| PUT | `/movies/{id}` | 🛡️ | Update movie |
| DELETE | `/movies/{id}` | 🛡️ | Soft-delete movie |

### Theaters — `/theaters`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/theaters` | 🔓 | Get all active theaters |
| GET | `/theaters/{id}` | 🔓 | Get theater by ID |
| GET | `/theaters/city/{city}` | 🔓 | Get theaters by city |
| POST | `/theaters` | 🛡️ | Create theater |
| PUT | `/theaters/{id}` | 🛡️ | Update theater |
| DELETE | `/theaters/{id}` | 🛡️ | Soft-delete theater |

### Shows — `/shows`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/shows/movie/{movieId}?date=` | 🔓 | Shows for movie on date |
| GET | `/shows/movie/{movieId}/upcoming` | 🔓 | All upcoming shows for movie |
| GET | `/shows/movie/{movieId}/dates` | 🔓 | Available show dates for movie |
| GET | `/shows/{id}` | 🔓 | Get show by ID |
| POST | `/shows` | 🛡️ | Create show (auto-generates seats) |
| DELETE | `/shows/{id}` | 🛡️ | Remove show |

### Seats — `/seats`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/seats/show/{showId}` | 🔓 | Get all seats for a show |
| POST | `/seats/show/{showId}/lock` | 🔑 | Lock selected seats (10 min TTL) |

### Bookings — `/bookings`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/bookings/initiate` | 🔑 | Create Razorpay order + lock seats |
| POST | `/bookings/verify-payment` | 🔑 | Verify payment signature + confirm booking |
| GET | `/bookings/my` | 🔑 | Get user's booking history |
| GET | `/bookings/{id}` | 🔑 | Get booking by ID |
| POST | `/bookings/{id}/cancel` | 🔑 | Cancel a booking |

---

## 🧪 Test Results

### Running Tests
```bash
mvn test
```

### Test Report (GitHub Pages)
> 📊 [**View Full HTML Test Report →**](https://naveenparamasivam.github.io/cinebook-backend/)

<!-- Add a screenshot of the test report below -->
<!-- ![Test Report](docs/screenshots/test-report.png) -->

### Test Summary

| Test Class | Tests | Status |
|------------|-------|--------|
| `MovieServiceTest` | 8 | ✅ All Passing |
| `SeatServiceTest` | 9 | ✅ All Passing |
| **Total** | **17** | ✅ |

### Test Coverage by Layer

**MovieServiceTest** covers:
- ✅ Get all active movies
- ✅ Get movie by ID (found & not found)
- ✅ Create movie
- ✅ Update movie (partial + not found)
- ✅ Soft delete movie
- ✅ Search movies

**SeatServiceTest** covers:
- ✅ Lock available seats successfully
- ✅ Reject booking of already-booked seat
- ✅ Reject seat locked by another user
- ✅ Re-lock seat with expired lock
- ✅ Count mismatch throws exception
- ✅ Get seats sorted by row and number
- ✅ Expired locked seat shown as AVAILABLE
- ✅ Release seats back to AVAILABLE

---

## ⚙️ Local Setup

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+

### 1. Clone the repository
```bash
git clone https://github.com/NaveenParamasivam/cinebook-backend.git
cd cinebook-backend
```

### 2. Configure environment variables
```bash
cp .env.example .env
```
Edit `.env` with your values:
```env
DB_URL=jdbc:mysql://localhost:3306/cinebook?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=your_password

JWT_SECRET=your_very_long_random_secret_key_min_32_chars

MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_16_char_app_password

RAZORPAY_KEY_ID=rzp_test_xxxxxxxxxxxx
RAZORPAY_KEY_SECRET=your_razorpay_secret

ADMIN_EMAIL=admin@cinebook.com
ADMIN_PASSWORD=Admin@123
ADMIN_REGISTRATION_SECRET=your_admin_secret

CORS_ALLOWED_ORIGINS=http://localhost:5173
```

> 💡 For Gmail, generate an **App Password** at: Google Account → Security → 2-Step Verification → App Passwords

### 3. Start the server
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api`

### 4. Admin account
The default admin is **auto-created on first startup** using the `ADMIN_EMAIL` and `ADMIN_PASSWORD` from your `.env`. No manual DB steps needed.

---

## 🗄️ Database Schema

```
users ─────────────────────────────────────────────────── bookings
  id, email, password, first_name, last_name,               id, booking_reference, user_id, show_id,
  phone, role, enabled                                       seat_count, total_amount, booking_status,
                                                             payment_status, razorpay_order_id
movies ────────────────────────────────────────────────── shows
  id, title, description, genre, duration_minutes,           id, movie_id, theater_id,
  rating, imdb_rating, language, poster_url,                 show_date, show_time, ticket_price
  trailer_url, release_date, director, cast

theaters ─────────────────────────────────────────────── show_seats
  id, name, city, address, state, pincode,                   id, show_id, row_label, seat_number,
  phone, total_seats, total_rows, seats_per_row              status, booking_id, locked_until
```

---

## 🔐 Security

- **JWT Authentication** — stateless, token in `Authorization: Bearer <token>` header
- **Role-based Access** — `ROLE_USER` for customers, `ROLE_ADMIN` for management
- **Seat Locking** — 10-minute optimistic lock prevents double bookings; expired locks auto-released every 2 minutes via scheduler
- **Payment Verification** — Razorpay signature verified using HMAC-SHA256 with constant-time comparison
- **No hardcoded secrets** — all sensitive values in `.env` / environment variables

---

## 📦 Key Dependencies

```xml
Spring Boot 3.2.5
Spring Security + JWT (JJWT 0.12.5)
Spring Data JPA + MySQL Connector
Spring Mail (Gmail SMTP)
Razorpay Java SDK 1.4.3
Lombok + MapStruct
JUnit 5 + Mockito + AssertJ
```

---


## 👨‍💻 Author

**Naveen Paramasivam**  
GitHub: [@NaveenParamasivam](https://github.com/NaveenParamasivam)

---
