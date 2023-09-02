package com.example.mcc.view.advisor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mcc.controller.AdvisorController
import com.example.mcc.model.Attachment
import com.example.mcc.model.AttachmentType
import com.example.mcc.model.TrainingProgram
import com.example.mcc.view.Utils
import com.example.mcc.view.common.DropdownMenu
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProgramScreen(navController: NavController, advisorName: String, advisorId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Programming") }
    val coverPicture = remember { mutableStateOf<Uri?>(null) }
    val availableCategories = Utils.getAvailableCategories()

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                coverPicture.value = uri
            }
        }
    )
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CenterAlignedTopAppBar(title = { Text(text = "New Training Program") },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            })
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = coverPicture.value ?: Color.LightGray,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(150.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                    .padding(4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentScale = ContentScale.Crop
            )
            if (coverPicture.value == null) {
                Text(text = "Cover Picture", style = MaterialTheme.typography.bodyLarge)
            }
        }
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Program Title") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Program Price $") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Program Description") },
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        DropdownMenu(
            selectedItem = selectedCategory,
            items = availableCategories,
            title = "Category",
            onSelectedItem = { selectedCategory = it },
            onSelectedItem2 = { selectedCategory = it })

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            onClick = {
                val program = TrainingProgram(
                    advisorId = advisorId,
                    advisorName = advisorName,
                    title = title,
                    category = selectedCategory,
                    price = price,
                    description = description,
                )
                coverPicture.value?.let { uri ->
                    scope.launch {
                        AdvisorController.addTrainingProgram(
                            program,
                            Attachment(uri, AttachmentType.IMAGE)
                        )
                    }
                }
            }) {
            Text(text = "Add")
        }

    }
}