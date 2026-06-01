package com.calico.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.itemsIndexed as lazyItemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.calico.launcher.data.CalicoDatabase
import com.calico.launcher.data.GameFavoriteStore
import com.calico.launcher.data.GameLibraryLoadResult
import com.calico.launcher.data.GameLibraryLoader
import com.calico.launcher.data.MusicLibraryLoader
import com.calico.launcher.data.SAMPLE_GAMES
import com.calico.launcher.data.SAMPLE_TASKBAR_ITEMS
import com.calico.launcher.emulators.EmulatorRegistry
import com.calico.launcher.model.Game
import com.calico.launcher.model.GameFileType
import com.calico.launcher.model.GameSort
import com.calico.launcher.model.MusicTrack
import com.calico.launcher.model.Platform
import com.calico.launcher.model.TaskbarItem
import com.calico.launcher.providers.ArtworkCache
import com.calico.launcher.providers.CredentialStore
import com.calico.launcher.providers.GameArtwork
import com.calico.launcher.providers.GameArtworkRepository
import com.calico.launcher.providers.ProviderConnectionStatus
import com.calico.launcher.providers.ProviderCredentials
import com.calico.launcher.providers.RetroAchievement
import com.calico.launcher.providers.RetroAchievementsSummary
import com.calico.launcher.ui.theme.CalicoBlue
import com.calico.launcher.ui.theme.CalicoBlueLight
import com.calico.launcher.ui.theme.CalicoDim
import com.calico.launcher.ui.theme.CalicoInk
import com.calico.launcher.ui.theme.CalicoPanel
import com.calico.launcher.ui.theme.CalicoScreen
import com.calico.launcher.ui.theme.CalicoTheme
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.compose.AsyncImage
import coil.decode.BitmapFactoryDecoder
import coil.decode.GifDecoder
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min
import kotlin.random.Random

class MainActivity : ComponentActivity(), ImageLoaderFactory {
    private val emulatorRegistry = EmulatorRegistry()
    private var usePhysicalDualScreen by mutableStateOf(false)
    private var gamepadKeyHandler: ((Int) -> Boolean)? = null
    private val handledGamepadKeyDowns = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
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
                    setGamepadKeyHandler = { gamepadKeyHandler = it },
                )
            }
        }
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
                add(GifDecoder.Factory())
            }
            .build()

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    fun keepImmersiveMode() {
        hideSystemBars()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val handled = when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount == 0 && gamepadKeyHandler?.invoke(event.keyCode) == true) {
                    handledGamepadKeyDowns += event.keyCode
                    true
                } else {
                    event.keyCode in handledGamepadKeyDowns || super.dispatchKeyEvent(event)
                }
            }
            KeyEvent.ACTION_UP -> {
                if (handledGamepadKeyDowns.remove(event.keyCode)) {
                    true
                } else {
                    super.dispatchKeyEvent(event)
                }
            }
            else -> super.dispatchKeyEvent(event)
        }
        if (handled && event.action == KeyEvent.ACTION_DOWN) {
            hideSystemBars()
        }
        return handled
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

