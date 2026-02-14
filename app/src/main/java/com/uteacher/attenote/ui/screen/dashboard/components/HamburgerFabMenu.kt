package com.uteacher.attenote.ui.screen.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun HamburgerFabMenu(
    expanded: Boolean,
    alignLeft: Boolean,
    onToggle: () -> Unit,
    onCreateClass: () -> Unit,
    onManageClasses: () -> Unit,
    onManageStudents: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val columnAlignment = if (alignLeft) Alignment.Start else Alignment.End
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        horizontalAlignment = columnAlignment,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = columnAlignment,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FabMenuItem(text = "Create Class", onClick = onCreateClass, alignLeft = alignLeft)
                FabMenuItem(text = "Edit Class", onClick = onManageClasses, alignLeft = alignLeft)
                FabMenuItem(text = "Manage Students", onClick = onManageStudents, alignLeft = alignLeft)
                FabMenuItem(text = "Settings", onClick = onSettings, alignLeft = alignLeft)
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = "Menu",
                modifier = Modifier.graphicsLayer(rotationZ = if (expanded) 90f else 0f)
            )
        }
    }
}

@Composable
private fun FabMenuItem(
    text: String,
    onClick: () -> Unit,
    alignLeft: Boolean,
    modifier: Modifier = Modifier
) {
    if (alignLeft) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallFloatingActionButton(
                onClick = onClick,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(text = "+")
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            SmallFloatingActionButton(
                onClick = onClick,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(text = "+")
            }
        }
    }
}
