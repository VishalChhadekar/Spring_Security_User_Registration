package com.spring.security.event;

import org.springframework.context.ApplicationEvent;

import com.spring.security.entity.User;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegistrationCompleteEvent extends ApplicationEvent {
	private User user;
	private String applicationUrl;

	public RegistrationCompleteEvent(User user, String applicationUrl) {
		super(user);
		this.user = user;
		this.applicationUrl = applicationUrl;
	}
}
