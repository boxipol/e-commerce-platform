package com.pd.ecommerce.kafka;

import com.pd.ecommerce.event.UserCreatedEvent;
import com.pd.ecommerce.event.UserDeletedEvent;
import com.pd.ecommerce.event.UserUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserEventProducer Tests")
class UserEventProducerTest {

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	@InjectMocks
	private UserEventProducer eventProducer;

	private String testEmail;
	private Instant testInstant;


	@BeforeEach
	void setUp() {
		testEmail = "test@example.com";
		testInstant = Instant.now();

		// Mock KafkaTemplate behavior - return a completed future
		when(kafkaTemplate.send(anyString(), anyString(), any()))
			.thenReturn(CompletableFuture.completedFuture(null));
	}

	@Test
	@DisplayName("sendUserRegistered - should publish user created event")
	void testSendUserRegistered() {
		// Given
		UserCreatedEvent event = new UserCreatedEvent(testEmail, testInstant);

		// When
		eventProducer.sendUserRegistered(event);

		// Then
		ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

		verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

		assertThat(topicCaptor.getValue()).isEqualTo("user.created");
		assertThat(keyCaptor.getValue()).isEqualTo(testEmail);
		assertThat(eventCaptor.getValue()).isInstanceOf(UserCreatedEvent.class);
	}

	@Test
	@DisplayName("sendUserUpdated - should publish user updated event")
	void testSendUserUpdated() {
		// Given
		UserUpdatedEvent event = new UserUpdatedEvent(testEmail, testInstant);

		// When
		eventProducer.sendUserUpdated(event);

		// Then
		ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

		verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

		assertThat(topicCaptor.getValue()).isEqualTo("user.updated");
		assertThat(keyCaptor.getValue()).isEqualTo(testEmail);
		assertThat(eventCaptor.getValue()).isInstanceOf(UserUpdatedEvent.class);
	}

	@Test
	@DisplayName("sendUserDeleted - should publish user deleted event")
	void testSendUserDeleted() {
		// Given
		UserDeletedEvent event = new UserDeletedEvent(testEmail, testInstant);

		// When
		eventProducer.sendUserDeleted(event);

		// Then
		ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

		verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

		assertThat(topicCaptor.getValue()).isEqualTo("user.deleted");
		assertThat(keyCaptor.getValue()).isEqualTo(testEmail);
		assertThat(eventCaptor.getValue()).isInstanceOf(UserDeletedEvent.class);
	}

	@Test
	@DisplayName("sendUserRegistered - should use email as message key")
	void testEventKeyIsEmail() {
		// Given
		String specificEmail = "specific@example.com";
		UserCreatedEvent event = new UserCreatedEvent(specificEmail, testInstant);

		// When
		eventProducer.sendUserRegistered(event);

		// Then
		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
		verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), any());

		assertThat(keyCaptor.getValue()).isEqualTo(specificEmail);
	}

	@Test
	@DisplayName("sendUserUpdated - should preserve timestamp in event")
	void testEventPreservesTimestamp() {
		// Given
		Instant specificTime = Instant.parse("2026-06-16T10:30:00Z");
		UserUpdatedEvent event = new UserUpdatedEvent(testEmail, specificTime);

		// When
		eventProducer.sendUserUpdated(event);

		// Then
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
		verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

		UserUpdatedEvent capturedEvent = (UserUpdatedEvent) eventCaptor.getValue();
		assertThat(capturedEvent.createdAt()).isEqualTo(specificTime);
	}


	@Test
	@DisplayName("sendUserRegistered - should publish user created event")
	void testSendUserRegistered2() {
		// Given
		UserCreatedEvent event = new UserCreatedEvent(testEmail, testInstant);

		// When
		eventProducer.sendUserRegistered(event);

		// Then
		ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

		verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

		assertThat(topicCaptor.getValue()).isEqualTo("user.created");
		assertThat(keyCaptor.getValue()).isEqualTo(testEmail);
		assertThat(eventCaptor.getValue()).isInstanceOf(UserCreatedEvent.class);
	}

	@Test
	@DisplayName("sendUserUpdated - should publish user updated event")
	void testSendUserUpdated2() {
		// Given
		UserUpdatedEvent event = new UserUpdatedEvent(testEmail, testInstant);

		// When
		eventProducer.sendUserUpdated(event);

		// Then
		ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

		verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

		assertThat(topicCaptor.getValue()).isEqualTo("user.updated");
		assertThat(keyCaptor.getValue()).isEqualTo(testEmail);
		assertThat(eventCaptor.getValue()).isInstanceOf(UserUpdatedEvent.class);
	}

	@Test
	@DisplayName("sendUserDeleted - should publish user deleted event")
	void testSendUserDeleted2() {
		// Given
		UserDeletedEvent event = new UserDeletedEvent(testEmail, testInstant);

		// When
		eventProducer.sendUserDeleted(event);

		// Then
		ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

		verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

		assertThat(topicCaptor.getValue()).isEqualTo("user.deleted");
		assertThat(keyCaptor.getValue()).isEqualTo(testEmail);
		assertThat(eventCaptor.getValue()).isInstanceOf(UserDeletedEvent.class);
	}

	@Test
	@DisplayName("sendUserRegistered - should use email as message key")
	void testEventKeyIsEmail2() {
		// Given
		String specificEmail = "specific@example.com";
		UserCreatedEvent event = new UserCreatedEvent(specificEmail, testInstant);

		// When
		eventProducer.sendUserRegistered(event);

		// Then
		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
		verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), any());

		assertThat(keyCaptor.getValue()).isEqualTo(specificEmail);
	}

	@Test
	@DisplayName("sendUserUpdated - should preserve timestamp in event")
	void testEventPreservesTimestamp2() {
		// Given
		Instant specificTime = Instant.parse("2026-06-16T10:30:00Z");
		UserUpdatedEvent event = new UserUpdatedEvent(testEmail, specificTime);

		// When
		eventProducer.sendUserUpdated(event);

		// Then
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
		verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

		UserUpdatedEvent capturedEvent = (UserUpdatedEvent) eventCaptor.getValue();
		assertThat(capturedEvent.createdAt()).isEqualTo(specificTime);
	}
}