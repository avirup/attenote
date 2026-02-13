package com.uteacher.attendancetracker.ui.screen.manageclass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uteacher.attendancetracker.ui.screen.manageclass.components.ClassCard
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun ManageClassListScreen(
    onNavigateToEditClass: (classId: Long) -> Unit,
    viewModel: ManageClassListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.error) {
        if (!uiState.error.isNullOrBlank()) {
            delay(2500)
            viewModel.onErrorShown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.classes.isEmpty() -> {
                Text(
                    text = "No classes found.\nCreate a class from the Dashboard.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.classes,
                        key = { classItem -> classItem.classId }
                    ) { classItem ->
                        ClassCard(
                            classItem = classItem,
                            onOpenCloseToggled = { isOpen ->
                                viewModel.onOpenClosedToggled(classItem.classId, isOpen)
                            },
                            onEditClicked = {
                                onNavigateToEditClass(classItem.classId)
                            }
                        )
                    }
                }
            }
        }

        if (!uiState.error.isNullOrBlank()) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(text = uiState.error!!)
            }
        }
    }
}
