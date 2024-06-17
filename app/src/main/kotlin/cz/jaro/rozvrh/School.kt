@file:Suppress("UnusedReceiverParameter", "ObjectPropertyName")

package cz.jaro.rozvrh

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.Filled.School: ImageVector
    get() {
        if (_school != null) {
            return _school!!
        }
        _school = materialIcon(
            name = "Filled.School",
            autoMirror = true
        ) {
            materialPath {
                lineTo(5F, 13.18F)
                verticalLineToRelative(4F)
                lineTo(12F, 21F)
                lineToRelative(7F, -3.82F)
                verticalLineToRelative(-4F)
                lineTo(12F, 17F)
                lineToRelative(-7F, -3.82F)
                close()
                moveTo(12F, 3F)
                lineTo(1F, 9F)
                lineToRelative(11F, 6F)
                lineToRelative(9F, -4.91F)
                verticalLineTo(17F)
                horizontalLineToRelative(2F)
                verticalLineTo(9F)
                lineTo(12F, 3F)
                close()
            }
        }
        return _school!!
    }
private var _school: ImageVector? = null