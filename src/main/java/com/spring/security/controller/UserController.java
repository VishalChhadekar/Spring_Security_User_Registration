package com.spring.security.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.weaver.NewConstructorTypeMunger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.security.entity.Password;
import com.spring.security.entity.User;
import com.spring.security.entity.VerificationToken;
import com.spring.security.event.RegistrationCompleteEvent;
import com.spring.security.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private ApplicationEventPublisher publisher;

	// REGISTRATION
	@PostMapping("/register")
	public String saveUser(@RequestBody User user, final HttpServletRequest request) {
		User user2 = userService.saveUser(user);
		publisher.publishEvent(new RegistrationCompleteEvent(user2, applicationUrl(request)));
		return "success";
	}

	private String applicationUrl(HttpServletRequest request) {
		return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
	}

	// VERIFING REGISTRATION
	@GetMapping("/verifyRegistration")
	public String verifyUserRegistration(@RequestParam("token") String token) {
		// verify the token
		String retult = userService.verifyVerificationToken(token);
		if (retult.equalsIgnoreCase("valid")) {
			return "User Verification successful";
		}
		return "Bad User";
	}

	// RESEND VERIFICATION TOKEN
	@GetMapping("/resendVerificationToken")
	public String resendUserRegistrationVerificationToken(@RequestParam("token") String oldToken,
			HttpServletRequest request) {
		VerificationToken verificationToken = userService.generateNewUserRegistrationVerificationToken(oldToken);
		User user = verificationToken.getUser();
		// generate a url to resending the token
		resendVerificationTokenMail(user, applicationUrl(request), verificationToken);
		return "Verification link has been send";
	}

	// GENERATE A URL TO RESEND VERIFCATIO TOKEN
	private void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
		String url = applicationUrl + "/resendVerificationToken?token=" + verificationToken.getToken();
		log.info("Click to verify your account: " + url);
	}

	// RESET PASSWORD (mail/link)
	@PostMapping("/resetPassword")
	public String resetPassword(@RequestBody Password password, HttpServletRequest request) {
		User user = userService.findUserByEmail(password.getEmail());

		// create a url: to reset password
		String url = "";
		if (user != null) {
			String token = UUID.randomUUID().toString();
			userService.createPasswordResetToketForUser(user, token);
			url = passwordResetTokenMail(user, applicationUrl(request), token);
		}
		return url;
	}

	// GENERATE LINK/MAIL TO RESET PASSWORD
	private String passwordResetTokenMail(User user, String applicationUrl, String token) {
		String url = applicationUrl + "/savePassword?token=" + token;
		log.info("Click to reset password: {}", url);
		return url;
	}

	// HANDLING SAVE PASSWORD URL
	@PostMapping("/savePassword")
	public String savePassword(@RequestParam("token") String token, @RequestBody Password password) {
		// validate the passwordResetToken
		String result = userService.validatePasswordResetToken(token);
		if (!result.equalsIgnoreCase("valid")) {
			return "invalid";
		}
		// else: update the password
		User user = userService.getUserByPasswordResetToken(token);
		if (user != null) {
			userService.changePassword(user, password.getNewPassword());
		} else {
			return "invalid token";
		}
		return "Password Reset successfull";
	}
}
