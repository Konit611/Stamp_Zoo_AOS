package com.konit.stampzooaos.feature.settings

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konit.stampzooaos.R
import com.konit.stampzooaos.core.localization.LanguageStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val langStore = LanguageStore(app)
    val language = langStore.languageFlow

    fun setLanguage(lang: String, activity: androidx.activity.ComponentActivity?) {
        viewModelScope.launch {
            langStore.setLanguage(lang, activity)
        }
    }
    
    fun getLanguageDisplayName(lang: String): String {
        return when (lang) {
            "ko" -> "한국어"
            "en" -> "English"
            "ja" -> "日本語"
            "zh" -> "中文"
            else -> "日本語"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel = viewModel(), 
    onBackClick: (() -> Unit)? = null,
    onLanguageClick: (() -> Unit)? = null,
    onAppInfoClick: (() -> Unit)? = null
) {
    val currentLanguage by vm.language.collectAsState(initial = "ja")
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.tab_settings),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF2F2F7),
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F7))
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 언어 설정
            SettingsItem(
                icon = Icons.Default.Settings,
                iconTint = Color(0xFF007AFF),
                title = stringResource(id = R.string.settings_language),
                subtitle = vm.getLanguageDisplayName(currentLanguage),
                onClick = { onLanguageClick?.invoke() }
            )
            
            // 앱 정보
            SettingsItem(
                icon = Icons.Default.Info,
                iconTint = Color(0xFF007AFF),
                title = stringResource(id = R.string.settings_app_info),
                subtitle = null,
                onClick = { onAppInfoClick?.invoke() }
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.padding(horizontal = 12.dp))
            
            // 타이틀
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            
            // 서브타이틀 (언어인 경우)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            }
            
            // 화살표
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    vm: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val currentLanguage by vm.language.collectAsState(initial = "ja")
    val activity = androidx.compose.ui.platform.LocalContext.current as? androidx.activity.ComponentActivity
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_language),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF2F2F7),
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F7))
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 언어 선택 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LanguageButton(
                    text = "한국어",
                    isSelected = currentLanguage == "ko",
                    onClick = { vm.setLanguage("ko", activity) },
                    modifier = Modifier.weight(1f)
                )
                LanguageButton(
                    text = "English",
                    isSelected = currentLanguage == "en",
                    onClick = { vm.setLanguage("en", activity) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LanguageButton(
                    text = "日本語",
                    isSelected = currentLanguage == "ja",
                    onClick = { vm.setLanguage("ja", activity) },
                    modifier = Modifier.weight(1f)
                )
                LanguageButton(
                    text = "中文",
                    isSelected = currentLanguage == "zh",
                    onClick = { vm.setLanguage("zh", activity) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun LanguageButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) 
                Color(0xFF007AFF)
            else 
                Color.White,
            contentColor = if (isSelected) 
                Color.White
            else 
                Color.Black
        ),
        border = if (!isSelected) 
            BorderStroke(1.dp, Color(0xFFE0E0E0))
        else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
