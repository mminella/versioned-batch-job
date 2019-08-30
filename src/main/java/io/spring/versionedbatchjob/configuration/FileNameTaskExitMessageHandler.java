/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.spring.versionedbatchjob.configuration;

import java.io.File;

import org.springframework.cloud.task.listener.annotation.AfterTask;
import org.springframework.cloud.task.repository.TaskExecution;
import org.springframework.stereotype.Component;

/**
 * @author Michael Minella
 */
@Component
public class FileNameTaskExitMessageHandler {

	@AfterTask
	public void afterTask(TaskExecution taskExecution) {
		String name = new File(BatchConfiguration.class.getProtectionDomain()
				.getCodeSource()
				.getLocation().getPath())
				.getParentFile()
				.getParentFile()
				.getName();

		System.out.println(">> The jar file being executed is = " + name.substring(0, name.length() - 1));

		taskExecution.setExitMessage("JAR executed was: " + name.substring(0, name.length() - 1));
	}
}
