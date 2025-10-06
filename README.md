PantryChef 🍳
=============

PantryChef is a zero-waste cooking companion designed to simplify meal preparation, optimise shopping, and inspire creativity in the kitchen. Our vision is to make cooking accessible and enjoyable while helping users save money and reduce food waste. By blending intelligent recommendations with a user-first design, PantryChef transforms everyday ingredients into exciting meals.

* * * * *

Table of Contents
-----------------

- [About The Project](#about-the-project)
- [Key Features](#key-features)
- [Technology Stack & Architecture](#technology-stack--architecture)
- [Getting Started (For Developers)](#getting-started-for-developers)
  - [Prerequisites](#prerequisites)
  - [Setup Instructions](#setup-instructions)
- [Installation (For Users)](#installation-for-users)
- [Project Team](#project-team)
- [Acknowledgments](#acknowledgments)

* * * * *

About The Project
-----------------

PantryChef is a complete kitchen partner designed to simplify meal preparation, reduce food waste, and inspire confidence in the kitchen. By combining AI-driven intelligence with a user-friendly design, the app transforms pantry management into a proactive and enjoyable experience. ^5^Our design strategy is firmly grounded in research, analyzing competing apps to address real user needs while introducing new, innovative features to stand apart from existing solutions.

* * * * *

Key Features
------------

PantryChef is packed with features designed to create a seamless cooking journey, from pantry to plate.

### Core Features

-   **Single Sign-On (SSO) & Biometric Authentication:** Securely log in via Google Sign-In, email/password, or biometrics (fingerprint/face ID) for quick and frictionless access.

-   **User Settings Management:** A dedicated profile section to manage account details, update dietary preferences, customize notifications, and switch between light/dark themes.

-   **REST API Integration:** A robust Node.js backend handles all data operations, ensuring a scalable and secure connection between the app and the database.

-   **Offline Mode with Synchronization:** View and edit your pantry, recipes, and shopping lists even without an internet connection. Changes are automatically synced when you're back online.

-   **Real-Time Push Notifications:** Receive timely expiry alerts for pantry items and personalized recipe suggestions via Firebase Cloud Messaging.

-   **Multi-Language Support:** Full support for English, Afrikaans, and isiZulu to ensure an inclusive experience for a diverse audience.

### Innovative Features

-   🧠 **AI-Powered Recipe Discovery:** Generate personalized recipes by analyzing your pantry's contents using the SuperCook and Edamam APIs.

-   🥫 **Intelligent Pantry Management:** Add items by scanning with your camera (Vision AI), using voice input, or manual entry. The app tracks quantities and expiration dates to help prevent waste.

-   🛒 **Smart Shopping List Generator:** Automatically create shopping lists from recipes. The app intelligently subtracts items you already have and organizes the list by store layout.

-   🧑‍🍳 **Interactive Recipe Assistant:** An integrated Cohere-powered chatbot provides real-time cooking help, offering ingredient substitutions, technique explanations, and troubleshooting advice.

-   📅 **Advanced Meal Planning:** Plan your meals for the week or month using a calendar interface and generate a consolidated shopping list for the entire period.

-   📉 **Food Waste Prevention Dashboard:** Get "Priority Recipes" that use soon-to-expire ingredients and view reports on your food usage habits.

-   📲 **Recipe Sharing:** Easily export and share your favorite recipes with friends and family via WhatsApp, email, or other platforms.

* * * * *

Technology Stack & Architecture
-------------------------------

PantryChef is built on a modern, three-tier architecture for scalability and maintainability.

-   **Frontend:**

    -   **Platform:** Native Android

    -   **Language:** Kotlin

    -   **Key Libraries:** Android Views, Navigation Component, ViewModel, LiveData, Retrofit, Glide, Coroutines.

-   **Backend (Middleware):**

    -   **Framework:** Node.js with Express.js

    -   **Hosting:** Render

-   **Authentication:**

    -   **Provider:** Firebase Authentication (Email/Password, Google SSO)

-   **Storage:**

    -   **Database:** Firebase Realtime Database

    -   **Image Storage:** Supabase Storage

-   **External APIs:**

    -   **Recipe Search:** SuperCook API, Edamam API

    -   **AI Chatbot:** Cohere API

    -   **Image Recognition:** Vision AI (.NET Service)

* * * * *

Getting Started (For Developers)
--------------------------------

To get a local copy up and running, follow these simple steps.

### Prerequisites

-   Android Studio (latest stable version recommended)

-   Git

### Setup Instructions

1.  **Clone the repository:**

    Bash

    ```
    git clone https://github.com/PROG7314-POE-SSB/PROG7314-POE-Part-2-Android-App.git

    ```

2.  Open in Android Studio:

    Open the cloned folder as a new project in Android Studio.

3.  Firebase Configuration (CRITICAL):

    This app requires a Firebase project to function.

    -   Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.

    -   Add an Android app to your project with the package name `com.ssba.pantrychef`.

    -   In your Firebase project, go to **Authentication > Sign-in method** and enable **Email/Password** and **Google**.

    -   Go to **Realtime Database** and create a database.

    -   Download the `google-services.json` file from your Firebase project settings and place it in the `app/` directory of your Android Studio project.

4.  Supabase & Google SSO Configuration:

    The app needs API keys to connect to Supabase and to enable Google SSO.

    -   In `app/src/main/res/values/`, create a new file named `secrets.xml`.

    -   Add your keys to this file. **Note:** This file is listed in `.gitignore` and should not be committed to version control.

        XML

        ```
        <?xml version="1.0" encoding="utf-8"?>
        <resources>
            <string name="supabase_url">YOUR_SUPABASE_URL</string>
            <string name="supabase_api_key">YOUR_SUPABASE_ANON_KEY</string>

            <string name="default_web_client_id">YOUR_GOOGLE_WEB_CLIENT_ID</string>
        </resources>

        ```

    -   To enable Google Sign-In, you must add your computer's **SHA-1 fingerprint** to your Firebase project settings. Follow the instructions from the Firebase documentation to generate it.

5.  Build and Run:

    Sync the project with Gradle files and run the application on an emulator or a physical device.

* * * * *

Installation (For Users)
------------------------

If you just want to try the app, you can download the latest pre-built APK file.

1.  Go to the **[Releases](https://github.com/PROG7314-POE-SSB/PROG7314-POE-Part-2-Android-App/releases)** tab of this GitHub repository.

2.  Under the latest release, find the `app-debug.apk` file in the "Assets" section.

3.  Download the file and install it on your Android device.

* * * * *

Project Team
------------

This application was designed and developed by **SSB Digital (Group 2)**:

-   Sashveer Lakhan Ramjathan (ST10361554)

-   Shravan Ramjathan (ST10247982)

-   Blaise Mikka de Gier (ST10249838)

* * * * *

Acknowledgments
---------------

-   UI/UX design created with

    [Figma](https://www.figma.com/).

-   System diagrams created with

    [Excalidraw](https://excalidraw.com/).

-   Project planning managed with

    [TeamGantt](https://www.teamgantt.com/).

-   External APIs:

    [SuperCook](https://www.supercook.com/), [Edamam](https://www.edamam.com/), [Cohere](https://cohere.com/).

* * * * *