@Composable
fun CalicoLauncherApp(
    emulatorRegistry: EmulatorRegistry,
    usePhysicalDualScreen: Boolean = false,
    games: List<Game> = SAMPLE_GAMES,
    taskbarItems: List<TaskbarItem> = SAMPLE_TASKBAR_ITEMS,
    setGamepadKeyHandler: (((Int) -> Boolean)?) -> Unit = {},
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var selectedSort by remember { mutableStateOf(GameSort.Favorites) }
    var showSort by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var showCredentials by remember { mutableStateOf(false) }
    var activeMenuAction by remember { mutableStateOf<MenuActionInfo?>(null) }
    var selectedMenuItemIndex by remember { mutableIntStateOf(0) }
    var retroAchievementsSummaries by remember { mutableStateOf<List<RetroAchievementsSummary>?>(null) }
    var selectedRetroAchievementsSummary by remember { mutableStateOf<RetroAchievementsSummary?>(null) }
    var selectedRetroAchievementsGameIndex by remember { mutableIntStateOf(0) }
    var selectedRetroAchievementIndex by remember { mutableIntStateOf(0) }
    var retroAchievementsSort by remember { mutableStateOf(RetroAchievementsSort.ByPlatform) }
    var isLoadingRetroAchievements by remember { mutableStateOf(false) }
    var isLoadingArtwork by remember { mutableStateOf(false) }
    var musicIndex by remember { mutableIntStateOf(0) }
    var isMusicPlaying by remember { mutableStateOf(false) }
    var isMusicLooping by remember { mutableStateOf(false) }
    var isMusicShuffleEnabled by remember { mutableStateOf(false) }
    var isCurrentSongFavorite by remember { mutableStateOf(false) }
    var musicTracks by remember { mutableStateOf<List<MusicTrack>>(emptyList()) }
    var showApps by remember { mutableStateOf(false) }
    var showWallpapers by remember { mutableStateOf(false) }
    var wallpaperUri by remember { mutableStateOf<String?>(null) }
    var consoleFoldersEnabled by remember { mutableStateOf(false) }
    var activeConsoleFolder by remember { mutableStateOf<ConsoleFolder?>(null) }
    var screensSwapped by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    val keepImmersiveMode = {
        (context as? MainActivity)?.keepImmersiveMode()
    }
    val coroutineScope = rememberCoroutineScope()
    val wallpaperStore = remember(context, inPreview) {
        if (inPreview) null else WallpaperStore(context)
    }
    val consoleFolderStore = remember(context, inPreview) {
        if (inPreview) null else ConsoleFolderStore(context)
    }
    val screenLayoutStore = remember(context, inPreview) {
        if (inPreview) null else ScreenLayoutStore(context)
    }
    val sfxPlayer = remember(context, inPreview) {
        if (inPreview) null else CalicoSfxPlayer(context)
    }
    val musicPlayer = remember(context, inPreview) {
        if (inPreview) null else CalicoMusicPlayer(context)
    }
    val emulationRootStore = remember(context, inPreview) {
        if (inPreview) null else EmulationRootStore(context)
    }
    var libraryGames by remember { mutableStateOf<List<Game>?>(null) }
    var isScanningLibrary by remember { mutableStateOf(false) }
    val taskbarStore = remember(context, inPreview) {
        if (inPreview) null else TaskbarStore(context)
    }
    val taskbarApps = remember { mutableStateListOf<String>() }
    val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val uriStr = it.toString()
            wallpaperUri = uriStr
            wallpaperStore?.save(uriStr)
            showWallpapers = false
        }
    }
    val credentialStore = remember(context, inPreview) {
        if (inPreview) null else CredentialStore(context)
    }
    val favoriteStore = remember(context, inPreview) {
        if (inPreview) null else GameFavoriteStore(context)
    }
    val artworkCache = remember(context, inPreview) {
        if (inPreview) null else ArtworkCache(context)
    }
    val artworkRepository = remember(artworkCache) { GameArtworkRepository(artworkCache = artworkCache) }
    var credentials by remember(context, inPreview) {
        mutableStateOf(credentialStore?.load() ?: ProviderCredentials())
    }
    LaunchedEffect(wallpaperStore) {
        wallpaperUri = wallpaperStore?.load()
    }
    LaunchedEffect(consoleFolderStore) {
        consoleFoldersEnabled = consoleFolderStore?.loadEnabled() ?: false
    }
    LaunchedEffect(screenLayoutStore) {
        screensSwapped = screenLayoutStore?.loadScreensSwapped() ?: false
    }
    LaunchedEffect(taskbarStore) {
        taskbarApps.clear()
        taskbarApps.addAll(taskbarStore?.load().orEmpty())
    }
    val artworkByGame = remember { mutableStateMapOf<Int, GameArtwork>() }
    val favoriteOverrides = remember(favoriteStore) {
        mutableStateMapOf<Int, Boolean>().apply {
            putAll(favoriteStore?.loadOverrides().orEmpty())
        }
    }

    val favoriteSnapshot = favoriteOverrides.toMap()
    val catalogGames = libraryGames ?: games
    val displayGames = remember(catalogGames, favoriteSnapshot) {
        catalogGames.map { game ->
            favoriteSnapshot[game.id]?.let { isFavorite -> game.copy(isFavorite = isFavorite) } ?: game
        }
    }
    val sortedGames = remember(displayGames, selectedSort) {
        sortGames(displayGames, selectedSort)
    }
    val consoleFolders = remember(displayGames) {
        buildConsoleFolders(displayGames)
    }
    val folderGames = remember(displayGames, activeConsoleFolder, selectedSort) {
        activeConsoleFolder?.let { folder ->
            sortGames(filterGamesForFolder(displayGames, folder), selectedSort)
        }.orEmpty()
    }
    val gridItems = remember(consoleFoldersEnabled, activeConsoleFolder, sortedGames, consoleFolders) {
        when {
            consoleFoldersEnabled && activeConsoleFolder == null ->
                consoleFolders.map { LauncherGridItem.Folder(it) }
            consoleFoldersEnabled && activeConsoleFolder != null ->
                folderGames.map { LauncherGridItem.Game(it) }
            else -> sortedGames.map { LauncherGridItem.Game(it) }
        }
    }
    val safeSelectedIndex = selectedIndex.coerceInList(gridItems.size)
    val selectedGridItem = gridItems.getOrNull(safeSelectedIndex)
    val selectedGame = when (selectedGridItem) {
        is LauncherGridItem.Game -> selectedGridItem.game
        else -> sortedGames.firstOrNull() ?: displayGames.firstOrNull() ?: games.first()
    }
    val selectedFolder = (selectedGridItem as? LauncherGridItem.Folder)?.folder
    val selectedArtwork = artworkByGame[selectedGame.id]
    val sortedRetroAchievementsSummaries = remember(retroAchievementsSummaries, retroAchievementsSort) {
        retroAchievementsSummaries.orEmpty().sortedFor(retroAchievementsSort)
    }
    val nowPlayingLabel = musicTracks.getOrNull(musicIndex)?.let { track ->
        track.artist?.let { "$it — ${track.title}" } ?: track.title
    } ?: "Add music to Emulation/music/"

    DisposableEffect(musicPlayer) {
        onDispose { musicPlayer?.release() }
    }

    LaunchedEffect(isMusicPlaying, musicIndex, musicTracks, isMusicLooping) {
        val track = musicTracks.getOrNull(musicIndex) ?: return@LaunchedEffect
        if (isMusicPlaying) {
            musicPlayer?.play(track, isMusicLooping)
        } else {
            musicPlayer?.pause()
        }
    }

    fun applyLibraryLoadResult(result: GameLibraryLoadResult, musicTrackCount: Int = musicTracks.size) {
        when (result) {
            is GameLibraryLoadResult.Success -> {
                emulationRootStore?.save(result.rootUri)
                libraryGames = result.games
                activeConsoleFolder = null
                selectedIndex = 0
                artworkByGame.clear()
                activeMenuAction = MenuActionInfo(
                    title = if (result.games.isEmpty()) "Emulation Folder Loaded" else "Library Loaded",
                    message = buildString {
                        append("Loaded ${result.games.size} games from your emulation folder.")
                        if (musicTrackCount > 0) {
                            append("\nLoaded $musicTrackCount music tracks from Emulation/music/.")
                        }
                        if (result.games.isEmpty()) {
                            append("\n\nNo games were found under roms/. Platform folders should be named like switch, 3ds, or psp.")
                        }
                    },
                )
            }
            is GameLibraryLoadResult.InvalidRoot -> {
                activeMenuAction = MenuActionInfo(
                    title = "Invalid Emulation Folder",
                    message = buildString {
                        append("Missing required items:\n\n")
                        result.missingPaths.forEach { append("• ").append(it).append('\n') }
                        append("\nExpected layout:\nEmulation/\n  roms/\n  media/\n  music/\n  metadata.db")
                    },
                )
            }
            is GameLibraryLoadResult.Error -> {
                activeMenuAction = MenuActionInfo(
                    title = "Library Scan Failed",
                    message = result.message,
                )
            }
        }
    }

    fun loadEmulationLibrary(rootUri: Uri) {
        isScanningLibrary = true
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                GameLibraryLoader.load(
                    context = context,
                    rootUri = rootUri,
                    favoriteOverrides = favoriteStore?.loadOverrides().orEmpty(),
                )
            }
            val tracks = if (result is GameLibraryLoadResult.Success) {
                withContext(Dispatchers.IO) {
                    MusicLibraryLoader.load(context, rootUri)
                }
            } else {
                emptyList()
            }
            musicPlayer?.release()
            musicTracks = tracks
            musicIndex = 0
            isMusicPlaying = false
            applyLibraryLoadResult(result, tracks.size)
            isScanningLibrary = false
        }
    }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
            showMenu = false
            loadEmulationLibrary(it)
        }
    }

    LaunchedEffect(emulationRootStore, inPreview) {
        if (!inPreview) {
            emulationRootStore?.load()?.let { savedRoot ->
                loadEmulationLibrary(savedRoot)
            }
        }
    }

    fun selectRetroAchievementsItem() {
        if (selectedRetroAchievementsSummary == null) {
            sortedRetroAchievementsSummaries.getOrNull(selectedRetroAchievementsGameIndex)?.let {
                selectedRetroAchievementsSummary = it
                selectedRetroAchievementIndex = 0
            }
        }
    }

    fun backFromRetroAchievements() {
        if (selectedRetroAchievementsSummary != null) {
            selectedRetroAchievementsSummary = null
        } else {
            retroAchievementsSummaries = null
            selectedRetroAchievementsSummary = null
            isLoadingRetroAchievements = false
        }
    }

    fun forceCloseAllOverlays() {
        showDetails = false
        showMenu = false
        showSort = false
        showCredentials = false
        activeMenuAction = null
        showApps = false
        showWallpapers = false
        isLoadingRetroAchievements = false
        retroAchievementsSummaries = null
        selectedRetroAchievementsSummary = null
    }

    fun toggleConsoleFolders() {
        consoleFoldersEnabled = !consoleFoldersEnabled
        consoleFolderStore?.saveEnabled(consoleFoldersEnabled)
        if (!consoleFoldersEnabled) {
            activeConsoleFolder = null
        }
        selectedIndex = 0
    }

    fun toggleScreensSwapped() {
        screensSwapped = !screensSwapped
        screenLayoutStore?.saveScreensSwapped(screensSwapped)
        sfxPlayer?.screenSwap()
    }

    fun openConsoleFolder(folder: ConsoleFolder) {
        activeConsoleFolder = folder
        selectedIndex = 0
        sfxPlayer?.folderOpen()
    }

    fun backFromConsoleFolder() {
        if (activeConsoleFolder != null) {
            activeConsoleFolder = null
            selectedIndex = 0
            sfxPlayer?.folderClose()
        } else {
            sfxPlayer?.error()
            Toast.makeText(context, "Already in home directory", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleConsoleFolderBack() {
        if (showDetails) {
            showDetails = false
            sfxPlayer?.back()
        } else {
            backFromConsoleFolder()
        }
    }

    fun playGridNavigationSfx() {
        sfxPlayer?.navigate()
    }

    fun activateSelectedMenuItem() {
        when (selectedMenuItemIndex) {
            0 -> {
                val nextFavorite = !selectedGame.isFavorite
                favoriteOverrides[selectedGame.id] = nextFavorite
                favoriteStore?.save(selectedGame.id, nextFavorite)
            }
            1 -> {
                showMenu = false
                folderPicker.launch(null)
            }
            2 -> {
                showMenu = false
                showApps = true
            }
            3 -> {
                showMenu = false
                showWallpapers = true
            }
            4 -> {
                showMenu = false
                activeMenuAction = MenuActionInfo(
                    title = "Manual Metadata / Art",
                    message = "Manual edits will override provider metadata and selected artwork for ${selectedGame.name}.",
                )
            }
            5 -> {
                showMenu = false
                showCredentials = true
            }
            6 -> {
                coroutineScope.launch {
                    isLoadingArtwork = true
                    artworkByGame[selectedGame.id] = artworkRepository.loadArtwork(selectedGame, credentials)
                    isLoadingArtwork = false
                }
            }
            7 -> toggleConsoleFolders()
            8 -> toggleScreensSwapped()
        }
        if (selectedMenuItemIndex != 8) {
            sfxPlayer?.menuSelect()
        }
    }

    fun closeAllOverlays(): Boolean {
        val hadOverlay =
            showSort ||
            showMenu ||
            showDetails ||
            showCredentials ||
            activeMenuAction != null ||
            showApps ||
            showWallpapers ||
            isLoadingRetroAchievements ||
            retroAchievementsSummaries != null
        if (!hadOverlay) return false
        forceCloseAllOverlays()
        return true
    }

    fun handleLauncherKey(keyCode: Int): Boolean {
        val achievementsOpen = isLoadingRetroAchievements || retroAchievementsSummaries != null
        if (achievementsOpen) {
            if (isLoadingRetroAchievements) return true
            val summary = selectedRetroAchievementsSummary
            return when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_BUTTON_L1,
                -> {
                    if (summary == null) {
                        selectedRetroAchievementsGameIndex =
                            (selectedRetroAchievementsGameIndex - 1).floorMod(sortedRetroAchievementsSummaries.size)
                    } else {
                        selectedRetroAchievementIndex =
                            (selectedRetroAchievementIndex - 1).floorMod(summary.achievements.size)
                    }
                    sfxPlayer?.navigate()
                    true
                }
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_BUTTON_R1,
                -> {
                    if (summary == null) {
                        selectedRetroAchievementsGameIndex =
                            (selectedRetroAchievementsGameIndex + 1).floorMod(sortedRetroAchievementsSummaries.size)
                    } else {
                        selectedRetroAchievementIndex =
                            (selectedRetroAchievementIndex + 1).floorMod(summary.achievements.size)
                    }
                    sfxPlayer?.navigate()
                    true
                }
                KeyEvent.KEYCODE_BUTTON_A,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_NUMPAD_ENTER,
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_SPACE,
                -> {
                    selectRetroAchievementsItem()
                    sfxPlayer?.menuSelect()
                    true
                }
                KeyEvent.KEYCODE_BUTTON_B,
                KeyEvent.KEYCODE_BACK,
                KeyEvent.KEYCODE_ESCAPE,
                KeyEvent.KEYCODE_DEL,
                -> {
                    backFromRetroAchievements()
                    sfxPlayer?.back()
                    true
                }
                else -> false
            }
        }

        // Close the Apps panel before any other handling
        if (showApps) {
            return when (keyCode) {
                KeyEvent.KEYCODE_BUTTON_X,
                KeyEvent.KEYCODE_X,
                -> {
                    showApps = false
                    showMenu = false
                    showSort = true
                    true
                }
                KeyEvent.KEYCODE_BUTTON_Y,
                KeyEvent.KEYCODE_Y,
                KeyEvent.KEYCODE_BUTTON_B,
                KeyEvent.KEYCODE_BACK,
                KeyEvent.KEYCODE_ESCAPE,
                -> { showApps = false; true }
                else -> false
            }
        }

        // Close the Wallpapers panel before any other handling
        if (showWallpapers) {
            return when (keyCode) {
                KeyEvent.KEYCODE_BUTTON_X,
                KeyEvent.KEYCODE_X,
                -> {
                    showWallpapers = false
                    showMenu = false
                    showSort = true
                    true
                }
                KeyEvent.KEYCODE_BUTTON_Y,
                KeyEvent.KEYCODE_Y,
                KeyEvent.KEYCODE_BUTTON_B,
                KeyEvent.KEYCODE_BACK,
                KeyEvent.KEYCODE_ESCAPE,
                -> { showWallpapers = false; true }
                else -> false
            }
        }

        if (showMenu) {
            return when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_BUTTON_L1,
                -> {
                    selectedMenuItemIndex = (selectedMenuItemIndex - 1).floorMod(HAMBURGER_MENU_ITEM_COUNT)
                    sfxPlayer?.navigate()
                    true
                }
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_BUTTON_R1,
                -> {
                    selectedMenuItemIndex = (selectedMenuItemIndex + 1).floorMod(HAMBURGER_MENU_ITEM_COUNT)
                    sfxPlayer?.navigate()
                    true
                }
                KeyEvent.KEYCODE_BUTTON_X,
                KeyEvent.KEYCODE_X,
                -> {
                    showMenu = false
                    showSort = true
                    true
                }
                KeyEvent.KEYCODE_BUTTON_B,
                KeyEvent.KEYCODE_BACK,
                -> {
                    showMenu = false
                    true
                }
                KeyEvent.KEYCODE_BUTTON_Y,
                KeyEvent.KEYCODE_Y,
                -> {
                    showMenu = false
                    true
                }
                KeyEvent.KEYCODE_BUTTON_A,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_NUMPAD_ENTER,
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_SPACE,
                -> {
                    activateSelectedMenuItem()
                    true
                }
                else -> false
            }
        }

        if (showSort) {
            return when (keyCode) {
                KeyEvent.KEYCODE_BUTTON_X,
                KeyEvent.KEYCODE_X,
                -> {
                    showSort = false
                    true
                }
                KeyEvent.KEYCODE_BUTTON_Y,
                KeyEvent.KEYCODE_Y,
                -> {
                    showSort = false
                    showMenu = true
                    selectedMenuItemIndex = 0
                    true
                }
                else -> false
            }
        }

        if (showCredentials || activeMenuAction != null) {
            return when (keyCode) {
                KeyEvent.KEYCODE_BUTTON_Y,
                KeyEvent.KEYCODE_Y,
                -> {
                    showCredentials = false
                    activeMenuAction = null
                    showMenu = true
                    selectedMenuItemIndex = 0
                    true
                }
                else -> false
            }
        }

        return when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_X,
            -> {
                showApps = false
                showWallpapers = false
                activeMenuAction = null
                showMenu = false
                showSort = true
                true
            }
            KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_Y,
            -> {
                showApps = false
                showWallpapers = false
                activeMenuAction = null
                showSort = false
                showMenu = true
                selectedMenuItemIndex = 0
                true
            }
            KeyEvent.KEYCODE_BUTTON_L1 -> {
                val newIndex = (selectedIndex - 1).floorMod(gridItems.size)
                selectedIndex = newIndex
                playGridNavigationSfx()
                true
            }
            KeyEvent.KEYCODE_BUTTON_R1 -> {
                val newIndex = (selectedIndex + 1).floorMod(gridItems.size)
                selectedIndex = newIndex
                playGridNavigationSfx()
                true
            }
            KeyEvent.KEYCODE_BUTTON_L2 -> {
                if (!showDetails) {
                    sfxPlayer?.menuSelect()
                }
                showDetails = !showDetails
                true
            }
            KeyEvent.KEYCODE_BUTTON_R2 -> {
                val newIndex = (selectedIndex + 12).coerceAtMost(gridItems.lastIndex)
                selectedIndex = newIndex
                playGridNavigationSfx()
                true
            }
            KeyEvent.KEYCODE_BUTTON_A -> {
                when (val item = gridItems.getOrNull(selectedIndex)) {
                    is LauncherGridItem.Folder -> openConsoleFolder(item.folder)
                    is LauncherGridItem.Game -> {
                        sfxPlayer?.gameSelect()
                        emulatorRegistry.launcherFor(item.game)?.launch(context, item.game)
                    }
                    null -> Unit
                }
                true
            }
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BACK,
            -> {
                if (consoleFoldersEnabled) {
                    handleConsoleFolderBack()
                } else {
                    showDetails = false
                    showMenu = false
                    showSort = false
                    showCredentials = false
                    activeMenuAction = null
                    sfxPlayer?.back()
                }
                true
            }
            else -> false
        }
    }

    LaunchedEffect(showSort) {
        if (showSort) {
            keepImmersiveMode()
        }
    }

    var wasSortVisible by remember { mutableStateOf(false) }
    LaunchedEffect(showSort) {
        if (inPreview) return@LaunchedEffect
        when {
            showSort && !wasSortVisible -> sfxPlayer?.xyButtonOpen()
            !showSort && wasSortVisible -> sfxPlayer?.xyButtonClose()
        }
        wasSortVisible = showSort
    }

    var wasMenuVisible by remember { mutableStateOf(false) }
    LaunchedEffect(showMenu) {
        if (inPreview) return@LaunchedEffect
        when {
            showMenu && !wasMenuVisible -> sfxPlayer?.xyButtonOpen()
            !showMenu && wasMenuVisible -> sfxPlayer?.xyButtonClose()
        }
        wasMenuVisible = showMenu
    }

    SideEffect {
        setGamepadKeyHandler(::handleLauncherKey)
    }

    DisposableEffect(Unit) {
        onDispose {
            setGamepadKeyHandler(null)
            sfxPlayer?.release()
        }
    }

    var hasPlayedFrontendLaunchSfx by remember { mutableStateOf(false) }
    LaunchedEffect(sfxPlayer, inPreview) {
        if (!inPreview && sfxPlayer != null && !hasPlayedFrontendLaunchSfx) {
            sfxPlayer.frontendLaunch()
            hasPlayedFrontendLaunchSfx = true
        }
    }

    LaunchedEffect(inPreview) {
        if (!inPreview) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(selectedGame.id, credentials, inPreview, selectedGridItem) {
        if (selectedGridItem !is LauncherGridItem.Game) return@LaunchedEffect
        if (!inPreview && (credentials.hasSteamGridDb || credentials.hasScreenScraper || credentials.hasRetroAchievements)) {
            isLoadingArtwork = true
            artworkByGame[selectedGame.id] = artworkRepository.loadArtwork(selectedGame, credentials)
            isLoadingArtwork = false
        }
    }

    LaunchedEffect(gridItems.size) {
        selectedIndex = selectedIndex.coerceInList(gridItems.size)
    }

    LaunchedEffect(sortedGames, credentials, inPreview) {
        if (!inPreview && credentials.hasSteamGridDb) {
            sortedGames.take(12).forEach { game ->
                if (artworkByGame[game.id]?.iconUrl == null) {
                    artworkByGame[game.id] = artworkRepository.loadArtwork(game, credentials)
                }
            }
        }
    }

    LaunchedEffect(sortedRetroAchievementsSummaries.size) {
        selectedRetroAchievementsGameIndex = selectedRetroAchievementsGameIndex.coerceInList(sortedRetroAchievementsSummaries.size)
    }

    LaunchedEffect(selectedRetroAchievementsSummary?.gameId) {
        selectedRetroAchievementIndex = 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalicoScreen)
            .focusRequester(focusRequester)
            .focusable(),
    ) {
        Column(Modifier.fillMaxSize()) {
            val heroScreenWeight = if (usePhysicalDualScreen) 1920f else 1f
            val gridScreenWeight = if (usePhysicalDualScreen) 1240f else 1f

            @Composable
            fun HeroPane() {
                Box(
                    modifier = Modifier
                        .weight(heroScreenWeight)
                        .fillMaxWidth()
                        .clip(RectangleShape),
                ) {
                TopScreen(
                    selectedGame = selectedGame,
                    selectedFolder = selectedFolder,
                    activeConsoleFolder = activeConsoleFolder,
                    artwork = selectedArtwork,
                    isLoadingArtwork = isLoadingArtwork,
                    showDetails = showDetails,
                    consoleFoldersEnabled = consoleFoldersEnabled,
                    onToggleDetails = {
                        if (!showDetails) {
                            sfxPlayer?.menuSelect()
                        }
                        showDetails = !showDetails
                    },
                    onOpenMenu = {
                        showApps = false
                        showWallpapers = false
                        activeMenuAction = null
                        if (showMenu) {
                            showMenu = false
                        } else {
                            showSort = false
                            showMenu = true
                            selectedMenuItemIndex = 0
                        }
                    },
                    onLaunch = {
                        when (val item = gridItems.getOrNull(selectedIndex)) {
                            is LauncherGridItem.Folder -> openConsoleFolder(item.folder)
                            is LauncherGridItem.Game -> {
                                sfxPlayer?.gameSelect()
                                emulatorRegistry.launcherFor(item.game)?.launch(context, item.game)
                            }
                            null -> Unit
                        }
                    },
                    onBack = { handleConsoleFolderBack() },
                    modifier = Modifier.fillMaxSize(),
                )

                val topOverlayActive =
                    showSort ||
                    showMenu ||
                    showCredentials ||
                    activeMenuAction != null ||
                    showApps ||
                    showWallpapers

                if (topOverlayActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CalicoDim)
                            .clickable { forceCloseAllOverlays() },
                    )
                }

                SortPanel(
                    visible = showSort,
                    selectedSort = selectedSort,
                    onSortSelected = {
                        sfxPlayer?.navigate()
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
                    nowPlaying = nowPlayingLabel,
                    isMusicPlaying = isMusicPlaying,
                    isMusicLooping = isMusicLooping,
                    isMusicShuffleEnabled = isMusicShuffleEnabled,
                    isCurrentSongFavorite = isCurrentSongFavorite,
                    selectedMenuItemIndex = selectedMenuItemIndex,
                    consoleFoldersEnabled = consoleFoldersEnabled,
                    screensSwapped = screensSwapped,
                    onToggleFavorite = {
                        val nextFavorite = !selectedGame.isFavorite
                        favoriteOverrides[selectedGame.id] = nextFavorite
                        favoriteStore?.save(selectedGame.id, nextFavorite)
                    },
                    onToggleConsoleFolders = {
                        toggleConsoleFolders()
                        sfxPlayer?.menuSelect()
                    },
                    onToggleScreensSwapped = ::toggleScreensSwapped,
                    onShowMenuAction = { action ->
                        showMenu = false
                        activeMenuAction = action
                    },
                    onPreviousTrack = {
                        if (musicTracks.isNotEmpty()) {
                            musicIndex = (musicIndex - 1).floorMod(musicTracks.size)
                            isMusicPlaying = true
                        }
                    },
                    onTogglePlayback = {
                        if (musicTracks.isNotEmpty()) {
                            isMusicPlaying = !isMusicPlaying
                        }
                    },
                    onNextTrack = {
                        if (musicTracks.isNotEmpty()) {
                            musicIndex = if (isMusicShuffleEnabled) {
                                Random.nextInt(musicTracks.size)
                            } else {
                                (musicIndex + 1).floorMod(musicTracks.size)
                            }
                            isMusicPlaying = true
                        }
                    },
                    onToggleLoop = {
                        val nextLoop = !isMusicLooping
                        isMusicLooping = nextLoop
                        musicPlayer?.setLooping(nextLoop)
                    },
                    onToggleShuffle = {
                        isMusicShuffleEnabled = !isMusicShuffleEnabled
                    },
                    onSkipTrack = {
                        if (musicTracks.isNotEmpty()) {
                            musicIndex = if (isMusicShuffleEnabled) {
                                Random.nextInt(musicTracks.size)
                            } else {
                                (musicIndex + 1).floorMod(musicTracks.size)
                            }
                            isMusicPlaying = true
                        }
                    },
                    onToggleMusicFavorite = {
                        isCurrentSongFavorite = !isCurrentSongFavorite
                    },
                    onOpenFolderPicker = {
                        showMenu = false
                        folderPicker.launch(null)
                    },
                    onOpenApps = {
                        showMenu = false
                        showApps = true
                    },
                    onOpenWallpapers = {
                        showMenu = false
                        showWallpapers = true
                    },
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

                MenuActionPanel(
                    action = activeMenuAction,
                    onDismiss = { activeMenuAction = null },
                )

                LibraryScanningOverlay(visible = isScanningLibrary)

                AppsPanel(
                    visible = showApps,
                    taskbarApps = taskbarApps,
                    onToggleTaskbarApp = { packageName ->
                        if (taskbarApps.contains(packageName)) {
                            taskbarApps.remove(packageName)
                        } else {
                            taskbarApps.add(packageName)
                        }
                        taskbarStore?.save(taskbarApps.toList())
                    },
                    onDismiss = { showApps = false },
                )

                WallpaperPanel(
                    visible = showWallpapers,
                    wallpaperUri = wallpaperUri,
                    onPickWallpaper = { wallpaperPicker.launch(arrayOf("image/*")) },
                    onClearWallpaper = {
                        wallpaperUri = null
                        wallpaperStore?.clear()
                    },
                    onDismiss = { showWallpapers = false },
                )

                CredentialsPanel(
                    visible = showCredentials,
                    credentials = credentials,
                    artworkRepository = artworkRepository,
                    onSave = { nextCredentials ->
                        credentialStore?.save(nextCredentials)
                        credentials = nextCredentials
                        artworkByGame.clear()
                        showCredentials = false
                    },
                    onClear = {
                        credentialStore?.clear()
                        credentials = ProviderCredentials()
                        artworkByGame.clear()
                    },
                    onDismiss = { showCredentials = false },
                )

                RetroAchievementsPanel(
                    summaries = retroAchievementsSummaries,
                    selectedSummary = selectedRetroAchievementsSummary,
                    selectedSort = retroAchievementsSort,
                    selectedGameIndex = selectedRetroAchievementsGameIndex,
                    selectedAchievementIndex = selectedRetroAchievementIndex,
                    isLoading = isLoadingRetroAchievements,
                    artworkByGame = artworkByGame,
                    onSelectSummary = {
                        selectedRetroAchievementsSummary = it
                        selectedRetroAchievementIndex = 0
                    },
                    onSortSelected = {
                        retroAchievementsSort = it
                        selectedRetroAchievementsGameIndex = 0
                        sfxPlayer?.navigate()
                    },
                    onBackToGames = { selectedRetroAchievementsSummary = null },
                    onSelectPressed = ::selectRetroAchievementsItem,
                    onBackPressed = ::backFromRetroAchievements,
                    onDismiss = {
                        retroAchievementsSummaries = null
                        selectedRetroAchievementsSummary = null
                        isLoadingRetroAchievements = false
                    },
                )
            }
            }

            @Composable
            fun GridPane() {
            BottomScreen(
                gridItems = gridItems,
                selectedIndex = selectedIndex,
                artworkByGame = artworkByGame,
                taskbarApps = taskbarApps,
                wallpaperUri = wallpaperUri,
                onSelectIndex = { index ->
                    if (index != selectedIndex) {
                        playGridNavigationSfx()
                    }
                    selectedIndex = index
                },
                onOpenSort = {
                    keepImmersiveMode()
                    showApps = false
                    showWallpapers = false
                    activeMenuAction = null
                    if (showSort) {
                        showSort = false
                    } else {
                        showMenu = false
                        showSort = true
                    }
                },
                onOpenMenu = {
                    keepImmersiveMode()
                    showWallpapers = false
                    activeMenuAction = null
                    if (showMenu) {
                        showMenu = false
                    } else {
                        showSort = false
                        showMenu = true
                        selectedMenuItemIndex = 0
                    }
                },
                onOpenRetroAchievements = {
                    sfxPlayer?.retroAchievements()
                    showDetails = false
                    showMenu = false
                    showSort = false
                    showCredentials = false
                    activeMenuAction = null
                    retroAchievementsSummaries = null
                    selectedRetroAchievementsSummary = null
                    selectedRetroAchievementsGameIndex = 0
                    selectedRetroAchievementIndex = 0
                    isLoadingRetroAchievements = true
                    coroutineScope.launch {
                        retroAchievementsSummaries = sortedGames.map { game ->
                            artworkRepository.loadRetroAchievements(game, credentials)
                        }.filter { it.gameId != null }
                        isLoadingRetroAchievements = false
                    }
                },
                modifier = Modifier
                    .weight(gridScreenWeight)
                    .fillMaxWidth(),
            )
            }

            if (screensSwapped) {
                GridPane()
                HeroPane()
            } else {
                HeroPane()
                GridPane()
            }
        }
    }
}

