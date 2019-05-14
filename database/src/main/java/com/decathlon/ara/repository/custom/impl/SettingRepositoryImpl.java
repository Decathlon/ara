package com.decathlon.ara.repository.custom.impl;

import com.decathlon.ara.domain.QSetting;
import com.decathlon.ara.repository.custom.SettingRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SettingRepositoryImpl implements SettingRepositoryCustom {

    @NonNull
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Map<String, String> getProjectSettings(long projectId) {
        return jpaQueryFactory.select(QSetting.setting.code, QSetting.setting.value)
                .from(QSetting.setting)
                .where(QSetting.setting.projectId.eq(Long.valueOf(projectId)))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(QSetting.setting.code),
                        tuple -> {
                            final String value = tuple.get(QSetting.setting.value);
                            return value == null ? "" : value;
                        }));
    }

}
