/*
 * Copyright 2011-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.gradle.plugins.rpm

import com.netflix.gradle.plugins.packaging.AbstractPackagingCopyAction
import com.netflix.gradle.plugins.packaging.SystemPackagingTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional
import org.redline_rpm.header.Architecture
import org.redline_rpm.header.Os
import org.redline_rpm.header.RpmType
import org.gradle.api.internal.ConventionMapping
import org.gradle.api.internal.IConventionAware

class Rpm extends SystemPackagingTask {
    @InputFile @Optional
    File changeLogFile

    Rpm() {
        super()
        extension = 'rpm'
    }

    @Override
    String assembleArchiveName() {
        String name = getPackageName();
        name += getVersion() ? "-${getVersion()}" : ''
        name += getRelease() ? "-${getRelease()}" : ''
        name += getArchString() ? ".${getArchString()}" : ''
        name += getExtension() ? ".${getExtension()}" : ''
        return name;
    }

    @Override
    AbstractPackagingCopyAction createCopyAction() {
        return new RpmCopyAction(this)
    }

    @Override
    protected void applyConventions() {
        super.applyConventions()

        // For all mappings, we're only being called if it wasn't explicitly set on the task. In which case, we'll want
        // to pull from the parentExten. And only then would we fallback on some other value.
        ConventionMapping mapping = ((IConventionAware) this).getConventionMapping()

        // Could come from extension
        mapping.map('fileType', { parentExten?.getFileType() })
        mapping.map('addParentDirs', { parentExten?.getAddParentDirs()?:true })
        mapping.map('archStr', {
            parentExten?.getArchStr()?:Architecture.NOARCH.name()
        })
        mapping.map('os', { parentExten?.getOs()?:Os.UNKNOWN})
        mapping.map('osName', { parentExten?.getOsName()?: ''})
        mapping.map('type', { parentExten?.getType()?:RpmType.BINARY })

        // NOTE: Believe parentExten is always null
        mapping.map('prefixes', { parentExten?.getPrefixes()?:[] })
    }

    void prefixes(String... addPrefixes) {
        exten.prefixes.addAll(addPrefixes)
    }

    List<String> getPrefixes() {
        exten.prefixes
    }

    public File getChangeLogFile() {
        return changeLogFile;
    }

    public void setChangeLogFile(File changeLogFile) {
        this.changeLogFile = changeLogFile;
    }
}
