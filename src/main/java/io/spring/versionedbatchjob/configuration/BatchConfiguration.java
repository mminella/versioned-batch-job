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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * @author Michael Minella
 */
@EnableBatchProcessing
@EnableTask
@Configuration
public class BatchConfiguration {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private Environment environment;

	@Autowired
	private VersionedBatchJobProperties versionedBatchJobProperties;

	@Bean
	public Job job() {
		return this.jobBuilderFactory.get("job")
				.incrementer(new RunIdIncrementer())
				.start(step1())
				.build();
	}

	@Bean
	public Step step1() {
		return this.stepBuilderFactory.get("step1")
				.tasklet(new EnvironmentalTasklet(this.environment, this.versionedBatchJobProperties.getWait()))
				.build();
	}

	public static class EnvironmentalTasklet implements Tasklet {

		final private Environment environment;

		final private long wait;

		private long timeWaited = 0L;

		public EnvironmentalTasklet(Environment environment, long wait) {
			this.environment = environment;
			this.wait = wait;
		}

		@Override
		public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

			if(timeWaited == 0) {
				String name = new File(BatchConfiguration.class.getProtectionDomain()
						.getCodeSource()
						.getLocation().getPath())
						.getParentFile()
						.getParentFile()
						.getName();

				Map<String, Object> properties = this.getProperties();

//				Gson gson = new GsonBuilder().setPrettyPrinting().create();
//				String jsonProperties = gson.toJson(properties);

				System.out.println(">> The jar file being executed is = " + name.substring(0, name.length() - 1));

				System.out.println("********** PROPERTIES *************");
				System.out.println(properties.toString());
			}

			System.out.println(">> Sleeping for 3 seconds");
			Thread.sleep(3000L);
			this.timeWaited += 3000L;

			if(this.timeWaited >= this.wait) {
				return RepeatStatus.FINISHED;
			}
			else {
				return RepeatStatus.CONTINUABLE;
			}
		}

		private Map<String, Object> getProperties() {
			Map<String, Object> map = new TreeMap<>();

			for(Iterator it = ((AbstractEnvironment) this.environment).getPropertySources().iterator(); it.hasNext(); ) {
				PropertySource propertySource = (PropertySource) it.next();
				if (propertySource instanceof MapPropertySource) {
					map.putAll(((MapPropertySource) propertySource).getSource());
				}
			}

			return map;
		}
	}
}
