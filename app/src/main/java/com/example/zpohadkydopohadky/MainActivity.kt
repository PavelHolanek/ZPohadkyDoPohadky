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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zpohadkydopohadky.ui.theme.ZPohadkyDoPohadkyTheme
import kotlin.math.min
import java.io.BufferedReader
import java.io.InputStreamReader

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
    val context = LocalContext.current
    val board = remember {
        loadBoardSpecFromAssets(
            context = context,
            lineFiles = listOf("A", "E"),
            width = 764f,
            height = 1040f
        )
    }
    val mapPainter = painterResource(id = R.drawable.game_map)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
    ) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()
        val imageWidth = if (mapPainter.intrinsicSize.width > 0f) {
            mapPainter.intrinsicSize.width
        } else {
            containerWidth
        }
        val imageHeight = if (mapPainter.intrinsicSize.height > 0f) {
            mapPainter.intrinsicSize.height
        } else {
            containerHeight
        }
        val scale = min(containerWidth / imageWidth, containerHeight / imageHeight)
        val displayWidth = imageWidth * scale
        val displayHeight = imageHeight * scale
        val offsetX = (containerWidth - displayWidth) / 2f
        val offsetY = (containerHeight - displayHeight) / 2f

        Image(
            painter = mapPainter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = 6.dp.toPx()
            for (square in board.squares) {
                val normalizedX = square.x / board.width
                val normalizedY = square.y / board.height
                val x = offsetX + normalizedX * displayWidth
                val y = offsetY + normalizedY * displayHeight
                drawCircle(
                    color = Color(0xFF1E88E5),
                    radius = radius,
                    center = Offset(x, y)
                )
            }
        }
    }
}

private data class BoardSquare(
    val line: Char,
    val index: Int,
    val x: Float,
    val y: Float,
    val tag: String? = null
)

private data class BoardSpec(
    val width: Float,
    val height: Float,
    val squares: List<BoardSquare>
)

private fun loadBoardSpecFromAssets(
    context: android.content.Context,
    lineFiles: List<String>,
    width: Float,
    height: Float
): BoardSpec {
    val squares = mutableListOf<BoardSquare>()
    for (line in lineFiles) {
        val path = "map/$line.csv"
        try {
            val input = context.assets.open(path)
            BufferedReader(InputStreamReader(input)).useLines { lines ->
                var index = 1
                lines.forEach { raw ->
                    val trimmed = raw.trim()
                    if (trimmed.isEmpty()) return@forEach
                    val parts = trimmed.split(',')
                    if (parts.size < 2) return@forEach
                    val x = parts[0].trim().toFloatOrNull() ?: return@forEach
                    val y = parts[1].trim().toFloatOrNull() ?: return@forEach
                    val tag = parts.getOrNull(2)?.trim().orEmpty().ifBlank { null }
                    squares.add(BoardSquare(line[0], index, x, y, tag))
                    index++
                }
            }
        } catch (_: Exception) {
            continue
        }
    }
    return BoardSpec(width = width, height = height, squares = squares)
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
