package com.decathlon.ara.defect.rtc;

import com.decathlon.ara.defect.rtc.bean.State;
import com.decathlon.ara.defect.rtc.bean.WorkItem;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.service.SettingProviderService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.support.Settings;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RtcDefectAdapterTest {

    @Mock
    private SettingService settingService;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RtcDateTimeAdapter rtcDateTimeAdapter;

    @Mock
    private SettingProviderService settingProviderService;

    private RtcDefectAdapter cut;

    @Before
    public void setUp() {
        when(restTemplateBuilder.requestFactory(ArgumentMatchers.<Class<ClientHttpRequestFactory>>any()))
                .thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        cut = new RtcDefectAdapter(settingService, restTemplateBuilder, rtcDateTimeAdapter, settingProviderService);
    }

    @Test
    public void isValidId_should_work() {
        long anyProjectId = 42;

        assertThat(cut.isValidId(anyProjectId, "-2147483648")).isTrue();
        assertThat(cut.isValidId(anyProjectId, "-1")).isTrue();
        assertThat(cut.isValidId(anyProjectId, "0")).isTrue();
        assertThat(cut.isValidId(anyProjectId, "1")).isTrue();
        assertThat(cut.isValidId(anyProjectId, "2147483647")).isTrue();

        assertThat(cut.isValidId(anyProjectId, "-100000000000")).isFalse();
        assertThat(cut.isValidId(anyProjectId, "-2147483649")).isFalse(); // RTC will respond ERROR 500 Invalid number format
        assertThat(cut.isValidId(anyProjectId, "2147483648")).isFalse(); // if outside of Integer's [ MIN_VALUE .. MAX_VALUE ]
        assertThat(cut.isValidId(anyProjectId, "10000000000")).isFalse();
        assertThat(cut.isValidId(anyProjectId, " -42 ")).isFalse();
        assertThat(cut.isValidId(anyProjectId, " 42")).isFalse();
        assertThat(cut.isValidId(anyProjectId, "42 ")).isFalse();
        assertThat(cut.isValidId(anyProjectId, "42not")).isFalse();
    }

    @Test
    public void getIdFormatHint_should_return_must_be_a_number() {
        // GIVEN
        long anyProjectId = 42;

        // WHEN
        final String hint = cut.getIdFormatHint(anyProjectId);

        // THEN
        assertThat(hint).isEqualTo("must be a number");
    }

    @Test
    public void getCode_should_return_rtc() {
        assertThat(cut.getCode()).isEqualTo("rtc");
    }

    @Test
    public void getName_should_return_RTC() {
        assertThat(cut.getName()).isEqualTo("RTC (IBM Rational Team Concert)");
    }

    @Test
    public void buildDefectsQueryUrl_should_work() {
        // GIVEN
        long aProjectId = 42;
        when(settingService.get(aProjectId, Settings.DEFECT_RTC_ROOT_URL)).thenReturn("url/");
        when(settingService.get(aProjectId, Settings.DEFECT_RTC_WORK_ITEM_RESOURCE_PATH)).thenReturn("path");
        when(settingService.getList(aProjectId, Settings.DEFECT_RTC_WORK_ITEM_TYPES))
                .thenReturn(Arrays.asList("t1", "T2"));
        String filter = "FILTER";
        int batchSize = 11;

        // WHEN
        String url = cut.buildDefectsQueryUrl(aProjectId, filter, batchSize);

        // THEN
        assertThat(url).isEqualTo("url/path?fields=work" +
                "item/workItem[(type/id='t1' or type/id='t2') and (FILTER)]/(id|state/name|resolutionDate)&size=11");
    }

    @Test
    public void toFilter_should_work() {
        // GIVEN
        String fieldName = "field";
        List<String> fieldValues = Arrays.asList("1", "2", "'C'");

        // WHEN
        final String filter = cut.toFilter(fieldName, fieldValues);

        // THEN
        assertThat(filter).isEqualTo("field=1 or field=2 or field='C'");
    }

    @Test
    public void toProblemStatus_should_return_OPEN_for_an_OPEN_state() {
        // GIVEN
        long aProjectId = 42;
        when(settingService.getList(aProjectId, Settings.DEFECT_RTC_CLOSED_STATES))
                .thenReturn(Arrays.asList("a", "b", "c"));
        when(settingService.getList(aProjectId, Settings.DEFECT_RTC_OPEN_STATES))
                .thenReturn(Arrays.asList("d", "e", "f"));
        final WorkItem workItemWithOpenState = new WorkItem().withState(new State().withName("e"));

        // WHEN
        final ProblemStatus status = cut.toProblemStatus(aProjectId, workItemWithOpenState);

        // THEN
        assertThat(status).isEqualTo(ProblemStatus.OPEN);
    }

    @Test
    public void toProblemStatus_should_return_CLOSED_for_a_CLOSED_state() {
        // GIVEN
        long aProjectId = 42;
        when(settingService.getList(aProjectId, Settings.DEFECT_RTC_CLOSED_STATES))
                .thenReturn(Arrays.asList("a", "b", "c"));
        when(settingService.getList(aProjectId, Settings.DEFECT_RTC_OPEN_STATES))
                .thenReturn(Arrays.asList("d", "e", "f"));
        final WorkItem workItemWithClosedState = new WorkItem().withState(new State().withName("b"));

        // WHEN
        final ProblemStatus status = cut.toProblemStatus(aProjectId, workItemWithClosedState);

        // THEN
        assertThat(status).isEqualTo(ProblemStatus.CLOSED);
    }

    @Test
    public void toProblemStatus_should_return_CLOSED_for_a_state_configured_as_both_OPEN_and_CLOSED() {
        // GIVEN
        long aProjectId = 42;
        when(settingService.getList(aProjectId, Settings.DEFECT_RTC_CLOSED_STATES))
                .thenReturn(Arrays.asList("a", "b"));
        when(settingService.getList(aProjectId, Settings.DEFECT_RTC_OPEN_STATES))
                .thenReturn(Arrays.asList("a", "c"));
        final WorkItem workItemWithOpenAndClosedState = new WorkItem().withState(new State().withName("a"));

        // WHEN
        final ProblemStatus status = cut.toProblemStatus(aProjectId, workItemWithOpenAndClosedState);

        // THEN
        assertThat(status).isEqualTo(ProblemStatus.CLOSED);
    }

    @Test
    public void toProblemStatus_should_return_OPEN_for_an_unknown_state() {
        // GIVEN
        long aProjectId = 42;
        when(settingService.getList(aProjectId, Settings.DEFECT_RTC_CLOSED_STATES))
                .thenReturn(Collections.emptyList());
        when(settingService.getList(aProjectId, Settings.DEFECT_RTC_OPEN_STATES))
                .thenReturn(Collections.emptyList());
        final WorkItem workItemWithUnknownState = new WorkItem().withState(new State().withName("a"));

        // WHEN
        final ProblemStatus status = cut.toProblemStatus(aProjectId, workItemWithUnknownState);

        // THEN
        assertThat(status).isEqualTo(ProblemStatus.OPEN);
    }

    @Test
    public void toProblemStatus_should_compare_ignoring_case_with_OPEN() {
        // GIVEN
        long aProjectId = 42;
        when(settingService.getList(aProjectId, Settings.DEFECT_RTC_CLOSED_STATES))
                .thenReturn(Collections.singletonList("aAa"));
        when(settingService.getList(aProjectId, Settings.DEFECT_RTC_OPEN_STATES))
                .thenReturn(Collections.singletonList("bBb"));
        final WorkItem workItemWithOpenUpperCasedState = new WorkItem().withState(new State().withName("Bbb"));

        // WHEN
        final ProblemStatus status = cut.toProblemStatus(aProjectId, workItemWithOpenUpperCasedState);

        // THEN
        assertThat(status).isEqualTo(ProblemStatus.OPEN);
    }

    @Test
    public void toProblemStatus_should_compare_ignoring_case_with_CLOSED() {
        // GIVEN
        long aProjectId = 42;
        when(settingService.getList(aProjectId, Settings.DEFECT_RTC_CLOSED_STATES))
                .thenReturn(Collections.singletonList("aAa"));
        when(settingService.getList(aProjectId, Settings.DEFECT_RTC_OPEN_STATES))
                .thenReturn(Collections.singletonList("bBb"));
        final WorkItem workItemWithClosedUpperCasedState = new WorkItem().withState(new State().withName("Aaa"));

        // WHEN
        final ProblemStatus status = cut.toProblemStatus(aProjectId, workItemWithClosedUpperCasedState);

        // THEN
        assertThat(status).isEqualTo(ProblemStatus.CLOSED);
    }

    @Test
    public void addCookies_should_add_cookies() {
        // GIVEN
        Map<String, String> cookies = new HashMap<>();
        cookies.put("key1", "value1");
        cookies.put("key2", "value2");
        HttpHeaders headers = new HttpHeaders();

        // WHEN
        cut.addCookies(cookies, headers);

        // THEN
        assertThat(headers.getFirst("Cookie")).isIn(
                "key1=value1; key2=value2", // Map has no specified order: any order is fine
                "key2=value2; key1=value1");
    }

    @Test
    public void extractCookies_should_extract_set_cookies() {
        // GIVEN
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Some", Collections.singletonList("Too_be=Ignored"));
        headers.put("Set-Cookie", Arrays.asList("key1=value1", "key2=value2; some other properties", "wrong_format"));
        ResponseEntity<Map<String, List<String>>> response =
                new ResponseEntity<>(CollectionUtils.toMultiValueMap(headers), HttpStatus.OK);

        // WHEN
        final Map<String, String> cookies = cut.extractCookies(response);

        // THEN
        assertThat(cookies).containsExactly(entry("key1", "value1"), entry("key2", "value2"));
    }

    @Test
    public void extractCookies_should_return_none_if_no_cookies_set() {
        // GIVEN
        Map<String, List<String>> headers = new HashMap<>();
        ResponseEntity<Map<String, List<String>>> response =
                new ResponseEntity<>(CollectionUtils.toMultiValueMap(headers), HttpStatus.OK);

        // WHEN
        final Map<String, String> cookies = cut.extractCookies(response);

        // THEN
        assertThat(cookies).isEmpty();
    }

}
