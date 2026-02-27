package com.example.zpohadkydopohadky

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zpohadkydopohadky.ui.theme.ZPohadkyDoPohadkyTheme
import kotlin.math.min
import kotlin.random.Random
import java.io.BufferedReader
import java.io.InputStreamReader
import android.graphics.Paint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZPohadkyDoPohadkyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val showGame = remember { mutableStateOf(false) }
                    val players = remember { mutableStateOf(emptyList<Player>()) }
                    val autoplay = remember { mutableStateOf(false) }
                    if (showGame.value) {
                        GameScreen(
                            modifier = Modifier.padding(innerPadding),
                            players = players.value,
                            autoplay = autoplay.value,
                            onExitToMenu = {
                                players.value = emptyList()
                                showGame.value = false
                            }
                        )
                    } else {
                        IntroScreen(
                            modifier = Modifier.padding(innerPadding),
                            autoplay = autoplay.value,
                            onAutoplayChange = { autoplay.value = it },
                            onStartGame = { activePlayers ->
                                players.value = activePlayers
                                showGame.value = true
                            }
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
    autoplay: Boolean = false,
    onAutoplayChange: (Boolean) -> Unit = {},
    onStartGame: (List<Player>) -> Unit = {}
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
            .background(Color(0xFFDAB38C))
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(
                checked = autoplay,
                onCheckedChange = onAutoplayChange
            )
            Text(
                text = "Autoplay",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(androidx.compose.ui.Alignment.CenterVertically)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val activePlayers = avatarIndices
                    .mapIndexedNotNull { index, avatarIndex ->
                        if (avatarIndex >= 0) {
                            Player(name = names[index], avatarIndex = avatarIndex, order = index)
                        } else {
                            null
                        }
                    }
                onStartGame(activePlayers)
            },
            enabled = isAnyPlayerActive,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Hrát")
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun GameScreen(
    modifier: Modifier = Modifier,
    players: List<Player>,
    autoplay: Boolean = false,
    onExitToMenu: () -> Unit = {}
) {
    val context = LocalContext.current
    val avatarResIds = listOf(
        R.drawable.carodejnice,
        R.drawable.princezna,
        R.drawable.trpaslik,
        R.drawable.cert,
        R.drawable.vodnik,
        R.drawable.kral,
    )
    val playerColors = listOf(
        Color(0xFFFFD54F),
        Color(0xFF1976D2),
        Color(0xFFD32F2F),
        Color(0xFF388E3C)
    )
    val board = remember {
        loadBoardSpecFromRaw(
            context = context,
            lineFiles = listOf("A", "B", "C", "D", "E", "F", "G"),
            width = 764f,
            height = 1040f
        )
    }
    val mapPainter = painterResource(id = R.drawable.game_map)
    val diceValue = remember { mutableStateOf<Int?>(null) }
    val playerStates = remember(players) {
        mutableStateListOf<PlayerState>().apply {
            addAll(
                players.map { player ->
                    PlayerState(
                        name = player.name,
                        avatarResId = avatarResIds.getOrNull(player.avatarIndex) ?: avatarResIds.first(),
                        color = playerColors.getOrNull(player.order) ?: Color.White,
                        line = 'A',
                        index = 1
                    )
                }
            )
        }
    }
    val currentPlayerIndex = remember { mutableStateOf(0) }
    val currentPlayer = playerStates.getOrNull(currentPlayerIndex.value)
    val currentPlayerName = currentPlayer?.name?.ifBlank { "Hráč ${currentPlayerIndex.value + 1}" }
        ?: "Hráč"
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val isMoving = remember { mutableStateOf(false) }
    val winner = remember { mutableStateOf<PlayerState?>(null) }
    val labelPaint = remember {
        Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.BLACK
            textAlign = Paint.Align.LEFT
        }
    }
    labelPaint.textSize = with(density) { 12.sp.toPx() }

    fun startRoll() {
        if (playerStates.isEmpty() || isMoving.value || winner.value != null) return
        val roll = Random.nextInt(1, 7)
        diceValue.value = roll
        isMoving.value = true
        coroutineScope.launch {
            movePlayerAnimated(
                board = board,
                states = playerStates,
                playerIndex = currentPlayerIndex.value,
                steps = roll,
                stepDelayMs = 300L
            )
            if (currentPlayer?.hasFlag("WIN") == true) {
                winner.value = currentPlayer
            }
            diceValue.value = null
            if (roll != 6) {
                currentPlayerIndex.value =
                    (currentPlayerIndex.value + 1) % playerStates.size
            }
            isMoving.value = false
        }
    }

    LaunchedEffect(autoplay, currentPlayerIndex.value, isMoving.value, winner.value) {
        if (autoplay && !isMoving.value && winner.value == null) {
            delay(300L)
            startRoll()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFDAB38C))
    ) {
        DicePanel(
            playerName = currentPlayerName,
            playerAvatarResId = currentPlayer?.avatarResId,
            playerColor = currentPlayer?.color ?: Color.White,
            diceValue = diceValue.value,
            onDiceRoll = {
                startRoll()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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
                    val tag = square.tag
                    if (!tag.isNullOrBlank()) {
                        drawContext.canvas.nativeCanvas.drawText(
                            tag,
                            x - radius - 8.dp.toPx(),
                            y + 6.dp.toPx(),
                            labelPaint
                        )
                    }
                }
            }

            val avatarSizePx = with(density) { 28.dp.toPx() }
            val grouped = playerStates.groupBy { it.line to it.index }
            for ((position, group) in grouped) {
                val square = board.findSquare(position.first, position.second) ?: continue
                val normalizedX = square.x / board.width
                val normalizedY = square.y / board.height
                val baseX = offsetX + normalizedX * displayWidth
                val baseY = offsetY + normalizedY * displayHeight
                group.forEachIndexed { idx, playerState ->
                    val offsetPx = idx * 8
                    val x = baseX - avatarSizePx / 2f + offsetPx
                    val y = baseY - avatarSizePx / 2f + offsetPx
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .offset { IntOffset(x.toInt(), y.toInt()) }
                            .clip(CircleShape)
                            .background(playerState.color)
                            .border(1.dp, Color.White, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = playerState.avatarResId),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(3.dp)
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }

    if (winner.value != null) {
        WinPopup(
            player = winner.value!!,
            onNewGame = {
                winner.value = null
                onExitToMenu()
            }
        )
    }
}

@Composable
private fun DicePanel(
    playerName: String,
    playerAvatarResId: Int?,
    playerColor: Color,
    diceValue: Int?,
    onDiceRoll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val diceResId = when (diceValue) {
        1 -> R.drawable.dice1
        2 -> R.drawable.dice2
        3 -> R.drawable.dice3
        4 -> R.drawable.dice4
        5 -> R.drawable.dice5
        6 -> R.drawable.dice6
        else -> R.drawable.dice_question_mark
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(playerColor)
                    .border(1.dp, Color.White, CircleShape)
            ) {
                if (playerAvatarResId != null) {
                    Image(
                        painter = painterResource(id = playerAvatarResId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .height(56.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Na tahu:",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Image(
            painter = painterResource(id = diceResId),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clickable(onClick = onDiceRoll)
        )
    }
}

@Composable
private fun WinPopup(
    player: PlayerState,
    onNewGame: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000))
    ) {
        Column(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.Center)
                .background(Color(0xFFF8F4F2), RoundedCornerShape(20.dp))
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(player.color)
                    .border(2.dp, Color.White, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = player.avatarResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Výhra",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNewGame) {
                Text(text = "Nová Hra")
            }
        }
    }
}

data class Player(
    val name: String,
    val avatarIndex: Int,
    val order: Int
)

private data class PlayerState(
    val name: String,
    val avatarResId: Int,
    val color: Color,
    val line: Char,
    val index: Int,
    val flags: MutableSet<String> = mutableSetOf()
) {
    fun hasFlag(flag: String): Boolean = flags.contains(flag)
    fun addFlag(flag: String) {
        flags.add(flag)
    }
    fun removeFlag(flag: String) {
        flags.remove(flag)
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

private fun BoardSpec.findSquareByTag(tag: String): BoardSquare? {
    return squares.firstOrNull { it.tag == tag }
}

private fun BoardSpec.findSquare(line: Char, index: Int): BoardSquare? {
    return squares.firstOrNull { it.line == line && it.index == index }
}

private fun BoardSquare.nextSquareStart(
    board: BoardSpec,
    currentPlayerState: PlayerState,
    startingSquare: BoardSquare,
    diceValue: Int,
    stepsRemaining: Int
): BoardSquare? {
    if (!tag.isNullOrBlank()) {
        val currentSquareTag = tag
        val starting = startingSquare
        val dice = diceValue
        val remaining = stepsRemaining
        // Special cases for start movement (to be defined later).
        if (tag == "San" && diceValue != 3) {
            return null;
        }
        if (tag == "Tun" && diceValue != 6 && !currentPlayerState.hasFlag("STUDNA")) {
            return null;
        }
        if ((tag == "Zabak1" || tag == "Zabak2") && diceValue == 1) {
            if (currentPlayerState.hasFlag("STUDNA"))
            {
                currentPlayerState.removeFlag("STUDNA")
                return board.findSquareByTag("Koruna")
            }
            else
            {
                return board.findSquareByTag("Studna")
            }
        }
        if (tag == "I") {
            return board.findSquareByTag("StartE")
        }
        if (tag == "II") {
            currentPlayerState.addFlag("E_OBRACENE")
            return board.findSquareByTag("EndE")
        }
        if (tag == "Carodejnice"){
            if(currentPlayerState.hasFlag("STUDNA")){
                currentPlayerState.removeFlag("STUDNA")
            }
            if(currentPlayerState.hasFlag("B_ZNOVA")){
                currentPlayerState.removeFlag("B_ZNOVA")
                return board.findSquareByTag("StartB")
            }
        }
    }
    if (tag in listOf("Carodejnice", "Vodnik", "Zabak1", "Zabak2", "Studna"))
    {
        if (currentPlayerState.hasFlag("STUDNA") || currentPlayerState.hasFlag("E_OBRACENE"))
        {
            return board.findSquare(line, index - 1)
        }
        return board.findSquare(line, index + 1)
    }

    return nextSquarePassing(
        board = board,
        currentPlayerState = currentPlayerState,
        startingSquare = startingSquare,
        diceValue = diceValue,
        stepsRemaining = stepsRemaining
    )
}

private fun BoardSquare.nextSquarePassing(
    board: BoardSpec,
    currentPlayerState: PlayerState,
    startingSquare: BoardSquare,
    diceValue: Int,
    stepsRemaining: Int
): BoardSquare? {
    if (!tag.isNullOrBlank()) {
        val currentSquareTag = tag
        val starting = startingSquare
        val dice = diceValue
        val remaining = stepsRemaining
        // Special cases for passing movement (to be defined later).
        //currentPlayerState.addFlag("EXAMPLE_FLAG")
        //currentPlayerState.removeFlag("EXAMPLE_FLAG")number % 2 != 0
        when (tag) {
            "Carodejnice"->
            {
                if(currentPlayerState.hasFlag("STUDNA"))
                {
                    currentPlayerState.removeFlag("STUDNA")
                    return board.findSquare(line, index + 1)
                }
                if(diceValue % 2 == 0)
                {
                    return board.findSquareByTag("StartC")
                }
                else
                {
                    return board.findSquareByTag("StartB")
                }
            }
            "Brana"->
            {
                if(diceValue % 2 == 0)
                {
                    return board.findSquareByTag("StartG")
                }
                else
                {
                    return board.findSquareByTag("StartF")
                }
            }
            "Vodnik"->{
                if (!currentPlayerState.hasFlag("STUDNA"))
                {
                    return null
                }
            }
            "Zabak1"->{
                if (currentPlayerState.hasFlag("STUDNA") && !currentPlayerState.hasFlag("NASTEVA_ZABAKA"))
                {
                    currentPlayerState.addFlag("STOP")
                    return startingSquare
                }
            }
            "Zabak2"->{
                if (!currentPlayerState.hasFlag("STUDNA") && !currentPlayerState.hasFlag("NASTEVA_ZABAKA"))
                {
                    currentPlayerState.addFlag("STOP")
                    return startingSquare
                }
            }
            "StartC"->{
                if (currentPlayerState.hasFlag("STUDNA"))
                {
                    return board.findSquareByTag("Carodejnice")
                }
            }
            "Studna"->{
                currentPlayerState.addFlag("STOP")
                return startingSquare
            }
            "EndB"->return board.findSquareByTag("BPokracovani")
            "EndD"->return board.findSquareByTag("DPokracovani")
            "EndE"->{
                if(!currentPlayerState.hasFlag("E_OBRACENE"))
                {
                    return board.findSquareByTag("II")
                }
            }
            "StartE"->{
                if(currentPlayerState.hasFlag("E_OBRACENE"))
                {
                    currentPlayerState.removeFlag("E_OBRACENE")
                    return board.findSquareByTag("I")
                }
            }
            "KonecI"->{
                if (stepsRemaining !=1 )
                {
                    currentPlayerState.addFlag("STOP")
                    return startingSquare
                }
            }
            "KonecII"->{
                if (stepsRemaining !=1 )
                {
                    currentPlayerState.addFlag("STOP")
                    return startingSquare
                }
            }
        }
    }
    if (currentPlayerState.hasFlag("STUDNA") || currentPlayerState.hasFlag("E_OBRACENE"))
    {
        return board.findSquare(line, index - 1)
    }
    return board.findSquare(line, index + 1)
}

private fun BoardSquare.nextSquareEnd(
    board: BoardSpec,
    currentPlayerState: PlayerState,
    startingSquare: BoardSquare,
    diceValue: Int,
    stepsRemaining: Int
): BoardSquare? {
    if (!tag.isNullOrBlank()) {
        val currentSquareTag = tag
        val starting = startingSquare
        val dice = diceValue
        val remaining = stepsRemaining
        // Special cases for end movement (to be defined later).
        when (tag) {
            "Mec"-> return board.findSquareByTag("ZaSani")
            "CernyKun"-> return board.findSquareByTag("Lucifer")
            "BilyKun"-> return board.findSquareByTag("Vrch")
            "HnedyKun"-> {
                currentPlayerState.addFlag("B_ZNOVA")
                return board.findSquareByTag("Carodejnice")
            }
            "Princezna"->{
                if (!currentPlayerState.hasFlag("STUDNA"))
                {
                    return board.findSquareByTag("Vodnik")
                }
            }
            "Vodnik"->{
                if (currentPlayerState.hasFlag("STUDNA"))
                {
                    return board.findSquareByTag("Princezna")
                }
            }
            "PtakOhnivak"->{
                if (currentPlayerState.hasFlag("STUDNA"))
                {
                    currentPlayerState.removeFlag("STUDNA")
                    return board.findSquareByTag("Vrch")
                }
                else
                {
                    return board.findSquareByTag("Tun")
                }
            }
            "Jablko2"-> {
                if (currentPlayerState.hasFlag("JABLKO")) {
                    currentPlayerState.removeFlag("JABLKO")
                }
                else
                {
                    currentPlayerState.addFlag("JABLKO")
                    return board.findSquareByTag("Jablko1")
                }
            }
            "Jablko1"-> {
                if (currentPlayerState.hasFlag("JABLKO")) {
                    currentPlayerState.removeFlag("JABLKO")
                }
                else
                {
                    currentPlayerState.addFlag("JABLKO")
                    return board.findSquareByTag("Jablko2")
                }
            }
            "Cert"-> return board.findSquare(line, index - 6)
            "Cert_prvni"-> return board.findSquareByTag("Lucifer")
            "Vrana"-> return board.findSquareByTag("Carodejnice")
            "Holubice"-> return board.findSquareByTag("Vrch")
            "CernePrase"-> {
                currentPlayerState.removeFlag("E_OBRACENE")
                return board.findSquareByTag("Lucifer")
            }
            "BilePrase"-> {
                currentPlayerState.removeFlag("E_OBRACENE")
                return board.findSquareByTag("Vrch")
            }
            "Obr"-> return board.findSquareByTag("Vrch")
            "Brana"-> return board.findSquareByTag("Carodejnice")
            "Studna"->{
                currentPlayerState.removeFlag("NASTEVA_ZABAKA")
                currentPlayerState.addFlag("STUDNA")
            }
            "Had"-> return board.findSquareByTag("Koruna")
            "Drak"-> return board.findSquareByTag("Hvezda")
            "KonecI"-> return board.findSquareByTag("I")
            "KonecII"-> {

                return board.findSquareByTag("II")
            }
            "Zabak1"->{
                currentPlayerState.addFlag("NASTEVA_ZABAKA")
            }
            "Zabak2"->{
                currentPlayerState.addFlag("NASTEVA_ZABAKA")
            }
            "Poklad"->{
                currentPlayerState.addFlag("WIN")
                //ADD_WIN_POPUP
            }
        }
    }
    return null
}

private suspend fun movePlayerAnimated(
    board: BoardSpec,
    states: MutableList<PlayerState>,
    playerIndex: Int,
    steps: Int,
    stepDelayMs: Long
) {
    val state = states.getOrNull(playerIndex) ?: return
    var current = board.findSquare(state.line, state.index) ?: return
    val startingSquare = current

    for (step in 1..steps) {
        val stepsRemaining = steps - step + 1
        val next = if (step == 1) {
            current.nextSquareStart(
                board = board,
                currentPlayerState = state,
                startingSquare = startingSquare,
                diceValue = steps,
                stepsRemaining = stepsRemaining
            )
        } else {
            current.nextSquarePassing(
                board = board,
                currentPlayerState = state,
                startingSquare = startingSquare,
                diceValue = steps,
                stepsRemaining = stepsRemaining
            )
        } ?: break
        current = next
        states[playerIndex] = state.copy(line = current.line, index = current.index)
        delay(stepDelayMs)
        if (state.hasFlag("STOP"))
        {
            state.removeFlag("STOP")
            break;
        }
    }

    val end1 = current.nextSquareEnd(
        board = board,
        currentPlayerState = state,
        startingSquare = startingSquare,
        diceValue = steps,
        stepsRemaining = 0
    )
    if (end1 != null) {
        current = end1
        states[playerIndex] = state.copy(line = current.line, index = current.index)
        delay(stepDelayMs)
        val end2 = end1.nextSquareEnd(
            board = board,
            currentPlayerState = state,
            startingSquare = startingSquare,
            diceValue = steps,
            stepsRemaining = 0
        )
        if (end2 != null) {
            current = end2
            states[playerIndex] = state.copy(line = current.line, index = current.index)
            delay(stepDelayMs)
        }
    }
    delay(stepDelayMs)
}

private fun loadBoardSpecFromRaw(
    context: android.content.Context,
    lineFiles: List<String>,
    width: Float,
    height: Float
): BoardSpec {
    val squares = mutableListOf<BoardSquare>()
    for (line in lineFiles) {
        val resourceName = line.lowercase()
        val resId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (resId == 0) continue
        try {
            val input = context.resources.openRawResource(resId)
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
                    squares.add(BoardSquare(line.uppercase()[0], index, x, y, tag))
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
            .background(Color(0xFFF8F4F2))
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
        GameScreen(
            players = listOf(
                Player(name = "Anna", avatarIndex = 0, order = 0),
                Player(name = "Petr", avatarIndex = 1, order = 1)
            )
        )
    }
}
