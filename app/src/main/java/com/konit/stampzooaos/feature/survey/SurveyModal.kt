package com.konit.stampzooaos.feature.survey

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.konit.stampzooaos.R
import com.konit.stampzooaos.core.config.AppLinks
import com.konit.stampzooaos.ui.theme.ZooBackground
import com.konit.stampzooaos.ui.theme.ZooPopGreen

/**
 * 설문(アンケート) 안내 모달. 구글폼으로 연결한다. iOS SurveyView 대응.
 * URL이 비어있으면 "준비 중" 토스트.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyModal(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val comingSoonMsg = stringResource(id = R.string.survey_coming_message)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ZooBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                tint = ZooPopGreen,
                modifier = Modifier.size(56.dp)
            )

            Text(
                text = stringResource(id = R.string.survey_title),
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
            )

            Text(
                text = stringResource(id = R.string.survey_body),
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val uri = AppLinks.url(AppLinks.SURVEY_FORM)
                    if (uri != null) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        onDismiss()
                    } else {
                        Toast.makeText(context, comingSoonMsg, Toast.LENGTH_SHORT).show()
                    }
                },
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
                    text = stringResource(id = R.string.survey_take),
                    fontWeight = FontWeight.SemiBold,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
