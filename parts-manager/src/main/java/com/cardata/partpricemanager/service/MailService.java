package com.cardata.partpricemanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public final class MailService {

	private final JavaMailSender mailSender;


	public void sendMail(String to, String subject, String text) {
		var message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		message.setFrom("your_email@gmail.com"); // optional, default = configured user
		mailSender.send(message);
	}
}