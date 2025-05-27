# AIInspire

AIInspire is a modern Android application built with Kotlin and Jetpack Compose, designed to deliver daily AI-generated inspiration and to help me familiarize myself with integrating Google Gemini AI into mobile apps.

## Features

- Select your desired category from a curated list of categories and receive inspirational quotes for the selected category. 


## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Build System:** Gradle (Kotlin DSL)
- **Minimum SDK:** 24
- **Target SDK:** 35

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/AIInspire.git
   ```

2. Open the project in Android Studio.

3. Create a `secrets.properties` file in the root directory and add your API keys (if required):
   ```properties
   API_KEY=your_api_key_here
   ```

4. Sync the project with Gradle files.

5. Run the app on an emulator or physical device.

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/           # Kotlin source files
│   │   ├── res/           # Resources
│   │   └── AndroidManifest.xml
│   ├── test/              # Unit tests
│   └── androidTest/       # Instrumentation tests
```

## Building

To build the app, run:

```bash
./gradlew assembleDebug
```

For release build:

```bash
./gradlew assembleRelease
```

## Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumentation tests:
```bash
./gradlew connectedAndroidTest
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request



## Acknowledgments

- Google Gemini SDK for AI generated quotes using prompts. 
