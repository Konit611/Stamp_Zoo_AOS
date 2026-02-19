package com.konit.stampzooaos.feature.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.LaunchedEffect
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
import com.konit.stampzooaos.data.Facility
import com.konit.stampzooaos.data.ZooRepository
import com.konit.stampzooaos.ui.theme.ZooBackground
import com.konit.stampzooaos.ui.theme.ZooPopGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExplorerAnimalsViewModel @Inject constructor(
    private val repo: ZooRepository
) : ViewModel() {
    private val _animals = MutableStateFlow<List<Animal>>(emptyList())
    val animals: StateFlow<List<Animal>> = _animals
    
    fun loadAnimals(facilityId: String) {
        viewModelScope.launch {
            val data = repo.loadZooData()
            _animals.value = data?.animals?.filter { it.facilityId == facilityId } ?: emptyList()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerAnimalsScreen(
    facility: Facility,
    vm: ExplorerAnimalsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val currentLanguage = getCurrentLanguage()
    val animals by vm.animals.collectAsState()
    val pagerState = rememberPagerState(pageCount = { animals.size })
    
    // 동물 목록 로드
    LaunchedEffect(facility.facilityId) {
        vm.loadAnimals(facility.facilityId)
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = facility.getLocalizedName(currentLanguage),
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
        if (animals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = padding.calculateTopPadding(),
                        start = 0.dp,
                        end = 0.dp
                    )
                    .background(ZooBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(id = R.string.loading_animals))
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = padding.calculateTopPadding(),
                        start = 0.dp,
                        end = 0.dp
                    )
                    .background(ZooBackground)
            ) { page ->
                AnimalDetailCard(animal = animals[page])
            }
        }
    }
}

@Composable
fun AnimalDetailCard(animal: Animal) {
    val currentLanguage = getCurrentLanguage()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
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
        
        Spacer(modifier = Modifier.height(100.dp)) // 탭바 공간
    }
}

