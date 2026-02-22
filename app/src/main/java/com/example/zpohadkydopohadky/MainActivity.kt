package com.example.zpohadkydopohadky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zpohadkydopohadky.ui.theme.ZPohadkyDoPohadkyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZPohadkyDoPohadkyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val showGame = remember { mutableStateOf(false) }
                    if (showGame.value) {
                        GameScreen(modifier = Modifier.padding(innerPadding))
                    } else {
                        IntroScreen(
                            modifier = Modifier.padding(innerPadding),
                            onStartGame = { showGame.value = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IntroScreen(
    modifier: Modifier = Modifier,
    onStartGame: () -> Unit = {}
) {
    val avatarIds = listOf(
        R.drawable.carodejnice,
        R.drawable.princezna,
        R.drawable.trpaslik,
        R.drawable.cert,
        R.drawable.vodnik,
        R.drawable.kral,
    )
    val names = remember { mutableStateListOf("", "", "", "") }
    val avatarIndices = remember { mutableStateListOf(-1, -1, -1, -1) }
    val isAnyPlayerActive = avatarIndices.any { it >= 0 }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Z pohádky do pohádky",
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(modifier = Modifier.height(24.dp))

        for (index in 0 until 4) {
            val avatarIndex = avatarIndices[index]
            val avatarResId = if (avatarIndex >= 0) avatarIds[avatarIndex] else null
            PlayerSetupRow(
                playerNumber = index + 1,
                name = names[index],
                avatarResId = avatarResId,
                onNameChange = { names[index] = it },
                onAvatarClick = {
                    val nextIndex = findNextAvailableAvatarIndex(
                        currentIndex = avatarIndex,
                        totalAvatars = avatarIds.size,
                        usedIndices = avatarIndices
                    )
                    avatarIndices[index] = nextIndex
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStartGame,
            enabled = isAnyPlayerActive,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Hrát")
        }
    }
}

@Composable
private fun GameScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
    ) {
        Image(
            painter = painterResource(id = R.drawable.game_map),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun findNextAvailableAvatarIndex(
    currentIndex: Int,
    totalAvatars: Int,
    usedIndices: List<Int>
): Int {
    if (totalAvatars == 0) return -1
    val used = usedIndices.toMutableSet()
    used.remove(currentIndex)
    var candidate = if (currentIndex == -1) 0 else currentIndex + 1
    var steps = 0
    while (steps <= totalAvatars) {
        if (candidate >= totalAvatars) candidate = -1
        if (candidate == -1 || !used.contains(candidate)) return candidate
        candidate++
        steps++
    }
    return -1
}

@Composable
private fun PlayerSetupRow(
    playerNumber: Int,
    name: String,
    avatarResId: Int?,
    onNameChange: (String) -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AvatarPicker(
            avatarResId = avatarResId,
            onClick = onAvatarClick,
            modifier = Modifier.size(64.dp)
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text(text = "Jméno hráče $playerNumber") },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
        )
    }
}

@Composable
private fun AvatarPicker(
    avatarResId: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (avatarResId == null) Color(0xFFB0B0B0) else Color.Transparent
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFF2F2F2))
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
    ) {
        if (avatarResId != null) {
            Image(
                painter = painterResource(id = avatarResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IntroScreenPreview() {
    ZPohadkyDoPohadkyTheme {
        IntroScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    ZPohadkyDoPohadkyTheme {
        GameScreen()
    }
}
