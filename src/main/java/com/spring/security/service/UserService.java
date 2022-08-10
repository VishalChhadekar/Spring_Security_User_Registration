package com.spring.security.service;

import com.spring.security.entity.User;
import com.spring.security.entity.VerificationToken;

public interface UserService {

	User saveUser(User user);

	void saveVerificationTokenForUser(String token, User user);

	String verifyVerificationToken(String token);

	VerificationToken generateNewUserRegistrationVerificationToken(String oldToken);

	User findUserByEmail(String email);

	void createPasswordResetToketForUser(User user, String token);

	String validatePasswordResetToken(String token);

	User getUserByPasswordResetToken(String token);

	void changePassword(User user, String newPassword);

}
