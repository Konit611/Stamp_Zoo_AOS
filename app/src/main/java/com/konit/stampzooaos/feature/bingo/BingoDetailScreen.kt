package com.konit.stampzooaos.feature.bingo

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.konit.stampzooaos.R
import com.konit.stampzooaos.core.config.AppLinks
import com.konit.stampzooaos.core.localization.getCurrentLanguage
import com.konit.stampzooaos.core.localization.getLocalizedName
import com.konit.stampzooaos.core.ui.ZooImage
import com.konit.stampzooaos.ui.theme.ZooBackground
import com.konit.stampzooaos.ui.theme.ZooPointBlack
import com.konit.stampzooaos.ui.theme.ZooPopGreen
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoDetailScreen(
    vm: BingoHomeViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val stampSlots by vm.stampSlots.collectAsState()
    val prizeApplied by vm.prizeApplied.collectAsState()
    val context = LocalContext.current

    var showApplyConfirm by remember { mutableStateOf(false) }
    var showComingSoon by remember { mutableStateOf(false) }
    var showUnlockPrompt by remember { mutableStateOf(false) }
    var unlockInput by remember { mutableStateOf("") }
    var showUnlockFailed by remember { mutableStateOf(false) }

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
                    containerColor = ZooBackground,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ZooBackground)
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
                    Row(
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

            // 경품 응모 섹션
            PrizeApplicationSection(
                isApplied = prizeApplied,
                onApplyClick = { showApplyConfirm = true },
                onUnlockLongPress = {
                    unlockInput = ""
                    showUnlockPrompt = true
                }
            )

            // 하단 여백 추가 (탭바 높이 고려)
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // 응모 확인 다이얼로그
    if (showApplyConfirm) {
        AlertDialog(
            onDismissRequest = { showApplyConfirm = false },
            title = { Text(stringResource(id = R.string.prize_confirm_title)) },
            text = { Text(stringResource(id = R.string.prize_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showApplyConfirm = false
                    // URL이 있으면 열고 응모 완료 저장, 없으면 준비 중 안내
                    val uri = AppLinks.url(AppLinks.PRIZE_APPLICATION)
                    if (uri != null) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        vm.applyPrize()
                    } else {
                        showComingSoon = true
                    }
                }) {
                    Text(stringResource(id = R.string.prize_apply_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyConfirm = false }) {
                    Text(stringResource(id = R.string.dialog_cancel))
                }
            }
        )
    }

    // 응모 URL 준비 중 다이얼로그
    if (showComingSoon) {
        AlertDialog(
            onDismissRequest = { showComingSoon = false },
            title = { Text(stringResource(id = R.string.coming_soon_title)) },
            text = { Text(stringResource(id = R.string.prize_coming_message)) },
            confirmButton = {
                TextButton(onClick = { showComingSoon = false }) {
                    Text(stringResource(id = R.string.dialog_ok))
                }
            }
        )
    }

    // 숨김 해제용 비밀번호 입력 다이얼로그
    if (showUnlockPrompt) {
        AlertDialog(
            onDismissRequest = {
                showUnlockPrompt = false
                unlockInput = ""
            },
            title = { Text(stringResource(id = R.string.prize_unlock_title)) },
            text = {
                Column {
                    Text(stringResource(id = R.string.prize_unlock_message))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = unlockInput,
                        onValueChange = { unlockInput = it },
                        label = { Text(stringResource(id = R.string.prize_unlock_password)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val success = vm.unlockPrize(unlockInput)
                    showUnlockPrompt = false
                    unlockInput = ""
                    if (!success) showUnlockFailed = true
                }) {
                    Text(stringResource(id = R.string.prize_unlock_action))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUnlockPrompt = false
                    unlockInput = ""
                }) {
                    Text(stringResource(id = R.string.dialog_cancel))
                }
            }
        )
    }

    // 해제 실패 다이얼로그
    if (showUnlockFailed) {
        AlertDialog(
            onDismissRequest = { showUnlockFailed = false },
            title = { Text(stringResource(id = R.string.prize_unlock_failed_title)) },
            text = { Text(stringResource(id = R.string.prize_unlock_failed_message)) },
            confirmButton = {
                TextButton(onClick = { showUnlockFailed = false }) {
                    Text(stringResource(id = R.string.dialog_ok))
                }
            }
        )
    }
}

// 경품 응모 섹션. 미응모: 활성 버튼. 응모 완료: 비활성 + 5초 롱프레스 시 해제.
@Composable
private fun PrizeApplicationSection(
    isApplied: Boolean,
    onApplyClick: () -> Unit,
    onUnlockLongPress: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        if (isApplied) {
            // 응모 완료 (비활성). 숨김 기능: 5초 길게 누르면 해제 프롬프트.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.Gray)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            // 5초 안에 손을 떼지 않으면(타임아웃) 해제 트리거
                            val up = withTimeoutOrNull(5000L) { waitForUpOrCancellation() }
                            if (up == null) onUnlockLongPress()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.prize_applied),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        } else {
            // 미응모: 경품 응모 버튼
            Button(
                onClick = onApplyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ZooPopGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.prize_apply),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
            .background(ZooPointBlack),
        contentAlignment = Alignment.Center
    ) {
        if (slot.isCollected && slot.animal != null) {
            // 수집된 스탬프 이미지
            ZooImage(
                resourceName = slot.animal.stampImage,
                contentDescription = slot.animal.getLocalizedName(currentLanguage),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(15.dp))
            )
        }
    }
}

