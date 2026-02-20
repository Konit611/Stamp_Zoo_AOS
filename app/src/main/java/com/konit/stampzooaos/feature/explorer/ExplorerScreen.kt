package com.konit.stampzooaos.feature.explorer

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
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
import androidx.navigation.NavController
import com.konit.stampzooaos.ui.navigation.ExplorerDetailRoute
import com.konit.stampzooaos.R
import com.konit.stampzooaos.core.localization.getCurrentLanguage
import com.konit.stampzooaos.core.localization.getLocalizedName
import com.konit.stampzooaos.core.localization.getLocalizedLocation
import com.konit.stampzooaos.core.ui.ZooImage
import com.konit.stampzooaos.data.Facility
import com.konit.stampzooaos.data.ZooRepository
import com.konit.stampzooaos.ui.theme.ZooAccentGreen
import com.konit.stampzooaos.ui.theme.ZooBackground
import com.konit.stampzooaos.ui.theme.ZooDeepBlue
import com.konit.stampzooaos.ui.theme.ZooPopGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ExplorerCategory {
    ALL, ZOO, AQUARIUM
}

@HiltViewModel
class ExplorerViewModel @Inject constructor(
    private val repo: ZooRepository
) : ViewModel() {
    private val _facilities = MutableStateFlow<List<Facility>>(emptyList())
    val facilities: StateFlow<List<Facility>> = _facilities

    private val _selectedCategory = MutableStateFlow(ExplorerCategory.ALL)
    val selectedCategory: StateFlow<ExplorerCategory> = _selectedCategory

    private val _collectedAnimalIds = MutableStateFlow<Set<String>>(emptySet())
    val collectedAnimalIds: StateFlow<Set<String>> = _collectedAnimalIds

    val filteredFacilities: StateFlow<List<Facility>> = combine(
        _facilities, _selectedCategory
    ) { all, category ->
        when (category) {
            ExplorerCategory.ALL -> all
            ExplorerCategory.ZOO -> all.filter { it.type == "zoo" }
            ExplorerCategory.AQUARIUM -> all.filter { it.type == "aquarium" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repo.loadZooData()
            _facilities.value = data.facilities
        }
        viewModelScope.launch {
            repo.getCollectedAnimalIds().collect { ids ->
                _collectedAnimalIds.value = ids.toSet()
            }
        }
    }

    fun setCategory(category: ExplorerCategory) {
        _selectedCategory.value = category
    }
}

@Composable
fun ExplorerScreen(
    vm: ExplorerViewModel = hiltViewModel(),
    navController: NavController
) {
    val selectedCategory by vm.selectedCategory.collectAsState()
    val filteredFacilities by vm.filteredFacilities.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZooBackground)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // 제목
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.explorer_participating),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.explorer_zoos_aquariums),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 카테고리 필터
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                text = stringResource(id = R.string.category_all),
                selected = selectedCategory == ExplorerCategory.ALL,
                onClick = { vm.setCategory(ExplorerCategory.ALL) }
            )
            CategoryChip(
                text = stringResource(id = R.string.category_zoo),
                selected = selectedCategory == ExplorerCategory.ZOO,
                onClick = { vm.setCategory(ExplorerCategory.ZOO) }
            )
            CategoryChip(
                text = stringResource(id = R.string.category_aquarium),
                selected = selectedCategory == ExplorerCategory.AQUARIUM,
                onClick = { vm.setCategory(ExplorerCategory.AQUARIUM) }
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 시설 그리드
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                count = filteredFacilities.size,
                key = { index -> filteredFacilities[index].id }
            ) { index ->
                val facility = filteredFacilities[index]
                FacilityCard(
                    facility = facility,
                    onClick = {
                        navController.navigate(ExplorerDetailRoute(facility.id))
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) ZooPopGreen
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else Color.Black
        )
    }
}

@Composable
fun FacilityCard(
    facility: Facility,
    onClick: () -> Unit
) {
    val isZoo = facility.type == "zoo"
    val currentLanguage = getCurrentLanguage()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 이미지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .padding(top = 12.dp, start = 12.dp, end = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.1f))
            ) {
                if (facility.image != null) {
                    ZooImage(
                        resourceName = facility.image,
                        contentDescription = facility.getLocalizedName(currentLanguage),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // 정보 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = facility.getLocalizedName(currentLanguage),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = facility.getLocalizedLocation(currentLanguage),
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // 화살표 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isZoo) ZooAccentGreen
                                else ZooDeepBlue
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Detail",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
