package com.example.lexipath.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.lexipath.MainActivity
import com.example.lexipath.data.local.LexiPathDatabase
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class LexiPathWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = LexiPathDatabase.getDatabase(context)
        val today = LocalDate.now().toString()
        
        val todaysContent = try {
            database.dailyContentDao().getContentByDate(today).first()
        } catch (e: Exception) {
            null
        }

        provideContent {
            GlanceTheme {
                LexiPathWidgetContent(
                    word = todaysContent?.word ?: "LexiPath",
                    meaning = todaysContent?.meaning ?: "Your daily vocabulary companion",
                    hasContent = todaysContent != null,
                    contentId = todaysContent?.id ?: ""
                )
            }
        }
    }
}

@Composable
private fun LexiPathWidgetContent(
    word: String,
    meaning: String,
    hasContent: Boolean,
    contentId: String?
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF6750A4)))
            .clickable(
                actionStartActivity<MainActivity>()
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (hasContent) "Word of the Day" else "LexiPath",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(Color.White.copy(alpha = 0.8f))
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = word,
                style = TextStyle(
                    fontSize = if (word.length > 12) 16.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.White)
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = if (meaning.length > 60) meaning.take(57) + "..." else meaning,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = ColorProvider(Color.White.copy(alpha = 0.9f))
                ),
                maxLines = 2
            )

            if (hasContent) {
                Spacer(modifier = GlanceModifier.height(8.dp))

                Box(
                    modifier = GlanceModifier
                        .background(ColorProvider(Color.White.copy(alpha = 0.2f)))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickable(
                            actionStartActivity<MainActivity>()
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Take Quiz",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorProvider(Color.White)
                        )
                    )
                }
            }
        }
    }
}
