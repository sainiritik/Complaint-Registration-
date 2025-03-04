package com.example.ritik_2.ui.theme

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.ritik_2.R
import kotlinx.coroutines.delay

@Composable
fun RegisterComplainScreen(
    onSaveClick: (String, String, String) -> Unit,
    onResetClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    var complainText by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var selectedUrgency by remember { mutableStateOf("Normal") }
    var selectedCategory by remember { mutableStateOf("IT") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessPopup by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val black = colorResource(id = R.color.black)
    val grayMuted = colorResource(id = R.color.gray_muted)
    val offWhite = colorResource(id = R.color.off_white)
    val warmBeige = colorResource(id = R.color.warm_beige)
    val softTan = colorResource(id = R.color.soft_tan)
    val warmGold = colorResource(id = R.color.warm_gold)
    val brightOrange = colorResource(id = R.color.bright_orange)
    val deepOrange = colorResource(id = R.color.deep_orange)
    val boldRed = colorResource(id = R.color.bold_red)
    val emerald = colorResource(id = R.color.emerald)
    val platinum = colorResource(id = R.color.platinum)
    val bitterSweet = colorResource(id = R.color.bittersweet)
    val aero = colorResource(id = R.color.aero)
    val snow = colorResource(id = R.color.snow)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.Blue, Color.Cyan))),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = snow)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🚀 Submit Your Complaint", style = MaterialTheme.typography.headlineSmall, color = bitterSweet)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = complainText,
                    onValueChange = { complainText = it },
                    placeholder = { Text("Describe your issue...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions { keyboardController?.hide() },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = emerald,
                        unfocusedBorderColor = aero,
                        cursorColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Text("Urgency", style = MaterialTheme.typography.bodyLarge, color = black)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf("High", "Medium", "Normal").forEach { urgency ->
                                FilterChip(
                                    selected = selectedUrgency == urgency,
                                    onClick = { selectedUrgency = urgency },
                                    label = { Text(urgency) },
                                    leadingIcon = if (selectedUrgency == urgency) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors( // FIXED
                                        containerColor = softTan,
                                        selectedContainerColor = offWhite,
                                        labelColor = black,
                                        selectedLabelColor = warmBeige
                                    )
                                )

                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Assign to Category", style = MaterialTheme.typography.bodyLarge, color = black)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf("IT", "HR", "Others").forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category) },
                                    leadingIcon = if (selectedCategory == category) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors( // FIXED
                                        containerColor = softTan,
                                        selectedContainerColor = offWhite,
                                        labelColor = black,
                                        selectedLabelColor = warmBeige
                                    )
                                )

                            }
                        }

                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { complainText = ""; onResetClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Reset")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset")
                    }

                    Button(
                        onClick = { showConfirmDialog = true },
                        enabled = complainText.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit")
                    }
                }
            }
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Text(
                        "Confirm Submission",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to submit this complaint?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            onSaveClick(complainText, selectedUrgency, selectedCategory)
                            complainText = ""
                            showSuccessPopup = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Fresh Green Color
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(8.dp)
                            .height(48.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Submit", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit", color = Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showConfirmDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp),
                        modifier = Modifier
                            .padding(8.dp)
                            .height(48.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }


        if (showSuccessPopup) {
            LaunchedEffect(Unit) {
                delay(2000)
                showSuccessPopup = false
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Green, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text("Complaint Saved Successfully!", color = Color.White)
            }
        }
    }
}
