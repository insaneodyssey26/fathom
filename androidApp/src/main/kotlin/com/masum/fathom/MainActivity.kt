package com.masum.fathom

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.masum.fathom.core.AppGraph
import com.masum.fathom.core.notification.NotificationChannels
import com.masum.fathom.core.widget.DailyWisdomWidgetProvider
import com.masum.fathom.ui.shell.FathomApp

enum class NavigationItem {
    FEED,
    PATHWAYS,
    SEARCH,
    LIBRARY
}

class MainActivity : ComponentActivity() {

    private var launchPassageId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationChannels.register(applicationContext)
        launchPassageId = intent?.getStringExtra(DailyWisdomWidgetProvider.EXTRA_PASSAGE_ID)

        setContent {
            val context = LocalContext.current
            val graph = remember { AppGraph(context.applicationContext) }
            FathomApp(graph = graph, launchPassageId = launchPassageId)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchPassageId = intent.getStringExtra(DailyWisdomWidgetProvider.EXTRA_PASSAGE_ID)
    }
}
