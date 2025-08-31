package com.example.lexipath.ui.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lexipath.data.models.QuizType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    contentId: String,
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedAnswer by remember { mutableStateOf("") }

    LaunchedEffect(contentId) {
        // Load content by ID - simplified for demo
        // In real implementation, you'd fetch the content
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Quiz") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Quiz Type Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuizType.values().forEach { type ->
                    FilterChip(
                        selected = uiState.currentQuizType == type,
                        onClick = { viewModel.selectQuizType(type) },
                        label = {
                            Text(
                                when (type) {
                                    QuizType.mcq -> "Multiple Choice"
                                    QuizType.fill_blank -> "Fill Blank"
                                    QuizType.situation -> "Situation"
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            uiState.currentQuestion?.let { question ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = question.question,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        when (question.type) {
                            QuizType.mcq -> {
                                Column(modifier = Modifier.selectableGroup()) {
                                    question.options.forEach { option ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .selectable(
                                                    selected = selectedAnswer == option,
                                                    onClick = { selectedAnswer = option },
                                                    role = Role.RadioButton
                                                )
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = selectedAnswer == option,
                                                onClick = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = option)
                                        }
                                    }
                                }
                            }
                            QuizType.fill_blank, QuizType.situation -> {
                                OutlinedTextField(
                                    value = selectedAnswer,
                                    onValueChange = { selectedAnswer = it },
                                    label = { Text("Your answer") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (!uiState.showResult) {
                            Button(
                                onClick = { viewModel.submitAnswer(selectedAnswer) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = selectedAnswer.isNotBlank() && !uiState.isSubmitting
                            ) {
                                if (uiState.isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Submit Answer")
                                }
                            }
                        } else {
                            // Show result
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (uiState.isCorrect) {
                                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    } else {
                                        Color(0xFFF44336).copy(alpha = 0.1f)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (uiState.isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Close,
                                        contentDescription = null,
                                        tint = if (uiState.isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (uiState.isCorrect) "Correct!" else "Incorrect",
                                            fontWeight = FontWeight.Bold,
                                            color = if (uiState.isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                                        )
                                        if (!uiState.isCorrect) {
                                            Text(
                                                text = "Correct answer: ${uiState.correctAnswer}",
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.nextQuestion() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Next Question")
                                }
                                Button(
                                    onClick = onNavigateBack,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Finish")
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (uiState.error != null) {
                val errorMessage = uiState.error!!
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
