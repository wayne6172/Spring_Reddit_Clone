package com.example.demo;

import com.example.demo.model.User;
import com.example.demo.model.VerificationToken;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VerificationTokenRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DemoApplicationTests {

	private HttpHeaders httpHeaders;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private VerificationTokenRepository verificationTokenRepository;

	private final String username = "tester";
	private final String password = "tester";

	@Autowired
	private MockMvc mockMvc;

	@Before
	public void init(){
		verificationTokenRepository.deleteAll();
		userRepository.deleteAll();

		httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
	}

	@After
	public void clear(){
	}

	@Test
	public void signUpAndLogin() throws Exception{
		signupUser();

		loginUserButNotVerification();

		userVerification();

		loginUser();
	}

	public void signupUser() throws Exception {
		JSONObject request = new JSONObject();
		request.put("email", "UnitTest@test.email");
		request.put("username", username);
		request.put("password", password);

		RequestBuilder requestBuilder =
				MockMvcRequestBuilders
						.post("/api/auth/signup")
						.headers(httpHeaders)
						.content(request.toString());

		mockMvc.perform(requestBuilder)
				.andDo(print())
				.andExpect(status().isOk());
	}

	public void loginUserButNotVerification() throws Exception{
		JSONObject request = new JSONObject();
		request.put("username",username);
		request.put("password",password);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/auth/login")
				.headers(httpHeaders)
				.content(request.toString());

		mockMvc.perform(requestBuilder)
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	public void userVerification() throws Exception{
		User user = userRepository.findByUsername(username).orElseThrow(() -> {
			Assert.fail("Not found user by username in database");
			return null;
		});

		List<VerificationToken> verificationTokens
				= verificationTokenRepository.findAll();

		Long id = user.getUserId();
		VerificationToken verificationToken =
				verificationTokens.stream().filter(data -> data.getUser().getUserId().equals(id)).findFirst()
						.orElseThrow(() -> {Assert.fail("not found user's verificationToken"); return null;});

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get("/api/auth/accountVerification/" + verificationToken.getToken());

		mockMvc.perform(requestBuilder)
				.andDo(print())
				.andExpect(status().isOk());
	}

	public void loginUser() throws Exception {
		JSONObject request = new JSONObject();
		request.put("username",username);
		request.put("password",password);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post("/api/auth/login")
				.headers(httpHeaders)
				.content(request.toString());

		mockMvc.perform(requestBuilder)
				.andDo(print())
				.andExpect(status().isOk());
	}
}