private data class MenuActionInfo(
    val title: String,
    val message: String,
)

private enum class RetroAchievementsSort {
    ByPlatform,
    RecentlyEarned,
    Completion,
}

private const val HAMBURGER_MENU_ITEM_COUNT = 9

private fun RetroAchievementsSummary.toPanelMessage(): String {
    val header = buildString {
        append(gameTitle)
        gameId?.let { append(" (#").append(it).append(")") }
        append("\n")
        append(message)
    }
    if (achievements.isEmpty()) return header

    val preview = achievements.take(10).joinToString(separator = "\n\n") { achievement ->
        buildString {
            append("• ")
            append(achievement.title)
            if (achievement.points > 0) append(" (${achievement.points} pts)")
            if (achievement.description.isNotBlank()) {
                append("\n  ")
                append(achievement.description)
            }
        }
    }
    val remaining = achievements.size - 10
    return if (remaining > 0) {
        "$header\n\n$preview\n\n+$remaining more"
    } else {
        "$header\n\n$preview"
    }
}

@Composable
private fun TopScreen(
    selectedGame: Game,
    selectedFolder: ConsoleFolder?,
    activeConsoleFolder: ConsoleFolder?,
    artwork: GameArtwork?,
    isLoadingArtwork: Boolean,
    showDetails: Boolean,
    consoleFoldersEnabled: Boolean,
    onToggleDetails: () -> Unit,
    onOpenMenu: () -> Unit,
    onLaunch: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.18f), Color.Transparent, CalicoBlueLight.copy(alpha = 0.10f)),
                ),
            )
    ) {
        if (selectedFolder != null && activeConsoleFolder == null) {
            FolderHeroCard(
                folder = selectedFolder,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            HeroCard(
                game = selectedGame,
                artwork = artwork,
                isLoadingArtwork = isLoadingArtwork,
                showDetails = showDetails,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (selectedFolder == null || activeConsoleFolder != null) {
            PlaytimePill(
                game = selectedGame,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 18.dp),
            )
        }

        StatusPill(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp),
        )

        ButtonLegend(
            onToggleDetails = onToggleDetails,
            onOpenMenu = onOpenMenu,
            onLaunch = onLaunch,
            showBackButton = consoleFoldersEnabled,
            onBack = onBack,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 28.dp),
        )
    }
}

