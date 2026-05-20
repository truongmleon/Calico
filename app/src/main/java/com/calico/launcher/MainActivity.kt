package com.calico.launcher

import android.os.BatteryManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.calico.launcher.data.CalicoDatabase
import com.calico.launcher.data.SAMPLE_GAMES
import com.calico.launcher.data.SAMPLE_TASKBAR_ITEMS
import com.calico.launcher.emulators.EmulatorRegistry
import com.calico.launcher.model.Game
import com.calico.launcher.model.GameSort
import com.calico.launcher.model.TaskbarItem
import com.calico.launcher.providers.CredentialStore
import com.calico.launcher.providers.GameArtwork
import com.calico.launcher.providers.GameArtworkRepository
import com.calico.launcher.providers.ProviderConnectionStatus
import com.calico.launcher.providers.ProviderCredentials
import com.calico.launcher.ui.theme.CalicoBlue
import com.calico.launcher.ui.theme.CalicoBlueLight
import com.calico.launcher.ui.theme.CalicoDim
import com.calico.launcher.ui.theme.CalicoInk
import com.calico.launcher.ui.theme.CalicoPanel
import com.calico.launcher.ui.theme.CalicoScreen
import com.calico.launcher.ui.theme.CalicoTheme
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val emulatorRegistry = EmulatorRegistry()
    private var usePhysicalDualScreen by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CalicoDatabase(this).writableDatabase.close()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(this@MainActivity)
                    .windowLayoutInfo(this@MainActivity)
                    .collect { layoutInfo ->
                        usePhysicalDualScreen = layoutInfo.displayFeatures
                            .filterIsInstance<FoldingFeature>()
                            .any { feature ->
                                feature.occlusionType == FoldingFeature.OcclusionType.FULL ||
                                    feature.state == FoldingFeature.State.HALF_OPENED
                            }
                    }
            }
        }

        setContent {
            CalicoTheme {
                CalicoLauncherApp(
                    emulatorRegistry = emulatorRegistry,
                    usePhysicalDualScreen = usePhysicalDualScreen,
                )
            }
        }
    }
}

