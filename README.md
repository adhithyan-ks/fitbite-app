# FitBite App

FitBite is a comprehensive Android application designed to empower users on their health and fitness journey. It provides tools for nutritional tracking, body metrics analysis, and personalized health advice.

## Features

- **Nutritional Insights**: Upload or scan food items to understand their nutritional value using AI-powered analysis.
- **BMI Calculator**: Quickly calculate and track your Body Mass Index (BMI).
- **AI-Powered Chat**: An interactive chat interface powered by Groq to get answers to your fitness and health queries.
- **Secure Authentication**: Robust user login and signup system with persistent session management via Supabase.
- **Profile Management**: Personalize your experience by managing your health profile and account security.
- **Daily Motivation**: Receive curated health tips directly on your dashboard.

## Technologies Used

- **Language**: Java
- **AI Integration**: [Groq API](https://groq.com/) for high-speed Llama-powered inference, used for food analysis and the health assistant chat.
- **Backend-as-a-Service**: [Supabase](https://supabase.com/) (Authentication and Data storage).
- **Networking**: [OkHttp](https://square.github.io/okhttp/) for efficient API requests.
- **JSON Handling**: [Gson](https://github.com/google/gson) for data serialization/deserialization.
- **UI Framework**: Android Material Components for a modern, responsive design.
- **Markdown Support**: [Markwon](https://github.com/noties/Markwon) for rendering rich text in chat.

## Architecture

The project follows a standard Android Activity-based architecture, organized into logical packages for maintainability:

- **`com.example.fitbiteapp.api`**: Contains the `SupabaseClient` and API communication logic.
- **AI Logic**: Integrated within `ChatActivity` and `FoodUploadActivity` using the Groq API for real-time intelligence.
- **Data Management**: `SessionManager` handles user sessions using SharedPreferences.
- **UI Components**: Custom adapters (like `ChatAdapter`) and models (`ChatMessage`) facilitate data display in lists and recyclers.

## Getting Started

1. **Prerequisites**: Android Studio Jellyfish or newer.
2. **Setup**:
   - Clone this repository.
   - Sync the project with Gradle files.
   - Configure your **Groq API Key** and **Supabase credentials** in the `config.java` file.
3. **Run**: Deploy the application to an emulator or a physical device running Android 7.0 (API 24) or higher.
