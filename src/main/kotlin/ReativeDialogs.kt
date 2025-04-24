package one.bitby.retroblock.ui

import com.intellij.ide.actions.BigPopupUI
import com.intellij.ide.actions.searcheverywhere.SearchAdapter
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereUI
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.NotificationsManager
import com.intellij.openapi.options.newEditor.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.wm.IdeFrame
import java.awt.*
import java.awt.event.HierarchyEvent
import javax.swing.JDialog
import javax.swing.JTree
import javax.swing.SwingUtilities

class RelativeDialogsProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        RelativeDialogs().registerListener()
    }
}

class RelativeDialogs {

    fun registerListener() {
        Toolkit.getDefaultToolkit().addAWTEventListener({ event ->
            if (event is HierarchyEvent && event.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L) {
                val component = event.source
                if (component is BigPopupUI) {
                    val bounds = event.changedParent.bounds.percent(75, 85)
                    // add this listener only once
                    (component as? SearchEverywhereUI)?.addSearchListener(object : SearchAdapter() {
                        override fun searchStarted(
                            pattern: String,
                            contributors: MutableCollection<out SearchEverywhereContributor<*>>
                        ) {
                            findParentOfType<Window>(component)?.let { win ->
                                SwingUtilities.invokeLater {
                                    component.preferredSize = bounds.size
                                    component.bounds = bounds
                                    win.bounds = bounds
                                    win.revalidate()
                                }
                            }
                        }

                        override fun searchFinished(items: MutableList<Any>) {
                            findParentOfType<Window>(component)?.let { win ->
                                SwingUtilities.invokeLater {
                                    component.preferredSize = bounds.size
                                    component.bounds = bounds
                                    win.bounds = bounds
                                    win.revalidate()
                                }
                            }
                            component.removeSearchListener(this)
                        }
                    })
                    findParentOfType<Window>(component)?.let { win ->
                        SwingUtilities.invokeLater {
                            component.withMaximumSize(bounds.size.width, bounds.size.height)
                            component.withPreferredSize(bounds.size.width, bounds.size.height)
                            component.withMinimumWidth(bounds.size.width)
                            component.withMinimumHeight(bounds.size.height)
                            component.preferredSize = bounds.size
                            component.bounds = bounds
                            win.bounds = bounds
                            win.revalidate()
                        }
                    }
                    return@addAWTEventListener
                }
                if (component is SettingsEditor) {
                    val bounds = event.changedParent.bounds.percent(70, 90)
                    findParentOfType<Window>(component)?.let { win ->
                        SwingUtilities.invokeLater {
                            component.preferredSize = bounds.size
                            component.bounds = bounds
                            win.bounds = bounds
                            win.revalidate()
                        }
                    }
                    return@addAWTEventListener
                }
                if (component::class.java.name.contains("Switcher\$SwitcherPanel")) {
                    val bounds = event.changedParent.bounds.percent(50, 70)
//                    NotificationsManager.getNotificationsManager().showNotification(
//                        Notification(
//                            "retroblock",
//                            "Modal debug",
//                            component::class.java.name,
//                            NotificationType.INFORMATION
//                        ), null
//                    )
                    findParentOfType<Window>(component as Component)?.let { win ->
                        SwingUtilities.invokeLater {
                            component.preferredSize = bounds.size
                            component.bounds = bounds
                            win.bounds = bounds
                            win.revalidate()
                        }
                    }
                    return@addAWTEventListener
                }
                if (component::class.java.name.contains("Bookmark")) {
                    val bounds = event.changedParent.bounds.percent(70, 85)
                    findParentOfType<Window>(component as Component)?.let { win ->
                        if (win is IdeFrame) return@addAWTEventListener
                        SwingUtilities.invokeLater {
                            component.preferredSize = bounds.size
                            component.bounds = bounds
                            win.bounds = bounds
                            win.revalidate()
                        }
                    }
                    return@addAWTEventListener
                }
                if (component is JTree) {
//                    NotificationsManager.getNotificationsManager().showNotification(
//                        Notification(
//                            "retroblock",
//                            "Modal debug",
//                            component::class.java.name,
//                            NotificationType.INFORMATION
//                        ), null
//                    )
                    if (
                        component::class.java.name.contains("FileStructure") ||
                        component::class.java.name.contains("GitBranches")
                    ) {
                        val bounds = event.changedParent.bounds.percent(45, 75)
                        findParentOfType<Window>(component)?.let { win ->
                            SwingUtilities.invokeLater {
                                win.bounds = bounds
                                win.revalidate()
                            }
                        }
                    }
                    return@addAWTEventListener
                }
                if (component is JDialog) {
                    SwingUtilities.invokeLater {
                        var bounds = if (component.size.width < 380 && component.size.height < 250) {
                            event.changedParent.bounds.center(
                                (component.size.width * 1.6).toInt(),
                                (component.size.height * 1.6).toInt()
                            )
                        } else if (component.size.width < 800 && component.size.height < 600) {
                            event.changedParent.bounds.center(
                                (component.size.width * 1.4).toInt(),
                                (component.size.height * 1.4).toInt()
                            )
                        } else {
                            event.changedParent.bounds.center(
                                (component.size.width * 1.2).toInt(),
                                (component.size.height * 1.2).toInt()
                            )
                        }
                        val xb = event.changedParent.bounds.percent(70, 90)
                        if (bounds.size.width > xb.width || bounds.size.height > xb.height) bounds = xb
                        component.preferredSize = bounds.size
                        component.bounds = bounds
                        component.revalidate()
                    }
                }
            }
        }, AWTEvent.HIERARCHY_EVENT_MASK)
    }

    fun Rectangle.center(nwidth: Int, nheight: Int): Rectangle {
        val nx = this.x + (width - nwidth) / 2
        val ny = this.y + (height - nheight) / 2
        return Rectangle(nx, ny, nwidth, nheight)
    }

    fun Rectangle.percent(xp: Int, yp: Int): Rectangle {
        val nwidth = (width * xp / 100.0).toInt()
        val nheight = (height * yp / 100.0).toInt()
        val nx = this.x + (width - nwidth) / 2
        val ny = this.y + (height - nheight) / 2
        return Rectangle(nx, ny, nwidth, nheight)
    }

    inline fun <reified T : Component> findParentOfType(component: Component): T? {
        var parent: Component? = component.parent
        while (parent != null) {
            if (parent is T) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }
}
