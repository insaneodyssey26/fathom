package com.masum.fathom.core.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.compose.ui.graphics.toArgb
import com.masum.fathom.R
import com.masum.fathom.ui.theme.palette
import com.masum.fathom.ui.theme.toneFor

class DailyWisdomViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return DailyWisdomViewsFactory(applicationContext, intent)
    }
}

class DailyWisdomViewsFactory(
    private val context: Context,
    private val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private val widgetId: Int = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )

    private data class DisplayItem(
        val label: String? = null,
        val text: String
    )

    private val items = mutableListOf<DisplayItem>()

    override fun onCreate() {
        loadData()
    }

    override fun onDataSetChanged() {
        loadData()
    }

    private fun loadData() {
        items.clear()
        val passageId = WidgetRepository.getWidgetPassageId(context, widgetId)
        val showInfo = WidgetRepository.getWidgetShowInfo(context, widgetId)
        val passage = WidgetRepository.loadPassageById(context, passageId)

        if (showInfo) {
            val titleAndContext = buildString {
                if (!passage.explanationTitle.isNullOrBlank()) {
                    append(passage.explanationTitle)
                    append("\n\n")
                }
                append(passage.explanationBody.orEmpty())
            }.trim()

            if (titleAndContext.isNotEmpty()) {
                items.add(
                    DisplayItem(
                        label = "CANONICAL CONTEXT & MEANING",
                        text = titleAndContext
                    )
                )
            }

            if (!passage.spiritualInsight.isNullOrBlank()) {
                items.add(
                    DisplayItem(
                        label = "SPIRITUAL PRACTICE REFLECTION",
                        text = passage.spiritualInsight
                    )
                )
            }

            if (!passage.sourceAttribution.isNullOrBlank()) {
                items.add(
                    DisplayItem(
                        label = "HERITAGE TRADITION",
                        text = passage.sourceAttribution
                    )
                )
            }

            if (items.isEmpty()) {
                items.add(DisplayItem(label = null, text = passage.translation))
            }
        } else {
            val paragraphs = passage.translation
                .split("\n\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (paragraphs.isEmpty()) {
                items.add(DisplayItem(label = null, text = passage.translation))
            } else {
                paragraphs.forEach { paragraph ->
                    items.add(DisplayItem(label = null, text = paragraph))
                }
            }
        }
    }

    override fun onDestroy() {
        items.clear()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position !in items.indices) return null
        val item = items[position]
        val colors = WidgetRepository.readTheme(context).palette()
        val passageId = WidgetRepository.getWidgetPassageId(context, widgetId)
        val passage = WidgetRepository.loadPassageById(context, passageId)
        val tone = toneFor(passage.traditionId, colors)

        val views = RemoteViews(context.packageName, R.layout.widget_passage_item).apply {
            if (!item.label.isNullOrBlank()) {
                setViewVisibility(R.id.widget_item_label, View.VISIBLE)
                setTextViewText(R.id.widget_item_label, item.label.uppercase())
                setTextColor(R.id.widget_item_label, tone.hue.toArgb())
            } else {
                setViewVisibility(R.id.widget_item_label, View.GONE)
            }
            setTextViewText(R.id.widget_item_text, item.text)
            setTextColor(R.id.widget_item_text, colors.textPrimary.toArgb())
        }

        val fillInIntent = Intent().apply {
            putExtra(WidgetRepository.EXTRA_PASSAGE_ID, passageId)
        }
        views.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}