@Composable
private fun BottomScreen(
    gridItems: List<LauncherGridItem>,
    selectedIndex: Int,
    artworkByGame: Map<Int, GameArtwork>,
    taskbarApps: List<String>,
    wallpaperUri: String?,
    onSelectIndex: (Int) -> Unit,
    onOpenSort: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenRetroAchievements: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        // Background: wallpaper image or default gradient
        if (wallpaperUri != null) {
            AsyncImage(
                model = wallpaperUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.28f)),
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.22f), Color(0xFFF8FBFF), Color(0xFFEFF6FF)),
                        ),
                    ),
            )
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 92.dp),
        ) {
            val columns = 4
            val visibleRows = 3
            val horizontalGap = 8.dp
            val verticalGap = 8.dp
            val gridEdgePadding = 6.dp
            val cellWidth = (maxWidth - gridEdgePadding * 2f - horizontalGap * (columns - 1)) / columns
            val cellHeight = (maxHeight - verticalGap * (visibleRows - 1)) / visibleRows
            val tileSize = min(cellWidth.value, cellHeight.value).dp
            val iconFrameSize = (tileSize.value * 0.92f).dp

            LazyHorizontalGrid(
                rows = GridCells.Fixed(visibleRows),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = gridEdgePadding),
                horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                verticalArrangement = Arrangement.spacedBy(verticalGap),
            ) {
                items(gridItems.size, key = { index ->
                    when (val item = gridItems[index]) {
                        is LauncherGridItem.Game -> "game-${item.game.id}"
                        is LauncherGridItem.Folder -> "folder-${item.folder.id}"
                    }
                }) { index ->
                    when (val item = gridItems[index]) {
                        is LauncherGridItem.Game -> GameTile(
                            game = item.game,
                            artwork = artworkByGame[item.game.id],
                            selected = index == selectedIndex,
                            tileSize = tileSize,
                            iconFrameSize = iconFrameSize,
                            onClick = { onSelectIndex(index) },
                        )
                        is LauncherGridItem.Folder -> FolderTile(
                            folder = item.folder,
                            selected = index == selectedIndex,
                            tileSize = tileSize,
                            iconFrameSize = iconFrameSize,
                            onClick = { onSelectIndex(index) },
                        )
                    }
                }
            }
        }

        BottomDock(
            taskbarApps = taskbarApps,
            onOpenSort = onOpenSort,
            onOpenMenu = onOpenMenu,
            onOpenRetroAchievements = onOpenRetroAchievements,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        )
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
    val heroModel = rememberArtworkImageModel(game.heroUri ?: artwork?.heroUrl ?: artwork?.screenshotUrl)

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(CalicoBlue.copy(alpha = 0.72f), Color(0xFFF4F8FF)),
                ),
            ),
    ) {
        if (heroModel != null) {
            AsyncImage(
                model = heroModel,
                contentDescription = "${game.name} hero artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }

        SoftImageVignette()

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.58f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val logoModel = rememberArtworkImageModel(artwork?.logoUrl)
            if (logoModel != null) {
                AsyncImage(
                    model = logoModel,
                    contentDescription = "${game.name} logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 96.dp),
                )
            } else {
                Text(
                    text = game.name,
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }

        AnimatedVisibility(
            visible = showDetails,
            enter = slideInHorizontally { -it } + fadeIn(tween(220)),
            exit = slideOutHorizontally { -it } + fadeOut(tween(180)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.52f)
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.00f to Color.Black.copy(alpha = 0.92f),
                                0.60f to Color.Black.copy(alpha = 0.60f),
                                1.00f to Color.Transparent,
                            ),
                        ),
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 28.dp, top = 72.dp, end = 36.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Platform logo image; fall back to text if asset missing
                    val platformLogoPath = game.platform.platformLogoAssetPath()
                    AsyncImage(
                        model = platformLogoPath,
                        contentDescription = "${game.platform.name} logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .heightIn(max = 52.dp)
                            .widthIn(max = 160.dp),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        game.name,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    game.releaseDate?.take(4)?.let { year ->
                        Text(
                            year,
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    if (!game.description.isNullOrBlank()) {
                        Text(
                            game.description!!,
                            color = Color.White.copy(alpha = 0.82f),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    val devText = game.developers.joinToString()
                    if (devText.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            devText,
                            color = Color.White.copy(alpha = 0.55f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        if (isLoadingArtwork) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(18.dp)
                    .size(28.dp),
                color = CalicoBlue,
                strokeWidth = 3.dp,
            )
        }
    }
}

@Composable
private fun BoxScope.SoftImageVignette() {
    // Full-span gradients: each gradient spans the ENTIRE canvas so there is no
    // visible rectangular end-boundary — the gradient just fades to transparent
    // naturally in the middle. Corners receive two overlapping gradients, making
    // them slightly brighter (correct vignette behaviour).
    Canvas(modifier = Modifier.matchParentSize()) {
        val w = size.width
        val h = size.height
        val white = Color.White

        // Top: full height, opaque at top → transparent at ~50%
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to white.copy(alpha = 0.52f),
                    0.12f to white.copy(alpha = 0.36f),
                    0.24f to white.copy(alpha = 0.18f),
                    0.36f to white.copy(alpha = 0.07f),
                    0.48f to white.copy(alpha = 0.01f),
                    1.00f to Color.Transparent,
                ),
                startY = 0f,
                endY = h,
            ),
        )
        // Bottom: full height, transparent at ~50% → opaque at bottom
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to Color.Transparent,
                    0.52f to white.copy(alpha = 0.01f),
                    0.64f to white.copy(alpha = 0.07f),
                    0.76f to white.copy(alpha = 0.18f),
                    0.88f to white.copy(alpha = 0.36f),
                    1.00f to white.copy(alpha = 0.52f),
                ),
                startY = 0f,
                endY = h,
            ),
        )
        // Left: full width, opaque at left → transparent at ~45%
        drawRect(
            brush = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0.00f to white.copy(alpha = 0.46f),
                    0.10f to white.copy(alpha = 0.30f),
                    0.22f to white.copy(alpha = 0.14f),
                    0.34f to white.copy(alpha = 0.04f),
                    0.46f to white.copy(alpha = 0.01f),
                    1.00f to Color.Transparent,
                ),
                startX = 0f,
                endX = w,
            ),
        )
        // Right: full width, transparent at ~55% → opaque at right
        drawRect(
            brush = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0.00f to Color.Transparent,
                    0.54f to white.copy(alpha = 0.01f),
                    0.66f to white.copy(alpha = 0.04f),
                    0.78f to white.copy(alpha = 0.14f),
                    0.90f to white.copy(alpha = 0.30f),
                    1.00f to white.copy(alpha = 0.46f),
                ),
                startX = 0f,
                endX = w,
            ),
        )
    }
}


