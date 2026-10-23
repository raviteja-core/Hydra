package com.raviteja.water_reminder.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// VibeMove Editorial Theme Palette
// Inspired by high-end modern editorial design
// ==========================================

// --- Dark Mode Palette (Primary Canvas) ---
val CharcoalBg = Color(0xFF141518)             // Deep matte charcoal background
val CharcoalSurface = Color(0xFF1D1F24)        // Layered surface card
val CharcoalSurfaceElevated = Color(0xFF262930)// Elevated cards, dock & modals
val CharcoalBorder = Color(0x1FFFFFFF)         // Delicate 12% white outline
val CharcoalBorderMedium = Color(0x33FFFFFF)   // 20% white outline for interactive elements

// --- Accent Colors ---
val WarmCoral = Color(0xFFFF6955)              // Primary accent: Vibrant terracotta coral
val WarmCoralLight = Color(0xFFFF8975)         // Light coral for gradients & highlights
val WarmCoralMuted = Color(0xFFD95340)         // Darker coral for active borders
val WarmCoralSubtle = Color(0x29FF6955)        // 16% coral container tint

val SageGreen = Color(0xFF384E40)              // Editorial secondary card surface
val SageGreenLight = Color(0xFF86A391)         // Sage typography & icon accent
val SageGreenSubtle = Color(0x29384E40)        // Subtle sage tint

val EditorialNavy = Color(0xFF253444)          // Cool slate navy for water balance
val EditorialNavyLight = Color(0xFF7597B8)

// --- Text & Foreground ---
val TextCream = Color(0xFFF5F2EB)              // Warm off-white primary text
val TextMuted = Color(0xFF9E9EA3)              // Soft silver-gray secondary text
val TextFaint = Color(0xFF686A72)              // Metadata & disabled state

// --- Light Mode Palette (Editorial Print) ---
val PaperLightBg = Color(0xFFF7F5F0)           // Warm archival paper background
val PaperLightSurface = Color(0xFFFFFFFF)      // Pure white surface card
val PaperLightSurfaceElevated = Color(0xFFEFECE5) // Soft neutral card
val PaperLightBorder = Color(0xFFE0DDD5)       // Subtle paper border
val TextDark = Color(0xFF17181A)               // Charcoal high-contrast text
val TextDarkMuted = Color(0xFF6C6E75)          // Muted dark secondary text

// Legacy aliases for backward compatibility where needed
val HydraBg = CharcoalBg
val HydraCard = CharcoalSurface
val HydraPrimary = WarmCoral
val HydraSecondary = SageGreenLight
val HydraTertiary = EditorialNavyLight
val HydraSurface = CharcoalSurfaceElevated
val HydraOnSurface = TextCream
val HydraTextMuted = TextMuted

