package com.decathlon.ara.defect.rtc;

import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.defect.DefectAdapter;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.defect.rtc.bean.WorkItem;
import com.decathlon.ara.defect.rtc.bean.WorkItemContainer;
import com.decathlon.ara.service.SettingProviderService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.service.support.Settings;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class RtcDefectAdapter implements DefectAdapter {

    private static final Pattern COOKIE_PATTERN = Pattern.compile("^([^=]*)=([^;]*)");
    private static final Pattern RTC_ID_PATTERN = Pattern.compile("[-]?[0-9]{1,10}");

    private static final String BAD_RETURN_STATUS = "Bad return status (";

    private final SettingService settingService;
    private final RestTemplate restTemplate;
    private final RtcDateTimeAdapter rtcDateTimeAdapter;
    private final SettingProviderService settingProviderService;

    @Autowired
    public RtcDefectAdapter(SettingService settingService,
                            RestTemplateBuilder restTemplateBuilder,
                            RtcDateTimeAdapter rtcDateTimeAdapter,
                            SettingProviderService settingProviderService) {
        this.settingService = settingService;
        this.rtcDateTimeAdapter = rtcDateTimeAdapter;
        this.settingProviderService = settingProviderService;

        // Create a RestTemplate bypassing SSL for the self-signed-certificate of RTC
        restTemplate = restTemplateBuilder.requestFactory(AllTrustingClientHttpRequestFactory.class).build();
    }

    /**
     * Called the first time defects get queried or if a long period has passed from last incremental indexation.<br>
     * The parameter will only contain defects assigned to ARA problems, to reduce initialisation workload.<br>
     * It is up to the implementation to query them in bulk or one by one.
     *
     * @param projectId the ID of the project in which to work
     * @param ids       all IDs of defects/issues/bugs/... to query in the external issue-tracker
     * @return the statuses of the requested defects: a requested defect missing from the returned list or having a null
     * status is considered nonexistent and an error will be shown to users (can be empty but NEVER null)
     * @throws FetchException on any network issue, wrong HTTP response status code or parsing issue
     */
    @Override
    public List<Defect> getStatuses(long projectId, List<String> ids) throws FetchException {
        List<Defect> defects = new ArrayList<>();

        final Map<String, String> cookies = authenticate(projectId);

        List<String> realIds = ids.stream()
                .filter(id -> isValidId(projectId, id))
                .collect(Collectors.toList());
        if (!realIds.isEmpty()) {
            final int batchSize = settingService.getInt(projectId, Settings.DEFECT_RTC_BATCH_SIZE);
            for (List<String> idsPartition : ListUtils.partition(realIds, batchSize)) {
                String url = buildDefectsQueryUrl(projectId, toFilter("id", idsPartition), batchSize);
                // We request exactly one page: it is full, but we do not want to request the next page: it will be empty
                // Trick queryDefects to think server returned less than we requested, so it knows it got the last page
                defects.addAll(queryDefects(projectId, cookies, url, batchSize + 1));
            }
        }

        return defects;
    }

    /**
     * Called at regular interval for incremental indexation of defects.<br>
     * Return all defects that were modified since the given date:<br>
     * ARA will update concerned problems and ignore defects that are not assigned to any problem.
     *
     * @param projectId the ID of the project in which to work
     * @param since     the date from which to get updated defects
     * @return all defects that were modified since the given date (can be empty but NEVER null)
     * @throws FetchException on any network issue, wrong HTTP response status code or parsing issue
     */
    @Override
    public List<Defect> getChangedDefects(long projectId, Date since) throws FetchException {
        final Map<String, String> cookies = authenticate(projectId);

        final int batchSize = settingService.getInt(projectId, Settings.DEFECT_RTC_BATCH_SIZE);
        String url = buildDefectsQueryUrl(projectId, "modified>'{since}'", batchSize);
        // We request exactly one page: it is full, but we do not want to request the next page: it will be empty
        // Trick queryDefects to think server returned less than we requested, so it knows it got the last page
        return queryDefects(projectId, cookies, url, batchSize, rtcDateTimeAdapter.marshal(since));
    }

    /**
     * Validate a user input for a defect ID in the tracker.
     *
     * @param projectId the ID of the project in which to work
     * @param id        a user-typed defect ID
     * @return true if it is a valid ID for the backed defect tracking system
     */
    @Override
    public boolean isValidId(long projectId, String id) {
        if (!RTC_ID_PATTERN.matcher(id).matches()) {
            return false;
        }
        try {
            final Integer parsedId = Integer.valueOf(id);
            log.trace("Parsed defect ID {} (logged for no 'Result of method not used' warning)", parsedId);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Get the guiding help text to display to users when they type a wrongly-formatted defect ID.
     *
     * @param projectId the ID of the project in which to work
     * @return the message to be inserted in a sentence (no capital-case, no end-point: eg. "must be a positive number")
     */
    @Override
    public String getIdFormatHint(long projectId) {
        return "must be a number";
    }

    /**
     * @return the code to uniquely identify this defect adapter class (stored in the project settings in database)
     */
    @Override
    public String getCode() {
        return "rtc";
    }

    /**
     * @return "RTC (IBM Rational Team Concert)"
     */
    @Override
    public String getName() {
        return "RTC (IBM Rational Team Concert)";
    }

    @Override
    public List<SettingDTO> getSettingDefinitions() {
        return settingProviderService.getDefectRtcDefinitions();
    }

    /**
     * Build the URL to request RTC for work-items resources matching the given {@code filter},
     * by page of {@code batchSize} elements.
     *
     * @param projectId the ID of the project in which to work
     * @param filter    the filter query to use to return only wanted work-items
     * @param batchSize the number of work-items to return by page
     * @return the URL to request RTC work-items and load them as {@link WorkItemContainer}
     */
    String buildDefectsQueryUrl(long projectId, String filter, int batchSize) {
        final List<String> workItemTypes = settingService.getList(projectId, Settings.DEFECT_RTC_WORK_ITEM_TYPES);
        final String typeFilter = toFilter("type/id", workItemTypes.stream()
                .map(t -> "'" + t.toLowerCase() + "'")
                .collect(Collectors.toList()));
        String fullFilters = "(" + typeFilter + ") and (" + filter + ")";
        final String fields = "work" + "item/workItem[" + fullFilters + "]/(id|state/name|resolutionDate)";
        final String rootUrl = settingService.get(projectId, Settings.DEFECT_RTC_ROOT_URL);
        final String workItemResourcePath = settingService.get(projectId, Settings.DEFECT_RTC_WORK_ITEM_RESOURCE_PATH);
        return rootUrl + workItemResourcePath + "?fields=" + fields + "&size=" + batchSize;
    }

    /**
     * Given an authenticated session and an URL, request RTC and return a list of work-item IDs with their problem
     * status equivalents. This method handles requesting for all pages of the query, and returns all results.
     *
     * @param projectId         the ID of the project in which to work
     * @param cookies           an authenticated session
     * @param url               the work-item resource URL to query for filtered work-items
     * @param requestedPageSize the number of work-items requested per page: if we got less than that, we've got the last page
     * @param uriVariables      "{example}" variables to replace in the given URL
     * @return a list of work-item IDs with their problem status equivalents (can be empty but NEVER null)
     * @throws FetchException on any network issue, wrong HTTP response status code or parsing issue
     */
    private List<Defect> queryDefects(long projectId, Map<String, String> cookies, String url, int requestedPageSize, Object... uriVariables) throws FetchException {
        List<Defect> defects = new ArrayList<>();

        final String rootUrl = settingService.get(projectId, Settings.DEFECT_RTC_ROOT_URL);
        final String preAuthenticatePath = settingService.get(projectId, Settings.DEFECT_RTC_PRE_AUTHENTICATE_PATH);

        HttpHeaders headers = new HttpHeaders();
        addCookies(cookies, headers);
        headers.add(HttpHeaders.REFERER, rootUrl + preAuthenticatePath);

        HttpEntity<String> request = new HttpEntity<>("", headers);

        final URI uri = UriComponentsBuilder.fromHttpUrl(url).build(uriVariables);
        final ResponseEntity<WorkItemContainer> response;
        try {
            response = restTemplate.exchange(uri, HttpMethod.GET, request, WorkItemContainer.class);
        } catch (RestClientException e) {
            throw new FetchException(e, "Error while querying defects: " + e.getMessage(), uri.toString());
        }
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new FetchException(BAD_RETURN_STATUS + response.getStatusCode() + ") while querying defects", uri.toString());
        }

        final WorkItemContainer responseBody = response.getBody();
        if (responseBody != null && responseBody.getWorkItem() != null) {
            final List<WorkItem> workItems = responseBody.getWorkItem();
            for (WorkItem workItem : workItems) {
                defects.add(new Defect(workItem.getId(), toProblemStatus(projectId, workItem), workItem.getResolutionDate()));
            }
            // RTC will always return a nextPageUrl even on the last page.
            // To avoid generating a useless request to get the page after the last one,
            // don't request next page if we got less results than we requested
            if (workItems.size() == requestedPageSize) {
                final String rawNextPageUrl = responseBody.getNextPageUrl();
                try {
                    final String nextPageUrl = URLDecoder.decode(rawNextPageUrl, StandardCharsets.UTF_8.name());
                    defects.addAll(queryDefects(projectId, cookies, nextPageUrl, requestedPageSize));
                } catch (UnsupportedEncodingException e) {
                    log.error("Ignoring URL of next page because it cannot be parsed: {}", url, e);
                }
            }
        }

        return defects;
    }

    /**
     * @param fieldName   the name of the field to filter
     * @param fieldValues a list of values for the filter
     * @return the filter portion of the query string to use to query only these work-items
     */
    String toFilter(String fieldName, List<String> fieldValues) {
        StringBuilder builder = new StringBuilder();
        for (String fieldValue : fieldValues) {
            if (builder.length() > 0) {
                builder.append(" or ");
            }
            builder.append(fieldName).append("=").append(fieldValue);
        }
        return builder.toString();
    }

    /**
     * Transform an RTC work-item state into an ARA problem status.
     *
     * @param projectId the ID of the project in which to work
     * @param workItem  a work-item from RTC
     * @return the status of the matching problem on ARA
     */
    ProblemStatus toProblemStatus(long projectId, WorkItem workItem) {
        final String state = workItem.getState().getName();
        final String lowerState = state.toLowerCase();

        final List<String> closedStates = settingService.getList(projectId, Settings.DEFECT_RTC_CLOSED_STATES);
        final List<String> openStates = settingService.getList(projectId, Settings.DEFECT_RTC_OPEN_STATES);

        if (closedStates.stream().anyMatch(s -> s.equalsIgnoreCase(lowerState))) {
            if (openStates.contains(lowerState)) {
                log.error("Work item status \"{}\" is configured both as OPEN and CLOSED: CLOSED have priority", state);
            }
            return ProblemStatus.CLOSED;
        } else if (openStates.stream().anyMatch(s -> s.equalsIgnoreCase(lowerState))) {
            return ProblemStatus.OPEN;
        }
        log.error("Work item status \"{}\" is not configured as OPEN nor as CLOSED: consider it OPEN", state);
        return ProblemStatus.OPEN;
    }

    /**
     * Do the complicated RTC authentication process.
     *
     * @param projectId the ID of the project in which to work
     * @return the session cookies
     * @throws FetchException on any network issue, wrong HTTP response status code or parsing issue
     */
    private Map<String, String> authenticate(long projectId) throws FetchException {
        final Map<String, String> cookies = new HashMap<>();

        preAuthenticate(projectId, cookies);
        doAuthenticate(projectId, cookies);
        preAuthenticate(projectId, cookies);

        return cookies;
    }

    /**
     * Do a GET request on RTC to get a session ID before authenticating AND after authentication,
     * to validate it and get a new session id.
     *
     * @param projectId the ID of the project in which to work
     * @param cookies   the session cookies: will be modified with cookies needed to authentication
     * @throws FetchException on any network issue, wrong HTTP response status code or parsing issue
     */
    private void preAuthenticate(long projectId, Map<String, String> cookies) throws FetchException {
        HttpHeaders headers = new HttpHeaders();
        addCookies(cookies, headers);
        HttpEntity<String> request = new HttpEntity<>("", headers);

        final String url = "" +
                settingService.get(projectId, Settings.DEFECT_RTC_ROOT_URL) +
                settingService.get(projectId, Settings.DEFECT_RTC_PRE_AUTHENTICATE_PATH);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new FetchException(BAD_RETURN_STATUS + response.getStatusCode() + ") while pre/post-authenticating",
                    url);
        }

        cookies.putAll(extractCookies(response));
    }

    /**
     * Simulate the Ajax call made to send username/password to RTC in order to authenticate.
     *
     * @param projectId the ID of the project in which to work
     * @param cookies   the session cookies with an existing session-id: a new one will be set after authentication
     * @throws FetchException on any network issue, wrong HTTP response status code or parsing issue
     */
    private void doAuthenticate(long projectId, Map<String, String> cookies) throws FetchException {
        final String referer = "" +
                settingService.get(projectId, Settings.DEFECT_RTC_ROOT_URL) +
                settingService.get(projectId, Settings.DEFECT_RTC_PRE_AUTHENTICATE_PATH);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(HttpHeaders.REFERER, referer);
        addCookies(cookies, headers);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("j_username", settingService.get(projectId, Settings.DEFECT_RTC_USERNAME));
        form.add("j_password", settingService.get(projectId, Settings.DEFECT_RTC_PASSWORD));

        final String url = "" +
                settingService.get(projectId, Settings.DEFECT_RTC_ROOT_URL) +
                settingService.get(projectId, Settings.DEFECT_RTC_AUTHENTICATE_PATH);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if (response.getStatusCode() != HttpStatus.FOUND) {
            throw new FetchException(BAD_RETURN_STATUS + response.getStatusCode() + ") while authenticating", url);
        }

        cookies.putAll(extractCookies(response));
    }

    /**
     * Append cookies to append to the request's headers.
     *
     * @param cookies the cookies to append to the request's headers
     * @param headers the request's headers
     */
    void addCookies(Map<String, String> cookies, HttpHeaders headers) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            if (builder.length() > 0) {
                builder.append("; ");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        headers.add(HttpHeaders.COOKIE, builder.toString());
    }

    /**
     * Extract all cookies set by the HTTP server in its response.
     *
     * @param response the HTTP response where to extract set cookies
     * @return a map of cookie-name / cookie-value
     */
    Map<String, String> extractCookies(ResponseEntity<?> response) {
        Map<String, String> cookies = new HashMap<>();
        final List<String> cookieSetters = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookieSetters != null) {
            for (String headerValue : cookieSetters) {
                final Matcher matcher = COOKIE_PATTERN.matcher(headerValue);
                if (matcher.find()) {
                    cookies.put(matcher.group(1), matcher.group(2));
                }
            }
        }
        return cookies;
    }

    private static class AllTrustingClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

        @Override
        protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
            if (connection instanceof HttpsURLConnection) {
                try {
                    // Install the all-trusting trust manager
                    final SSLContext sslContext = SSLContext.getInstance("SSL");
                    final TrustManager[] trustManagers = { new UnquestioningTrustManager() };
                    sslContext.init(null, trustManagers, null);
                    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                    ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    throw new NotGonnaHappenException("Disabling SSL with the most basic configuration", e);
                }
            }
            super.prepareConnection(connection, httpMethod);
        }

    }

    private static class UnquestioningTrustManager implements X509TrustManager {

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            // No questions asked
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            // No questions asked
        }

    }

}
