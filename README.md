CampusCon 🎓

CampusCon is a campus networking platform designed to connect students and societies within universities and colleges. It enables seamless registration, communication, event sharing, group chats, and community engagement, all in a secure and scalable ecosystem.

🚀 Features

Role-based Access → Separate registration/login flows for Students and Societies.

Authentication → Email-based OTP verification, JWT-secured login, and password management.

Messaging → Direct chats, group messaging, and WebSocket-powered real-time updates.

Bricks (Posts) → Create, save, and explore campus posts.

Events → Share and join university/college events.

Profile Management → Upload deeds, manage saved items, and view activity.

Settings & Notifications → Clean UI for managing preferences and alerts.

🏗️ Tech Stack
Frontend (Mobile)

Flutter

Dio (API Networking)

BLoC (State Management)

Backend

Spring Boot (Java)

JPA/Hibernate (Database)

WebSockets (Real-time Messaging)

Spring Security + JWT (Authentication)

Docker (Containerization)

Database & Infra

PostgreSQL (Preferred DB)

AWS / Local Docker Setup

📂 Project Structure
Backend (/backend)
backend/
 ├── src/main/java/com/campuscon/
 │   ├── config/          # Configurations (CORS, Security, WebSocket, etc.)
 │   ├── controllers/     # REST Controllers
 │   ├── services/        # Business Logic
 │   ├── repositories/    # JPA Repositories
 │   ├── models/          # Entity Models
 │   ├── dtos/            # Data Transfer Objects
 │   ├── exceptions/      # Custom Exception Handling
 │   ├── utils/           # Utility Classes
 │   └── CampusconApplication.java
 ├── resources/
 │   ├── application.properties
 │   └── static/
 └── pom.xml

Frontend (/frontend)
frontend/
 ├── lib/
 │   ├── blocs/        # State Management
 │   ├── screens/      # UI Screens
 │   ├── services/     # Dio API Calls
 │   ├── models/       # Data Models
 │   └── main.dart
 ├── pubspec.yaml

⚡ Getting Started
Prerequisites

Java 17+

Maven 3.9+

PostgreSQL 14+

Flutter SDK 3+

Docker (optional, for containerized setup)

Backend Setup
# Navigate to backend folder
cd backend

# Build the project
mvn clean install

# Run the server
mvn spring-boot:run


Server will start at → http://localhost:8080

Frontend Setup
# Navigate to frontend folder
cd frontend

# Install dependencies
flutter pub get

# Run the app
flutter run

🔑 Environment Variables

Create an .env (or update application.properties) with:

DB_URL=jdbc:postgresql://localhost:5432/campuscon
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_pass
JWT_SECRET=your_secret_key
EMAIL_HOST=smtp.example.com
EMAIL_PORT=587
EMAIL_USERNAME=your_email
EMAIL_PASSWORD=your_password

🧪 API Testing

You can test endpoints via:

Postman Collection (to be added)

Swagger UI → http://localhost:8080/swagger-ui.html

📌 Roadmap

 AI Moderation for messages/posts

 Push Notifications

 Advanced Event Management

 Multi-college networking

🤝 Contributing

We welcome contributions!

Fork the repo

Create a feature branch (git checkout -b feature-x)

Commit your changes

Push and create a PR

📜 License

CampusCon is licensed under the MIT License.
