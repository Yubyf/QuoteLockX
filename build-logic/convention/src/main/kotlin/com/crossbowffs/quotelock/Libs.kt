package com.crossbowffs.quotelock

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project

internal val Project.libs: LibrariesForLibs
    get() = extensions.getByName("libs") as LibrariesForLibs