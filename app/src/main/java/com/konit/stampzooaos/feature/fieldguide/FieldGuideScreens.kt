package com.konit.stampzooaos.feature.fieldguide

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.konit.stampzooaos.core.localization.getLocalizedDetail
import com.konit.stampzooaos.core.ui.ZooImage
import com.konit.stampzooaos.data.Animal
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

data class AnimalGuideSlot(
    val index: Int,
    val animal: Animal?,
    val isCollected: Boolean
)

@HiltViewModel
class FieldGuideViewModel @Inject constructor(
    private val repo: ZooRepository
) : ViewModel() {
    
    private val _collectedAnimals = MutableStateFlow<List<Animal>>(emptyList())
    val collectedAnimals: StateFlow<List<Animal>> = _collectedAnimals
    
    private val _guideSlots = MutableStateFlow<List<AnimalGuideSlot>>(emptyList())
    val guideSlots: StateFlow<List<AnimalGuideSlot>> = _guideSlots
    
    private val zooData by lazy { repo.loadZooData() }

    val allAnimals: List<Animal>
        get() = zooData.animals

    init {
        // 수집된 동물 로드
        viewModelScope.launch {
            repo.getAllBingoAnimals().collect { bingoAnimals ->
                updateCollectedAnimals(bingoAnimals)
            }
        }
    }
    
    private fun updateCollectedAnimals(bingoAnimals: List<BingoAnimalEntity>) {
        val collectedAnimalIds = bingoAnimals.map { it.animalId }.toSet()
        val collected = zooData.animals.filter { it.id in collectedAnimalIds }
        _collectedAnimals.value = collected
        
        // 도감 슬롯 계산 (수집된 동물 + 여유분, 3의 배수로 맞춤)
        val collectedCount = collected.size
        val minSlots = 12
        val slotsNeeded = maxOf(collectedCount + 6, minSlots)
        val roundedSlots = ((slotsNeeded + 2) / 3) * 3
        
        val slots = (0 until roundedSlots).map { index ->
            val animal = collected.getOrNull(index)
            AnimalGuideSlot(
                index = index,
                animal = animal,
                isCollected = animal != null
            )
        }
        _guideSlots.value = slots
    }
}

@Composable
fun FieldGuideList(onClick: (Animal) -> Unit = {}, vm: FieldGuideViewModel = hiltViewModel()) {
    val currentLanguage = getCurrentLanguage()
    val guideSlots by vm.guideSlots.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZooBackground)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 100.dp // 탭바 공간
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 제목 섹션
            item(span = { GridItemSpan(3) }) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.fieldguide_collected),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.fieldguide_animal_guide),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            
            // 도감 격자
            items(guideSlots.size) { index ->
                AnimalGuideCell(
                    slot = guideSlots[index],
                    onClick = {
                        guideSlots[index].animal?.let { onClick(it) }
                    }
                )
            }
        }
    }
}

@Composable
fun AnimalGuideCell(
    slot: AnimalGuideSlot,
    onClick: () -> Unit
) {
    val currentLanguage = getCurrentLanguage()
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(15.dp))
            .background(ZooPointBlack)
            .clickable(enabled = slot.isCollected, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (slot.animal != null) {
            // 수집된 동물 - 스탬프 이미지 표시
            ZooImage(
                resourceName = slot.animal.stampImage,
                contentDescription = slot.animal.getLocalizedName(currentLanguage),
                modifier = Modifier.fillMaxSize()
            )
        }
        // 빈 슬롯은 검은 배경만 표시
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldGuideDetail(animal: Animal, onBackClick: (() -> Unit)? = null) {
    val currentLanguage = getCurrentLanguage()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = animal.getLocalizedName(currentLanguage),
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
            
            // 카드 컨테이너
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ZooPopGreen.copy(alpha = 0.3f))
            ) {
                // 동물 이미지
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Blue.copy(alpha = 0.7f))
                ) {
                    ZooImage(
                        resourceName = animal.image,
                        contentDescription = animal.getLocalizedName(currentLanguage),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // 동물 정보
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // 동물 이름
                    Text(
                        text = animal.getLocalizedName(currentLanguage),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 설명
                    Text(
                        text = animal.getLocalizedDetail(currentLanguage),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.Black.copy(alpha = 0.9f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 하단 여백 추가 (탭바 높이 고려)
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
