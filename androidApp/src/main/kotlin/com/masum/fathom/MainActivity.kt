package com.masum.fathom

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import com.masum.fathom.core.subscription.SubscriptionManager
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
    private lateinit var subscriptionManager: SubscriptionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationChannels.register(applicationContext)
        subscriptionManager = SubscriptionManager(applicationContext)
        com.masum.fathom.core.subscription.LicenseSyncWorker.schedule(applicationContext)

        intent?.data?.let { uri ->
            if (subscriptionManager.handleDeepLink(uri)) {
                Toast.makeText(this, "Welcome, Fathom Patron. Sanctuary unlocked.", Toast.LENGTH_LONG).show()
            }
        }

        launchPassageId = intent?.getStringExtra(DailyWisdomWidgetProvider.EXTRA_PASSAGE_ID)

        setContent {
            val context = LocalContext.current
            val graph = remember { AppGraph(context.applicationContext) }
            FathomApp(
                graph = graph,
                launchPassageId = launchPassageId,
                subscriptionManager = subscriptionManager
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            if (subscriptionManager.handleDeepLink(uri)) {
                Toast.makeText(this, "Welcome, Fathom Patron. Sanctuary unlocked.", Toast.LENGTH_LONG).show()
            }
        }
        launchPassageId = intent.getStringExtra(DailyWisdomWidgetProvider.EXTRA_PASSAGE_ID)
    }
}
