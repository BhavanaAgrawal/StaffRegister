# Project Plan

StaffRegister - A comprehensive Android application for managing staff records, attendance, and details.

## Project Brief

# StaffRegister Project Brief

A comprehensive Android application designed to streamline the management of staff records, attendance, and professional details. This MVP focuses on a clean, adaptive interface for efficient workforce administration and payroll processing.

## Features

*   **Staff Directory & Profiles:** A central hub to manage detailed staff records including names, specific job roles, contact information, and professional credentials.
*   **Attendance Tracking:** A streamlined system for recording daily check-ins and check-outs to monitor staff presence and punctuality.
*   **Salary Calculation:** An automated tool that calculates wages for staff members based on their attendance history and role-specific pay rates.
*   **Profile Photo Capture:** Integrated camera functionality to capture and update staff photos directly within the app for easy identification.

## High-Level Technical Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose with **Material Design 3** for a vibrant and modern aesthetic.
*   **Navigation:** **Jetpack Navigation 3** (state-driven) for robust app-wide navigation and state management.
*   **Adaptive Strategy:** **Compose Material Adaptive** library to ensure the UI scales seamlessly across phones, tablets, and foldables.
*   **Concurrency:** Kotlin Coroutines and Flow for handling background tasks and reactive UI updates.
*   **Media:** CameraX for managing high-quality profile image capture.
*   **Image Loading:** Coil for efficient asynchronous image display and caching.

## Implementation Steps

### Task_1_Foundation_And_Staff_Management: Set up the core architecture including Room database for Staff and Attendance, Jetpack Navigation 3, and the Material 3 theme. Implement the Staff Directory (List and Detail) screens.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Room database with Staff and Attendance entities is initialized
  - Navigation 3 structure is implemented
  - Material 3 theme with Edge-to-Edge support is configured
  - Staff List and Add/Edit screens are functional
  - Project builds successfully
- **StartTime:** 2026-09-18 01:37:27 IST

### Task_2_Attendance_Salary_And_Camera: Implement Attendance tracking (check-in/out), Salary calculation logic based on attendance, and CameraX integration for capturing staff profile photos.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Daily check-in and check-out recording works
  - Salary is calculated correctly based on attendance and pay rates
  - CameraX captures photos and saves them to internal storage
  - Coil is used to display staff photos
  - No crashes during data operations or photo capture

### Task_3_Adaptive_UI_And_Polishing: Apply Compose Material Adaptive for multi-pane layouts (phones, tablets, foldables). Refine the UI with a vibrant Material 3 color scheme and create an adaptive app icon.
- **Status:** PENDING
- **Acceptance Criteria:**
  - UI scales seamlessly using Adaptive Pane Scaffolds
  - Vibrant and energetic Material 3 color scheme applied
  - Adaptive app icon is implemented and visible
  - Full Edge-to-Edge display is verified across all screens

### Task_4_Run_And_Verify: Perform a final build and verification of the application to ensure it meets all requirements and is stable.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Application builds and runs without crashes
  - All existing tests pass
  - Staff management, attendance, and salary features work as expected
  - Critic agent confirms alignment with requirements and Material Design 3 guidelines