@Composable
fun CalicoLauncherApp(
    emulatorRegistry: EmulatorRegistry,
    usePhysicalDualScreen: Boolean = false,
    games: List<Game> = SAMPLE_GAMES,
    taskbarItems: List<TaskbarItem> = SAMPLE_TASKBAR_ITEMS,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var selectedSort by remember { mutableStateOf(GameSort.Console) }
    var showSort by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var showCredentials by remember { mutableStateOf(false) }
    var isLoadingArtwork by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialStore = remember(context) { CredentialStore(context) }
    val artworkRepository = remember { GameArtworkRepository() }
    var credentials by remember { mutableStateOf(credentialStore.load()) }
    val artworkByGame = remember { mutableStateMapOf<Int, GameArtwork>() }

    val sortedGames = remember(games, selectedSort) {
        when (selectedSort) {
            GameSort.Console -> games.sortedWith(compareBy({ it.platform.name }, { it.sortTitle }))
            GameSort.Name -> games.sortedBy { it.sortTitle }
            GameSort.LastPlayed -> games.sortedByDescending { it.lastPlayedAt ?: "" }
            GameSort.TotalHours -> games.sortedByDescending { it.durationSeconds }
            GameSort.Favorites -> games.sortedByDescending { it.isFavorite }
        }
    }
    val selectedGame = sortedGames[selectedIndex.coerceIn(sortedGames.indices)]
    val selectedArtwork = artworkByGame[selectedGame.id]

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(selectedGame.id, credentials) {
        if (credentials.hasSteamGridDb || credentials.hasScreenScraper || credentials.hasRetroAchievements) {
            isLoadingArtwork = true
            artworkByGame[selectedGame.id] = artworkRepository.loadArtwork(selectedGame, credentials)
            isLoadingArtwork = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalicoScreen)
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyUp) return@onPreviewKeyEvent false
                when (event.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_BUTTON_X -> {
                        showSort = true
                        true
                    }
                    KeyEvent.KEYCODE_BUTTON_Y -> {
                        showMenu = true
                        true
                    }
                    KeyEvent.KEYCODE_BUTTON_L1 -> {
                        selectedIndex = (selectedIndex - 1).floorMod(sortedGames.size)
                        true
                    }
                    KeyEvent.KEYCODE_BUTTON_R1 -> {
                        selectedIndex = (selectedIndex + 1).floorMod(sortedGames.size)
                        true
                    }
                    KeyEvent.KEYCODE_BUTTON_L2 -> {
                        showDetails = true
                        true
                    }
                    KeyEvent.KEYCODE_BUTTON_R2 -> {
                        selectedIndex = (selectedIndex + 12).coerceAtMost(sortedGames.lastIndex)
                        true
                    }
                    KeyEvent.KEYCODE_BUTTON_A -> {
                        emulatorRegistry.launcherFor(selectedGame)?.launch(context, selectedGame)
                        true
                    }
                    KeyEvent.KEYCODE_BUTTON_B -> {
                        showDetails = false
                        showMenu = false
                        showSort = false
                        true
                    }
                    else -> false
                }
            },
    ) {
        Column(Modifier.fillMaxSize()) {
            val topScreenWeight = if (usePhysicalDualScreen) 1920f else 1f
            val bottomScreenWeight = if (usePhysicalDualScreen) 1240f else 1f

            TopScreen(
                selectedGame = selectedGame,
                artwork = selectedArtwork,
                isLoadingArtwork = isLoadingArtwork,
                showDetails = showDetails,
                onShowDetails = { showDetails = true },
                onLaunch = { emulatorRegistry.launcherFor(selectedGame)?.launch(context, selectedGame) },
                modifier = Modifier
                    .weight(topScreenWeight)
                    .fillMaxWidth(),
            )
            BottomScreen(
                games = sortedGames,
                selectedGame = selectedGame,
                artworkByGame = artworkByGame,
                taskbarItems = taskbarItems,
                onSelectGame = { game -> selectedIndex = sortedGames.indexOf(game) },
                onOpenSort = { showSort = true },
                onOpenMenu = { showMenu = true },
                modifier = Modifier
                    .weight(bottomScreenWeight)
                    .fillMaxWidth(),
            )
        }

        if (showSort || showMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CalicoDim)
                    .clickable {
                        showSort = false
                        showMenu = false
                    },
            )
        }

        SortPanel(
            visible = showSort,
            selectedSort = selectedSort,
            onSortSelected = {
                selectedSort = it
                selectedIndex = 0
                showSort = false
            },
            onDismiss = { showSort = false },
        )

        MenuPanel(
            visible = showMenu,
            selectedGame = selectedGame,
            artwork = selectedArtwork,
            isLoadingArtwork = isLoadingArtwork,
            onRefreshArtwork = {
                coroutineScope.launch {
                    isLoadingArtwork = true
                    artworkByGame[selectedGame.id] = artworkRepository.loadArtwork(selectedGame, credentials)
                    isLoadingArtwork = false
                }
            },
            onOpenCredentials = {
                showMenu = false
                showCredentials = true
            },
            onDismiss = { showMenu = false },
        )

        CredentialsPanel(
            visible = showCredentials,
            credentials = credentials,
            artworkRepository = artworkRepository,
            onSave = { nextCredentials ->
                credentialStore.save(nextCredentials)
                credentials = nextCredentials
                showCredentials = false
            },
            onClear = {
                credentialStore.clear()
                credentials = ProviderCredentials()
                artworkByGame.clear()
            },
            onDismiss = { showCredentials = false },
        )
    }
}

@Composable
private fun TopScreen(
    selectedGame: Game,
    artwork: GameArtwork?,
    isLoadingArtwork: Boolean,
    showDetails: Boolean,
    onShowDetails: () -> Unit,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(Color.White, CalicoBlueLight.copy(alpha = 0.42f)),
                ),
            )
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PillIcon(Icons.Default.SportsEsports, "Discord")
            PillIcon(Icons.Default.Web, "Messages")
        }

        StatusPill(modifier = Modifier.align(Alignment.TopEnd))

        HeroCard(
            game = selectedGame,
            artwork = artwork,
            isLoadingArtwork = isLoadingArtwork,
            showDetails = showDetails,
            modifier = Modifier.align(Alignment.Center),
        )

        ButtonLegend(
            onShowDetails = onShowDetails,
            onLaunch = onLaunch,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun BottomScreen(
    games: List<Game>,
    selectedGame: Game,
    artworkByGame: Map<Int, GameArtwork>,
    taskbarItems: List<TaskbarItem>,
    onSelectGame: (Game) -> Unit,
    onOpenSort: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color.White)
            .padding(14.dp),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(92.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 82.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(games, key = { it.id }) { game ->
                GameTile(
                    game = game,
                    artwork = artworkByGame[game.id],
                    selected = game.id == selectedGame.id,
                    onClick = { onSelectGame(game) },
                )
            }
        }

        FilledIconButton(
            onClick = onOpenSort,
            modifier = Modifier.align(Alignment.BottomStart),
        ) {
            Icon(Icons.Default.Sort, contentDescription = "Sort and filter")
        }

        Taskbar(
            items = taskbarItems,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        FilledIconButton(
            onClick = onOpenMenu,
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
        }
    }
}

