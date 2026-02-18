package com.konit.stampzooaos.feature.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konit.stampzooaos.R
import com.konit.stampzooaos.core.localization.getCurrentLanguage
import com.konit.stampzooaos.core.localization.getLocalizedName
import com.konit.stampzooaos.core.localization.getLocalizedDetail
import com.konit.stampzooaos.data.Facility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerDetailScreen(
    facility: Facility,
    onBackClick: (() -> Unit)? = null,
    onAnimalsClick: (() -> Unit)? = null
) {
    val currentLanguage = getCurrentLanguage()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
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
                actions = {
                    if (onAnimalsClick != null) {
                        Button(
                            onClick = onAnimalsClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00C853),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = R.string.see_animals),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F7))
                .padding(
                    top = padding.calculateTopPadding(),
                    start = 0.dp,
                    end = 0.dp
                    // bottom padding 제거
                )
                .verticalScroll(rememberScrollState())
        ) {
            // 헤더 이미지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Green.copy(alpha = 0.6f))
            ) {
                if (facility.image != null) {
                    com.konit.stampzooaos.core.ui.ZooImage(
                        resourceName = facility.image,
                        contentDescription = facility.getLocalizedName(currentLanguage),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 정보 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // 제목과 로고
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 시설 이름 (길이에 따라 폰트 크기 조정)
                    val facilityName = facility.getLocalizedName(currentLanguage)
                    val fontSize = when {
                        // 사케노후루사토치토세와 노보리베츠는 더 큰 폰트 크기 적용
                        facility.facilityId == "chitose" || facility.facilityId == "noboribetsu" -> {
                            when {
                                facilityName.length > 12 -> 24.sp
                                facilityName.length > 8 -> 22.sp
                                else -> MaterialTheme.typography.headlineMedium.fontSize
                            }
                        }
                        // 일반적인 경우
                        facilityName.length > 12 -> 22.sp  // 매우 긴 경우
                        facilityName.length > 8 -> 20.sp  // 긴 경우
                        else -> MaterialTheme.typography.headlineMedium.fontSize  // 기본
                    }
                    
                    Text(
                        text = facilityName,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        lineHeight = fontSize * 1.2f
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // 로고 이미지 (null이 아니고 빈 문자열이 아닐 때만 표시)
                    if (!facility.logoImage.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            com.konit.stampzooaos.core.ui.ZooImage(
                                resourceName = facility.logoImage,
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 설명
                Text(
                    text = facility.getLocalizedDetail(currentLanguage),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
            }
            
            // 하단 여백 추가 (탭바 높이 고려)
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
