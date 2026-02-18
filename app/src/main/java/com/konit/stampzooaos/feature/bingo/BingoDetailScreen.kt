package com.konit.stampzooaos.feature.bingo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konit.stampzooaos.R
import com.konit.stampzooaos.core.localization.getCurrentLanguage
import com.konit.stampzooaos.core.localization.getLocalizedName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoDetailScreen(
    vm: BingoHomeViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val stampSlots by vm.stampSlots.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(id = R.string.bingo_detail),
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
            Spacer(modifier = Modifier.height(20.dp))
            
            // 3x3 스탬프 그리드
            Column(
                modifier = Modifier.padding(horizontal = 30.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (row in 0..2) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0..2) {
                            val index = row * 3 + col
                            if (index < stampSlots.size) {
                                BingoDetailStampCard(
                                    slot = stampSlots[index],
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 스탬프 랠리 정보
            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                // 제목
                Text(
                    text = stringResource(id = R.string.stamp_rally_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 32.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 설명
                Text(
                    text = stringResource(id = R.string.stamp_rally_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
            }
            
            // 하단 여백 추가 (탭바 높이 고려)
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun BingoDetailStampCard(slot: StampSlot, modifier: Modifier = Modifier) {
    val currentLanguage = getCurrentLanguage()
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(15.dp))
            .background(com.konit.stampzooaos.ui.theme.ZooPointBlack),
        contentAlignment = Alignment.Center
    ) {
        if (slot.isCollected && slot.animal != null) {
            // 수집된 스탬프 이미지
            com.konit.stampzooaos.core.ui.ZooImage(
                resourceName = slot.animal.stampImage,
                contentDescription = slot.animal.getLocalizedName(currentLanguage),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(15.dp))
            )
        }
    }
}

