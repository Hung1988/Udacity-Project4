package com.example.demo;

import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SareetaApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Test
	public void contextLoads() {
	}

	@Test
	public void createUser() throws Exception
	{
		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setUsername("test01");
		createUserRequest.setPassword("12345");
		createUserRequest.setConfirmPassword("12345");
		mvc.perform( post("/api/user/create")
						.content(asJsonString(createUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.username").exists());
	}

	@Test
	public void login() throws Exception {
		//create user
		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setUsername("test02");
		createUserRequest.setPassword("12345");
		createUserRequest.setConfirmPassword("12345");
		mvc.perform( post("/api/user/create")
						.content(asJsonString(createUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));

		String userBody = "{\"username\": \"test02\", \"password\": \"12345\"}";

		// login user
		mvc.perform(post("/login")
						.content(userBody)
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	@Test
	public void actions() throws Exception {
		//create user
		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setUsername("test03");
		createUserRequest.setPassword("12345");
		createUserRequest.setConfirmPassword("12345");
		mvc.perform( post("/api/user/create")
						.content(asJsonString(createUserRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());

		String userBody = "{\"username\": \"test03\", \"password\": \"12345\"}";

		//login
		MvcResult result = mvc.perform(post("/login")
				.content(userBody).contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().isOk()).andReturn();

		String token = result.getResponse().getHeader("Authorization");

		assert token != null;

		// get item by id
		mvc.perform(MockMvcRequestBuilders
						.get("/api/item/{id}",1)
						.header(HttpHeaders.AUTHORIZATION,token)
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		// get item by name when item not exist
		mvc.perform(MockMvcRequestBuilders
						.get("/api/item/name/{name}","itemTest")
						.header(HttpHeaders.AUTHORIZATION,token)
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

		// get item by name
		mvc.perform(MockMvcRequestBuilders
						.get("/api/item/name/{name}","Round Widget")
						.header(HttpHeaders.AUTHORIZATION,token)
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		//Add item to cart
		ModifyCartRequest modifyCartRequest = new ModifyCartRequest();
		modifyCartRequest.setUsername("test03");
		modifyCartRequest.setItemId(1);
		modifyCartRequest.setQuantity(1);
		mvc.perform(post("/api/cart/addToCart")
						.content(asJsonString(modifyCartRequest))
						.header(HttpHeaders.AUTHORIZATION,token)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		//Remove item to cart
		mvc.perform(post("/api/cart/removeFromCart")
						.content(asJsonString(modifyCartRequest))
						.header(HttpHeaders.AUTHORIZATION,token)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		//Get history Order
		mvc.perform(MockMvcRequestBuilders
						.get("/api/order/history/{username}", "test03")
						.content(asJsonString(modifyCartRequest))
						.header(HttpHeaders.AUTHORIZATION,token)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		//Submit Order
		mvc.perform(post("/api/order/submit/{username}", "test03")
						.content(asJsonString(modifyCartRequest))
						.header(HttpHeaders.AUTHORIZATION,token)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