@Composable
private fun PlaytimePill(game: Game, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = CalicoPanel,
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = CalicoInk,
                modifier = Modifier.size(21.dp),
            )
            Text(
                text = "${game.hoursPlayed} Hours Played",
                color = CalicoInk,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun StatusPill(modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    val battery = remember(context, inPreview) {
        if (inPreview) {
            100
        } else {
            context.getSystemService(BatteryManager::class.java)
                ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                ?.takeIf { it >= 0 } ?: 0
        }
    }

    LaunchedEffect(inPreview) {
        if (!inPreview) {
            while (true) {
                now = LocalDateTime.now()
                delay(30_000)
            }
        }
    }

    Surface(
        modifier = modifier,
        color = CalicoPanel,
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(now.format(DateTimeFormatter.ofPattern("HH:mm")), fontWeight = FontWeight.Bold)
            Text("|", fontWeight = FontWeight.Bold)
            Text(now.format(DateTimeFormatter.ofPattern("M/d")), fontWeight = FontWeight.Bold)
            Text("|", fontWeight = FontWeight.Bold)
            Text("$battery%", fontWeight = FontWeight.Bold)
            SegmentedBatteryIcon(percent = battery)
        }
    }
}

@Composable
private fun SegmentedBatteryIcon(percent: Int) {
    val filledBars = when {
        percent >= 80 -> 3
        percent >= 45 -> 2
        percent >= 15 -> 1
        else -> 0
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 1.5.dp, height = 5.dp)
                .background(CalicoInk, RoundedCornerShape(topStart = 1.dp, bottomStart = 1.dp)),
        )
        Box(
            modifier = Modifier
                .size(width = 20.dp, height = 10.dp)
                .border(1.2.dp, CalicoInk, RoundedCornerShape(1.dp))
                .padding(horizontal = 2.dp, vertical = 2.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (index < filledBars) CalicoInk else Color.Transparent,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopScreenControls(
    onOpenSort: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = CalicoPanel,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onOpenSort, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.SwapVert, contentDescription = "Sort and filter", tint = CalicoInk)
            }
            IconButton(onClick = onOpenSort, modifier = Modifier.size(32.dp)) {
                ControllerButtonGlyph("X", size = 22.dp, color = CalicoInk, contentColor = Color.White)
            }
            IconButton(onClick = onOpenMenu, modifier = Modifier.size(32.dp)) {
                ControllerButtonGlyph("Y", size = 22.dp, color = CalicoInk, contentColor = Color.White)
            }
            IconButton(onClick = onOpenMenu, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = CalicoInk)
            }
        }
    }
}

@Composable
private fun ButtonLegend(
    onToggleDetails: () -> Unit,
    onOpenMenu: () -> Unit,
    onLaunch: () -> Unit,
    showBackButton: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = CalicoPanel,
        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 22.dp, top = 9.dp, bottom = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendAction("A", "Select", onLaunch)
            if (showBackButton) {
                LegendAction("B", "Back", onBack)
            }
            LegendAction("-", "Details", onToggleDetails)
            LegendAction("+", "Menu", onOpenMenu)
        }
    }
}

@Composable
private fun LegendAction(
    button: String,
    label: String,
    onClick: () -> Unit,
) {
    val legendInk = CalicoInk
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ControllerButtonGlyph(button, size = 20.dp, color = Color.Transparent, contentColor = legendInk)
        Text(
            text = label,
            color = legendInk,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun ControllerButtonGlyph(
    text: String,
    size: Dp,
    color: Color,
    contentColor: Color,
) {
    val glyphFontSize = when (text) {
        "-" -> 18.sp
        "+" -> 16.sp
        else -> 12.sp
    }
    val borderColor = if (color == Color.Transparent) contentColor else Color.Transparent

    Box(
        modifier = Modifier
            .size(size)
            .background(color, CircleShape)
            .border(0.6.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = glyphFontSize,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = glyphFontSize,
            textAlign = TextAlign.Center,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                baselineShift = BaselineShift(0f),
            ),
        )
    }
}

@Composable
private fun rememberArtworkImageModel(model: Any?): Any? {
    val context = LocalContext.current
    return remember(context, model) {
        if (model is String && model.startsWith("http")) {
            ImageRequest.Builder(context)
                .data(model)
                .addHeader("Accept", "image/avif,image/webp,image/png,image/jpeg,*/*")
                .addHeader("User-Agent", "CalicoLauncher/0.1")
                .crossfade(true)
                .build()
        } else {
            model
        }
    }
}

@Composable
private fun GameTile(
    game: Game,
    artwork: GameArtwork?,
    selected: Boolean,
    tileSize: Dp,
    iconFrameSize: Dp,
    onClick: () -> Unit,
) {
    val wiggle by animateFloatAsState(targetValue = if (selected) 0f else 0f, label = "wiggle")
    val inPreview = LocalInspectionMode.current
    val overlayAssetPath = game.platform.overlayAssetPath()
    val tileArtwork = game.iconUri ?: artwork?.iconUrl ?: game.heroUri ?: artwork?.heroUrl ?: artwork?.screenshotUrl
    val tileArtworkModel = rememberArtworkImageModel(tileArtwork)
    val selectedBorderWidth = 4.dp
    val selectedBorderOutset = selectedBorderWidth
    val selectedBorderSize = iconFrameSize + selectedBorderOutset * 2f
    val selectedBorderRadius = (iconFrameSize.value * 0.075f).dp + selectedBorderOutset
    val favoritePadding = (tileSize.value * 0.11f).dp
    val favoriteSize = (iconFrameSize.value * 0.18f).dp
    val favoriteScale by animateFloatAsState(
        targetValue = if (selected) 1.20f else 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "favoriteScale",
    )
    val selectionAnimation = tween<Float>(durationMillis = 180, easing = FastOutSlowInEasing)
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.10f else 1f,
        animationSpec = selectionAnimation,
        label = "iconScale",
    )
    val selectedBorderAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = selectionAnimation,
        label = "selectedBorderAlpha",
    )
    val iconFrameShadowColor by animateColorAsState(
        targetValue = if (selected) CalicoBlue.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.18f),
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "iconFrameShadowColor",
    )

    Column(
        modifier = Modifier
            .graphicsLayer(rotationZ = wiggle)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(tileSize),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(iconFrameSize)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
                    .shadow(
                        elevation = if (selected) 18.dp else 3.dp,
                        shape = RoundedCornerShape(14.dp),
                        clip = false,
                        ambientColor = iconFrameShadowColor,
                        spotColor = iconFrameShadowColor,
                    )
                    .background(Color.White, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (tileArtworkModel != null) {
                        AsyncImage(
                            model = tileArtworkModel,
                            contentDescription = "${game.name} icon",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize(),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFDFEFF), Color(0xFFEAF4FF)),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = game.name.initials(),
                                style = MaterialTheme.typography.titleMedium,
                                color = CalicoInk,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                if (inPreview) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(3.dp, CalicoBlue.copy(alpha = 0.45f), RoundedCornerShape(selectedBorderRadius)),
                    )
                } else {
                    AsyncImage(
                        model = overlayAssetPath,
                        contentDescription = "${game.platform.name} border overlay",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.matchParentSize(),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(selectedBorderSize)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        alpha = selectedBorderAlpha
                    }
                    .border(
                        width = selectedBorderWidth,
                        brush = achievementSelectionBrush(),
                        shape = RoundedCornerShape(selectedBorderRadius),
                    ),
            )
            if (game.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite",
                    tint = CalicoBlue,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(favoritePadding)
                        .size(favoriteSize)
                        .graphicsLayer {
                            scaleX = favoriteScale
                            scaleY = favoriteScale
                        },
                )
            }
        }
    }
}

