package com.spring.security.event.listener;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.spring.security.entity.User;
import com.spring.security.event.RegistrationCompleteEvent;
import com.spring.security.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {
	@Autowired
	private UserService userService;

	@Override
	public void onApplicationEvent(RegistrationCompleteEvent event) {
		// get user from event
		User user = event.getUser();
		// generate a new token
		String token = UUID.randomUUID().toString();
		userService.saveVerificationTokenForUser(token, user);

		// sent a link/mail to user to verify the registration
		String url = event.getApplicationUrl() + "/verifyRegistration?token=" + token;

		// for demo purpose, we are sending mail; just printing the link to console
		// using Slf4j
		log.info("Click here to verify your account: {}" + url);

	}
}
