package com.example.mcc.view.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mcc.controller.AdvisorController
import com.example.mcc.controller.AuthController
import com.example.mcc.controller.StudentController
import com.example.mcc.model.TrainingProgram
import com.example.mcc.view.Utils
import com.example.mcc.view.common.LoadingScreen
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentScreen(
    navController: NavController,
) {
    val trainingProgramsOfStudent = remember { mutableStateListOf<Pair<String, TrainingProgram>>() }
    val availableTrainingPrograms = remember { mutableStateListOf<Pair<String, TrainingProgram>>() }
    val categories = listOf("All") + Utils.getAvailableCategories()
    val pagerState = rememberPagerState(0)
    val pages = listOf("Available Programs", "My Programs")
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(key1 = Unit, block = {
        if (trainingProgramsOfStudent.isEmpty()) {
            isLoading = true
            val currentUserToken = Utils.getCurrentUserToken(context)
            val stringUserPair = AuthController.getUserByToken(currentUserToken)
            stringUserPair?.first?.let {
                trainingProgramsOfStudent.addAll(StudentController.getTrainingProgramsOf(it))
            }
            isLoading = false
        }

        if (availableTrainingPrograms.isEmpty()) {
            isLoading = true
            availableTrainingPrograms.addAll(AdvisorController.getAllTrainingPrograms())
            availableTrainingPrograms.removeAll(trainingProgramsOfStudent)
            isLoading = false
        }
    })
    Column {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 3.dp
                )
            },
        ) {
            pages.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title, fontSize = 14.sp) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
            }
        }
        HorizontalPager(
            pageCount = pages.size,
            state = pagerState,
        ) { page ->
            if (page == 0) {
                val query = remember { mutableStateOf("") }
                val selectedCategory = remember { mutableStateOf("All") }
                Column(modifier = Modifier.fillMaxSize()) {
                    RoundedSearchBar(
                        query = query.value,
                        onQueryChange = { query.value = it },
                        onSearchClick = {})
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(categories) { topic ->
                            TopicItem(
                                selected = topic == selectedCategory.value,
                                text = topic,
                                onSelectedChange = {
                                    selectedCategory.value = topic
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(availableTrainingPrograms
                            .filter {
                                if (selectedCategory.value != "All") {
                                    it.second.category == selectedCategory.value
                                } else {
                                    true
                                }
                            }
                            .filter {
                                it.second.title.contains(
                                    query.value,
                                    true
                                )
                            }) { (programId, program) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(horizontal = 16.dp)
                                    .clickable {
                                        val imageUrlEncoded = URLEncoder.encode(
                                            program.coverPicture,
                                            StandardCharsets.UTF_8.toString()
                                        )
                                        navController.navigate("training_details_screen/${imageUrlEncoded}/${program.title}/${program.description}/${program.price}/${programId}/${program.advisorId}")
                                    },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    AsyncImage(
                                        model = program.coverPicture,
                                        contentDescription = "cover picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(color = Color.Black.copy(alpha = 0.4f))
                                            .fillMaxSize()
                                    )
                                    Text(
                                        text = program.title,
                                        color = White,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(16.dp)
                                    )
                                    Text(
                                        text = program.category,
                                        color = White,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                    )
                                    Text(
                                        text = program.advisorName,
                                        color = White,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                val query = remember { mutableStateOf("") }
                val selectedCategory = remember { mutableStateOf("All") }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    RoundedSearchBar(
                        query = query.value,
                        onQueryChange = { query.value = it },
                        onSearchClick = {})
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(categories) { topic ->
                            TopicItem(
                                selected = topic == selectedCategory.value,
                                text = topic,
                                onSelectedChange = {
                                    selectedCategory.value = topic
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(trainingProgramsOfStudent
                            .filter {
                                if (selectedCategory.value != "All") {
                                    it.second.category == selectedCategory.value
                                } else {
                                    true
                                }
                            }
                            .filter {
                                it.second.title.contains(
                                    query.value,
                                    true
                                )
                            }) { (programId, program) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(horizontal = 16.dp)
                                    .clickable {
                                        val imageUrlEncoded = URLEncoder.encode(
                                            program.coverPicture,
                                            StandardCharsets.UTF_8.toString()
                                        )
                                        navController.navigate("subscribed_program_details_screen/${imageUrlEncoded}/${program.title}/${program.description}/${program.price}/${programId}/${program.advisorId}")
                                    },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    AsyncImage(
                                        model = program.coverPicture,
                                        contentDescription = "cover picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(color = Color.Black.copy(alpha = 0.4f))
                                            .fillMaxSize()
                                    )
                                    Text(
                                        text = program.title,
                                        color = White,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(16.dp)
                                    )
                                    Text(
                                        text = program.category,
                                        color = White,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                    )
                                    Text(
                                        text = program.advisorName,
                                        color = White,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    LoadingScreen(isLoading)
}

@Composable
fun TopicItem(
    selected: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    onSelectedChange: () -> Unit,
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
        contentColor = if (selected) White else MaterialTheme.colorScheme.primary,
        shape = CircleShape,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Color.Transparent else MaterialTheme.colorScheme.primary
        ),
        modifier = modifier.clickable { onSelectedChange() }
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .heightIn(min = 48.dp),
        placeholder = {
            Text(
                text = "Search...",
                fontSize = 14.sp,
                color = Color.Gray,
            )
        },
        singleLine = true,
        leadingIcon = {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Clear",
                    tint = Color.Gray
                )
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchClick() }
        ),
        shape = RoundedCornerShape(10.dp)
    )
}