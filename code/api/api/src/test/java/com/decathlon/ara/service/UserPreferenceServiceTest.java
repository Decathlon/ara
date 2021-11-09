package com.decathlon.ara.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import com.decathlon.ara.configuration.security.TestAuthentication;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.domain.UserPreference;
import com.decathlon.ara.repository.UserPreferenceRepository;
import com.decathlon.ara.repository.UserRepository;
import com.decathlon.ara.service.UserPreferenceService.Preference;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {
    
    @Mock
    private UserPreferenceRepository userPreferenceRepository;
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserPreferenceService userPreferenceService;
    
    @Test
    void getValueShouldReturnDefaultValueWhenPreferenceIsNotSetForUser() {
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication());
        Preference preference = new Preference("test", "default");
        String value = userPreferenceService.getValue(preference);
        Assertions.assertEquals(preference.defaultValue(), value);
    }
    
    @Test
    void getValueShouldReturnDatabaseValueWhenPreferenceIsSetForUser() {
        TestAuthentication authentication = new TestAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Preference preference = new Preference("test", "default");
        UserPreference userPreference = new UserPreference();
        userPreference.setValue("database");
        Mockito.when(userPreferenceRepository.findByIdUserIdAndIdKey(authentication.getName(), preference.key())).thenReturn(userPreference);
        String value = userPreferenceService.getValue(preference);
        Assertions.assertEquals(userPreference.getValue(), value);
    }
    
    @Test
    void isEnabledShouldReturnDefaultWhenPreferenceIsNotSetForUserAndDefaultValueIsABoolean() {
        TestAuthentication authentication = new TestAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Preference preference = new Preference("test", Boolean.TRUE);
        boolean enabled = userPreferenceService.isEnabled(preference);
        Assertions.assertTrue(enabled);
    }
    
    @Test
    void isEnabledShouldReturnBooleanValueOfDefaultValueWhenPreferenceIsNotSetForUserAndDefaultValueIsAStringReprensentationOfABoolean() {
        TestAuthentication authentication = new TestAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Preference preference = new Preference("test", Boolean.TRUE.toString());
        boolean enabled = userPreferenceService.isEnabled(preference);
        Assertions.assertTrue(enabled);
    }
    
    @Test
    void isEnabledShouldReturnFalseWhenPreferenceIsNotSetForUserAndDefaultValueIsNotABooleanOrStringRepresentationOfABoolean() {
        TestAuthentication authentication = new TestAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Preference preference = new Preference("test", "notABooleanValue");
        boolean enabled = userPreferenceService.isEnabled(preference);
        Assertions.assertFalse(enabled);
    }
    
    @Test
    void isEnabledShouldReturnFalseWhenPreferenceIsNotSetForUserAndDefaultValueIsNull() {
        TestAuthentication authentication = new TestAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Preference preference = new Preference("test", null);
        boolean enabled = userPreferenceService.isEnabled(preference);
        Assertions.assertFalse(enabled);
    }
    
    @Test
    void isEnabledShouldReturnFalseWhenPreferenceIsSetForUserWithNotAStringReprensentationOfABoolean() {
        TestAuthentication authentication = new TestAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Preference preference = new Preference("test", Boolean.TRUE);
        UserPreference userPreference = new UserPreference();
        userPreference.setValue("notABooleanValue");
        Mockito.when(userPreferenceRepository.findByIdUserIdAndIdKey(authentication.getName(), preference.key())).thenReturn(userPreference);
        boolean enabled = userPreferenceService.isEnabled(preference);
        Assertions.assertFalse(enabled);
    }
    
    @Test
    void isEnabledShouldReturnBooleanValueOfDatabaseValueWhenPreferenceIsSetForUserWithStringReprensentationOfABoolean() {
        TestAuthentication authentication = new TestAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Preference preference = new Preference("test", Boolean.FALSE);
        UserPreference userPreference = new UserPreference();
        userPreference.setValue("true");
        Mockito.when(userPreferenceRepository.findByIdUserIdAndIdKey(authentication.getName(), preference.key())).thenReturn(userPreference);
        boolean enabled = userPreferenceService.isEnabled(preference);
        Assertions.assertTrue(enabled);
    }
    
    @Test
    void setValueShouldCreateUserPreferenceIfItNotAlreadyExists() {
        TestAuthentication authentication = new TestAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Preference preference = new Preference("test", "default");
        User user = new User();
        Mockito.when(userRepository.findByMemberName(authentication.getName())).thenReturn(user);
        userPreferenceService.setValue(preference, "value");
        ArgumentCaptor<UserPreference> captor = ArgumentCaptor.forClass(UserPreference.class);
        Mockito.verify(userPreferenceRepository).save(captor.capture());
        UserPreference savedPreference = captor.getValue();
        Assertions.assertEquals(user, savedPreference.getUser());
        Assertions.assertEquals(preference.key(), savedPreference.getKey());
        Assertions.assertEquals("value", savedPreference.getValue());
    }
    
    @Test
    void setValueShouldUpdateUserPreferenceIfItAlreadyExists() {
        TestAuthentication authentication = new TestAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Preference preference = new Preference("test", "default");
        UserPreference userPreference = new UserPreference();
        Mockito.when(userPreferenceRepository.findByIdUserIdAndIdKey(authentication.getName(), preference.key())).thenReturn(userPreference);
        userPreferenceService.setValue(preference, "value");
        ArgumentCaptor<UserPreference> captor = ArgumentCaptor.forClass(UserPreference.class);
        Mockito.verify(userPreferenceRepository).save(captor.capture());
        UserPreference savedPreference = captor.getValue();
        Assertions.assertEquals(userPreference, savedPreference);
        Assertions.assertEquals("value", savedPreference.getValue());
    }
    
    @Test
    void setValueShouldSetStringRepresentationOfValueObjectWhenValueIsNotAString() {
        TestAuthentication authentication = new TestAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Preference preference = new Preference("test", Boolean.FALSE);
        User user = new User();
        Mockito.when(userRepository.findByMemberName(authentication.getName())).thenReturn(user);
        userPreferenceService.setValue(preference, Boolean.TRUE);
        ArgumentCaptor<UserPreference> captor = ArgumentCaptor.forClass(UserPreference.class);
        Mockito.verify(userPreferenceRepository).save(captor.capture());
        UserPreference savedPreference = captor.getValue();
        Assertions.assertEquals(user, savedPreference.getUser());
        Assertions.assertEquals(preference.key(), savedPreference.getKey());
        Assertions.assertEquals(Boolean.TRUE.toString(), savedPreference.getValue());
    }
    
    @Test
    void setValueShouldSetNullValueWhenValueIsNull() {
        TestAuthentication authentication = new TestAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Preference preference = new Preference("test", Boolean.FALSE);
        User user = new User();
        Mockito.when(userRepository.findByMemberName(authentication.getName())).thenReturn(user);
        userPreferenceService.setValue(preference, null);
        ArgumentCaptor<UserPreference> captor = ArgumentCaptor.forClass(UserPreference.class);
        Mockito.verify(userPreferenceRepository).save(captor.capture());
        UserPreference savedPreference = captor.getValue();
        Assertions.assertEquals(user, savedPreference.getUser());
        Assertions.assertEquals(preference.key(), savedPreference.getKey());
        Assertions.assertNull(savedPreference.getValue());
    }

}
