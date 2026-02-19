package com.konit.stampzooaos.ui.navigation

import kotlinx.serialization.Serializable

// Top-level destinations
@Serializable object BingoHomeRoute
@Serializable object ExplorerRoute
@Serializable object FieldGuideRoute

// Detail routes
@Serializable data class ExplorerDetailRoute(val facilityId: String)
@Serializable data class ExplorerAnimalsRoute(val facilityId: String)
@Serializable data class FieldGuideDetailRoute(val animalId: String)

// Other routes
@Serializable object SettingsRoute
@Serializable object LanguageSelectionRoute
@Serializable object AppInfoRoute
@Serializable object BingoDetailRoute
@Serializable object ScannerRoute
