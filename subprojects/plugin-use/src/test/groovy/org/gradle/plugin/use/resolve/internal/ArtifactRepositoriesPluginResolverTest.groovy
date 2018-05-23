/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.plugin.use.resolve.internal

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.internal.artifacts.DependencyResolutionServices
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.DefaultVersionComparator
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.DefaultVersionSelectorScheme
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.MavenVersionSelectorScheme
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionParser
import org.gradle.api.internal.artifacts.repositories.ArtifactRepositoryInternal
import org.gradle.groovy.scripts.StringScriptSource
import org.gradle.plugin.management.internal.DefaultPluginRequest
import org.gradle.plugin.management.internal.PluginRequestInternal
import org.gradle.plugin.use.internal.DefaultPluginId
import org.junit.Ignore
import spock.lang.Specification

import static org.gradle.plugin.use.resolve.internal.ArtifactRepositoriesPluginResolver.SOURCE_NAME

class ArtifactRepositoriesPluginResolverTest extends Specification {
    def versionSelectorScheme = new MavenVersionSelectorScheme(new DefaultVersionSelectorScheme(new DefaultVersionComparator(), new VersionParser()))
    def repository = Mock(ArtifactRepositoryInternal) {
        getDisplayName() >> "maven(url)"
    }
    def repositories = Mock(RepositoryHandler) {
        iterator() >> [repository].iterator()
    }
    def resolution = Mock(DependencyResolutionServices) {
        getResolveRepositoryHandler() >> repositories
    }
    def result = Mock(PluginResolutionResult)

    def resolver = new ArtifactRepositoriesPluginResolver(resolution, versionSelectorScheme)

    PluginRequestInternal request(String id, String version = null) {
        new DefaultPluginRequest(DefaultPluginId.of(id), version, true, 1, new StringScriptSource("test", "test"))
    }

    def "fail pluginRequests without versions"() {
        when:
        resolver.resolve(request("plugin"), result)

        then:
        1 * result.notFound(SOURCE_NAME, "plugin dependency must include a version number for this source")
    }

    @Ignore
    def "fail pluginRequests with SNAPSHOT versions"() {
        when:
        resolver.resolve(request("plugin", "1.1-SNAPSHOT"), result)

        then:
        1 * result.notFound(SOURCE_NAME, "snapshot plugin versions are not supported")
    }

    def "fail pluginRequests with dynamic versions"() {
        when:
        resolver.resolve(request("plugin", "latest.revision"), result)

        then:
        1 * result.notFound(SOURCE_NAME, "dynamic plugin versions are not supported")
    }
}