@Composable
private fun DockClickTarget(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
private fun BottomDock(
    taskbarApps: List<String>,
    onOpenSort: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenRetroAchievements: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .zIndex(10f)
            .fillMaxWidth()
            .height(58.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Surface(
            modifier = Modifier.height(58.dp),
            color = CalicoPanel,
            shape = RoundedCornerShape(topEnd = 14.dp),
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier.padding(start = 18.dp, end = 12.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DockClickTarget(onClick = onOpenSort, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = "Sort and filter",
                        tint = CalicoInk,
                        modifier = Modifier.size(22.dp),
                    )
                }
                DockClickTarget(onClick = onOpenSort, modifier = Modifier.size(34.dp)) {
                    ControllerButtonGlyph("X", size = 22.dp, color = CalicoInk, contentColor = Color.White)
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Taskbar(
                taskbarApps = taskbarApps,
                onOpenRetroAchievements = onOpenRetroAchievements,
            )
        }

        Surface(
            modifier = Modifier.height(58.dp),
            color = CalicoPanel,
            shape = RoundedCornerShape(topStart = 14.dp),
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 18.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DockClickTarget(onClick = onOpenMenu, modifier = Modifier.size(34.dp)) {
                    ControllerButtonGlyph("Y", size = 22.dp, color = CalicoInk, contentColor = Color.White)
                }
                DockClickTarget(onClick = onOpenMenu, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = CalicoInk)
                }
            }
        }
    }
}

