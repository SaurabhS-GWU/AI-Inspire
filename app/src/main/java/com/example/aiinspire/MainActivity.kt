package com.example.aiinspire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aiinspire.ui.theme.AIInspireTheme
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

// Data class to represent quote categories
data class QuoteCategory(
    val name: String,
    val description: String,
    val group: String
)

data class Quote(
    val text: String,
    val category: String
)

// List of all available categories
val quoteCategories = listOf(
    // Core Categories
    QuoteCategory("Innovation & Creativity", "Breakthrough thinking and creative problem-solving", "Core"),
    QuoteCategory("Learning & Growth", "Motivation for continuous learning and skill development", "Core"),
    QuoteCategory("Technology & Future", "Inspirational thoughts about tech advancement and possibilities", "Core"),
    QuoteCategory("Career & Success", "Professional motivation and achievement-focused quotes", "Core"),
    QuoteCategory("Persistence & Resilience", "Overcoming challenges and bouncing back from failures", "Core"),
    QuoteCategory("Leadership", "Leading teams and driving change", "Core"),
    
    // Specialized Categories
    QuoteCategory("Entrepreneurship", "Startup motivation and business building inspiration", "Specialized"),
    QuoteCategory("Data & Analytics", "Quotes about insights, patterns, and data-driven decisions", "Specialized"),
    QuoteCategory("Coding & Development", "Programming and technical development motivation", "Specialized"),
    QuoteCategory("AI & Machine Learning", "Specific to artificial intelligence and ML journey", "Specialized"),
    QuoteCategory("Digital Transformation", "About adapting to and embracing technological change", "Specialized"),
    QuoteCategory("Problem Solving", "Creative approaches to challenges and solutions", "Specialized"),
    
    // Lifestyle Categories
    QuoteCategory("Daily Motivation", "General encouragement for everyday challenges", "Lifestyle"),
    QuoteCategory("Focus & Productivity", "Getting things done and maintaining concentration", "Lifestyle"),
    QuoteCategory("Teamwork & Collaboration", "Working effectively with others", "Lifestyle"),
    QuoteCategory("Change & Adaptation", "Embracing transformation and new opportunities", "Lifestyle"),
    
    // Mood-Based Categories
    QuoteCategory("Monday Motivation", "Week starter energy", "Mood-Based"),
    QuoteCategory("Breakthrough Moments", "For when you need a push", "Mood-Based"),
    QuoteCategory("Confidence Boost", "Self-belief and empowerment", "Mood-Based")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIInspireTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WelcomeScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen() {
    var showDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<QuoteCategory?>(null) }
    var quotes by remember { mutableStateOf<List<Quote>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showCategorySelection by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Initialize Gemini
    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    // Function to generate quotes
    suspend fun generateQuotes() {
        try {
            val prompt = """
                Generate 10 unique, inspiring quotes related to ${selectedCategory?.name}.
                The quotes should be relevant to ${selectedCategory?.description}.
                Format each quote on a new line.
                Make the quotes concise, impactful, and memorable.
                Do not include attribution or authors.
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val quotesText = response.text?.split("\n")?.filter { it.isNotEmpty() } ?: emptyList()
            quotes = quotesText.map { Quote(it.trim(), selectedCategory?.name ?: "") }
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Error: ${e.message}")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Banner (Always visible)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDialog = true }
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "AI Inspire",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your Daily Dose of AI-Powered Inspiration",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (showCategorySelection) {
                // Category Selection UI
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Select Your Inspiration Category",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Expand") },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            placeholder = { Text("Choose a category") }
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            quoteCategories.groupBy { it.group }.forEach { (group, categories) ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = group,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    onClick = {},
                                    enabled = false
                                )
                                
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(category.name)
                                                Text(
                                                    text = category.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCategory = category
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                if (selectedCategory == null) {
                                    snackbarHostState.showSnackbar("Please select a category first")
                                    return@launch
                                }
                                isLoading = true
                                try {
                                    generateQuotes()
                                    showCategorySelection = false
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Get Inspirational Quotes")
                        }
                    }
                }
            } else {
                // Category Header
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = selectedCategory?.name ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = selectedCategory?.description ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Quotes List with SwipeRefresh
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = {
                        scope.launch {
                            isRefreshing = true
                            generateQuotes()
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(quotes) { quote ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = quote.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Select Different Category Button
                Button(
                    onClick = { 
                        showCategorySelection = true
                        quotes = emptyList()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Select Different Category")
                }
            }
        }
    }

    // Information Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text("About AI Inspire")
            },
            text = {
                Text(
                    "Welcome to AI Inspire! This app provides personalized inspirational quotes based on your preferences and mood. Simply answer a few prompts, and let AI craft the perfect dose of motivation just for you.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Got it!")
                }
            }
        )
    }
}