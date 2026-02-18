package com.konit.stampzooaos.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun ZooImage(
    resourceName: String?, 
    contentDescription: String?, 
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val model = resourceName?.let { name ->
        // Try drawable resource first
        val context = androidx.compose.ui.platform.LocalContext.current
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (resId != 0) resId else null
    } ?: 0
    AsyncImage(
        model = if (model != 0) model else null,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}

