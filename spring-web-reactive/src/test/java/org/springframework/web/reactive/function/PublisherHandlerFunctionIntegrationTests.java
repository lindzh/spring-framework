/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.web.reactive.function;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.springframework.web.reactive.function.Router.route;

/**
 * @author Arjen Poutsma
 */
public class PublisherHandlerFunctionIntegrationTests
		extends AbstractRoutingFunctionIntegrationTests {

	private RestTemplate restTemplate;

	@Before
	public void createRestTemplate() {
		this.restTemplate = new RestTemplate();
	}

	@Override
	protected RoutingFunction<?> routingFunction() {
		PersonHandler personHandler = new PersonHandler();
		return route(RequestPredicates.GET("/mono"), personHandler::mono)
				.and(route(RequestPredicates.GET("/flux"), personHandler::flux));
	}


	@Test
	public void mono() throws Exception {
		ResponseEntity<Person> result =
				restTemplate.getForEntity("http://localhost:" + port + "/mono", Person.class);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals("John", result.getBody().getName());
	}

	@Test
	public void flux() throws Exception {
		ParameterizedTypeReference<List<Person>> reference = new ParameterizedTypeReference<List<Person>>() {};
		ResponseEntity<List<Person>> result =
				restTemplate.exchange("http://localhost:" + port + "/flux", HttpMethod.GET, null, reference);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		List<Person> body = result.getBody();
		assertEquals(2, body.size());
		assertEquals("John", body.get(0).getName());
		assertEquals("Jane", body.get(1).getName());
	}


	private static class PersonHandler {

		public Response<Publisher<Person>> mono(Request request) {
			Person person = new Person("John");
			return Response.ok().stream(Mono.just(person), Person.class);
		}

		public Response<Publisher<Person>> flux(Request request) {
			Person person1 = new Person("John");
			Person person2 = new Person("Jane");
			return Response.ok().stream(Flux.just(person1, person2), Person.class);
		}

	}

	private static class Person {

		private String name;

		@SuppressWarnings("unused")
		public Person() {
		}

		public Person(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Person person = (Person) o;
			return !(this.name != null ? !this.name.equals(person.name) : person.name != null);
		}

		@Override
		public int hashCode() {
			return this.name != null ? this.name.hashCode() : 0;
		}

		@Override
		public String toString() {
			return "Person{" +
					"name='" + name + '\'' +
					'}';
		}
	}


}