@Composable
private fun HeroCard(
    game: Game,
    artwork: GameArtwork?,
    isLoadingArtwork: Boolean,
    showDetails: Boolean,
    modifier: Modifier = Modifier,
) {
    val heroModel = artwork?.heroUrl ?: artwork?.screenshotUrl

    Card(
        modifier = modifier
            .fillMaxWidth(0.52f)
            .aspectRatio(16f / 9f)
            .shadow(24.dp, RoundedCornerShape(32.dp), clip = false),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = CalicoInk),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(CalicoBlue.copy(alpha = 0.95f), CalicoInk),
                    ),
                )
                .padding(24.dp),
        ) {
            if (heroModel != null) {
                AsyncImage(
                    model = heroModel,
                    contentDescription = "${game.name} hero artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer(alpha = 0.82f),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, CalicoInk.copy(alpha = 0.92f)),
                            ),
                        ),
                )
            }

            Column(Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = game.platform.name,
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = game.name,
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (showDetails) {
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    GameDetails(game)
                }
            }

            if (isLoadingArtwork) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(28.dp),
                    color = Color.White,
                    strokeWidth = 3.dp,
                )
            }
        }
    }
}

@Composable
private fun GameDetails(game: Game) {
    Surface(
        color = CalicoPanel,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth(0.48f),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Details", style = MaterialTheme.typography.titleMedium)
            Text(game.description.orEmpty(), maxLines = 4, overflow = TextOverflow.Ellipsis)
            Text("Released ${game.releaseDate ?: "Unknown"}")
            Text("Developer ${game.developers.joinToString().ifBlank { "Unknown" }}")
            Text("Genre ${game.genres.joinToString().ifBlank { "Unknown" }}")
            Text("${game.hoursPlayed} hours played")
        }
    }
}

@Composable
private fun StatusPill(modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val context = LocalContext.current
    val battery = remember {
        context.getSystemService(BatteryManager::class.java)
            ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            ?.takeIf { it >= 0 } ?: 0
    }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(30_000)
        }
    }

    Surface(
        modifier = modifier,
        color = CalicoPanel,
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(now.format(DateTimeFormatter.ofPattern("HH:mm")))
            Text("|")
            Text(now.format(DateTimeFormatter.ofPattern("M/d")))
            Text("|")
            Icon(Icons.Default.BatteryFull, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("$battery%")
        }
    }
}

@Composable
private fun ButtonLegend(
    onShowDetails: () -> Unit,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = CalicoPanel,
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendAction("A", "Select", Icons.Default.PlayArrow, onLaunch)
            LegendAction("B", "Back", Icons.Default.ArrowBack) {}
            LegendAction("-", "Details", Icons.Default.Info, onShowDetails)
            LegendAction("+", "Menu", Icons.Default.Menu) {}
        }
    }
}

@Composable
private fun LegendAction(
    button: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(button, fontWeight = FontWeight.Bold)
        Icon(icon, contentDescription = label, modifier = Modifier.size(17.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun GameTile(game: Game, artwork: GameArtwork?, selected: Boolean, onClick: () -> Unit) {
    val wiggle by animateFloatAsState(targetValue = if (selected) 0f else 0f, label = "wiggle")

    Column(
        modifier = Modifier
            .graphicsLayer(rotationZ = wiggle)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(82.dp)
                .shadow(if (selected) 14.dp else 2.dp, RoundedCornerShape(22.dp))
                .background(Color.White, RoundedCornerShape(22.dp))
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) CalicoBlue else CalicoBlueLight,
                    shape = RoundedCornerShape(22.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (artwork?.iconUrl != null) {
                AsyncImage(
                    model = artwork.iconUrl,
                    contentDescription = "${game.name} icon",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(58.dp)
                        .padding(4.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = CalicoInk,
                    modifier = Modifier.size(34.dp),
                )
            }
            if (game.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite",
                    tint = CalicoBlue,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(16.dp),
                )
            }
        }
        Text(
            text = game.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = game.platform.name,
            style = MaterialTheme.typography.labelLarge,
            color = CalicoBlue,
            maxLines = 1,
        )
    }
}

@Composable
private fun Taskbar(items: List<TaskbarItem>, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = CalicoPanel,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.filter { it.isEnabled }.forEach { item ->
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = when (item.itemType) {
                            "system_action" -> Icons.Default.Star
                            "bookmark" -> Icons.Default.Web
                            else -> Icons.Default.Apps
                        },
                        contentDescription = item.name,
                    )
                }
            }
        }
    }
}

