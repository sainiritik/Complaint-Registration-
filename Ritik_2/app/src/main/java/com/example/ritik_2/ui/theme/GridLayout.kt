package com.example.ritik_2.ui.theme

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ritik_2.R

@Composable
fun GridLayout(
    items: List<Pair<Int, String>>,
    onCardClick: (Int) -> Unit
) {
    val cardColors = mapOf(
        1 to Color(0xFFFFA726), // Orange
        2 to Color(0xFF42A5F5), // Blue
        3 to Color(0xFF66BB6A), // Green
        4 to Color(0xFFEF5350)  // Red
    )

    val cardIcons = mapOf(
        1 to R.drawable.ic_baseline_report_24,
        2 to R.drawable.ic_baseline_visibility_24,
        3 to R.drawable.ic_baseline_settings_24,
        4 to R.drawable.ic_baseline_help_24
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp) // More spacing for better UI
            ) {
                rowItems.forEach { (id, label) ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onCardClick(id) }
                            .padding(8.dp)
                            .animateContentSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = cardColors[id] ?: Color.Gray
                        ),
                        elevation = CardDefaults.elevatedCardElevation(8.dp) // Slightly increased for better shadow
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = cardIcons[id] ?: R.drawable.ic_baseline_assignment_24),
                                contentDescription = label,
                                tint = Color.White,
                                modifier = Modifier.size(56.dp) // Increased icon size
                            )
                            Spacer(modifier = Modifier.height(16.dp)) // More spacing for better separation
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                    }
                }
                // ✅ Fix uneven item layout
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGridLayout() {
    Ritik_2Theme {
        val cardItems = listOf(
            Pair(1, "Register a Complaint"),
            Pair(2, "View Complaints"),
            Pair(3, "Settings"),
            Pair(4, "Help & Support")
        )
        GridLayout(items = cardItems, onCardClick = {})
    }
}
