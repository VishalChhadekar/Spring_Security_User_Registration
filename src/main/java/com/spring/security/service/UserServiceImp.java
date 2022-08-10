package com.spring.security.service;

import java.util.Calendar;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.spring.security.entity.PasswordResetToken;
import com.spring.security.entity.User;
import com.spring.security.entity.VerificationToken;
import com.spring.security.repository.PasswordResetTokenRepository;
import com.spring.security.repository.UserRepository;
import com.spring.security.repository.VerificationTokenRepository;

@Service
public class UserServiceImp implements UserService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private VerificationTokenRepository verificationTokenRepository;
	@Autowired
	private PasswordResetTokenRepository passwordResetTokenRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public User saveUser(User user) {
		User newUser = new User();
		newUser.setFirstName(user.getFirstName());
		newUser.setLastName(user.getLastName());
		newUser.setEmail(user.getEmail());
		newUser.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(newUser);
	}

	@Override
	public void saveVerificationTokenForUser(String token, User user) {
		VerificationToken verificationToken = new VerificationToken(token, user);
		verificationTokenRepository.save(verificationToken);
	}

	// VERIFY TOKEN COME WITH URL/EMAIL CLIKED BY USER
	@Override
	public String verifyVerificationToken(String token) {
		// get token from DB
		VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
		// if token is not present return invalid
		if (verificationToken == null) {
			return "invalid";
		}
		// else verify the token, and check does token expired or not
		User user = verificationToken.getUser(); // we are able to get the user: because of OneToOne relation between
													// User and VerificationToken
		Calendar calendar = Calendar.getInstance();

		// check is expired or not
		if (verificationToken.getExpirationTime().getTime() - calendar.getTime().getTime() <= 0) {
			// delete token from DB
			verificationTokenRepository.delete(verificationToken);
			return "Token is expired";
		}
		// else: if token is not expired, update enabled=true;---> i.e. User is
		// validated
		user.setEnabled(true);
		userRepository.save(user);
		return "valid";
	}

	// GENERATE NEW VERIFICATION TOKEN
	@Override
	public VerificationToken generateNewUserRegistrationVerificationToken(String oldToken) {
		// get verification object from DB
		VerificationToken verificationToken = verificationTokenRepository.findByToken(oldToken);
		// generate new token
		String newToken = UUID.randomUUID().toString();
		// set new token to verificationToken object
		verificationToken.setToken(newToken);
		// save verificationToken object back to DB
		verificationTokenRepository.save(verificationToken);
		return verificationToken;
	}

	// FIND USER BY EMAIL
	@Override
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	// PASSWORD RESET TOKEN
	@Override
	public void createPasswordResetToketForUser(User user, String token) {
		PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
		passwordResetTokenRepository.save(passwordResetToken);
	}

	// VALIDATING PASSWORD RESET TOKEN
	@Override
	public String validatePasswordResetToken(String token) {

		PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
		// if no token present in DB return invalid
		if (passwordResetToken == null) {
			return "Invalid";
		}
		User user = passwordResetToken.getUser();
		Calendar calendar = Calendar.getInstance();

		// check does token is expired or not
		if (passwordResetToken.getExpirationTime().getTime() - calendar.getTime().getTime() <= 0) {
			passwordResetTokenRepository.delete(passwordResetToken);
			return "token is expired";
		}
		return "Valid";
	}

	// RETURN USER BY passwordResteToken
	@Override
	public User getUserByPasswordResetToken(String token) {
		return passwordResetTokenRepository.findByToken(token).getUser();
	}

	// UPDATE PASSWORD
	@Override
	public void changePassword(User user, String newPassword) {
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}
}
