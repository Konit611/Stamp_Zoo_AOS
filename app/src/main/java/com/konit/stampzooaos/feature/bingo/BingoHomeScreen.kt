package com.konit.stampzooaos.feature.bingo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.konit.stampzooaos.R
import com.konit.stampzooaos.core.localization.getCurrentLanguage
import com.konit.stampzooaos.core.localization.getLocalizedName
import com.konit.stampzooaos.core.ui.ZooImage
import com.konit.stampzooaos.data.Animal
import com.konit.stampzooaos.data.BingoCard
import com.konit.stampzooaos.data.ZooRepository
import com.konit.stampzooaos.data.local.entity.BingoAnimalEntity
import com.konit.stampzooaos.ui.theme.ZooBackground
import com.konit.stampzooaos.ui.theme.ZooPointBlack
import com.konit.stampzooaos.ui.theme.ZooPopGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StampSlot(
    val bingoNumber: Int,
    val animal: Animal?,
    val isCollected: Boolean
)

@HiltViewModel
class BingoHomeViewModel @Inject constructor(
    private val repo: ZooRepository
) : ViewModel() {
    private val _bingo = MutableStateFlow<BingoCard?>(null)
    val bingo: StateFlow<BingoCard?> = _bingo
    
    private val _stampSlots = MutableStateFlow<List<StampSlot>>(emptyList())
    val stampSlots: StateFlow<List<StampSlot>> = _stampSlots
    
    private val _collectedCount = MutableStateFlow(0)
    val collectedCount: StateFlow<Int> = _collectedCount
    
    private val zooData by lazy { repo.loadZooData() }

    init {
        val data = repo.loadZooData()
        _bingo.value = data.bingoCards.firstOrNull { it.isActive }
        
        // 수집된 스탬프 로드
        viewModelScope.launch {
            repo.getAllBingoAnimals().collect { bingoAnimals ->
                updateStampSlots(bingoAnimals)
            }
        }
        
        // 수집 카운트 로드
        viewModelScope.launch {
            repo.getCollectedCount().collect { count ->
                _collectedCount.value = count
            }
        }
    }
    
    private fun updateStampSlots(bingoAnimals: List<BingoAnimalEntity>) {
        val slots = (1..9).map { number ->
            val bingoAnimal = bingoAnimals.find { it.bingoNumber == number }
            val animal = bingoAnimal?.let { ba ->
                zooData.animals.find { it.id == ba.animalId }
            }
            StampSlot(
                bingoNumber = number,
                animal = animal,
                isCollected = bingoAnimal != null
            )
        }
        _stampSlots.value = slots
    }
    
    fun getStampSlot(index: Int): StampSlot? {
        return _stampSlots.value.getOrNull(index)
    }
    
    fun isCompleted(): Boolean {
        return _collectedCount.value >= 9
    }
    
    fun getProgressRate(): Float {
        return _collectedCount.value / 9f
    }
}


@Composable
fun BingoHomeScreen(
    vm: BingoHomeViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit = {},
    onDetailClick: () -> Unit = {},
    onQRClick: () -> Unit = {},
    onAppInfoClick: () -> Unit = {}
) {
    val bingo by vm.bingo.collectAsState()
    val stampSlots by vm.stampSlots.collectAsState()
    val collectedCount by vm.collectedCount.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZooBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 상단 헤더
            HeaderSection(
                onAppInfoClick = onAppInfoClick,
                onSettingsClick = onSettingsClick
            )

            // 메인 컨텐츠
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // 제목
                TitleSection(onDetailClick = onDetailClick)

                Spacer(modifier = Modifier.height(16.dp))

                // 스탬프 그리드
                StampGrid(stampSlots = stampSlots)

                Spacer(modifier = Modifier.height(16.dp))

                // QR 버튼
                QRButton(onClick = onQRClick)

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// 상단 헤더
@Composable
fun HeaderSection(
    onAppInfoClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 앱 로고
        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onAppInfoClick() },
            contentAlignment = Alignment.Center
        ) {
            ZooImage(
                resourceName = "app_logo",
                contentDescription = "App Logo",
                modifier = Modifier.size(45.dp)
            )
        }

        // 설정 아이콘
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// 제목 섹션
@Composable
fun TitleSection(onDetailClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.bingo_home_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            lineHeight = 40.sp
        )

        // 화살표 버튼
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Black, shape = CircleShape)
                .clickable { onDetailClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Detail",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// 스탬프 그리드
@Composable
fun StampGrid(stampSlots: List<StampSlot>) {
    Column(
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
                        StampCard(
                            slot = stampSlots[index],
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// QR 버튼
@Composable
fun QRButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ZooPopGreen,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(25.dp)
    ) {
        Text(
            text = stringResource(id = R.string.bingo_qr_button),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// 스탬프 카드
@Composable
fun StampCard(slot: StampSlot, modifier: Modifier = Modifier) {
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