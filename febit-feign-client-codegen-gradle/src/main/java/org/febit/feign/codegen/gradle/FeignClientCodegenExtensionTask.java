/*
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.feign.codegen.gradle;

import lombok.val;
import org.febit.devkit.gradle.util.GradleUtils;
import org.febit.feign.codegen.ClientCodegen;
import org.febit.feign.codegen.MetaResolver;
import org.febit.feign.codegen.util.ClassNamings;
import org.febit.lang.annotation.NonNullApi;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.classpath.DefaultClassPath;

import javax.inject.Inject;
import java.net.URLClassLoader;

@NonNullApi
public class FeignClientCodegenExtensionTask extends DefaultTask {

    @Inject
    public FeignClientCodegenExtensionTask() {
        setGroup(Constants.GROUP_NAME);
        setDescription("Generate api definitions as feign clients.");
    }

    private void generate(
            FeignClientCodegenExtension extension,
            ClassLoader classloader
    ) {
        val metaResolver = MetaResolver.create(classloader);

        val beans = metaResolver.scanBeans(extension.getScanPackages());
        val clients = metaResolver.resolveClients(beans.values());

        GradleUtils.println("Found [{0}] clients", clients.size());

        val codegen = ClientCodegen.builder()
                .beans(beans)
                .clients(clients)
                .clientName(extension.getClient().getName())
                .clientUrl(extension.getClient().getUrl())
                .clientBasePackage(extension.getClient().getBasePackage())
                .clientConfigClasses(extension.getClient().getConfigClasses())
                .clientNaming(ClassNamings.chain(
                        extension.getNaming().getGlobal(),
                        extension.getNaming().getClient()
                ))
                .pojoNaming(ClassNamings.chain(
                        extension.getNaming().getGlobal(),
                        extension.getNaming().getPojo()
                ))
                .excludedClasses(extension.getExcludedClasses())
                .targetDir(extension.getTargetSourceDir())
                .build();

        codegen.emit();
    }

    private FeignClientCodegenExtension getExtension() {
        return getProject().getExtensions()
                .getByType(FeignClientCodegenExtension.class);
    }

    @TaskAction
    public void run() {
        val extension = getExtension();
        val classloader = resolveClassLoader();

        generate(extension, classloader);
    }

    private ClassLoader resolveClassLoader() {
        val extension = getExtension();

        val sourceProj = extension.getSourceProject();

        val classpath = GradleUtils.mainSourceSet(sourceProj).getOutput()
                .plus(sourceProj.getConfigurations().getByName("compileClasspath"));

        val urls = DefaultClassPath.of(classpath.getFiles())
                .getAsURLArray();

        // NOTE: Using current classloader as parent.
        return new URLClassLoader(urls, getClass().getClassLoader());
    }

}