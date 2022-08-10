package com.spring.security.entity;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
public class VerificationToken {
	// expiration time of token is 10 min
	private static final int EXPIRATION_TIME = 10;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String token;
	private Date expirationTime;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_USER_VERIFY_TOKEN"))
	private User user;

	// Constructor with user
	public VerificationToken(String token, User user) {
		this.token = token;
		this.user = user;
		this.expirationTime = calculateExpirationTime(EXPIRATION_TIME);
	}

	// Constructor without user
	public VerificationToken(String token) {
		super();
		this.token = token;
		this.expirationTime = calculateExpirationTime(EXPIRATION_TIME);
	}

	// this will add the 10 min into the real time (current time)
	public Date calculateExpirationTime(int expiraton_time) {

		Calendar calendar = Calendar.getInstance(); // get current time instance
		calendar.setTimeInMillis(new Date().getTime());
		calendar.add(Calendar.MINUTE, expiraton_time);// add 10 minutes into current time instance
		return new Date(calendar.getTime().getTime());// Returning time until token will be valid
	}

}
