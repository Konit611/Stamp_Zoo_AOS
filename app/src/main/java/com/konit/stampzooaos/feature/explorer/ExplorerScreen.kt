package com.konit.stampzooaos.feature.explorer

import android.app.Application
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.konit.stampzooaos.R
import com.konit.stampzooaos.core.localization.getCurrentLanguage
import com.konit.stampzooaos.core.localization.getLocalizedName
import com.konit.stampzooaos.core.localization.getLocalizedLocation
import com.konit.stampzooaos.data.Facility
import com.konit.stampzooaos.data.ZooRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ExplorerCategory {
    ALL, ZOO, AQUARIUM
}

class ExplorerViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ZooRepository(app)
    private val _facilities = MutableStateFlow<List<Facility>>(emptyList())
    val facilities: StateFlow<List<Facility>> = _facilities
    
    private val _selectedCategory = MutableStateFlow(ExplorerCategory.ALL)
    val selectedCategory: StateFlow<ExplorerCategory> = _selectedCategory
    
    init {
        viewModelScope.launch {
            val data = repo.loadZooData()
            _facilities.value = data?.facilities ?: emptyList()
        }
    }
    
    fun setCategory(category: ExplorerCategory) {
        _selectedCategory.value = category
    }
    
    fun getFilteredFacilities(): List<Facility> {
        val all = _facilities.value
        return when (_selectedCategory.value) {
            ExplorerCategory.ALL -> all
            ExplorerCategory.ZOO -> all.filter { it.type == "zoo" }
            ExplorerCategory.AQUARIUM -> all.filter { it.type == "aquarium" }
        }
    }
}

@Composable
fun ExplorerScreen(
    vm: ExplorerViewModel = viewModel(),
    navController: NavController
) {
    val selectedCategory by vm.selectedCategory.collectAsState()
    val filteredFacilities = vm.getFilteredFacilities()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
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
                        navController.navigate("explorer/detail/${facility.id}")
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
                if (selected) com.konit.stampzooaos.ui.theme.ZooPopGreen
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
                    com.konit.stampzooaos.core.ui.ZooImage(
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
                                if (isZoo) Color(0xFF00C853)
                                else com.konit.stampzooaos.ui.theme.ZooDeepBlue
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
