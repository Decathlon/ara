package com.decathlon.ara.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.domain.UserPreference;
import com.decathlon.ara.repository.UserPreferenceRepository;
import com.decathlon.ara.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class UserPreferenceService {

    record Preference(String key, Object defaultValue) {
    }

    public static final Preference DEFAULT_PROJECT = new Preference("defaultProject", null);

    private UserPreferenceRepository userPreferenceRepository;
    private UserRepository userRepository;

    public UserPreferenceService(UserPreferenceRepository userPreferenceRepository, UserRepository userRepository) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.userRepository = userRepository;
    }

    public boolean isEnabled(Preference preference) {
        return Boolean.parseBoolean(getValue(preference));
    }

    public String getValue(Preference preference) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPreference userPreference = userPreferenceRepository.findByIdUserIdAndIdKey(authentication.getName(), preference.key());
        if (userPreference != null) {
            return userPreference.getValue();
        } else {
            if (preference.defaultValue() != null) {
                return preference.defaultValue().toString();
            } else {
                return null;
            }
        }
    }

    @Transactional
    public void setValue(Preference preference, Object value) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPreference userPreference = userPreferenceRepository.findByIdUserIdAndIdKey(authentication.getName(), preference.key());
        if (userPreference == null) {
            userPreference = new UserPreference(userRepository.findByMemberName(authentication.getName()), preference.key());
        }
        userPreference.setValue(value == null ? null : value.toString());
        userPreferenceRepository.save(userPreference);
    }

}