@Composable
private fun Taskbar(
    taskbarApps: List<String>,
    onOpenRetroAchievements: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier,
        color = CalicoPanel,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DockGlyphButton(onClick = {}, contentDescription = "Apps") {
                NineDotGlyph(Modifier.size(23.dp))
            }
            DockGlyphButton(onClick = onOpenRetroAchievements, contentDescription = "RetroAchievements") {
                TrophyGlyph(Modifier.size(24.dp))
            }
            taskbarApps.forEach { packageName ->
                val icon = remember(packageName) { 
                    try { context.packageManager.getApplicationIcon(packageName) } catch (e: Exception) { null } 
                }
                if (icon != null) {
                    DockGlyphButton(
                        onClick = { context.packageManager.getLaunchIntentForPackage(packageName)?.let { context.startActivity(it) } },
                        contentDescription = packageName,
                    ) {
                        AsyncImage(model = icon, contentDescription = null, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DockGlyphButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(42.dp),
    ) {
        Box(
            modifier = Modifier.semantics { this.contentDescription = contentDescription },
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun NineDotGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val iconColor = Color(0xFFE91E4D)
        val dotRadius = size.minDimension * 0.095f
        val spacing = size.minDimension * 0.31f
        val strokeWidth = size.minDimension * 0.085f
        val start = Offset(
            x = (size.width - spacing * 2f) / 2f,
            y = (size.height - spacing * 2f) / 2f,
        )
        repeat(3) { row ->
            repeat(3) { column ->
                val center = Offset(start.x + spacing * column, start.y + spacing * row)
                drawCircle(
                    color = iconColor,
                    radius = dotRadius,
                    center = center,
                    style = Stroke(width = strokeWidth),
                )
            }
        }
    }
}

@Composable
private fun TrophyGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val iconColor = Color(0xFFFFA22E)
        val strokeWidth = size.minDimension * 0.105f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        val cupTopLeft = Offset(size.width * 0.31f, size.height * 0.18f)
        val cupSize = Size(size.width * 0.38f, size.height * 0.31f)
        drawRoundRect(
            color = iconColor,
            topLeft = cupTopLeft,
            size = cupSize,
            cornerRadius = CornerRadius(size.minDimension * 0.08f),
            style = stroke,
        )
        drawLine(
            color = iconColor,
            start = Offset(size.width * 0.31f, size.height * 0.27f),
            end = Offset(size.width * 0.16f, size.height * 0.27f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = iconColor,
            start = Offset(size.width * 0.16f, size.height * 0.27f),
            end = Offset(size.width * 0.21f, size.height * 0.43f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = iconColor,
            start = Offset(size.width * 0.69f, size.height * 0.27f),
            end = Offset(size.width * 0.84f, size.height * 0.27f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = iconColor,
            start = Offset(size.width * 0.84f, size.height * 0.27f),
            end = Offset(size.width * 0.79f, size.height * 0.43f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = iconColor,
            start = Offset(size.width * 0.5f, size.height * 0.50f),
            end = Offset(size.width * 0.5f, size.height * 0.70f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = iconColor,
            start = Offset(size.width * 0.35f, size.height * 0.72f),
            end = Offset(size.width * 0.65f, size.height * 0.72f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = iconColor,
            start = Offset(size.width * 0.28f, size.height * 0.84f),
            end = Offset(size.width * 0.72f, size.height * 0.84f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun OverlayPanelAnchor(
    visible: Boolean,
    alignment: Alignment,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.align(alignment)) {
                content()
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
    OverlayPanelAnchor(visible = visible, alignment = Alignment.CenterStart) {
        val context = LocalContext.current

        // Collect platform folders from virtual_consoles (no by_platform subfolder)
        val vcPlatformFolders = remember {
            try {
                context.assets.list("virtual_consoles")
                    ?.filter { !it.startsWith(".") }
                    ?.toList() ?: emptyList()
            } catch (e: Exception) { emptyList() }
        }

        // Auto-cycle index for virtual consoles carousel
        var vcIndex by remember { mutableIntStateOf(0) }
        LaunchedEffect(vcPlatformFolders) {
            if (vcPlatformFolders.isNotEmpty()) {
                while (true) {
                    delay(5_000)
                    vcIndex = (vcIndex + 1) % vcPlatformFolders.size
                }
            }
        }

        SortSidePanel {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            ) {
                SortPanelHero(selectedSort = selectedSort)
                Spacer(Modifier.height(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameSort.entries.forEach { sort ->
                        SortRow(
                            sort = sort,
                            selected = sort == selectedSort,
                            onClick = { onSortSelected(sort) },
                        )
                    }
                }
                if (vcPlatformFolders.isNotEmpty()) {
                    val currentFolder = vcPlatformFolders[vcIndex]
                    val vcExtensions = listOf("gif", "webp", "png")
                    val vcLogoPath = remember(currentFolder) {
                        vcExtensions.firstNotNullOfOrNull { ext ->
                            val path = "virtual_consoles/$currentFolder/logo.$ext"
                            try {
                                context.assets.open(path).close()
                                "file:///android_asset/$path"
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }
                    if (vcLogoPath != null) {
                        AsyncImage(
                            model = vcLogoPath,
                            contentDescription = currentFolder,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
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
    nowPlaying: String,
    isMusicPlaying: Boolean,
    isMusicLooping: Boolean,
    isMusicShuffleEnabled: Boolean,
    isCurrentSongFavorite: Boolean,
    selectedMenuItemIndex: Int,
    consoleFoldersEnabled: Boolean,
    screensSwapped: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleConsoleFolders: () -> Unit,
    onToggleScreensSwapped: () -> Unit,
    onShowMenuAction: (MenuActionInfo) -> Unit,
    onPreviousTrack: () -> Unit,
    onTogglePlayback: () -> Unit,
    onNextTrack: () -> Unit,
    onToggleLoop: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSkipTrack: () -> Unit,
    onToggleMusicFavorite: () -> Unit,
    onOpenFolderPicker: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenWallpapers: () -> Unit,
    onRefreshArtwork: () -> Unit,
    onOpenCredentials: () -> Unit,
    onDismiss: () -> Unit,
) {
    OverlayPanelAnchor(visible = visible, alignment = Alignment.CenterEnd) {
        SidePanel(alignment = Alignment.CenterEnd, onDismiss = onDismiss) {
            Text("Now Playing", style = MaterialTheme.typography.titleLarge)
            Text(
                if (isMusicPlaying) nowPlaying else "$nowPlaying paused",
                color = CalicoInk.copy(alpha = 0.72f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = onPreviousTrack) { Icon(Icons.Default.FastRewind, contentDescription = "Back") }
                IconButton(onClick = onTogglePlayback) { Icon(Icons.Default.PlayArrow, contentDescription = if (isMusicPlaying) "Pause" else "Play") }
                IconButton(onClick = onNextTrack) { Icon(Icons.Default.FastForward, contentDescription = "Forward") }
                IconButton(onClick = onToggleLoop) {
                    Icon(
                        Icons.Default.Loop,
                        contentDescription = "Loop",
                        tint = if (isMusicLooping) CalicoBlue else CalicoInk,
                    )
                }
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isMusicShuffleEnabled) CalicoBlue else CalicoInk,
                    )
                }
                IconButton(onClick = onSkipTrack) { Icon(Icons.Default.SkipNext, contentDescription = "Skip") }
                IconButton(onClick = onToggleMusicFavorite) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = if (isCurrentSongFavorite) CalicoBlue else CalicoInk,
                    )
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            MenuItem(
                Icons.Default.Favorite,
                if (selectedGame.isFavorite) "Remove ${selectedGame.name} from favorites" else "Add ${selectedGame.name} to favorites",
                selected = selectedMenuItemIndex == 0,
                onClick = onToggleFavorite,
            )
            MenuItem(
                Icons.Default.Folder,
                "Choose emulation folder",
                selected = selectedMenuItemIndex == 1,
                onClick = onOpenFolderPicker,
            )
            MenuItem(
                Icons.Default.Apps,
                "Apps",
                selected = selectedMenuItemIndex == 2,
                onClick = onOpenApps,
            )
            MenuItem(
                Icons.Default.Wallpaper,
                "Wallpaper",
                selected = selectedMenuItemIndex == 3,
                onClick = onOpenWallpapers,
            )
            MenuItem(
                Icons.Default.Info,
                "Manual metadata and art",
                selected = selectedMenuItemIndex == 4,
                onClick = {
                    onShowMenuAction(
                        MenuActionInfo(
                            title = "Manual Metadata / Art",
                            message = "Manual edits will override provider metadata and selected artwork for ${selectedGame.name}.",
                        ),
                    )
                },
            )
            MenuItem(Icons.Default.Search, "API", selected = selectedMenuItemIndex == 5, onClick = onOpenCredentials)
            MenuItem(
                icon = Icons.Default.Search,
                label = if (isLoadingArtwork) "Loading provider assets..." else "Refresh game assets",
                selected = selectedMenuItemIndex == 6,
                onClick = onRefreshArtwork,
            )
            MenuItem(
                icon = Icons.Default.Folder,
                label = if (consoleFoldersEnabled) "Disable console folders" else "Enable console folders",
                selected = selectedMenuItemIndex == 7,
                onClick = onToggleConsoleFolders,
            )
            MenuItem(
                icon = Icons.Default.SwapVert,
                label = if (screensSwapped) "Restore default screen layout" else "Swap top and bottom screens",
                selected = selectedMenuItemIndex == 8,
                onClick = onToggleScreensSwapped,
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
private fun MenuActionPanel(
    action: MenuActionInfo?,
    onDismiss: () -> Unit,
) {
    OverlayPanelAnchor(visible = action != null, alignment = Alignment.CenterEnd) {
        SidePanel(alignment = Alignment.CenterEnd, onDismiss = onDismiss) {
            Text(action?.title.orEmpty(), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(14.dp))
            Text(
                text = action?.message.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = CalicoInk.copy(alpha = 0.78f),
            )
        }
    }
}

@Composable
private fun LibraryScanningOverlay(visible: Boolean) {
    if (!visible) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = CalicoBlue)
            Spacer(Modifier.height(12.dp))
            Text("Scanning library…", color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun AppsPanel(
    visible: Boolean,
    taskbarApps: List<String>,
    onToggleTaskbarApp: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val apps = remember { mutableStateListOf<Triple<String, String, Drawable>>() }

    LaunchedEffect(visible) {
        if (visible && apps.isEmpty()) {
            withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val loaded = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .mapNotNull { ai ->
                        pm.getLaunchIntentForPackage(ai.packageName) ?: return@mapNotNull null
                        Triple(
                            pm.getApplicationLabel(ai).toString(),
                            ai.packageName,
                            pm.getApplicationIcon(ai),
                        )
                    }
                    .sortedBy { it.first }
                apps.addAll(loaded)
            }
        }
    }

    OverlayPanelAnchor(visible = visible, alignment = Alignment.CenterEnd) {
        SidePanel(alignment = Alignment.CenterEnd, onDismiss = onDismiss) {
            Text("Apps", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "Launch installed Android apps",
                style = MaterialTheme.typography.bodyMedium,
                color = CalicoInk.copy(alpha = 0.58f),
            )
            Spacer(Modifier.height(12.dp))

            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = CalicoBlue)
                }
            } else {
                apps.forEach { (name, packageName, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                context.packageManager
                                    .getLaunchIntentForPackage(packageName)
                                    ?.let { context.startActivity(it) }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AsyncImage(
                            model = icon,
                            contentDescription = name,
                            modifier = Modifier.size(40.dp),
                        )
                        Text(
                            name,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { onToggleTaskbarApp(packageName) }) {
                            Icon(
                                if (taskbarApps.contains(packageName)) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = if (taskbarApps.contains(packageName)) "Unpin from taskbar" else "Pin to taskbar",
                                tint = if (taskbarApps.contains(packageName)) CalicoBlue else CalicoInk.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WallpaperPanel(
    visible: Boolean,
    wallpaperUri: String?,
    onPickWallpaper: () -> Unit,
    onClearWallpaper: () -> Unit,
    onDismiss: () -> Unit,
) {
    OverlayPanelAnchor(visible = visible, alignment = Alignment.CenterEnd) {
        SidePanel(alignment = Alignment.CenterEnd, onDismiss = onDismiss) {
            Text("Bottom Screen Wallpaper", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "Set a background image for the game grid",
                style = MaterialTheme.typography.bodyMedium,
                color = CalicoInk.copy(alpha = 0.58f),
            )
            Spacer(Modifier.height(16.dp))

            // Preview thumbnail
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 4.dp,
                color = CalicoBlueLight,
            ) {
                if (wallpaperUri != null) {
                    AsyncImage(
                        model = wallpaperUri,
                        contentDescription = "Wallpaper preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "No wallpaper set",
                            color = CalicoInk.copy(alpha = 0.55f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onPickWallpaper,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (wallpaperUri != null) "Change Wallpaper" else "Pick Wallpaper Image")
            }

            if (wallpaperUri != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEEEE), RoundedCornerShape(12.dp))
                        .clickable(onClick = onClearWallpaper)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFFCC3333),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Remove Wallpaper",
                        color = Color(0xFFCC3333),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun RetroAchievementsPanel(
    summaries: List<RetroAchievementsSummary>?,
    selectedSummary: RetroAchievementsSummary?,
    selectedSort: RetroAchievementsSort,
    selectedGameIndex: Int,
    selectedAchievementIndex: Int,
    isLoading: Boolean,
    artworkByGame: Map<Int, GameArtwork>,
    onSelectSummary: (RetroAchievementsSummary) -> Unit,
    onSortSelected: (RetroAchievementsSort) -> Unit,
    onBackToGames: () -> Unit,
    onSelectPressed: () -> Unit,
    onBackPressed: () -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(visible = isLoading || summaries != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.72f))
                .clickable(onClick = onDismiss),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 86.dp)
                    .clickable(enabled = false) {}
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val visibleSummary = selectedSummary
                val sortedSummaries = remember(summaries, selectedSort) {
                    summaries.orEmpty().sortedFor(selectedSort)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (visibleSummary != null) {
                        IconButton(onClick = onBackToGames, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to games", tint = CalicoInk)
                        }
                        AchievementHeaderChip(text = visibleSummary.gameTitle)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RetroAchievementsSort.entries.forEach { sort ->
                                AchievementHeaderChip(
                                    text = sort.label,
                                    selected = sort == selectedSort,
                                    onClick = { onSortSelected(sort) },
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CalicoBlue)
                    }
                } else if (visibleSummary != null) {
                    if (visibleSummary.achievements.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = visibleSummary.message,
                                color = CalicoInk,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            lazyItemsIndexed(visibleSummary.achievements) { index, achievement ->
                                AchievementListRow(
                                    achievement = achievement,
                                    selected = index == selectedAchievementIndex.coerceInList(visibleSummary.achievements.size),
                                )
                            }
                        }
                    }
                } else if (summaries.isNullOrEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No RetroAchievements games found.",
                            color = CalicoInk,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        lazyItemsIndexed(sortedSummaries) { index, summary ->
                            AchievementGameRow(
                                summary = summary,
                                artworkByGame = artworkByGame,
                                selected = index == selectedGameIndex.coerceInList(sortedSummaries.size),
                                onClick = { onSelectSummary(summary) },
                            )
                        }
                    }
                }
            }
            AchievementControllerLegend(
                onSelect = onSelectPressed,
                onBack = onBackPressed,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 14.dp),
            )
        }
    }
}

@Composable
private fun AchievementGameRow(
    summary: RetroAchievementsSummary,
    artworkByGame: Map<Int, GameArtwork>,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val rowShape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 4.dp else 0.dp,
                brush = achievementSelectionBrush(),
                shape = rowShape,
            )
            .clickable(onClick = onClick),
        color = Color.White.copy(alpha = 0.68f),
        shape = rowShape,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(12.dp),
                color = CalicoBlueLight,
            ) {
                // Prefer SteamGridDB icon, then RetroAchievements game icon as fallback
                val iconUrl = artworkByGame[summary.sourceGameId]?.iconUrl ?: summary.gameIconUrl
                if (iconUrl != null) {
                    AsyncImage(
                        model = iconUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = CalicoBlue,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    text = summary.gameTitle,
                    color = CalicoInk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val achievements = summary.achievements.take(7)
                    repeat(7) { index ->
                        Surface(
                            modifier = Modifier.size(30.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = if (index < achievements.size) CalicoBlueLight else Color(0xFFE2E8F0),
                        ) {
                            val badgeUrl = achievements.getOrNull(index)?.badgeUrl
                            if (badgeUrl != null) {
                                AsyncImage(
                                    model = badgeUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else if (index < achievements.size) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = CalicoBlue,
                                    modifier = Modifier.padding(5.dp),
                                )
                            }
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${summary.earnedCount}/${summary.achievements.size}",
                        fontWeight = FontWeight.ExtraBold,
                        color = CalicoInk,
                    )
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = CalicoInk, modifier = Modifier.size(16.dp))
                }
                Box(
                    modifier = Modifier
                        .size(width = 190.dp, height = 10.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(summary.completionRatio)
                            .background(CalicoBlue, RoundedCornerShape(8.dp)),
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementHeaderChip(
    text: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        color = if (selected) CalicoBlue else CalicoPanel,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = if (selected) Color.White else CalicoInk,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
            )
        }
    }
}

@Composable
private fun AchievementListRow(
    achievement: RetroAchievement,
    selected: Boolean,
) {
    val rowShape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 4.dp else 0.dp,
                brush = achievementSelectionBrush(),
                shape = rowShape,
            ),
        color = Color.White.copy(alpha = if (achievement.isUnlocked) 0.90f else 0.62f),
        shape = rowShape,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(78.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (achievement.isUnlocked) CalicoBlueLight else Color(0xFFE2E8F0),
            ) {
                if (achievement.badgeUrl != null) {
                    AsyncImage(
                        model = achievement.badgeUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        colorFilter = if (achievement.isUnlocked) null else ColorFilter.colorMatrix(
                            ColorMatrix().apply { setToSaturation(0f) },
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = if (achievement.isUnlocked) 1f else 0.42f },
                    )
                } else {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (achievement.isUnlocked) CalicoBlue else CalicoInk.copy(alpha = 0.30f),
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    color = CalicoInk,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = achievement.description,
                    color = CalicoInk.copy(alpha = 0.66f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = achievement.earnedAt?.let { "Earned $it" } ?: "Not earned",
                    color = CalicoInk.copy(alpha = 0.56f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${achievement.points}", fontWeight = FontWeight.ExtraBold, color = CalicoInk)
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = CalicoInk, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AchievementControllerLegend(
    onSelect: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.92f),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendAction("A", "Select", onSelect)
            LegendAction("B", "Back", onBack)
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

    OverlayPanelAnchor(visible = visible, alignment = Alignment.CenterEnd) {
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
private fun SortPanelHero(selectedSort: GameSort) {
    val heroPath = "file:///android_asset/sorting_assets/${selectedSort.assetFolder}/hero.png"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        AsyncImage(
            model = heroPath,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.10f), Color.Black.copy(alpha = 0.65f)),
                    ),
                ),
        )
        AsyncImage(
            model = "file:///android_asset/sorting_assets/${selectedSort.assetFolder}/logo.png",
            contentDescription = selectedSort.label,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
                .height(52.dp)
                .widthIn(max = 260.dp),
        )
    }
}

@Composable
private fun SortSidePanel(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth(align = Alignment.Start, unbounded = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(340.dp),
            color = Color.White,
            shadowElevation = 0.dp,
        ) {
            Column(Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

@Composable
private fun SidePanel(
    alignment: Alignment,
    onDismiss: () -> Unit,
    overrideTopPadding: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Use fillMaxHeight + wrapContentWidth so the panel only occupies its own 340dp
    // column and does NOT intercept touches outside it (unlike fillMaxSize).
    val horizontalAlign = if (alignment == Alignment.CenterEnd)
        Alignment.End
    else
        Alignment.Start
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth(align = horizontalAlign, unbounded = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(340.dp),
            color = Color.White,
            shadowElevation = 18.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = overrideTopPadding,
                        bottom = 24.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SortRow(sort: GameSort, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 3.dp else 0.dp,
                brush = achievementSelectionBrush(),
                shape = shape,
            )
            .background(if (selected) CalicoBlueLight.copy(alpha = 0.22f) else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(sort.label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 3.dp else 0.dp,
                brush = achievementSelectionBrush(),
                shape = shape,
            )
            .background(if (selected) CalicoBlueLight.copy(alpha = 0.22f) else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
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
        GameSort.Favorites -> "Favorites"
        GameSort.Recent -> "Recently Played"
        GameSort.MostPlayed -> "Most Played"
        GameSort.NewlyAdded -> "Newly Added"
        GameSort.Unplayed -> "Unplayed"
    }

private val GameSort.assetFolder: String
    get() = when (this) {
        GameSort.Favorites -> "favorites"
        GameSort.Recent -> "recent"
        GameSort.MostPlayed -> "most_played"
        GameSort.NewlyAdded -> "newly_added"
        GameSort.Unplayed -> "unplayed"
    }

private val RetroAchievementsSort.label: String
    get() = when (this) {
        RetroAchievementsSort.ByPlatform -> "   Platform"
        RetroAchievementsSort.RecentlyEarned -> "Recently Earned"
        RetroAchievementsSort.Completion -> "Completion"
    }

private fun achievementSelectionBrush(): Brush =
    Brush.horizontalGradient(
        listOf(Color(0xFF49D8FF), Color(0xFF8B5CFF), Color(0xFFFF75D6)),
    )

private fun List<RetroAchievementsSummary>.sortedFor(sort: RetroAchievementsSort): List<RetroAchievementsSummary> =
    when (sort) {
        RetroAchievementsSort.ByPlatform -> sortedWith(compareBy({ it.platformName }, { it.gameTitle }))
        RetroAchievementsSort.RecentlyEarned -> sortedWith(
            compareByDescending<RetroAchievementsSummary> { it.lastEarnedAt ?: "" }
                .thenBy { it.gameTitle },
        )
        RetroAchievementsSort.Completion -> sortedWith(
            compareByDescending<RetroAchievementsSummary> { it.completionRatio }
                .thenBy { it.gameTitle },
        )
    }

private fun Int.floorMod(size: Int): Int {
    if (size == 0) return 0
    val result = this % size
    return if (result < 0) result + size else result
}

private fun Int.coerceInList(size: Int): Int =
    if (size <= 0) 0 else coerceIn(0, size - 1)

private sealed class ConsoleFolder {
    abstract val id: String
    abstract val label: String
    abstract val iconAssetPath: String

    data class PlatformFolder(val platform: Platform) : ConsoleFolder() {
        override val id = "platform-${platform.romFolderName}"
        override val label = platform.name
        override val iconAssetPath =
            "file:///android_asset/console_folders/by_platform/${platform.byPlatformFolder()}/icon.png"
    }

    data object Favorites : ConsoleFolder() {
        override val id = "favorites"
        override val label = "Favorites"
        override val iconAssetPath = "file:///android_asset/console_folders/favorites/icon.png"
    }

    data object Recent : ConsoleFolder() {
        override val id = "recent"
        override val label = "Recent"
        override val iconAssetPath = "file:///android_asset/console_folders/recent/icon.png"
    }

    data object NewlyAdded : ConsoleFolder() {
        override val id = "newly_added"
        override val label = "Newly Added"
        override val iconAssetPath = "file:///android_asset/console_folders/newly_added/icon.png"
    }
}

private sealed class LauncherGridItem {
    data class Game(val game: com.calico.launcher.model.Game) : LauncherGridItem()
    data class Folder(val folder: ConsoleFolder) : LauncherGridItem()
}

private fun buildConsoleFolders(games: List<com.calico.launcher.model.Game>): List<ConsoleFolder> {
    val platforms = games
        .map { it.platform }
        .distinctBy { it.romFolderName }
        .sortedBy { it.name.lowercase() }
    return buildList {
        add(ConsoleFolder.Favorites)
        add(ConsoleFolder.Recent)
        add(ConsoleFolder.NewlyAdded)
        addAll(platforms.map { ConsoleFolder.PlatformFolder(it) })
    }
}

private fun filterGamesForFolder(games: List<com.calico.launcher.model.Game>, folder: ConsoleFolder): List<com.calico.launcher.model.Game> =
    when (folder) {
        is ConsoleFolder.PlatformFolder -> games.filter { it.platform.romFolderName == folder.platform.romFolderName }
        ConsoleFolder.Favorites -> games.filter { it.isFavorite }
        ConsoleFolder.Recent -> games.filter { it.lastPlayedAt != null }
        ConsoleFolder.NewlyAdded -> games
    }

private fun sortGames(games: List<com.calico.launcher.model.Game>, sort: GameSort): List<com.calico.launcher.model.Game> =
    when (sort) {
        GameSort.Favorites -> games.sortedByDescending { it.isFavorite }
        GameSort.Recent -> games.sortedByDescending { it.lastPlayedAt ?: "" }
        GameSort.MostPlayed -> games.sortedByDescending { it.durationSeconds }
        GameSort.NewlyAdded -> games.sortedBy { it.sortTitle }
        GameSort.Unplayed -> games.sortedBy { it.durationSeconds }
    }

@Composable
private fun FolderHeroCard(
    folder: ConsoleFolder,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RectangleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(CalicoBlue.copy(alpha = 0.72f), Color(0xFFF4F8FF)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        SoftImageVignette()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.fillMaxWidth(0.62f),
        ) {
            AsyncImage(
                model = folder.iconAssetPath,
                contentDescription = "${folder.label} folder icon",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(24.dp)),
            )
            Text(
                text = folder.label,
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FolderTile(
    folder: ConsoleFolder,
    selected: Boolean,
    tileSize: Dp,
    iconFrameSize: Dp,
    onClick: () -> Unit,
) {
    val selectionAnimation = tween<Float>(durationMillis = 180, easing = FastOutSlowInEasing)
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.10f else 1f,
        animationSpec = selectionAnimation,
        label = "folderIconScale",
    )
    val selectedBorderAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = selectionAnimation,
        label = "folderSelectedBorderAlpha",
    )
    val selectedBorderWidth = 4.dp
    val selectedBorderOutset = selectedBorderWidth
    val selectedBorderSize = iconFrameSize + selectedBorderOutset * 2f
    val selectedBorderRadius = (iconFrameSize.value * 0.075f).dp + selectedBorderOutset

    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier.size(tileSize),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(iconFrameSize)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
                    .shadow(
                        elevation = if (selected) 18.dp else 3.dp,
                        shape = RoundedCornerShape(14.dp),
                        clip = false,
                    )
                    .background(Color.White, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = folder.iconAssetPath,
                    contentDescription = "${folder.label} folder",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(selectedBorderSize)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        alpha = selectedBorderAlpha
                    }
                    .border(
                        width = selectedBorderWidth,
                        brush = achievementSelectionBrush(),
                        shape = RoundedCornerShape(selectedBorderRadius),
                    ),
            )
        }
        Text(
            text = folder.label,
            style = MaterialTheme.typography.labelMedium,
            color = CalicoInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(tileSize),
        )
    }
}

private fun Platform.overlayAssetPath(): String {
    val folder = when (romFolderName) {
        "3ds" -> "n3ds"
        "ds" -> "nds"
        "gamecube" -> "gc"
        "ps1" -> "psx"
        else -> romFolderName
    }
    return "file:///android_asset/icon_overlays/$folder/overlay.png"
}

private fun Platform.byPlatformFolder(): String = when (romFolderName) {
    "3ds" -> "n3ds"
    "ds" -> "nds"
    "gamecube" -> "gc"
    "ps1" -> "psx"
    else -> romFolderName
}

private fun Platform.platformLogoAssetPath(): String =
    "file:///android_asset/by_platform/${byPlatformFolder()}/logo.png"

private fun Platform.platformHeroAssetPath(): String =
    "file:///android_asset/by_platform/${byPlatformFolder()}/hero.png"

private fun String.initials(): String =
    split(" ", "-", "_")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

@Preview(showBackground = true, widthDp = 960, heightDp = 720)
@Composable
private fun CalicoLauncherPreview() {
    CalicoTheme {
        CalicoLauncherApp(EmulatorRegistry())
    }
}