@Composable
private fun SortPanel(
    visible: Boolean,
    selectedSort: GameSort,
    onSortSelected: (GameSort) -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(visible = visible) {
        SidePanel(alignment = Alignment.CenterStart, onDismiss = onDismiss) {
            Text("Sort Games", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            GameSort.entries.forEach { sort ->
                SortRow(
                    sort = sort,
                    selected = sort == selectedSort,
                    onClick = { onSortSelected(sort) },
                )
            }
        }
    }
}

@Composable
private fun MenuPanel(
    visible: Boolean,
    selectedGame: Game,
    artwork: GameArtwork?,
    isLoadingArtwork: Boolean,
    onRefreshArtwork: () -> Unit,
    onOpenCredentials: () -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(visible = visible) {
        SidePanel(alignment = Alignment.CenterEnd, onDismiss = onDismiss) {
            Text("Now Playing", style = MaterialTheme.typography.titleLarge)
            Text("No background music selected", color = CalicoInk.copy(alpha = 0.72f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = {}) { Icon(Icons.Default.FastRewind, contentDescription = "Back") }
                IconButton(onClick = {}) { Icon(Icons.Default.PlayArrow, contentDescription = "Play") }
                IconButton(onClick = {}) { Icon(Icons.Default.FastForward, contentDescription = "Forward") }
                IconButton(onClick = {}) { Icon(Icons.Default.Loop, contentDescription = "Loop") }
                IconButton(onClick = {}) { Icon(Icons.Default.SkipNext, contentDescription = "Skip") }
                IconButton(onClick = {}) { Icon(Icons.Default.Favorite, contentDescription = "Favorite") }
            }
            Divider(Modifier.padding(vertical = 16.dp))
            MenuItem(Icons.Default.Favorite, "Add ${selectedGame.name} to favorites")
            MenuItem(Icons.Default.Settings, "Change emulator")
            MenuItem(Icons.Default.Folder, "Choose Emulation folder")
            MenuItem(Icons.Default.SportsEsports, "Controller / input mapping")
            MenuItem(Icons.Default.Web, "Bookmarks")
            MenuItem(Icons.Default.MusicNote, "Song albums")
            MenuItem(Icons.Default.Apps, "Modify taskbar")
            MenuItem(Icons.Default.Info, "Manual metadata and art")
            MenuItem(Icons.Default.Search, "Provider credentials", onOpenCredentials)
            MenuItem(
                icon = Icons.Default.Web,
                label = if (isLoadingArtwork) "Loading provider assets..." else "Refresh selected game assets",
                onClick = onRefreshArtwork,
            )
            artwork?.sourceSummary?.let { summary ->
                Spacer(Modifier.height(12.dp))
                Text("Asset sources", style = MaterialTheme.typography.titleMedium)
                Text(summary, style = MaterialTheme.typography.bodyMedium, color = CalicoInk.copy(alpha = 0.72f))
            }
        }
    }
}

@Composable
private fun CredentialsPanel(
    visible: Boolean,
    credentials: ProviderCredentials,
    artworkRepository: GameArtworkRepository,
    onSave: (ProviderCredentials) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    var steamGridDbApiKey by remember(credentials) { mutableStateOf(credentials.steamGridDbApiKey) }
    var screenScraperDeveloperId by remember(credentials) { mutableStateOf(credentials.screenScraperDeveloperId) }
    var screenScraperDeveloperPassword by remember(credentials) { mutableStateOf(credentials.screenScraperDeveloperPassword) }
    var screenScraperUserId by remember(credentials) { mutableStateOf(credentials.screenScraperUserId) }
    var screenScraperUserPassword by remember(credentials) { mutableStateOf(credentials.screenScraperUserPassword) }
    var retroAchievementsUsername by remember(credentials) { mutableStateOf(credentials.retroAchievementsUsername) }
    var retroAchievementsApiKey by remember(credentials) { mutableStateOf(credentials.retroAchievementsApiKey) }
    var status by remember { mutableStateOf(ProviderConnectionStatus()) }
    var isTesting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(visible = visible) {
        SidePanel(alignment = Alignment.CenterEnd, onDismiss = onDismiss) {
            Text("Provider Credentials", style = MaterialTheme.typography.titleLarge)
            Text(
                "Credentials are stored in encrypted app preferences when Android Keystore is available.",
                style = MaterialTheme.typography.bodyMedium,
                color = CalicoInk.copy(alpha = 0.72f),
            )
            Spacer(Modifier.height(14.dp))

            Text("SteamGridDB", style = MaterialTheme.typography.titleMedium)
            SecretField(
                value = steamGridDbApiKey,
                onValueChange = { steamGridDbApiKey = it },
                label = "API key",
            )

            Spacer(Modifier.height(14.dp))
            Text("ScreenScraper", style = MaterialTheme.typography.titleMedium)
            PlainField(
                value = screenScraperDeveloperId,
                onValueChange = { screenScraperDeveloperId = it },
                label = "Developer ID",
            )
            SecretField(
                value = screenScraperDeveloperPassword,
                onValueChange = { screenScraperDeveloperPassword = it },
                label = "Developer password",
            )
            PlainField(
                value = screenScraperUserId,
                onValueChange = { screenScraperUserId = it },
                label = "User ID",
            )
            SecretField(
                value = screenScraperUserPassword,
                onValueChange = { screenScraperUserPassword = it },
                label = "User password",
            )

            Spacer(Modifier.height(14.dp))
            Text("RetroAchievements", style = MaterialTheme.typography.titleMedium)
            PlainField(
                value = retroAchievementsUsername,
                onValueChange = { retroAchievementsUsername = it },
                label = "Username",
            )
            SecretField(
                value = retroAchievementsApiKey,
                onValueChange = { retroAchievementsApiKey = it },
                label = "Web API key",
            )

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val nextCredentials = ProviderCredentials(
                            steamGridDbApiKey = steamGridDbApiKey,
                            screenScraperDeveloperId = screenScraperDeveloperId,
                            screenScraperDeveloperPassword = screenScraperDeveloperPassword,
                            screenScraperUserId = screenScraperUserId,
                            screenScraperUserPassword = screenScraperUserPassword,
                            retroAchievementsUsername = retroAchievementsUsername,
                            retroAchievementsApiKey = retroAchievementsApiKey,
                        )
                        coroutineScope.launch {
                            isTesting = true
                            status = artworkRepository.testConnections(nextCredentials)
                            isTesting = false
                        }
                    },
                ) {
                    Text(if (isTesting) "Testing..." else "Test")
                }
                Button(
                    onClick = {
                        onSave(
                            ProviderCredentials(
                                steamGridDbApiKey = steamGridDbApiKey,
                                screenScraperDeveloperId = screenScraperDeveloperId,
                                screenScraperDeveloperPassword = screenScraperDeveloperPassword,
                                screenScraperUserId = screenScraperUserId,
                                screenScraperUserPassword = screenScraperUserPassword,
                                retroAchievementsUsername = retroAchievementsUsername,
                                retroAchievementsApiKey = retroAchievementsApiKey,
                            ),
                        )
                    },
                ) {
                    Text("Save")
                }
                Button(onClick = onClear) {
                    Text("Clear")
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("SteamGridDB: ${status.steamGridDb}", style = MaterialTheme.typography.bodyMedium)
            Text("ScreenScraper: ${status.screenScraper}", style = MaterialTheme.typography.bodyMedium)
            Text("RetroAchievements: ${status.retroAchievements}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PlainField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    )
}

@Composable
private fun SecretField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    )
}

@Composable
private fun SidePanel(
    alignment: Alignment,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(alignment)
                .fillMaxHeight()
                .width(340.dp),
            color = Color.White,
            shadowElevation = 18.dp,
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                content()
            }
        }
    }
}

@Composable
private fun SortRow(sort: GameSort, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) CalicoBlueLight else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(sort.label)
        if (selected) Icon(Icons.Default.Star, contentDescription = "Selected", tint = CalicoBlue)
    }
}

@Composable
private fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = CalicoBlue)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PillIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(
        color = CalicoPanel,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private val GameSort.label: String
    get() = when (this) {
        GameSort.Console -> "Console"
        GameSort.Name -> "Name"
        GameSort.LastPlayed -> "Last played"
        GameSort.TotalHours -> "Total hours"
        GameSort.Favorites -> "Favorites"
    }

private fun Int.floorMod(size: Int): Int {
    if (size == 0) return 0
    val result = this % size
    return if (result < 0) result + size else result
}

@Preview(showBackground = true, widthDp = 960, heightDp = 720)
@Composable
private fun CalicoLauncherPreview() {
    CalicoTheme {
        CalicoLauncherApp(EmulatorRegistry())
    }
}
