/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.CountryDeployment;
import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.ExecutionCompletionRequest;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.Setting;
import com.decathlon.ara.domain.TechnologySetting;
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.domain.enumeration.Result;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.repository.CountryDeploymentRepository;
import com.decathlon.ara.repository.ErrorRepository;
import com.decathlon.ara.repository.ExecutedScenarioRepository;
import com.decathlon.ara.repository.ExecutionCompletionRequestRepository;
import com.decathlon.ara.repository.ExecutionRepository;
import com.decathlon.ara.repository.RunRepository;
import com.decathlon.ara.repository.SettingRepository;
import com.decathlon.ara.repository.TechnologySettingRepository;
import com.decathlon.ara.scenario.cucumber.settings.CucumberSettings;
import com.decathlon.ara.scenario.postman.settings.PostmanSettings;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.support.Settings;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Disabled
@SpringBootTest
@TestExecutionListeners({
    TransactionalTestExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@TestPropertySource(
		locations = "classpath:application-db-h2.properties")
@Transactional
public class ExecutionResourceIntegration {

    @Autowired
    private CountryDeploymentRepository countryDeploymentRepository;

    @Autowired
    private ExecutedScenarioRepository executedScenarioRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private RunRepository runRepository;

    @Autowired
    private ErrorRepository errorRepository;

    @Autowired
    private ExecutionResource executionResource;

    @Autowired
    private ExecutionCompletionRequestRepository executionCompletionRequestRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private SettingService settingService;

    @Autowired
    private TechnologySettingRepository technologySettingRepository;

    private static final String ARA_DATA_BASE_FOLDER_PATH = "/opt/ara/data";

    private MultipartFile readZip(String zipPath) throws IOException {
        File zip = new File(zipPath);
        FileInputStream fileInputStream = new FileInputStream(zip);
        MultipartFile multipartFile = new MockMultipartFile("zip", zip.getName(), "application/zip", IOUtils.toByteArray(fileInputStream));
        return multipartFile;
    }

    private void deleteARADataFolder() {
        File ARADataFolder = new File(ARA_DATA_BASE_FOLDER_PATH);
        FileSystemUtils.deleteRecursively(ARADataFolder);
    }

    private List<String> getARADataFilesAndFoldersPaths() throws IOException {
        List<String> araDataContent = Files.walk(Paths.get(ARA_DATA_BASE_FOLDER_PATH))
                .map(Path::toString)
                .collect(Collectors.toList());

        return araDataContent;
    }

    @Test
    public void upload_saveTheExecution_whenNoErrorFound() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581908400000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        executions = executionRepository.findAll()
                .stream()
                .filter(execution -> "34910c9971abebce9f633920d8f8cf90853f38ea".equals(execution.getVersion()))
                .collect(Collectors.toList());
        assertThat(executions)
                .hasSize(1)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .containsOnly(
                        tuple(
                                "develop",
                                "day",
                                "v3",
                                "34910c9971abebce9f633920d8f8cf90853f38ea",
                                new Date(1581908100000L),
                                new Date(1581908400000L),
                                "https://build.company.com/demo/develop/night/54/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                ExecutionAcceptance.NEW,
                                null,
                                1L,
                                "develop",
                                "day",
                                1,
                                true,
                                "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                                QualityStatus.PASSED,
                                "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":0,\"passed\":24},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":64,\"failed\":0,\"passed\":64},\"percent\":100,\"status\":\"PASSED\"}]",
                                0L,
                                0L
                        )
                );

        runs = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getRuns()
        );

        assertThat(runs)
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/56/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/57/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/58/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        List<ExecutedScenario> frApiExecutedScenarios = new ArrayList<>(
                runs.stream()
                        .filter(run -> "fr".equals(run.getCountry().getCode()))
                        .filter(run -> "api".equals(run.getType().getCode()))
                        .findFirst()
                        .get()
                        .getExecutedScenarios()
        );

        assertThat(frApiExecutedScenarios)
                .hasSize(4)
                .extracting(
                        "featureFile",
                        "featureName",
                        "featureTags",
                        "tags",
                        "severity",
                        "name",
                        "cucumberId",
                        "line",
                        "content",
                        "startDateTime",
                        "screenshotUrl",
                        "videoUrl",
                        "logsUrl",
                        "httpRequestsUrl",
                        "javaScriptErrorsUrl",
                        "diffReportUrl",
                        "cucumberReportUrl",
                        "apiServer",
                        "seleniumNode"
                )
                .containsOnly(
                        tuple(
                                "choose-a-product.postman_collection.json",
                                "Our Lovely Store - Choose a product",
                                null,
                                "@severity-sanity-check",
                                "sanity-check",
                                "all ▶ Functionality 2099: List all our useless products",
                                "all/List all our useless products",
                                12,
                                String.format("-100000:passed:<Pre-Request Script>%n-1:passed:887000000:GET {{baseUrl}}/get%n0:passed:Status code is 200%n1:passed:The server should return 3 useless products%n100000:passed:<Test Script>"),
                                new Date(1548064824000L),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/56/Postman_Collection_Results/",
                                null,
                                null
                        ),
                        tuple(
                                "pay.postman_collection.json",
                                "Our Lovely Store - Pay",
                                null,
                                "@severity-sanity-check",
                                "sanity-check",
                                "fr+us ▶ Functionalities 2111 & 2112: Pay by Card",
                                "fr+us/Pay by Card",
                                2,
                                String.format("-100000:passed:<Pre-Request Script>%n-1:passed:788000000:POST {{baseUrl}}/post%n0:passed:Status code is 200%n1:passed:Response should validate the payment method is indeed By card%n2:passed:Response should indicate a succeed transaction status%n100000:passed:<Test Script>"),
                                new Date(1548064829000L),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/56/Postman_Collection_Results/",
                                null,
                                null
                        ),
                        tuple("pay.postman_collection.json",
                                "Our Lovely Store - Pay",
                                null,
                                "@severity-medium",
                                "medium",
                                "fr+us ▶ Functionalities 2111 & 2113: Pay by Gift Card",
                                "fr+us/Pay by Gift Card",
                                3,
                                String.format("-100000:passed:<Pre-Request Script>%n-1:passed:161000000:POST {{baseUrl}}/post%n0:passed:Status code is 200%n1:passed:Response should validate the payment method is indeed By gift card%n2:passed:Response should indicate a succeed transaction status%n100000:passed:<Test Script>"),
                                new Date(1548064829000L),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/56/Postman_Collection_Results/",
                                null,
                                null
                        ),
                        tuple(
                                "pay.postman_collection.json",
                                "Our Lovely Store - Pay",
                                null,
                                "@severity-medium",
                                "medium",
                                "us ▶ Functionalities 2111 & 2114: Pay by Mobile NFC",
                                "us/Pay by Mobile NFC",
                                10,
                                String.format("-100000:passed:<Pre-Request Script>%n-1:passed:706000000:POST {{baseUrl}}/post%n0:passed:Status code is 200%n1:passed:Response should validate the payment method is indeed By NFC%n2:passed:Response should indicate a succeed transaction status%n100000:passed:<Test Script>"),
                                new Date(1548064834000L),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/56/Postman_Collection_Results/",
                                null,
                                null
                        )
                );

        List<ExecutedScenario> frDesktopExecutedScenarios = new ArrayList<>(
                runs.stream()
                        .filter(run -> "fr".equals(run.getCountry().getCode()))
                        .filter(run -> "firefox-desktop".equals(run.getType().getCode()))
                        .findFirst()
                        .get()
                        .getExecutedScenarios()
        );

        assertThat(frDesktopExecutedScenarios)
                .hasSize(14)
                .extracting(
                        "featureFile",
                        "featureName",
                        "featureTags",
                        "tags",
                        "severity",
                        "name",
                        "cucumberId",
                        "line",
                        "content",
                        "startDateTime",
                        "screenshotUrl",
                        "videoUrl",
                        "logsUrl",
                        "httpRequestsUrl",
                        "javaScriptErrorsUrl",
                        "diffReportUrl",
                        "cucumberReportUrl",
                        "apiServer",
                        "seleniumNode"
                )
                .containsOnly(
                        tuple(
                                "ara/demo/features/account.feature",
                                "Account",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Create account",
                                "account;create-account",
                                6,
                                String.format("-100000:passed:8905814:@Before HooksGlue.before()%n7:passed:22624977:Given the user is on the account creation page%n8:passed:16984994:When the user enters a new login%n9:passed:6361311:And the user enters a new password%n10:passed:10315803:And the user validates the account creation%n11:passed:13832555:Then the user is connected%n100000:passed:119346:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-account-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/account.feature",
                                "Account",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Functionalities 2118 & 3116: Log in",
                                "account;log-in",
                                14,
                                String.format("-100000:passed:14347251:@Before HooksGlue.before()%n15:passed:11170211:Given the user is on the log-in page%n16:passed:14916288:When the user enters a login%n17:passed:8926662:And the user enters a password%n18:passed:6837016:And the user validates the connection%n19:passed:12279134:Then the user is connected%n100000:passed:128542:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-account-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-fr @country-xx @severity-sanity-check",
                                "sanity-check",
                                "Functionalities 2106 & 2107: Choose delivery option",
                                "buy-a-product;choose-delivery-option",
                                35,
                                String.format("-100000:passed:8839717:@Before HooksGlue.before()%n36:passed:10086692:Given the user has 1 product in cart%n37:passed:11483139:And the user goes to the cart page%n38:passed:8199819:When the user validates the order%n39:passed:17061935:And the user chooses the delivery option \"By pigeon\"%n40:passed:13938770:Then the user is redirected to the payment page%n41:passed:14299093:And the delivery option is \"By pigeon\"%n42:passed:7238928:And the delivery price is \"1 cent\"%n100000:passed:115976:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-sanity-check",
                                "sanity-check",
                                "Functionality 2103: Add a product to cart",
                                "buy-a-product;add-a-product-to-cart",
                                5,
                                String.format("-100000:passed:15516400:@Before HooksGlue.before()%n6:passed:6654714:Given the user is on the useless \"Unicorn baskets\" product details page%n7:passed:15126069:When the user clicks on the Add To Cart button%n8:passed:8775676:Then the cart now has 1 product%n100000:passed:132676:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-high",
                                "high",
                                "Functionality 2104: Show cart, average case",
                                "buy-a-product;show-cart,-average-case",
                                19,
                                String.format("-100000:passed:10162064:@Before HooksGlue.before()%n20:passed:10053252:Given the user has 5 products in cart%n21:passed:10168577:When the user goes to the cart page%n22:passed:15934223:Then the cart page shows 5 products%n100000:passed:70971:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-medium",
                                "medium",
                                "Functionality 2104: Show cart, lots of products",
                                "buy-a-product;show-cart,-lots-of-products",
                                26,
                                String.format("-100000:passed:15941947:@Before HooksGlue.before()%n27:passed:12864598:Given the user has 1000 products in cart%n28:passed:6311916:When the user goes to the cart page%n29:passed:9399468:Then the cart page shows 1000 products%n100000:passed:60962:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-sanity-check",
                                "sanity-check",
                                "Functionality 2104: Show cart, nominal case",
                                "buy-a-product;show-cart,-nominal-case",
                                12,
                                String.format("-100000:passed:8449727:@Before HooksGlue.before()%n13:passed:15232406:Given the user has 1 product in cart%n14:passed:17188711:When the user goes to the cart page%n15:passed:6624877:Then the cart page shows 1 product%n100000:passed:63859:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-medium",
                                "medium",
                                "Functionality 2098: Have a friendly home page",
                                "choose-a-product;have-a-friendly-home-page",
                                5,
                                String.format("-100000:passed:10466450:@Before HooksGlue.before()%n6:passed:15443603:Given the user is on the friendly home page%n7:passed:17808174:When the user pauses the annoying carousel%n8:passed:11608760:Then the annoying carousel finally stops making user's head spin%n100000:passed:113213:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Functionality 2099: List all our useless products",
                                "choose-a-product;list-all-our-useless-products",
                                11,
                                String.format("-100000:passed:8520231:@Before HooksGlue.before()%n12:passed:5957980:Given the user is on the useless-products listing page%n13:passed:7279394:When the user counts the visible products%n14:passed:15958267:Then there are 3 useless products%n100000:passed:65963:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-medium",
                                "medium",
                                "Functionality 2100: Show a product with irresistible details",
                                "choose-a-product;show-a-product-with-irresistible-details",
                                17,
                                String.format("-100000:passed:6461683:@Before HooksGlue.before()%n18:passed:11863227:Given the user is on the useless \"Unicorn baskets\" product details page%n19:passed:7554539:When the user clicks on Reviews%n20:passed:9146089:Then the review 1 is \"Don't buy them: there is only one basket in the box!\"%n100000:passed:112536:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "",
                                "",
                                "Functionality 2101: Sales Price on product details page",
                                "choose-a-product;sales-price-on-product-details-page",
                                23,
                                String.format("-100000:passed:7419591:@Before HooksGlue.before()%n24:passed:5439114:Given the \"Tuning stand-up paddle\" product is on sale with a \"50%%\" reduction%n25:passed:12366896:When the user goes to the product details page%n26:passed:7649661:Then the displayed price reduction is \"50%%\"%n100000:passed:70552:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/pay.feature", "Pay", "", "@country-fr @country-us @severity-sanity-check", "sanity-check", "Functionalities 2111 & 2112: Pay by Card", "pay;pay-by-card", 11,
                                String.format("-100000:passed:7410678:@Before HooksGlue.before()%n0:element:Background:%n4:passed:13264211:Given the user has products in cart%n5:passed:11298887:And the user chosen a delivery option%n6:passed:6387066:When the user goes to the payment page%n0:element:Scenario:%n12:passed:12043564:When the user choose the payment \"Card\"%n13:passed:14463862:And the user validates the payment%n14:passed:16809330:Then the order is accepted%n100000:passed:73231:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/pay.feature", "Pay", "", "@country-all @severity-medium", "medium", "Functionalities 2111 & 2113: Pay by Gift Card", "pay;pay-by-gift-card", 18,
                                String.format("-100000:passed:6692892:@Before HooksGlue.before()%n0:element:Background:%n4:passed:15808643:Given the user has products in cart%n5:passed:7168357:And the user chosen a delivery option%n6:passed:12572845:When the user goes to the payment page%n0:element:Scenario:%n19:passed:5140487:When the user choose the payment \"Gift Card\"%n20:passed:7758911:And the user validates the payment%n21:passed:14193785:Then the order is accepted%n100000:passed:161260:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/pay.feature",
                                "Pay",
                                "",
                                "@country-us @severity-medium",
                                "medium",
                                "Functionalities 2111 & 2114: Pay by Mobile NFC",
                                "pay;pay-by-mobile-nfc",
                                25,
                                String.format("-100000:passed:17549102:@Before HooksGlue.before()%n0:element:Background:%n4:passed:16630536:Given the user has products in cart%n5:passed:14546577:And the user chosen a delivery option%n6:passed:10492971:When the user goes to the payment page%n0:element:Scenario:%n26:passed:7613793:When the user choose the payment \"NFC\"%n27:passed:15458943:And the user validates the payment%n28:passed:11207967:Then the order is accepted%n100000:passed:89254:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/57/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                null,
                                null
                        )
                );

        List<ExecutedScenario> frMobileExecutedScenarios = new ArrayList<>(
                runs.stream()
                        .filter(run -> "fr".equals(run.getCountry().getCode()))
                        .filter(run -> "firefox-mobile".equals(run.getType().getCode()))
                        .findFirst()
                        .get()
                        .getExecutedScenarios()
        );

        assertThat(frMobileExecutedScenarios)
                .hasSize(14)
                .extracting(
                        "featureFile",
                        "featureName",
                        "featureTags",
                        "tags",
                        "severity",
                        "name",
                        "cucumberId",
                        "line",
                        "content",
                        "startDateTime",
                        "screenshotUrl",
                        "videoUrl",
                        "logsUrl",
                        "httpRequestsUrl",
                        "javaScriptErrorsUrl",
                        "diffReportUrl",
                        "cucumberReportUrl",
                        "apiServer",
                        "seleniumNode"
                )
                .containsOnly(
                        tuple(
                                "ara/demo/features/account.feature",
                                "Account",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Create account",
                                "account;create-account",
                                6,
                                String.format("-100000:passed:8905814:@Before HooksGlue.before()%n7:passed:22624977:Given the user is on the account creation page%n8:passed:16984994:When the user enters a new login%n9:passed:6361311:And the user enters a new password%n10:passed:10315803:And the user validates the account creation%n11:passed:13832555:Then the user is connected%n100000:passed:119346:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-account-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/account.feature",
                                "Account",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Functionalities 2118 & 3116: Log in",
                                "account;log-in",
                                14,
                                String.format("-100000:passed:14347251:@Before HooksGlue.before()%n15:passed:11170211:Given the user is on the log-in page%n16:passed:14916288:When the user enters a login%n17:passed:8926662:And the user enters a password%n18:passed:6837016:And the user validates the connection%n19:passed:12279134:Then the user is connected%n100000:passed:128542:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-account-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-fr @country-xx @severity-sanity-check",
                                "sanity-check",
                                "Functionalities 2106 & 2107: Choose delivery option",
                                "buy-a-product;choose-delivery-option",
                                35,
                                String.format("-100000:passed:8839717:@Before HooksGlue.before()%n36:passed:10086692:Given the user has 1 product in cart%n37:passed:11483139:And the user goes to the cart page%n38:passed:8199819:When the user validates the order%n39:passed:17061935:And the user chooses the delivery option \"By pigeon\"%n40:passed:13938770:Then the user is redirected to the payment page%n41:passed:14299093:And the delivery option is \"By pigeon\"%n42:passed:7238928:And the delivery price is \"1 cent\"%n100000:passed:115976:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-sanity-check",
                                "sanity-check",
                                "Functionality 2103: Add a product to cart",
                                "buy-a-product;add-a-product-to-cart",
                                5,
                                String.format("-100000:passed:15516400:@Before HooksGlue.before()%n6:passed:6654714:Given the user is on the useless \"Unicorn baskets\" product details page%n7:passed:15126069:When the user clicks on the Add To Cart button%n8:passed:8775676:Then the cart now has 1 product%n100000:passed:132676:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-high",
                                "high",
                                "Functionality 2104: Show cart, average case",
                                "buy-a-product;show-cart,-average-case",
                                19,
                                String.format("-100000:passed:10162064:@Before HooksGlue.before()%n20:passed:10053252:Given the user has 5 products in cart%n21:passed:10168577:When the user goes to the cart page%n22:passed:15934223:Then the cart page shows 5 products%n100000:passed:70971:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-medium",
                                "medium",
                                "Functionality 2104: Show cart, lots of products",
                                "buy-a-product;show-cart,-lots-of-products",
                                26,
                                String.format("-100000:passed:15941947:@Before HooksGlue.before()%n27:passed:12864598:Given the user has 1000 products in cart%n28:passed:6311916:When the user goes to the cart page%n29:passed:9399468:Then the cart page shows 1000 products%n100000:passed:60962:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature", "Buy a product",
                                "",
                                "@country-all @severity-sanity-check",
                                "sanity-check",
                                "Functionality 2104: Show cart, nominal case",
                                "buy-a-product;show-cart,-nominal-case",
                                12,
                                String.format("-100000:passed:8449727:@Before HooksGlue.before()%n13:passed:15232406:Given the user has 1 product in cart%n14:passed:17188711:When the user goes to the cart page%n15:passed:6624877:Then the cart page shows 1 product%n100000:passed:63859:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-medium",
                                "medium",
                                "Functionality 2098: Have a friendly home page",
                                "choose-a-product;have-a-friendly-home-page",
                                5,
                                String.format("-100000:passed:10466450:@Before HooksGlue.before()%n6:passed:15443603:Given the user is on the friendly home page%n7:passed:17808174:When the user pauses the annoying carousel%n8:passed:11608760:Then the annoying carousel finally stops making user's head spin%n100000:passed:113213:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Functionality 2099: List all our useless products",
                                "choose-a-product;list-all-our-useless-products",
                                11,
                                String.format("-100000:passed:8520231:@Before HooksGlue.before()%n12:passed:5957980:Given the user is on the useless-products listing page%n13:passed:7279394:When the user counts the visible products%n14:passed:15958267:Then there are 3 useless products%n100000:passed:65963:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-medium",
                                "medium",
                                "Functionality 2100: Show a product with irresistible details",
                                "choose-a-product;show-a-product-with-irresistible-details", 17,
                                String.format("-100000:passed:6461683:@Before HooksGlue.before()%n18:passed:11863227:Given the user is on the useless \"Unicorn baskets\" product details page%n19:passed:7554539:When the user clicks on Reviews%n20:passed:9146089:Then the review 1 is \"Don't buy them: there is only one basket in the box!\"%n100000:passed:112536:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html", null, null),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product", "@country-all",
                                "",
                                "",
                                "Functionality 2101: Sales Price on product details page",
                                "choose-a-product;sales-price-on-product-details-page",
                                23,
                                String.format("-100000:passed:7419591:@Before HooksGlue.before()%n24:passed:5439114:Given the \"Tuning stand-up paddle\" product is on sale with a \"50%%\" reduction%n25:passed:12366896:When the user goes to the product details page%n26:passed:7649661:Then the displayed price reduction is \"50%%\"%n100000:passed:70552:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html", null, null),
                        tuple(
                                "ara/demo/features/pay.feature",
                                "Pay",
                                "",
                                "@country-fr @country-us @severity-sanity-check",
                                "sanity-check",
                                "Functionalities 2111 & 2112: Pay by Card",
                                "pay;pay-by-card",
                                11,
                                String.format("-100000:passed:7410678:@Before HooksGlue.before()%n0:element:Background:%n4:passed:13264211:Given the user has products in cart%n5:passed:11298887:And the user chosen a delivery option%n6:passed:6387066:When the user goes to the payment page%n0:element:Scenario:%n12:passed:12043564:When the user choose the payment \"Card\"%n13:passed:14463862:And the user validates the payment%n14:passed:16809330:Then the order is accepted%n100000:passed:73231:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/pay.feature",
                                "Pay",
                                "",
                                "@country-all @severity-medium",
                                "medium", "Functionalities 2111 & 2113: Pay by Gift Card",
                                "pay;pay-by-gift-card",
                                18,
                                String.format("-100000:passed:6692892:@Before HooksGlue.before()%n0:element:Background:%n4:passed:15808643:Given the user has products in cart%n5:passed:7168357:And the user chosen a delivery option%n6:passed:12572845:When the user goes to the payment page%n0:element:Scenario:%n19:passed:5140487:When the user choose the payment \"Gift Card\"%n20:passed:7758911:And the user validates the payment%n21:passed:14193785:Then the order is accepted%n100000:passed:161260:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/pay.feature",
                                "Pay",
                                "",
                                "@country-us @severity-medium",
                                "medium",
                                "Functionalities 2111 & 2114: Pay by Mobile NFC",
                                "pay;pay-by-mobile-nfc",
                                25,
                                String.format("-100000:passed:17549102:@Before HooksGlue.before()%n0:element:Background:%n4:passed:16630536:Given the user has products in cart%n5:passed:14546577:And the user chosen a delivery option%n6:passed:10492971:When the user goes to the payment page%n0:element:Scenario:%n26:passed:7613793:When the user choose the payment \"NFC\"%n27:passed:15458943:And the user validates the payment%n28:passed:11207967:Then the order is accepted%n100000:passed:89254:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/58/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                null,
                                null
                        )
                );

        List<ExecutedScenario> usApiExecutedScenarios = new ArrayList<>(
                runs.stream()
                        .filter(run -> "us".equals(run.getCountry().getCode()))
                        .filter(run -> "api".equals(run.getType().getCode()))
                        .findFirst()
                        .get()
                        .getExecutedScenarios()
        );

        assertThat(usApiExecutedScenarios)
                .hasSize(4)
                .extracting(
                        "featureFile",
                        "featureName",
                        "featureTags",
                        "tags",
                        "severity",
                        "name",
                        "cucumberId",
                        "line",
                        "content",
                        "startDateTime",
                        "screenshotUrl",
                        "videoUrl",
                        "logsUrl",
                        "httpRequestsUrl",
                        "javaScriptErrorsUrl",
                        "diffReportUrl",
                        "cucumberReportUrl",
                        "apiServer",
                        "seleniumNode"
                )
                .containsOnly(
                        tuple(
                                "choose-a-product.postman_collection.json",
                                "Our Lovely Store - Choose a product",
                                null,
                                "@severity-sanity-check",
                                "sanity-check",
                                "all ▶ Functionality 2099: List all our useless products",
                                "all/List all our useless products",
                                12,
                                String.format("-100000:passed:<Pre-Request Script>%n-1:passed:887000000:GET {{baseUrl}}/get%n0:passed:Status code is 200%n1:passed:The server should return 3 useless products%n100000:passed:<Test Script>"),
                                new Date(1548064824000L),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/60/Postman_Collection_Results/",
                                null,
                                null
                        ),
                        tuple(
                                "pay.postman_collection.json",
                                "Our Lovely Store - Pay",
                                null,
                                "@severity-sanity-check",
                                "sanity-check",
                                "fr+us ▶ Functionalities 2111 & 2112: Pay by Card",
                                "fr+us/Pay by Card",
                                2,
                                String.format("-100000:passed:<Pre-Request Script>%n-1:passed:788000000:POST {{baseUrl}}/post%n0:passed:Status code is 200%n1:passed:Response should validate the payment method is indeed By card%n2:passed:Response should indicate a succeed transaction status%n100000:passed:<Test Script>"),
                                new Date(1548064829000L),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/60/Postman_Collection_Results/",
                                null,
                                null
                        ),
                        tuple("pay.postman_collection.json",
                                "Our Lovely Store - Pay",
                                null,
                                "@severity-medium",
                                "medium",
                                "fr+us ▶ Functionalities 2111 & 2113: Pay by Gift Card",
                                "fr+us/Pay by Gift Card",
                                3,
                                String.format("-100000:passed:<Pre-Request Script>%n-1:passed:161000000:POST {{baseUrl}}/post%n0:passed:Status code is 200%n1:passed:Response should validate the payment method is indeed By gift card%n2:passed:Response should indicate a succeed transaction status%n100000:passed:<Test Script>"),
                                new Date(1548064829000L),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/60/Postman_Collection_Results/",
                                null,
                                null
                        ),
                        tuple(
                                "pay.postman_collection.json", "Our Lovely Store - Pay",
                                null,
                                "@severity-medium", "medium", "us ▶ Functionalities 2111 & 2114: Pay by Mobile NFC",
                                "us/Pay by Mobile NFC",
                                10,
                                String.format("-100000:passed:<Pre-Request Script>%n-1:passed:706000000:POST {{baseUrl}}/post%n0:passed:Status code is 200%n1:passed:Response should validate the payment method is indeed By NFC%n2:passed:Response should indicate a succeed transaction status%n100000:passed:<Test Script>"),
                                new Date(1548064834000L),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/60/Postman_Collection_Results/",
                                null,
                                null
                        )
                );

        List<ExecutedScenario> usDesktopExecutedScenarios = new ArrayList<>(
                runs.stream()
                        .filter(run -> "us".equals(run.getCountry().getCode()))
                        .filter(run -> "firefox-desktop".equals(run.getType().getCode()))
                        .findFirst()
                        .get()
                        .getExecutedScenarios()
        );

        assertThat(usDesktopExecutedScenarios)
                .hasSize(14)
                .extracting(
                        "featureFile",
                        "featureName",
                        "featureTags",
                        "tags",
                        "severity",
                        "name",
                        "cucumberId",
                        "line",
                        "content",
                        "startDateTime",
                        "screenshotUrl",
                        "videoUrl",
                        "logsUrl",
                        "httpRequestsUrl",
                        "javaScriptErrorsUrl",
                        "diffReportUrl",
                        "cucumberReportUrl",
                        "apiServer",
                        "seleniumNode"
                )
                .containsOnly(
                        tuple(
                                "ara/demo/features/account.feature",
                                "Account",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Create account",
                                "account;create-account",
                                6,
                                String.format("-100000:passed:8905814:@Before HooksGlue.before()%n7:passed:22624977:Given the user is on the account creation page%n8:passed:16984994:When the user enters a new login%n9:passed:6361311:And the user enters a new password%n10:passed:10315803:And the user validates the account creation%n11:passed:13832555:Then the user is connected%n100000:passed:119346:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-account-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/account.feature",
                                "Account",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Functionalities 2118 & 3116: Log in",
                                "account;log-in",
                                14,
                                String.format("-100000:passed:14347251:@Before HooksGlue.before()%n15:passed:11170211:Given the user is on the log-in page%n16:passed:14916288:When the user enters a login%n17:passed:8926662:And the user enters a password%n18:passed:6837016:And the user validates the connection%n19:passed:12279134:Then the user is connected%n100000:passed:128542:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-account-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-fr @country-xx @severity-sanity-check",
                                "sanity-check",
                                "Functionalities 2106 & 2107: Choose delivery option",
                                "buy-a-product;choose-delivery-option",
                                35,
                                String.format("-100000:passed:8839717:@Before HooksGlue.before()%n36:passed:10086692:Given the user has 1 product in cart%n37:passed:11483139:And the user goes to the cart page%n38:passed:8199819:When the user validates the order%n39:passed:17061935:And the user chooses the delivery option \"By pigeon\"%n40:passed:13938770:Then the user is redirected to the payment page%n41:passed:14299093:And the delivery option is \"By pigeon\"%n42:passed:7238928:And the delivery price is \"1 cent\"%n100000:passed:115976:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-sanity-check",
                                "sanity-check",
                                "Functionality 2103: Add a product to cart",
                                "buy-a-product;add-a-product-to-cart",
                                5,
                                String.format("-100000:passed:15516400:@Before HooksGlue.before()%n6:passed:6654714:Given the user is on the useless \"Unicorn baskets\" product details page%n7:passed:15126069:When the user clicks on the Add To Cart button%n8:passed:8775676:Then the cart now has 1 product%n100000:passed:132676:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-high",
                                "high",
                                "Functionality 2104: Show cart, average case",
                                "buy-a-product;show-cart,-average-case",
                                19,
                                String.format("-100000:passed:10162064:@Before HooksGlue.before()%n20:passed:10053252:Given the user has 5 products in cart%n21:passed:10168577:When the user goes to the cart page%n22:passed:15934223:Then the cart page shows 5 products%n100000:passed:70971:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-medium",
                                "medium",
                                "Functionality 2104: Show cart, lots of products",
                                "buy-a-product;show-cart,-lots-of-products",
                                26,
                                String.format("-100000:passed:15941947:@Before HooksGlue.before()%n27:passed:12864598:Given the user has 1000 products in cart%n28:passed:6311916:When the user goes to the cart page%n29:passed:9399468:Then the cart page shows 1000 products%n100000:passed:60962:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-sanity-check",
                                "sanity-check",
                                "Functionality 2104: Show cart, nominal case",
                                "buy-a-product;show-cart,-nominal-case",
                                12,
                                String.format("-100000:passed:8449727:@Before HooksGlue.before()%n13:passed:15232406:Given the user has 1 product in cart%n14:passed:17188711:When the user goes to the cart page%n15:passed:6624877:Then the cart page shows 1 product%n100000:passed:63859:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-medium",
                                "medium",
                                "Functionality 2098: Have a friendly home page",
                                "choose-a-product;have-a-friendly-home-page",
                                5,
                                String.format("-100000:passed:10466450:@Before HooksGlue.before()%n6:passed:15443603:Given the user is on the friendly home page%n7:passed:17808174:When the user pauses the annoying carousel%n8:passed:11608760:Then the annoying carousel finally stops making user's head spin%n100000:passed:113213:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Functionality 2099: List all our useless products",
                                "choose-a-product;list-all-our-useless-products",
                                11,
                                String.format("-100000:passed:8520231:@Before HooksGlue.before()%n12:passed:5957980:Given the user is on the useless-products listing page%n13:passed:7279394:When the user counts the visible products%n14:passed:15958267:Then there are 3 useless products%n100000:passed:65963:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-medium",
                                "medium",
                                "Functionality 2100: Show a product with irresistible details",
                                "choose-a-product;show-a-product-with-irresistible-details",
                                17,
                                String.format("-100000:passed:6461683:@Before HooksGlue.before()%n18:passed:11863227:Given the user is on the useless \"Unicorn baskets\" product details page%n19:passed:7554539:When the user clicks on Reviews%n20:passed:9146089:Then the review 1 is \"Don't buy them: there is only one basket in the box!\"%n100000:passed:112536:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "",
                                "",
                                "Functionality 2101: Sales Price on product details page",
                                "choose-a-product;sales-price-on-product-details-page",
                                23,
                                String.format("-100000:passed:7419591:@Before HooksGlue.before()%n24:passed:5439114:Given the \"Tuning stand-up paddle\" product is on sale with a \"50%%\" reduction%n25:passed:12366896:When the user goes to the product details page%n26:passed:7649661:Then the displayed price reduction is \"50%%\"%n100000:passed:70552:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/pay.feature", "Pay", "", "@country-fr @country-us @severity-sanity-check", "sanity-check", "Functionalities 2111 & 2112: Pay by Card", "pay;pay-by-card", 11,
                                String.format("-100000:passed:7410678:@Before HooksGlue.before()%n0:element:Background:%n4:passed:13264211:Given the user has products in cart%n5:passed:11298887:And the user chosen a delivery option%n6:passed:6387066:When the user goes to the payment page%n0:element:Scenario:%n12:passed:12043564:When the user choose the payment \"Card\"%n13:passed:14463862:And the user validates the payment%n14:passed:16809330:Then the order is accepted%n100000:passed:73231:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/pay.feature", "Pay", "", "@country-all @severity-medium", "medium", "Functionalities 2111 & 2113: Pay by Gift Card", "pay;pay-by-gift-card", 18,
                                String.format("-100000:passed:6692892:@Before HooksGlue.before()%n0:element:Background:%n4:passed:15808643:Given the user has products in cart%n5:passed:7168357:And the user chosen a delivery option%n6:passed:12572845:When the user goes to the payment page%n0:element:Scenario:%n19:passed:5140487:When the user choose the payment \"Gift Card\"%n20:passed:7758911:And the user validates the payment%n21:passed:14193785:Then the order is accepted%n100000:passed:161260:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/pay.feature",
                                "Pay",
                                "",
                                "@country-us @severity-medium",
                                "medium",
                                "Functionalities 2111 & 2114: Pay by Mobile NFC",
                                "pay;pay-by-mobile-nfc",
                                25,
                                String.format("-100000:passed:17549102:@Before HooksGlue.before()%n0:element:Background:%n4:passed:16630536:Given the user has products in cart%n5:passed:14546577:And the user chosen a delivery option%n6:passed:10492971:When the user goes to the payment page%n0:element:Scenario:%n26:passed:7613793:When the user choose the payment \"NFC\"%n27:passed:15458943:And the user validates the payment%n28:passed:11207967:Then the order is accepted%n100000:passed:89254:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/61/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                null,
                                null
                        )
                );

        List<ExecutedScenario> usMobileExecutedScenarios = new ArrayList<>(
                runs.stream()
                        .filter(run -> "us".equals(run.getCountry().getCode()))
                        .filter(run -> "firefox-mobile".equals(run.getType().getCode()))
                        .findFirst()
                        .get()
                        .getExecutedScenarios()
        );

        assertThat(usMobileExecutedScenarios)
                .hasSize(14)
                .extracting(
                        "featureFile",
                        "featureName",
                        "featureTags",
                        "tags",
                        "severity",
                        "name",
                        "cucumberId",
                        "line",
                        "content",
                        "startDateTime",
                        "screenshotUrl",
                        "videoUrl",
                        "logsUrl",
                        "httpRequestsUrl",
                        "javaScriptErrorsUrl",
                        "diffReportUrl",
                        "cucumberReportUrl",
                        "apiServer",
                        "seleniumNode"
                )
                .containsOnly(
                        tuple(
                                "ara/demo/features/account.feature",
                                "Account",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Create account",
                                "account;create-account",
                                6,
                                String.format("-100000:passed:8905814:@Before HooksGlue.before()%n7:passed:22624977:Given the user is on the account creation page%n8:passed:16984994:When the user enters a new login%n9:passed:6361311:And the user enters a new password%n10:passed:10315803:And the user validates the account creation%n11:passed:13832555:Then the user is connected%n100000:passed:119346:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-account-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/account.feature",
                                "Account",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Functionalities 2118 & 3116: Log in",
                                "account;log-in",
                                14,
                                String.format("-100000:passed:14347251:@Before HooksGlue.before()%n15:passed:11170211:Given the user is on the log-in page%n16:passed:14916288:When the user enters a login%n17:passed:8926662:And the user enters a password%n18:passed:6837016:And the user validates the connection%n19:passed:12279134:Then the user is connected%n100000:passed:128542:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-account-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-fr @country-xx @severity-sanity-check",
                                "sanity-check",
                                "Functionalities 2106 & 2107: Choose delivery option",
                                "buy-a-product;choose-delivery-option",
                                35,
                                String.format("-100000:passed:8839717:@Before HooksGlue.before()%n36:passed:10086692:Given the user has 1 product in cart%n37:passed:11483139:And the user goes to the cart page%n38:passed:8199819:When the user validates the order%n39:passed:17061935:And the user chooses the delivery option \"By pigeon\"%n40:passed:13938770:Then the user is redirected to the payment page%n41:passed:14299093:And the delivery option is \"By pigeon\"%n42:passed:7238928:And the delivery price is \"1 cent\"%n100000:passed:115976:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-sanity-check",
                                "sanity-check",
                                "Functionality 2103: Add a product to cart",
                                "buy-a-product;add-a-product-to-cart",
                                5,
                                String.format("-100000:passed:15516400:@Before HooksGlue.before()%n6:passed:6654714:Given the user is on the useless \"Unicorn baskets\" product details page%n7:passed:15126069:When the user clicks on the Add To Cart button%n8:passed:8775676:Then the cart now has 1 product%n100000:passed:132676:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-high",
                                "high",
                                "Functionality 2104: Show cart, average case",
                                "buy-a-product;show-cart,-average-case",
                                19,
                                String.format("-100000:passed:10162064:@Before HooksGlue.before()%n20:passed:10053252:Given the user has 5 products in cart%n21:passed:10168577:When the user goes to the cart page%n22:passed:15934223:Then the cart page shows 5 products%n100000:passed:70971:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-medium",
                                "medium",
                                "Functionality 2104: Show cart, lots of products",
                                "buy-a-product;show-cart,-lots-of-products",
                                26,
                                String.format("-100000:passed:15941947:@Before HooksGlue.before()%n27:passed:12864598:Given the user has 1000 products in cart%n28:passed:6311916:When the user goes to the cart page%n29:passed:9399468:Then the cart page shows 1000 products%n100000:passed:60962:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature", "Buy a product",
                                "",
                                "@country-all @severity-sanity-check",
                                "sanity-check",
                                "Functionality 2104: Show cart, nominal case",
                                "buy-a-product;show-cart,-nominal-case",
                                12,
                                String.format("-100000:passed:8449727:@Before HooksGlue.before()%n13:passed:15232406:Given the user has 1 product in cart%n14:passed:17188711:When the user goes to the cart page%n15:passed:6624877:Then the cart page shows 1 product%n100000:passed:63859:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-medium",
                                "medium",
                                "Functionality 2098: Have a friendly home page",
                                "choose-a-product;have-a-friendly-home-page",
                                5,
                                String.format("-100000:passed:10466450:@Before HooksGlue.before()%n6:passed:15443603:Given the user is on the friendly home page%n7:passed:17808174:When the user pauses the annoying carousel%n8:passed:11608760:Then the annoying carousel finally stops making user's head spin%n100000:passed:113213:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Functionality 2099: List all our useless products",
                                "choose-a-product;list-all-our-useless-products",
                                11,
                                String.format("-100000:passed:8520231:@Before HooksGlue.before()%n12:passed:5957980:Given the user is on the useless-products listing page%n13:passed:7279394:When the user counts the visible products%n14:passed:15958267:Then there are 3 useless products%n100000:passed:65963:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-medium",
                                "medium",
                                "Functionality 2100: Show a product with irresistible details",
                                "choose-a-product;show-a-product-with-irresistible-details", 17,
                                String.format("-100000:passed:6461683:@Before HooksGlue.before()%n18:passed:11863227:Given the user is on the useless \"Unicorn baskets\" product details page%n19:passed:7554539:When the user clicks on Reviews%n20:passed:9146089:Then the review 1 is \"Don't buy them: there is only one basket in the box!\"%n100000:passed:112536:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html", null, null),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product", "@country-all",
                                "",
                                "",
                                "Functionality 2101: Sales Price on product details page",
                                "choose-a-product;sales-price-on-product-details-page",
                                23,
                                String.format("-100000:passed:7419591:@Before HooksGlue.before()%n24:passed:5439114:Given the \"Tuning stand-up paddle\" product is on sale with a \"50%%\" reduction%n25:passed:12366896:When the user goes to the product details page%n26:passed:7649661:Then the displayed price reduction is \"50%%\"%n100000:passed:70552:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html", null, null),
                        tuple(
                                "ara/demo/features/pay.feature",
                                "Pay",
                                "",
                                "@country-fr @country-us @severity-sanity-check",
                                "sanity-check",
                                "Functionalities 2111 & 2112: Pay by Card",
                                "pay;pay-by-card",
                                11,
                                String.format("-100000:passed:7410678:@Before HooksGlue.before()%n0:element:Background:%n4:passed:13264211:Given the user has products in cart%n5:passed:11298887:And the user chosen a delivery option%n6:passed:6387066:When the user goes to the payment page%n0:element:Scenario:%n12:passed:12043564:When the user choose the payment \"Card\"%n13:passed:14463862:And the user validates the payment%n14:passed:16809330:Then the order is accepted%n100000:passed:73231:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/pay.feature",
                                "Pay",
                                "",
                                "@country-all @severity-medium",
                                "medium", "Functionalities 2111 & 2113: Pay by Gift Card",
                                "pay;pay-by-gift-card",
                                18,
                                String.format("-100000:passed:6692892:@Before HooksGlue.before()%n0:element:Background:%n4:passed:15808643:Given the user has products in cart%n5:passed:7168357:And the user chosen a delivery option%n6:passed:12572845:When the user goes to the payment page%n0:element:Scenario:%n19:passed:5140487:When the user choose the payment \"Gift Card\"%n20:passed:7758911:And the user validates the payment%n21:passed:14193785:Then the order is accepted%n100000:passed:161260:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/pay.feature",
                                "Pay",
                                "",
                                "@country-us @severity-medium",
                                "medium",
                                "Functionalities 2111 & 2114: Pay by Mobile NFC",
                                "pay;pay-by-mobile-nfc",
                                25,
                                String.format("-100000:passed:17549102:@Before HooksGlue.before()%n0:element:Background:%n4:passed:16630536:Given the user has products in cart%n5:passed:14546577:And the user chosen a delivery option%n6:passed:10492971:When the user goes to the payment page%n0:element:Scenario:%n26:passed:7613793:When the user choose the payment \"NFC\"%n27:passed:15458943:And the user validates the payment%n28:passed:11207967:Then the order is accepted%n100000:passed:89254:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/62/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                null,
                                null
                        )
                );

        countryDeployments = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getCountryDeployments()
        );

        assertThat(countryDeployments)
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/55/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        errors = runs.stream()
                .map(Run::getExecutedScenarios)
                .flatMap(Collection::stream)
                .map(ExecutedScenario::getErrors)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(errors).isEmpty();

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecutionWithErrors_whenErrorsFound() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1582099200000.zip");
        executionResource.upload("the-demo-project", "master", "day", zip);

        executions = executionRepository.findAll().stream()
                .filter(execution -> "e10b1c9c9a6a9f478d10dd90109c467fd0974c1c".equals(execution.getVersion()))
                .collect(Collectors.toList());
        assertThat(executions)
                .hasSize(1)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .containsOnly(
                        tuple(
                                "master",
                                "day",
                                "v3",
                                "e10b1c9c9a6a9f478d10dd90109c467fd0974c1c",
                                new Date(1582098900000L),
                                new Date(1582099200000L),
                                "https://build.company.com/demo/master/day/50/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                ExecutionAcceptance.NEW,
                                null,
                                1L,
                                "master",
                                "day",
                                2,
                                true,
                                "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                                QualityStatus.FAILED,
                                "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":9,\"failed\":5,\"passed\":4},\"percent\":44,\"status\":\"FAILED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":2,\"failed\":1,\"passed\":1},\"percent\":50,\"status\":\"FAILED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":7,\"failed\":3,\"passed\":4},\"percent\":57,\"status\":\"FAILED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":18,\"failed\":9,\"passed\":9},\"percent\":50,\"status\":\"FAILED\"}]",
                                0L,
                                0L
                        )
                );

        runs = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getRuns()
        );

        assertThat(runs)
                .hasSize(2)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/52/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1582099200000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/53/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1582099200000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        List<ExecutedScenario> frApiExecutedScenarios = new ArrayList<>(
                runs.stream()
                        .filter(run -> "fr".equals(run.getCountry().getCode()))
                        .filter(run -> "api".equals(run.getType().getCode()))
                        .findFirst()
                        .get()
                        .getExecutedScenarios()
        );

        assertThat(frApiExecutedScenarios)
                .hasSize(4)
                .extracting(
                        "featureFile",
                        "featureName",
                        "featureTags",
                        "tags",
                        "severity",
                        "name",
                        "cucumberId",
                        "line",
                        "content",
                        "startDateTime",
                        "screenshotUrl",
                        "videoUrl",
                        "logsUrl",
                        "javaScriptErrorsUrl",
                        "diffReportUrl",
                        "cucumberReportUrl",
                        "apiServer",
                        "seleniumNode"
                )
                .containsOnly(
                        tuple(
                                "choose-a-product.postman_collection.json",
                                "Our Lovely Store - Choose a product",
                                null,
                                "@severity-sanity-check",
                                "sanity-check",
                                "all ▶ Functionality 2099: List all our useless products",
                                "all/List all our useless products",
                                12,
                                String.format("-100000:passed:<Pre-Request Script>%n-1:passed:735000000:GET {{baseUrl}}/get%n0:passed:Status code is 200%n1:passed:The server should return 3 useless products%n100000:passed:<Test Script>"),
                                new Date(1548064840000L),
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/52/Postman_Collection_Results/",
                                null,
                                null
                        ),
                        tuple(
                                "pay.postman_collection.json",
                                "Our Lovely Store - Pay",
                                null,
                                "@severity-sanity-check",
                                "sanity-check",
                                "fr+us ▶ Functionalities 2111 & 2112: Pay by Card",
                                "fr+us/Pay by Card",
                                2,
                                String.format("-100000:passed:<Pre-Request Script>%n-1:passed:710000000:POST {{baseUrl}}/post%n0:passed:Status code is 200%n1:passed:Response should validate the payment method is indeed By card%n2:failed:Response should indicate a succeed transaction status%n100000:passed:<Test Script>"),
                                new Date(1548064845000L),
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/52/Postman_Collection_Results/",
                                null,
                                null
                        ),
                        tuple("pay.postman_collection.json",
                                "Our Lovely Store - Pay",
                                null,
                                "@severity-medium",
                                "medium",
                                "fr+us ▶ Functionalities 2111 & 2113: Pay by Gift Card",
                                "fr+us/Pay by Gift Card",
                                3,
                                String.format("-100000:passed:<Pre-Request Script>%n-1:passed:127000000:POST {{baseUrl}}/post%n0:passed:Status code is 200%n1:passed:Response should validate the payment method is indeed By gift card%n2:passed:Response should indicate a succeed transaction status%n100000:passed:<Test Script>"),
                                new Date(1548064845000L),
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/52/Postman_Collection_Results/",
                                null,
                                null
                        ),
                        tuple(
                                "pay.postman_collection.json", "Our Lovely Store - Pay",
                                null,
                                "@severity-medium", "medium", "us ▶ Functionalities 2111 & 2114: Pay by Mobile NFC",
                                "us/Pay by Mobile NFC",
                                10,
                                String.format("-100000:passed:<Pre-Request Script>%n-1:passed:753000000:POST {{baseUrl}}/post%n0:passed:Status code is 200%n1:passed:Response should validate the payment method is indeed By NFC%n2:passed:Response should indicate a succeed transaction status%n100000:passed:<Test Script>"),
                                new Date(1548064849000L),
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/52/Postman_Collection_Results/",
                                null,
                                null
                        )
                );

        List<ExecutedScenario> frDesktopExecutedScenarios = new ArrayList<>(
                runs.stream()
                        .filter(run -> "fr".equals(run.getCountry().getCode()))
                        .filter(run -> "firefox-desktop".equals(run.getType().getCode()))
                        .findFirst()
                        .get()
                        .getExecutedScenarios()
        );

        assertThat(frDesktopExecutedScenarios)
                .hasSize(14)
                .extracting(
                        "featureFile",
                        "featureName",
                        "featureTags",
                        "tags",
                        "severity",
                        "name",
                        "cucumberId",
                        "line",
                        "content",
                        "startDateTime",
                        "screenshotUrl",
                        "videoUrl",
                        "logsUrl",
                        "httpRequestsUrl",
                        "javaScriptErrorsUrl",
                        "diffReportUrl",
                        "cucumberReportUrl",
                        "apiServer",
                        "seleniumNode"
                )
                .containsOnly(
                        tuple(
                                "ara/demo/features/account.feature",
                                "Account",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Create account",
                                "account;create-account",
                                6,
                                String.format("-100000:passed:13860142:@Before HooksGlue.before()%n7:passed:7805779:Given the user is on the account creation page%n8:passed:13989744:When the user enters a new login%n9:passed:14731724:And the user enters a new password%n10:passed:12943720:And the user validates the account creation%n11:failed:9798139:Then the user is connected%n100000:passed:965566:@After HooksGlue.after(Scenario)"),
                                Date.from(LocalDateTime.of(2019, 4, 9, 9, 3, 27, 164000000).atZone(ZoneId.systemDefault()).toInstant()),
                                "/demo-files/screenshots/create-account.png",
                                "/demo-files/videos/create-account.mp4",
                                "/demo-files/logs/create-account.txt",
                                "/demo-files/http-requests.txt",
                                "/demo-files/javascript-errors.txt",
                                "/demo-files/diff-report.html",
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-account-feature.html",
                                "API02",
                                "firefox04.nodes.selenium.project.company.com"
                        ),
                        tuple(
                                "ara/demo/features/account.feature",
                                "Account",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Functionalities 2118 & 3116: Log in",
                                "account;log-in",
                                14,
                                String.format("-100000:passed:10876128:@Before HooksGlue.before()%n15:passed:16723364:Given the user is on the log-in page%n16:passed:12161793:When the user enters a login%n17:passed:11819724:And the user enters a password%n18:passed:9702357:And the user validates the connection%n19:failed:8045795:Then the user is connected%n100000:passed:1004361:@After HooksGlue.after(Scenario)"),
                                Date.from(LocalDateTime.of(2019, 4, 9, 9, 3, 27, 164000000).atZone(ZoneId.systemDefault()).toInstant()),
                                "/demo-files/screenshots/log-in.png",
                                "/demo-files/videos/log-in.mp4",
                                "/demo-files/logs/log-in.txt",
                                "/demo-files/http-requests.txt",
                                "/demo-files/javascript-errors.txt",
                                "/demo-files/diff-report.html",
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-account-feature.html",
                                "API02",
                                "firefox07.nodes.selenium.project.company.com"
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-fr @country-xx @severity-sanity-check",
                                "sanity-check",
                                "Functionalities 2106 & 2107: Choose delivery option",
                                "buy-a-product;choose-delivery-option",
                                35,
                                String.format("-100000:passed:13018683:@Before HooksGlue.before()%n36:passed:13224445:Given the user has 1 product in cart%n37:passed:8575288:And the user goes to the cart page%n38:passed:8279978:When the user validates the order%n39:passed:15117389:And the user chooses the delivery option \"By pigeon\"%n40:passed:17651341:Then the user is redirected to the payment page%n41:failed:8140205:And the delivery option is \"By pigeon\"%n42:failed:7476151:And the delivery price is \"1 cent\"%n100000:passed:929234:@After HooksGlue.after(Scenario)"),
                                Date.from(LocalDateTime.of(2019, 4, 9, 9, 3, 27, 164000000).atZone(ZoneId.systemDefault()).toInstant()),
                                "/demo-files/screenshots/delivery.png",
                                "/demo-files/videos/delivery.mp4",
                                "/demo-files/logs/delivery.txt",
                                "/demo-files/http-requests.txt",
                                "/demo-files/javascript-errors.txt",
                                "/demo-files/diff-report.html",
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                "API01",
                                "firefox09.nodes.selenium.project.company.com"
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-sanity-check",
                                "sanity-check",
                                "Functionality 2103: Add a product to cart",
                                "buy-a-product;add-a-product-to-cart",
                                5,
                                String.format("-100000:passed:7297622:@Before HooksGlue.before()%n6:passed:12412026:Given the user is on the useless \"Unicorn baskets\" product details page%n7:passed:8152885:When the user clicks on the Add To Cart button%n8:passed:7846464:Then the cart now has 1 product%n100000:passed:65216:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature", "Buy a product",
                                "",
                                "@country-all @severity-high",
                                "high",
                                "Functionality 2104: Show cart, average case",
                                "buy-a-product;show-cart,-average-case",
                                19,
                                String.format("-100000:passed:10097074:@Before HooksGlue.before()%n20:passed:16541711:Given the user has 5 products in cart%n21:passed:6658524:When the user goes to the cart page%n22:failed:13321460:Then the cart page shows 5 products%n100000:passed:1429766:@After HooksGlue.after(Scenario)"),
                                Date.from(LocalDateTime.of(2019, 4, 9, 9, 3, 27, 164000000).atZone(ZoneId.systemDefault()).toInstant()),
                                "/demo-files/screenshots/cart.png",
                                "/demo-files/videos/cart.mp4",
                                "/demo-files/logs/cart.txt",
                                "/demo-files/http-requests.txt",
                                "/demo-files/javascript-errors.txt",
                                "/demo-files/diff-report.html",
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                "API02",
                                "firefox02.nodes.selenium.project.company.com"
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature", "Buy a product",
                                "",
                                "@country-all @severity-medium",
                                "medium",
                                "Functionality 2104: Show cart, lots of products",
                                "buy-a-product;show-cart,-lots-of-products",
                                26,
                                String.format("-100000:passed:8014898:@Before HooksGlue.before()%n27:passed:13321758:Given the user has 1000 products in cart%n28:passed:14273176:When the user goes to the cart page%n29:failed:14456018:Then the cart page shows 1000 products%n100000:passed:787502:@After HooksGlue.after(Scenario)"),
                                Date.from(LocalDateTime.of(2019, 4, 9, 9, 3, 27, 164000000).atZone(ZoneId.systemDefault()).toInstant()),
                                "/demo-files/screenshots/cart.png",
                                "/demo-files/videos/cart.mp4",
                                "/demo-files/logs/cart.txt",
                                "/demo-files/http-requests.txt",
                                "/demo-files/javascript-errors.txt",
                                "/demo-files/diff-report.html",
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                "API01",
                                "firefox06.nodes.selenium.project.company.com"
                        ),
                        tuple(
                                "ara/demo/features/buy-a-product.feature",
                                "Buy a product",
                                "",
                                "@country-all @severity-sanity-check",
                                "sanity-check",
                                "Functionality 2104: Show cart, nominal case",
                                "buy-a-product;show-cart,-nominal-case",
                                12,
                                String.format("-100000:passed:12182148:@Before HooksGlue.before()%n13:passed:7788840:Given the user has 1 product in cart%n14:passed:10579105:When the user goes to the cart page%n15:passed:10264428:Then the cart page shows 1 product%n100000:passed:58635:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-buy-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-medium",
                                "medium",
                                "Functionality 2098: Have a friendly home page",
                                "choose-a-product;have-a-friendly-home-page",
                                5,
                                String.format("-100000:passed:12351727:@Before HooksGlue.before()%n6:passed:17760124:Given the user is on the friendly home page%n7:passed:14718350:When the user pauses the annoying carousel%n8:passed:5217324:Then the annoying carousel finally stops making user's head spin%n100000:passed:88652:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Functionality 2099: List all our useless products",
                                "choose-a-product;list-all-our-useless-products",
                                11,
                                String.format("-100000:passed:6275581:@Before HooksGlue.before()%n12:passed:13185838:Given the user is on the useless-products listing page%n13:passed:14106282:When the user counts the visible products%n14:passed:12117241:Then there are 3 useless products%n100000:passed:73150:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "@severity-medium",
                                "medium",
                                "Functionality 2100: Show a product with irresistible details",
                                "choose-a-product;show-a-product-with-irresistible-details",
                                17,
                                String.format("-100000:passed:11717312:@Before HooksGlue.before()%n18:passed:14756002:Given the user is on the useless \"Unicorn baskets\" product details page%n19:passed:11054091:When the user clicks on Reviews%n20:passed:8845245:Then the review 1 is \"Don't buy them: there is only one basket in the box!\"%n100000:passed:110490:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/choose-a-product.feature",
                                "Choose a product",
                                "@country-all",
                                "",
                                "",
                                "Functionality 2101: Sales Price on product details page",
                                "choose-a-product;sales-price-on-product-details-page",
                                23,
                                String.format("-100000:passed:9676672:@Before HooksGlue.before()%n24:passed:8068913:Given the \"Tuning stand-up paddle\" product is on sale with a \"50%%\" reduction%n25:passed:10642452:When the user goes to the product details page%n26:passed:7331748:Then the displayed price reduction is \"50%%\"%n100000:passed:94833:@After HooksGlue.after(Scenario)"),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-choose-a-product-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "ara/demo/features/pay.feature",
                                "Pay",
                                "",
                                "@country-fr @country-us @severity-sanity-check",
                                "sanity-check",
                                "Functionalities 2111 & 2112: Pay by Card",
                                "pay;pay-by-card",
                                11,
                                String.format("-100000:passed:12288587:@Before HooksGlue.before()%n0:element:Background:%n4:passed:16647176:Given the user has products in cart%n5:passed:7776824:And the user chosen a delivery option%n6:passed:10698447:When the user goes to the payment page%n0:element:Scenario:%n12:passed:15524500:When the user choose the payment \"Card\"%n13:passed:5980467:And the user validates the payment%n14:failed:12397074:Then the order is accepted%n100000:passed:692766:@After HooksGlue.after(Scenario)"),
                                Date.from(LocalDateTime.of(2019, 4, 9, 9, 3, 27, 164000000).atZone(ZoneId.systemDefault()).toInstant()),
                                "/demo-files/screenshots/payment.png",
                                "/demo-files/videos/payment.mp4",
                                "/demo-files/logs/payment.txt",
                                "/demo-files/http-requests.txt",
                                "/demo-files/javascript-errors.txt",
                                "/demo-files/diff-report.html",
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                "API02",
                                "firefox03.nodes.selenium.project.company.com"
                        ),
                        tuple(
                                "ara/demo/features/pay.feature",
                                "Pay",
                                "",
                                "@country-all @severity-medium",
                                "medium",
                                "Functionalities 2111 & 2113: Pay by Gift Card",
                                "pay;pay-by-gift-card",
                                18,
                                String.format("-100000:passed:11089835:@Before HooksGlue.before()%n0:element:Background:%n4:passed:8216476:Given the user has products in cart%n5:passed:6626875:And the user chosen a delivery option%n6:passed:16567931:When the user goes to the payment page%n0:element:Scenario:%n19:passed:14988630:When the user choose the payment \"Gift Card\"%n20:passed:8316036:And the user validates the payment%n21:failed:17295441:Then the order is accepted%n100000:passed:842764:@After HooksGlue.after(Scenario)"),
                                Date.from(LocalDateTime.of(2019, 4, 9, 9, 3, 27, 164000000).atZone(ZoneId.systemDefault()).toInstant()),
                                "/demo-files/screenshots/payment.png",
                                "/demo-files/videos/payment.mp4",
                                "/demo-files/logs/payment.txt",
                                "/demo-files/http-requests.txt",
                                "/demo-files/javascript-errors.txt",
                                "/demo-files/diff-report.html",
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                "API02",
                                "firefox05.nodes.selenium.project.company.com"
                        ),
                        tuple(
                                "ara/demo/features/pay.feature",
                                "Pay",
                                "",
                                "@country-us @severity-medium",
                                "medium",
                                "Functionalities 2111 & 2114: Pay by Mobile NFC",
                                "pay;pay-by-mobile-nfc",
                                25,
                                String.format("-100000:passed:11521631:@Before HooksGlue.before()%n0:element:Background:%n4:passed:9105244:Given the user has products in cart%n5:passed:9618452:And the user chosen a delivery option%n6:passed:9320946:When the user goes to the payment page%n0:element:Scenario:%n26:passed:13803628:When the user choose the payment \"NFC\"%n27:passed:5382460:And the user validates the payment%n28:failed:12885319:Then the order is accepted%n100000:passed:743993:@After HooksGlue.after(Scenario)"),
                                Date.from(LocalDateTime.of(2019, 4, 9, 9, 3, 27, 164000000).atZone(ZoneId.systemDefault()).toInstant()),
                                "/demo-files/screenshots/payment.png",
                                "/demo-files/videos/payment.mp4",
                                "/demo-files/logs/payment.txt",
                                "/demo-files/http-requests.txt",
                                "/demo-files/javascript-errors.txt",
                                "/demo-files/diff-report.html",
                                "https://build.company.com/demo/test/53/cucumber-html-reports/report-feature_ara-demo-features-pay-feature.html",
                                "API02",
                                "firefox05.nodes.selenium.project.company.com"
                        )
                );

        countryDeployments = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getCountryDeployments()
        );

        assertThat(countryDeployments)
                .hasSize(1)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/51/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1582099200000L),
                                0L,
                                0L
                        )
                );

        errors = runs.stream()
                .map(Run::getExecutedScenarios)
                .flatMap(Collection::stream)
                .map(ExecutedScenario::getErrors)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(errors)
                .hasSize(10)
                .extracting(
                        "executedScenario.name",
                        "step",
                        "stepDefinition",
                        "stepLine",
                        "exception"
                )
                .containsOnly(
                        tuple(
                                "fr+us ▶ Functionalities 2111 & 2112: Pay by Card",
                                "Response should indicate a succeed transaction status",
                                "Response should indicate a succeed transaction status",
                                2,
                                String.format("AssertionError: expected 'failed' to deeply equal 'succeed'%n   at Object.eval sandbox-script.js:3:1)")
                        ),
                        tuple(
                                "Create account",
                                "the user is connected",
                                "^the user is connected$",
                                11,
                                String.format("ara.demo.WebsiteException: Website displayed an error message: Account creation failed for occult reasons.%n\tat ara.demo.AccountLogInGlue.the_user_is_connected(AccountLogInGlue.java:59)%n\tat ✽.Then the user is connected(ara/demo/features/account.feature:11)%n")
                        ),
                        tuple(
                                "Functionalities 2118 & 3116: Log in",
                                "the user is connected",
                                "^the user is connected$",
                                19,
                                String.format("ara.demo.WebsiteException: Website displayed an error message: Log in failed for occult reasons.%n\tat ara.demo.AccountLogInGlue.the_user_is_connected(AccountLogInGlue.java:59)%n\tat ✽.Then the user is connected(ara/demo/features/account.feature:19)%n")
                        ),
                        tuple(
                                "Functionalities 2106 & 2107: Choose delivery option",
                                "the delivery option is \"By pigeon\"",
                                "^the delivery option is \"([^\"]*)\"$",
                                41,
                                String.format("java.lang.AssertionError: expected:<[By pigeon]> but was:<[3D Printing]>%n\tat ara.demo.PaymentGlue.the_delivery_option_is(PaymentGlue.java:56)%n\tat ✽.And the delivery option is \"By pigeon\"(ara/demo/features/buy-a-product.feature:41)%n")
                        ),
                        tuple(
                                "Functionalities 2106 & 2107: Choose delivery option",
                                "the delivery price is \"1 cent\"",
                                "^the delivery price is \"([^\"]*)\"$",
                                42,
                                String.format("java.lang.AssertionError: expected:<[1 cent]> but was:<[50 cents]>%n\tat ara.demo.PaymentGlue.the_delivery_price_is(PaymentGlue.java:66)%n\tat ✽.And the delivery price is \"1 cent\"(ara/demo/features/buy-a-product.feature:42)%n")
                        ),
                        tuple(
                                "Functionality 2104: Show cart, average case",
                                "the cart page shows 5 products",
                                "^the cart page shows (\\d+) product[s]?$",
                                22,
                                String.format("java.lang.AssertionError: expected:<[5]> but was:<[1]>%n\tat ara.demo.CartGlue.the_cart_page_shows_products(CartGlue.java:44)%n\tat ✽.Then the cart page shows 5 products(ara/demo/features/buy-a-product.feature:22)%n")
                        ),
                        tuple(
                                "Functionality 2104: Show cart, lots of products",
                                "the cart page shows 1000 products",
                                "^the cart page shows (\\d+) product[s]?$",
                                29,
                                String.format("java.lang.AssertionError: expected:<[1000]> but was:<[1]>%n\tat ara.demo.CartGlue.the_cart_page_shows_products(CartGlue.java:44)%n\tat ✽.Then the cart page shows 1000 products(ara/demo/features/buy-a-product.feature:29)%n")
                        ),
                        tuple(
                                "Functionalities 2111 & 2112: Pay by Card",
                                "the order is accepted",
                                "^the order is accepted$",
                                14,
                                String.format("org.openqa.selenium.NoSuchElementException: Cannot locate {By.cssSelector: #order-confirmation}%n\tat ara.demo.PaymentGlue.the_order_is_accepted(PaymentGlue.java:76)%n\tat ✽.Then the order is accepted(ara/demo/features/pay.feature:14)%n")
                        ),
                        tuple(
                                "Functionalities 2111 & 2113: Pay by Gift Card",
                                "the order is accepted",
                                "^the order is accepted$",
                                21,
                                String.format("org.openqa.selenium.NoSuchElementException: Cannot locate {By.cssSelector: #order-confirmation}%n\tat ara.demo.PaymentGlue.the_order_is_accepted(PaymentGlue.java:76)%n\tat ✽.Then the order is accepted(ara/demo/features/pay.feature:21)%n")
                        ),
                        tuple(
                                "Functionalities 2111 & 2114: Pay by Mobile NFC",
                                "the order is accepted",
                                "^the order is accepted$",
                                28,
                                String.format("org.openqa.selenium.NoSuchElementException: Cannot locate {By.cssSelector: #order-confirmation}%n\tat ara.demo.PaymentGlue.the_order_is_accepted(PaymentGlue.java:76)%n\tat ✽.Then the order is accepted(ara/demo/features/pay.feature:28)%n")
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/master",
                        "/opt/ara/data/executions/the-demo-project/master/day",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1582099200000/fr/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/assets",
                        "/opt/ara/data/assets/http-logs"
                );
    }

    @Test
    public void upload_saveTheExecutionAsBlocked_whenNoExecutionFoundAndNoCycleDefinitionFileFound() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1493814468000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution execution = executionRepository.findAll()
                .stream()
                .filter(e -> "34910c9971abcdef9f633920d8f8cf90853f38ea".equals(e.getVersion()))
                .findFirst()
                .get();

        assertThat(execution.getBranch()).isEqualTo("develop");
        assertThat(execution.getName()).isEqualTo("day");
        assertThat(execution.getRelease()).isEqualTo("v3");
        assertThat(execution.getVersion()).isEqualTo("34910c9971abcdef9f633920d8f8cf90853f38ea");
        assertThat(execution.getBuildDateTime()).isEqualTo(new Date(1581908100000L));
        assertThat(execution.getTestDateTime()).isEqualTo(new Date(1581908400000L));
        assertThat(execution.getJobUrl()).isEqualTo("https://build.company.com/demo/develop/night/54/");
        assertThat(execution.getJobLink()).isEqualTo("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/");
        assertThat(execution.getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(execution.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(execution.getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
        assertThat(execution.getDiscardReason()).isNull();
        assertThat(execution.getCycleDefinition().getProjectId()).isEqualTo(1L);
        assertThat(execution.getCycleDefinition().getBranch()).isEqualTo("develop");
        assertThat(execution.getCycleDefinition().getName()).isEqualTo("day");
        assertThat(execution.getCycleDefinition().getBranchPosition()).isEqualTo(1);
        assertThat(execution.getBlockingValidation()).isEqualTo(false);
        assertThat(execution.getQualityThresholds()).isNull();
        assertThat(execution.getQualityStatus()).isEqualTo(QualityStatus.INCOMPLETE);
        assertThat(execution.getQualitySeverities()).isNull();
        assertThat(execution.getDuration()).isEqualTo(0L);
        assertThat(execution.getEstimatedDuration()).isEqualTo(0L);
        assertThat(execution.getRuns()).isEmpty();
        assertThat(execution.getCountryDeployments()).isEmpty();

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1493814468000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_doNotUpdateTheExecution_whenExecutionFoundAsDone() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        SortedSet<Run> runsToSave = new TreeSet<>();
        runsToSave.add(
                new Run()
                        .withCountry(new Country().withId(1L).withProjectId(1L).withCode("fr"))
                        .withType(new Type().withId(1L).withProjectId(1L).withCode("api"))
                        .withExecutionId(3L)
                        .withComment("a comment about this run")
                        .withPlatform("integration-platform")
                        .withJobUrl("https://build.company.com/demo/test/56/previous-url")
                        .withJobLink("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/api/previous-link")
                        .withStatus(JobStatus.DONE)
                        .withCountryTags("some_tags")
                        .withStartDateTime(new Date(1581966600000L))
                        .withEstimatedDuration(150L)
                        .withDuration(100L)
                        .withSeverityTags("medium")
                        .withIncludeInThresholds(true)
        );
        runsToSave.add(
                new Run()
                        .withCountry(new Country().withId(2L).withProjectId(1L).withCode("us"))
                        .withType(new Type().withId(2L).withProjectId(1L).withCode("firefox-desktop"))
                        .withExecutionId(5L)
                        .withComment("just another run")
                        .withPlatform("another-integration-platform")
                        .withJobUrl("https://build.company.com/demo/test/59/previous-url")
                        .withJobLink("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1590565462000/us/desktop/previous-link")
                        .withStatus(JobStatus.DONE)
                        .withCountryTags("some_tags")
                        .withStartDateTime(new Date(1599966600000L))
                        .withEstimatedDuration(250L)
                        .withDuration(120L)
                        .withSeverityTags("high")
                        .withIncludeInThresholds(false)
        );

        SortedSet<CountryDeployment> countryDeploymentsToSave = new TreeSet<>();
        countryDeploymentsToSave.add(
                new CountryDeployment()
                        .withCountry(new Country().withId(1L).withProjectId(1L).withCode("fr"))
                        .withExecutionId(15L)
                        .withPlatform("integration-1")
                        .withJobUrl("https://build.company.com/demo/deploy/fr/55/previous-url")
                        .withJobLink("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/previous-link/1")
                        .withStatus(JobStatus.UNAVAILABLE)
                        .withResult(Result.FAILURE)
                        .withStartDateTime(new Date(1550108400000L))
                        .withEstimatedDuration(100L)
                        .withDuration(300L)
        );
        countryDeploymentsToSave.add(
                new CountryDeployment()
                        .withCountry(new Country().withId(2L).withProjectId(1L).withCode("us"))
                        .withExecutionId(9L)
                        .withPlatform("integration-2")
                        .withJobUrl("https://build.company.com/demo/deploy/us/59/previous-url")
                        .withJobLink("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/previous-link/2")
                        .withStatus(JobStatus.DONE)
                        .withResult(Result.SUCCESS)
                        .withStartDateTime(new Date(1550208400000L))
                        .withEstimatedDuration(150L)
                        .withDuration(1000L)
        );

        Execution execution = new Execution()
                .withBranch("develop")
                .withName("night")
                .withRelease("version-03")
                .withVersion("previous-version")
                .withBuildDateTime(new Date(1581931300000L))
                .withTestDateTime(new Date(1581918500000L))
                .withJobUrl("https://build.company.com/demo/develop/night/54/")
                .withJobLink("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/previous-link")
                .withStatus(JobStatus.DONE)
                .withResult(Result.SUCCESS)
                .withAcceptance(ExecutionAcceptance.NEW)
                .withDiscardReason("no discard reason")
                .withCycleDefinition(new CycleDefinition().withId(2L).withProjectId(1L))
                .withBlockingValidation(false)
                .withQualityThresholds("{\"sanity-check\":{\"failure\":80,\"warning\":95},\"high\":{\"failure\":85,\"warning\":96},\"medium\":{\"failure\":91,\"warning\":73}}")
                .withQualityStatus(QualityStatus.PASSED)
                .withQualitySeverities("[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":42,\"failed\":10,\"passed\":42},\"percent\":99,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":18,\"failed\":10,\"passed\":8},\"percent\":60,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":10,\"passed\":14},\"percent\":70,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":99,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":74,\"failed\":20,\"passed\":54},\"percent\":80,\"status\":\"PASSED\"}]")
                .withDuration(3600L)
                .withEstimatedDuration(1500L)
                .withRuns(runsToSave)
                .withCountryDeployments(countryDeploymentsToSave);

        Execution savedExecution = executionRepository.save(execution);

        MultipartFile zip = readZip("src/test/resources/zip/1585743962000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution updatedExecution = executionRepository.findByProjectIdAndId(1L, savedExecution.getId());
        assertThat(updatedExecution.getBranch()).isEqualTo("develop");
        assertThat(updatedExecution.getName()).isEqualTo("night");
        assertThat(updatedExecution.getRelease()).isEqualTo("version-03");
        assertThat(updatedExecution.getVersion()).isEqualTo("previous-version");
        assertThat(updatedExecution.getBuildDateTime()).isEqualTo(new Date(1581931300000L));
        assertThat(updatedExecution.getTestDateTime()).isEqualTo(new Date(1581918500000L));
        assertThat(updatedExecution.getJobUrl()).isEqualTo("https://build.company.com/demo/develop/night/54/");
        assertThat(updatedExecution.getJobLink()).isEqualTo("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/previous-link");
        assertThat(updatedExecution.getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(updatedExecution.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(updatedExecution.getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
        assertThat(updatedExecution.getDiscardReason()).isEqualTo("no discard reason");
        assertThat(updatedExecution.getCycleDefinition().getId()).isEqualTo(2L);
        assertThat(updatedExecution.getBlockingValidation()).isEqualTo(false);
        assertThat(updatedExecution.getQualityThresholds()).isEqualTo("{\"sanity-check\":{\"failure\":80,\"warning\":95},\"high\":{\"failure\":85,\"warning\":96},\"medium\":{\"failure\":91,\"warning\":73}}");
        assertThat(updatedExecution.getQualityStatus()).isEqualTo(QualityStatus.PASSED);
        assertThat(updatedExecution.getQualitySeverities()).isEqualTo("[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":42,\"failed\":10,\"passed\":42},\"percent\":99,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":18,\"failed\":10,\"passed\":8},\"percent\":60,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":10,\"passed\":14},\"percent\":70,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":99,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":74,\"failed\":20,\"passed\":54},\"percent\":80,\"status\":\"PASSED\"}]");
        assertThat(updatedExecution.getDuration()).isEqualTo(3600L);
        assertThat(updatedExecution.getEstimatedDuration()).isEqualTo(1500L);

        assertThat(updatedExecution.getRuns())
                .hasSize(2)
                .extracting(
                        "country.id",
                        "type.id",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                1L,
                                1L,
                                "a comment about this run",
                                "integration-platform",
                                "https://build.company.com/demo/test/56/previous-url",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/api/previous-link",
                                JobStatus.DONE,
                                "some_tags",
                                new Date(1581966600000L),
                                150L,
                                100L,
                                "medium",
                                true
                        ),
                        tuple(
                                2L,
                                2L,
                                "just another run",
                                "another-integration-platform",
                                "https://build.company.com/demo/test/59/previous-url",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1590565462000/us/desktop/previous-link",
                                JobStatus.DONE,
                                "some_tags",
                                new Date(1599966600000L),
                                250L,
                                120L,
                                "high",
                                false
                        )
                );

        assertThat(updatedExecution.getCountryDeployments())
                .hasSize(2)
                .extracting(
                        "country.id",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                1L,
                                "integration-1",
                                "https://build.company.com/demo/deploy/fr/55/previous-url",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/previous-link/1",
                                JobStatus.UNAVAILABLE,
                                Result.FAILURE,
                                new Date(1550108400000L),
                                100L,
                                300L
                        ),
                        tuple(
                                2L,
                                "integration-2",
                                "https://build.company.com/demo/deploy/us/59/previous-url",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/previous-link/2",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1550208400000L),
                                150L,
                                1000L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743962000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_updateTheExecution_whenExecutionFoundButNotDoneAndDoesNotHaveRuns() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        Execution execution = new Execution()
                .withBranch("develop")
                .withName("night")
                .withRelease("version-03")
                .withVersion("previous-version")
                .withBuildDateTime(new Date(1581931300000L))
                .withTestDateTime(new Date(1581918500000L))
                .withJobUrl("https://build.company.com/demo/develop/night/9054/")
                .withJobLink("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1590565462000/previous-link")
                .withStatus(JobStatus.PENDING)
                .withResult(Result.FAILURE)
                .withAcceptance(ExecutionAcceptance.NEW)
                .withDiscardReason("no discard reason")
                .withCycleDefinition(new CycleDefinition().withId(2L).withProjectId(1L))
                .withBlockingValidation(false)
                .withQualityThresholds("{\"sanity-check\":{\"failure\":81,\"warning\":96},\"high\":{\"failure\":86,\"warning\":97},\"medium\":{\"failure\":90,\"warning\":75}}")
                .withQualityStatus(QualityStatus.FAILED)
                .withQualitySeverities("[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":42,\"failed\":10,\"passed\":42},\"percent\":99,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":18,\"failed\":10,\"passed\":8},\"percent\":60,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":10,\"passed\":14},\"percent\":70,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":99,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":74,\"failed\":20,\"passed\":54},\"percent\":80,\"status\":\"PASSED\"}]")
                .withDuration(3600L)
                .withEstimatedDuration(1500L);

        Execution savedExecution = executionRepository.save(execution);

        MultipartFile zip = readZip("src/test/resources/zip/1589198399000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution updatedExecution = executionRepository.findByProjectIdAndId(1L, savedExecution.getId());
        assertThat(updatedExecution.getBranch()).isEqualTo("develop");
        assertThat(updatedExecution.getName()).isEqualTo("day");
        assertThat(updatedExecution.getRelease()).isEqualTo("v3");
        assertThat(updatedExecution.getVersion()).isEqualTo("34910c9971abebce9f633920d8f8cf90853f38ea");
        assertThat(updatedExecution.getBuildDateTime()).isEqualTo(new Date(1581908100000L));
        assertThat(updatedExecution.getTestDateTime()).isEqualTo(new Date(1581908400000L));
        assertThat(updatedExecution.getJobUrl()).isEqualTo("https://build.company.com/demo/develop/night/9054/");
        assertThat(updatedExecution.getJobLink()).isEqualTo("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/");
        assertThat(updatedExecution.getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(updatedExecution.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(updatedExecution.getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
        assertThat(updatedExecution.getDiscardReason()).isNull();
        assertThat(updatedExecution.getCycleDefinition().getId()).isEqualTo(1L);
        assertThat(updatedExecution.getBlockingValidation()).isEqualTo(true);
        assertThat(updatedExecution.getQualityThresholds()).isEqualTo("{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}");
        assertThat(updatedExecution.getQualityStatus()).isEqualTo(QualityStatus.PASSED);
        assertThat(updatedExecution.getQualitySeverities()).isEqualTo("[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":0,\"passed\":24},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":64,\"failed\":0,\"passed\":64},\"percent\":100,\"status\":\"PASSED\"}]");
        assertThat(updatedExecution.getDuration()).isEqualTo(230L);
        assertThat(updatedExecution.getEstimatedDuration()).isEqualTo(150L);

        assertThat(updatedExecution.getRuns())
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/9056/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/9057/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/9058/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/9060/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/9061/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/9062/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        assertThat(updatedExecution.getCountryDeployments())
                .hasSize(2)
                .extracting(
                        "country.id",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                1L,
                                "integ",
                                "https://build.company.com/demo/deploy/fr/9055/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                70L,
                                80L
                        ),
                        tuple(
                                2L,
                                "integ",
                                "https://build.company.com/demo/deploy/us/9059/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                50L,
                                100L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1589198399000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_updateTheExecutionAndAddRuns_whenExecutionFoundButNotDoneAndHasSomeRuns() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        SortedSet<Run> runsToSave = new TreeSet<>();
        runsToSave.add(
                new Run()
                        .withCountry(new Country().withId(1L).withProjectId(1L).withCode("fr"))
                        .withType(new Type().withId(1L).withProjectId(1L).withCode("api"))
                        .withExecutionId(1L)
                        .withComment("a comment about this run")
                        .withPlatform("integration-platform")
                        .withJobUrl("https://build.company.com/demo/test/56/previous-url")
                        .withJobLink("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1590565462000/fr/api/previous-link")
                        .withStatus(JobStatus.DONE)
                        .withCountryTags("some_tags")
                        .withStartDateTime(new Date(1581966600000L))
                        .withEstimatedDuration(150L)
                        .withDuration(100L)
                        .withSeverityTags("high")
                        .withIncludeInThresholds(false)
        );
        runsToSave.add(
                new Run()
                        .withCountry(new Country().withId(2L).withProjectId(1L).withCode("us"))
                        .withType(new Type().withId(2L).withProjectId(1L).withCode("firefox-desktop"))
                        .withExecutionId(5L)
                        .withComment("just another run")
                        .withPlatform("another-integration-platform")
                        .withJobUrl("https://build.company.com/demo/test/59/previous-url")
                        .withJobLink("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1590565462000/us/desktop/previous-link")
                        .withStatus(JobStatus.DONE)
                        .withCountryTags("some_tags")
                        .withStartDateTime(new Date(1599966600000L))
                        .withEstimatedDuration(250L)
                        .withDuration(120L)
                        .withSeverityTags("medium")
                        .withIncludeInThresholds(false)
        );

        SortedSet<CountryDeployment> countryDeploymentsToSave = new TreeSet<>();
        countryDeploymentsToSave.add(
                new CountryDeployment()
                        .withCountry(new Country().withId(1L).withProjectId(1L).withCode("fr"))
                        .withExecutionId(1L)
                        .withPlatform("integration-1")
                        .withJobUrl("https://build.company.com/demo/deploy/fr/55/previous-url")
                        .withJobLink("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1590565462000/fr/previous-link/1")
                        .withStatus(JobStatus.UNAVAILABLE)
                        .withResult(Result.FAILURE)
                        .withStartDateTime(new Date(1550108400000L))
                        .withEstimatedDuration(100L)
                        .withDuration(300L)
        );
        countryDeploymentsToSave.add(
                new CountryDeployment()
                        .withCountry(new Country().withId(2L).withProjectId(1L).withCode("us"))
                        .withExecutionId(2L)
                        .withPlatform("integration-2")
                        .withJobUrl("https://build.company.com/demo/deploy/us/59/previous-url")
                        .withJobLink("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1590565462000/fr/previous-link/2")
                        .withStatus(JobStatus.DONE)
                        .withResult(Result.SUCCESS)
                        .withStartDateTime(new Date(1550208400000L))
                        .withEstimatedDuration(150L)
                        .withDuration(1000L)
        );

        Execution execution = new Execution()
                .withBranch("develop")
                .withName("night")
                .withRelease("version-03")
                .withVersion("previous-version")
                .withBuildDateTime(new Date(1581931300000L))
                .withTestDateTime(new Date(1581918500000L))
                .withJobUrl("https://build.company.com/demo/develop/night/8854/")
                .withJobLink("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1590565462000/previous-link")
                .withStatus(JobStatus.PENDING)
                .withResult(Result.FAILURE)
                .withAcceptance(ExecutionAcceptance.NEW)
                .withDiscardReason("no discard reason")
                .withCycleDefinition(new CycleDefinition().withId(2L).withProjectId(1L))
                .withBlockingValidation(false)
                .withQualityThresholds("{\"sanity-check\":{\"failure\":81,\"warning\":96},\"high\":{\"failure\":86,\"warning\":97},\"medium\":{\"failure\":90,\"warning\":75}}")
                .withQualityStatus(QualityStatus.FAILED)
                .withQualitySeverities("[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":42,\"failed\":10,\"passed\":42},\"percent\":99,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":18,\"failed\":10,\"passed\":8},\"percent\":60,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":10,\"passed\":14},\"percent\":70,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":99,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":74,\"failed\":20,\"passed\":54},\"percent\":80,\"status\":\"PASSED\"}]")
                .withDuration(3600L)
                .withEstimatedDuration(1500L)
                .withRuns(runsToSave)
                .withCountryDeployments(countryDeploymentsToSave);

        Execution savedExecution = executionRepository.save(execution);

        MultipartFile zip = readZip("src/test/resources/zip/1590565462000.zip");
        executionResource.upload("the-demo-project", "master", "day", zip);

        Execution updatedExecution = executionRepository.findByProjectIdAndId(1L, savedExecution.getId());
        assertThat(updatedExecution.getBranch()).isEqualTo("master");
        assertThat(updatedExecution.getName()).isEqualTo("day");
        assertThat(updatedExecution.getRelease()).isEqualTo("v3");
        assertThat(updatedExecution.getVersion()).isEqualTo("34910c9971abebce9f633920d8f8cf90853f38ea");
        assertThat(updatedExecution.getBuildDateTime()).isEqualTo(new Date(1581908100000L));
        assertThat(updatedExecution.getTestDateTime()).isEqualTo(new Date(1581908400000L));
        assertThat(updatedExecution.getJobUrl()).isEqualTo("https://build.company.com/demo/develop/night/8854/");
        assertThat(updatedExecution.getJobLink()).isEqualTo("/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/");
        assertThat(updatedExecution.getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(updatedExecution.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(updatedExecution.getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
        assertThat(updatedExecution.getDiscardReason()).isNull();
        assertThat(updatedExecution.getCycleDefinition().getId()).isEqualTo(3L);
        assertThat(updatedExecution.getBlockingValidation()).isEqualTo(true);
        assertThat(updatedExecution.getQualityThresholds()).isEqualTo("{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}");
        assertThat(updatedExecution.getQualityStatus()).isEqualTo(QualityStatus.PASSED);
        assertThat(updatedExecution.getQualitySeverities()).isEqualTo("[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":0,\"passed\":24},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":64,\"failed\":0,\"passed\":64},\"percent\":100,\"status\":\"PASSED\"}]");
        assertThat(updatedExecution.getDuration()).isEqualTo(1000L);
        assertThat(updatedExecution.getEstimatedDuration()).isEqualTo(2000L);

        assertThat(updatedExecution.getRuns())
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/8856/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/8857/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/8858/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/8860/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/8861/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/8862/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        assertThat(updatedExecution.getCountryDeployments())
                .hasSize(2)
                .extracting(
                        "country.id",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                1L,
                                "integ",
                                "https://build.company.com/demo/deploy/fr/8855/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                2L,
                                "integ",
                                "https://build.company.com/demo/deploy/us/8859/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/master",
                        "/opt/ara/data/executions/the-demo-project/master/day",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/api",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1590565462000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecutionAsBlocked_whenExecutionFoundButNotDoneAndNoCycleDefinitionFileFound() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        Execution execution = new Execution()
                .withBranch("develop")
                .withName("night")
                .withRelease("version-03")
                .withVersion("previous-version")
                .withBuildDateTime(new Date(1581931300000L))
                .withTestDateTime(new Date(1581918500000L))
                .withJobUrl("https://build.company.com/demo/develop/night/9054/")
                .withJobLink("/opt/ara/data/executions/the-demo-project/develop/day/incoming/1590565462000/previous-link")
                .withStatus(JobStatus.PENDING)
                .withResult(Result.FAILURE)
                .withAcceptance(ExecutionAcceptance.NEW)
                .withDiscardReason("no discard reason")
                .withCycleDefinition(new CycleDefinition().withId(2L).withProjectId(1L))
                .withBlockingValidation(true)
                .withQualityThresholds("{\"sanity-check\":{\"failure\":81,\"warning\":96},\"high\":{\"failure\":86,\"warning\":97},\"medium\":{\"failure\":90,\"warning\":75}}")
                .withQualityStatus(QualityStatus.FAILED)
                .withQualitySeverities("[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":42,\"failed\":10,\"passed\":42},\"percent\":99,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":18,\"failed\":10,\"passed\":8},\"percent\":60,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":10,\"passed\":14},\"percent\":70,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":99,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":74,\"failed\":20,\"passed\":54},\"percent\":80,\"status\":\"PASSED\"}]")
                .withDuration(3600L)
                .withEstimatedDuration(1500L);

        Execution savedExecution = executionRepository.save(execution);

        MultipartFile zip = readZip("src/test/resources/zip/9783108610000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution updatedExecution = executionRepository.findByProjectIdAndId(1L, savedExecution.getId());
        assertThat(updatedExecution.getBranch()).isEqualTo("develop");
        assertThat(updatedExecution.getName()).isEqualTo("day");
        assertThat(updatedExecution.getRelease()).isEqualTo("v3");
        assertThat(updatedExecution.getVersion()).isEqualTo("34910c9971abebce9f633920d8f8cf90853f38ea");
        assertThat(updatedExecution.getBuildDateTime()).isEqualTo(new Date(1581908100000L));
        assertThat(updatedExecution.getTestDateTime()).isEqualTo(new Date(1581908400000L));
        assertThat(updatedExecution.getJobUrl()).isEqualTo("https://build.company.com/demo/develop/night/9054/");
        assertThat(updatedExecution.getJobLink()).isEqualTo("/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/");
        assertThat(updatedExecution.getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(updatedExecution.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(updatedExecution.getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
        assertThat(updatedExecution.getDiscardReason()).isNull();
        assertThat(updatedExecution.getCycleDefinition().getId()).isEqualTo(1L);
        assertThat(updatedExecution.getBlockingValidation()).isEqualTo(false);
        assertThat(updatedExecution.getQualityThresholds()).isNull();
        assertThat(updatedExecution.getQualityStatus()).isEqualTo(QualityStatus.INCOMPLETE);
        assertThat(updatedExecution.getQualitySeverities()).isNull();
        assertThat(updatedExecution.getDuration()).isEqualTo(230L);
        assertThat(updatedExecution.getEstimatedDuration()).isEqualTo(150L);
        assertThat(updatedExecution.getRuns()).isEmpty();
        assertThat(updatedExecution.getCountryDeployments()).isEmpty();

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/9783108610000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_deleteExecutionCompletionRequest_whenExecutionCompletionRequestUrlFound() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();
        List<ExecutionCompletionRequest> executionCompletionRequests = executionCompletionRequestRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();
        assertThat(executionCompletionRequests).isEmpty();

        ExecutionCompletionRequest executionCompletionRequest = new ExecutionCompletionRequest("https://build.company.com/demo/develop/night/54/");
        executionCompletionRequestRepository.save(executionCompletionRequest);

        MultipartFile zip = readZip("src/test/resources/zip/1532653323000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        executionCompletionRequests = executionCompletionRequestRepository.findAll();
        assertThat(executionCompletionRequests).isEmpty();
    }

    @Test
    public void upload_deleteExecutionDirectory_whenIndexingSettingIsSetToTrue() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        Setting setting = settingRepository.findByProjectIdAndCode(1L, Settings.EXECUTION_INDEXER_FILE_DELETE_AFTER_INDEXING_AS_DONE);
        assertThat(setting.getValue()).isEqualTo(Boolean.FALSE.toString());
        setting.setValue(Boolean.TRUE.toString());
        settingRepository.save(setting);

        MultipartFile zip = readZip("src/test/resources/zip/1591910900000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .doesNotContain(
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1591910900000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecutionWithRunningCountryDeploymentsAndRuns_whenNoErrorFound() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1571509815000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        executions = executionRepository.findAll()
                .stream()
                .filter(execution -> "34910c9971abebce9f633920d8f8cf90853f38ea".equals(execution.getVersion()))
                .collect(Collectors.toList());
        assertThat(executions)
                .hasSize(1)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .containsOnly(
                        tuple(
                                "develop",
                                "day",
                                "v3",
                                "34910c9971abebce9f633920d8f8cf90853f38ea",
                                new Date(1581908100000L),
                                new Date(1581908400000L),
                                "https://build.company.com/demo/develop/night/1954/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                ExecutionAcceptance.NEW,
                                null,
                                1L,
                                "develop",
                                "day",
                                1,
                                true,
                                "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                                QualityStatus.PASSED,
                                "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":0,\"passed\":24},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":64,\"failed\":0,\"passed\":64},\"percent\":100,\"status\":\"PASSED\"}]",
                                0L,
                                0L
                        )
                );

        runs = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getRuns()
        );

        assertThat(runs)
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1956/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1957/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1958/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1960/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1961/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1962/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        countryDeployments = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getCountryDeployments()
        );

        assertThat(countryDeployments)
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/1955/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/1959/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/",
                                JobStatus.DONE,
                                null,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        errors = runs.stream()
                .map(Run::getExecutedScenarios)
                .flatMap(Collection::stream)
                .map(ExecutedScenario::getErrors)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(errors).isEmpty();

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1571509815000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecutionWithUnavailableCountryDeploymentsAndRuns_whenNoErrorFound() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1577309445000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        executions = executionRepository.findAll()
                .stream()
                .filter(execution -> "34910c9971abebce9f633920d8f8cf90853f38ea".equals(execution.getVersion()))
                .collect(Collectors.toList());
        assertThat(executions)
                .hasSize(1)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .containsOnly(
                        tuple(
                                "develop",
                                "day",
                                "v3",
                                "34910c9971abebce9f633920d8f8cf90853f38ea",
                                new Date(1581908100000L),
                                new Date(1581908400000L),
                                "https://build.company.com/demo/develop/night/1054/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                ExecutionAcceptance.NEW,
                                null,
                                1L,
                                "develop",
                                "day",
                                1,
                                true,
                                "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                                QualityStatus.INCOMPLETE,
                                "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":0,\"passed\":24},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":64,\"failed\":0,\"passed\":64},\"percent\":100,\"status\":\"INCOMPLETE\"}]",
                                0L,
                                0L
                        )
                );

        runs = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getRuns()
        );

        assertThat(runs)
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1056/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/api/",
                                JobStatus.UNAVAILABLE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1057/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1058/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1060/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1061/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1062/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        countryDeployments = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getCountryDeployments()
        );

        assertThat(countryDeployments)
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/1055/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/1059/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/",
                                JobStatus.UNAVAILABLE,
                                Result.NOT_BUILT,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        errors = runs.stream()
                .map(Run::getExecutedScenarios)
                .flatMap(Collection::stream)
                .map(ExecutedScenario::getErrors)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(errors).isEmpty();

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1577309445000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecutionWithPendingCountryDeploymentsAndRuns_whenNoErrorFound() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1321009871000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        executions = executionRepository.findAll()
                .stream()
                .filter(execution -> "34910c9971abebce9f633920d8f8cf90853f38ea".equals(execution.getVersion()))
                .collect(Collectors.toList());
        assertThat(executions)
                .hasSize(1)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .containsOnly(
                        tuple(
                                "develop",
                                "day",
                                "v3",
                                "34910c9971abebce9f633920d8f8cf90853f38ea",
                                new Date(1581908100000L),
                                new Date(1581908400000L),
                                "https://build.company.com/demo/develop/night/1154/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                ExecutionAcceptance.NEW,
                                null,
                                1L,
                                "develop",
                                "day",
                                1,
                                true,
                                "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                                QualityStatus.INCOMPLETE,
                                "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":0,\"passed\":24},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":64,\"failed\":0,\"passed\":64},\"percent\":100,\"status\":\"INCOMPLETE\"}]",
                                0L,
                                0L
                        )
                );

        runs = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getRuns()
        );

        assertThat(runs)
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                null,
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/api/",
                                JobStatus.UNAVAILABLE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1157/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1158/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1160/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1161/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/1162/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        countryDeployments = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getCountryDeployments()
        );

        assertThat(countryDeployments)
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/1155/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                null,
                                null,
                                null,
                                null
                        )
                );

        errors = runs.stream()
                .map(Run::getExecutedScenarios)
                .flatMap(Collection::stream)
                .map(ExecutedScenario::getErrors)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(errors).isEmpty();

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1321009871000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenBuildInformationIsRenamedInSettings() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();

        Setting setting = settingRepository.findByProjectIdAndCode(1L, Settings.EXECUTION_INDEXER_FILE_BUILD_INFORMATION_PATH);
        assertThat(setting).isNull();
        setting = new Setting()
                .withProjectId(1L)
                .withCode(Settings.EXECUTION_INDEXER_FILE_BUILD_INFORMATION_PATH)
                .withValue("/artefact/build/info/buildInfo.json");
        settingRepository.save(setting);

        MultipartFile zip = readZip("src/test/resources/zip/1592156114000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        executions = executionRepository.findAll()
                .stream()
                .filter(execution -> "34910c9971abebce9f633920d8f8cf90853f38ea".equals(execution.getVersion()))
                .collect(Collectors.toList());
        assertThat(executions)
                .hasSize(1)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .containsOnly(
                        tuple(
                                "develop",
                                "day",
                                "v3",
                                "34910c9971abebce9f633920d8f8cf90853f38ea",
                                new Date(1581908100000L),
                                new Date(1581908400000L),
                                "https://build.company.com/demo/develop/night/54/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                ExecutionAcceptance.NEW,
                                null,
                                1L,
                                "develop",
                                "day",
                                1,
                                true,
                                "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                                QualityStatus.PASSED,
                                "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":0,\"passed\":24},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":64,\"failed\":0,\"passed\":64},\"percent\":100,\"status\":\"PASSED\"}]",
                                0L,
                                0L
                        )
                );

        runs = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getRuns()
        );

        assertThat(runs)
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/56/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/57/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/58/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        countryDeployments = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getCountryDeployments()
        );

        assertThat(countryDeployments)
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/55/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-mobile/artefact",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-mobile/artefact/build",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-mobile/artefact/build/info",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-mobile/artefact/build/info/buildInfo.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/artefact",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/artefact/build",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/artefact/build/info",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/artefact/build/info/buildInfo.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-desktop/artefact",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-desktop/artefact/build",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-desktop/artefact/build/info",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-desktop/artefact/build/info/buildInfo.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/api/artefact",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/api/artefact/build",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/api/artefact/build/info",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/api/artefact/build/info/buildInfo.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/artefact",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/artefact/build",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/artefact/build/info",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/artefact/build/info/buildInfo.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-mobile/artefact",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-mobile/artefact/build",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-mobile/artefact/build/info",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-mobile/artefact/build/info/buildInfo.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/artefact",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/artefact/build",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/artefact/build/info",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/artefact/build/info/buildInfo.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-desktop/artefact",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-desktop/artefact/build",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-desktop/artefact/build/info",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-desktop/artefact/build/info/buildInfo.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/api/artefact",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/api/artefact/build",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/api/artefact/build/info",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/api/artefact/build/info/buildInfo.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1592156114000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenCycleDefinitionIsRenamedInSettings() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();

        Setting setting = settingRepository.findByProjectIdAndCode(1L, Settings.EXECUTION_INDEXER_FILE_CYCLE_DEFINITION_PATH);
        assertThat(setting).isNull();
        setting = new Setting()
                .withProjectId(1L)
                .withCode(Settings.EXECUTION_INDEXER_FILE_CYCLE_DEFINITION_PATH)
                .withValue("cycleDef.json");
        settingRepository.save(setting);

        MultipartFile zip = readZip("src/test/resources/zip/1585743320000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        executions = executionRepository.findAll()
                .stream()
                .filter(execution -> "34910c9971abebce9f633920d8f8cf90853f38ea".equals(execution.getVersion()))
                .collect(Collectors.toList());
        assertThat(executions)
                .hasSize(1)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .containsOnly(
                        tuple(
                                "develop",
                                "day",
                                "v3",
                                "34910c9971abebce9f633920d8f8cf90853f38ea",
                                new Date(1581908100000L),
                                new Date(1581908400000L),
                                "https://build.company.com/demo/develop/night/54/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                ExecutionAcceptance.NEW,
                                null,
                                1L,
                                "develop",
                                "day",
                                1,
                                true,
                                "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                                QualityStatus.PASSED,
                                "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":0,\"passed\":24},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":64,\"failed\":0,\"passed\":64},\"percent\":100,\"status\":\"PASSED\"}]",
                                0L,
                                0L
                        )
                );

        runs = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getRuns()
        );

        assertThat(runs)
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/56/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/57/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/58/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        countryDeployments = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getCountryDeployments()
        );

        assertThat(countryDeployments)
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/55/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/cycleDef.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1585743320000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenCucumberStepDefinitionsIsRenamedInSettings() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();

        Optional<TechnologySetting> technologySetting = technologySettingRepository.findByProjectIdAndCodeAndTechnology(1L, CucumberSettings.STEP_DEFINITIONS_PATH.getCode(), Technology.CUCUMBER);
        assertThat(technologySetting).isNotPresent();
        TechnologySetting setting = new TechnologySetting()
                .withProjectId(1L)
                .withCode(CucumberSettings.STEP_DEFINITIONS_PATH.getCode())
                .withTechnology(Technology.CUCUMBER)
                .withValue("stepDef.json");
        technologySettingRepository.save(setting);

        MultipartFile zip = readZip("src/test/resources/zip/1637539262000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        executions = executionRepository.findAll()
                .stream()
                .filter(execution -> "34910c9971abebce9f633920d8f8cf90853f38ea".equals(execution.getVersion()))
                .collect(Collectors.toList());
        assertThat(executions)
                .hasSize(1)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .containsOnly(
                        tuple(
                                "develop",
                                "day",
                                "v3",
                                "34910c9971abebce9f633920d8f8cf90853f38ea",
                                new Date(1581908100000L),
                                new Date(1581908400000L),
                                "https://build.company.com/demo/develop/night/54/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                ExecutionAcceptance.NEW,
                                null,
                                1L,
                                "develop",
                                "day",
                                1,
                                true,
                                "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                                QualityStatus.PASSED,
                                "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":0,\"passed\":24},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":64,\"failed\":0,\"passed\":64},\"percent\":100,\"status\":\"PASSED\"}]",
                                0L,
                                0L
                        )
                );

        runs = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getRuns()
        );

        assertThat(runs)
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/56/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/57/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/58/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        countryDeployments = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getCountryDeployments()
        );

        assertThat(countryDeployments)
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/55/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/firefox-mobile/stepDef.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/firefox-desktop/stepDef.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/firefox-mobile/stepDef.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/firefox-desktop/stepDef.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1637539262000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenCucumberReportFileIsRenamedInSettings() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();

        Optional<TechnologySetting> technologySetting = technologySettingRepository.findByProjectIdAndCodeAndTechnology(1L, CucumberSettings.REPORT_PATH.getCode(), Technology.CUCUMBER);
        assertThat(technologySetting).isNotPresent();
        TechnologySetting setting = new TechnologySetting()
                .withProjectId(1L)
                .withCode(CucumberSettings.REPORT_PATH.getCode())
                .withTechnology(Technology.CUCUMBER)
                .withValue("/result.json");
        technologySettingRepository.save(setting);

        MultipartFile zip = readZip("src/test/resources/zip/1289563870000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        executions = executionRepository.findAll()
                .stream()
                .filter(execution -> "34910c9971abebce9f633920d8f8cf90853f38ea".equals(execution.getVersion()))
                .collect(Collectors.toList());
        assertThat(executions)
                .hasSize(1)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .containsOnly(
                        tuple(
                                "develop",
                                "day",
                                "v3",
                                "34910c9971abebce9f633920d8f8cf90853f38ea",
                                new Date(1581908100000L),
                                new Date(1581908400000L),
                                "https://build.company.com/demo/develop/night/54/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                ExecutionAcceptance.NEW,
                                null,
                                1L,
                                "develop",
                                "day",
                                1,
                                true,
                                "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                                QualityStatus.PASSED,
                                "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":0,\"passed\":24},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":64,\"failed\":0,\"passed\":64},\"percent\":100,\"status\":\"PASSED\"}]",
                                0L,
                                0L
                        )
                );

        runs = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getRuns()
        );

        assertThat(runs)
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/56/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/57/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/58/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        countryDeployments = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getCountryDeployments()
        );

        assertThat(countryDeployments)
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/55/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/firefox-mobile/result.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/firefox-desktop/result.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/firefox-mobile/result.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/firefox-desktop/result.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1289563870000/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenPostmanReportsFolderIsRenamedInSettings() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();

        Optional<TechnologySetting> technologySetting = technologySettingRepository.findByProjectIdAndCodeAndTechnology(1L, PostmanSettings.REPORTS_PATH.getCode(), Technology.POSTMAN);
        assertThat(technologySetting).isNotPresent();
        TechnologySetting setting = new TechnologySetting()
                .withProjectId(1L)
                .withCode(PostmanSettings.REPORTS_PATH.getCode())
                .withTechnology(Technology.POSTMAN)
                .withValue("results");
        technologySettingRepository.save(setting);

        MultipartFile zip = readZip("src/test/resources/zip/1601874620000.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        executions = executionRepository.findAll()
                .stream()
                .filter(execution -> "34910c9971abebce9f633920d8f8cf90853f38ea".equals(execution.getVersion()))
                .collect(Collectors.toList());
        assertThat(executions)
                .hasSize(1)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .containsOnly(
                        tuple(
                                "develop",
                                "day",
                                "v3",
                                "34910c9971abebce9f633920d8f8cf90853f38ea",
                                new Date(1581908100000L),
                                new Date(1581908400000L),
                                "https://build.company.com/demo/develop/night/54/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                ExecutionAcceptance.NEW,
                                null,
                                1L,
                                "develop",
                                "day",
                                1,
                                true,
                                "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                                QualityStatus.PASSED,
                                "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":24,\"failed\":0,\"passed\":24},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":64,\"failed\":0,\"passed\":64},\"percent\":100,\"status\":\"PASSED\"}]",
                                0L,
                                0L
                        )
                );

        runs = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getRuns()
        );

        assertThat(runs)
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/56/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/57/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/58/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        countryDeployments = new ArrayList<>(
                executions.stream()
                        .findFirst()
                        .get()
                        .getCountryDeployments()
        );

        assertThat(countryDeployments)
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/55/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/api/results",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/api/results/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/api/results/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/api/results/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/us/api/results/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/api/results",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/api/results/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/api/results/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/api/results/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1601874620000/fr/api/results/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenCountryNotFoundInDB() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581908400001.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution execution = executionRepository.findByProjectIdAndJobUrl(1L, "https://build.company.com/demo/develop/night/54/");

        assertThat(execution)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .contains(
                        "develop",
                        "day",
                        "v3",
                        "34910c9971abebce9f633920d8f8cf90853f38ea",
                        new Date(1581908100000L),
                        new Date(1581908400000L),
                        "https://build.company.com/demo/develop/night/54/",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/",
                        JobStatus.DONE,
                        Result.SUCCESS,
                        ExecutionAcceptance.NEW,
                        null,
                        1L,
                        "develop",
                        "day",
                        1,
                        true,
                        "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                        QualityStatus.INCOMPLETE,
                        "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":16,\"failed\":0,\"passed\":16},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":4,\"failed\":0,\"passed\":4},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":12,\"failed\":0,\"passed\":12},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"PASSED\"}]",
                        0L,
                        0L
                );

        assertThat(execution.getRuns())
                .hasSize(3)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        assertThat(execution.getCountryDeployments())
                .hasSize(1)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400001/de/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenCountryFolderNotFound() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581908400002.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution execution = executionRepository.findByProjectIdAndJobUrl(1L, "https://build.company.com/demo/develop/night/54/");

        assertThat(execution)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .contains(
                        "develop",
                        "day",
                        "v3",
                        "34910c9971abebce9f633920d8f8cf90853f38ea",
                        new Date(1581908100000L),
                        new Date(1581908400000L),
                        "https://build.company.com/demo/develop/night/54/",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/",
                        JobStatus.DONE,
                        Result.SUCCESS,
                        ExecutionAcceptance.NEW,
                        null,
                        1L,
                        "develop",
                        "day",
                        1,
                        true,
                        "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                        QualityStatus.INCOMPLETE,
                        "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":16,\"failed\":0,\"passed\":16},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":4,\"failed\":0,\"passed\":4},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":12,\"failed\":0,\"passed\":12},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"INCOMPLETE\"}]",
                        0L,
                        0L
                );

        assertThat(execution.getRuns())
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        assertThat(execution.getCountryDeployments())
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                null,
                                null,
                                null,
                                null
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400002/us/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenTypeNotFoundInDB() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581908400003.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution execution = executionRepository.findByProjectIdAndJobUrl(1L, "https://build.company.com/demo/develop/night/54/");

        assertThat(execution)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .contains(
                        "develop",
                        "day",
                        "v3",
                        "34910c9971abebce9f633920d8f8cf90853f38ea",
                        new Date(1581908100000L),
                        new Date(1581908400000L),
                        "https://build.company.com/demo/develop/night/54/",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/",
                        JobStatus.DONE,
                        Result.SUCCESS,
                        ExecutionAcceptance.NEW,
                        null,
                        1L,
                        "develop",
                        "day",
                        1,
                        true,
                        "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                        QualityStatus.INCOMPLETE,
                        "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":30,\"failed\":0,\"passed\":30},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":22,\"failed\":0,\"passed\":22},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":60,\"failed\":0,\"passed\":60},\"percent\":100,\"status\":\"PASSED\"}]",
                        0L,
                        0L
                );

        assertThat(execution.getRuns())
                .hasSize(5)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/57/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/58/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        assertThat(execution.getCountryDeployments())
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/55/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/web-service",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/web-service/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/web-service/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/web-service/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/web-service/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/web-service/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400003/fr/web-service/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenTypeFolderNotFound() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581908400004.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution execution = executionRepository.findByProjectIdAndJobUrl(1L, "https://build.company.com/demo/develop/night/54/");

        assertThat(execution)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .contains(
                        "develop",
                        "day",
                        "v3",
                        "34910c9971abebce9f633920d8f8cf90853f38ea",
                        new Date(1581908100000L),
                        new Date(1581908400000L),
                        "https://build.company.com/demo/develop/night/54/",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/",
                        JobStatus.DONE,
                        Result.SUCCESS,
                        ExecutionAcceptance.NEW,
                        null,
                        1L,
                        "develop",
                        "day",
                        1,
                        true,
                        "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                        QualityStatus.INCOMPLETE,
                        "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":30,\"failed\":0,\"passed\":30},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":22,\"failed\":0,\"passed\":22},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":60,\"failed\":0,\"passed\":60},\"percent\":100,\"status\":\"INCOMPLETE\"}]",
                        0L,
                        0L
                );

        assertThat(execution.getRuns())
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/57/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/58/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        assertThat(execution.getCountryDeployments())
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/55/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400004/fr/firefox-desktop/stepDefinitions.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenNoRulesFound() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581908400005.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution execution = executionRepository.findByProjectIdAndJobUrl(1L, "https://build.company.com/demo/develop/night/54/");

        assertThat(execution)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .contains(
                        "develop",
                        "day",
                        "v3",
                        "34910c9971abebce9f633920d8f8cf90853f38ea",
                        new Date(1581908100000L),
                        new Date(1581908400000L),
                        "https://build.company.com/demo/develop/night/54/",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/",
                        JobStatus.DONE,
                        Result.SUCCESS,
                        ExecutionAcceptance.NEW,
                        null,
                        1L,
                        "develop",
                        "day",
                        1,
                        true,
                        "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                        QualityStatus.PASSED,
                        "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":0,\"failed\":0,\"passed\":0},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":0,\"failed\":0,\"passed\":0},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":0,\"failed\":0,\"passed\":0},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":0,\"failed\":0,\"passed\":0},\"percent\":100,\"status\":\"PASSED\"}]",
                        0L,
                        0L
                );

        assertThat(execution.getRuns()).isEmpty();

        assertThat(execution.getCountryDeployments()).isEmpty();

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400005/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenATestTypesFieldIsMissingFromCycleDefinition() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581908400006.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution execution = executionRepository.findByProjectIdAndJobUrl(1L, "https://build.company.com/demo/develop/night/54/");

        assertThat(execution)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .contains(
                        "develop",
                        "day",
                        "v3",
                        "34910c9971abebce9f633920d8f8cf90853f38ea",
                        new Date(1581908100000L),
                        new Date(1581908400000L),
                        "https://build.company.com/demo/develop/night/54/",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/",
                        JobStatus.DONE,
                        Result.SUCCESS,
                        ExecutionAcceptance.NEW,
                        null,
                        1L,
                        "develop",
                        "day",
                        1,
                        true,
                        "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                        QualityStatus.PASSED,
                        "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":16,\"failed\":0,\"passed\":16},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":4,\"failed\":0,\"passed\":4},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":12,\"failed\":0,\"passed\":12},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"PASSED\"}]",
                        0L,
                        0L
                );

        assertThat(execution.getRuns())
                .hasSize(3)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        assertThat(execution.getCountryDeployments())
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/55/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400006/fr/api/reports/choose-a-product.postman_collection_all.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenCountryFolderIsEmptyButContainsBuildInformationFile() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581908400007.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution execution = executionRepository.findByProjectIdAndJobUrl(1L, "https://build.company.com/demo/develop/night/54/");

        assertThat(execution)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .contains(
                        "develop",
                        "day",
                        "v3",
                        "34910c9971abebce9f633920d8f8cf90853f38ea",
                        new Date(1581908100000L),
                        new Date(1581908400000L),
                        "https://build.company.com/demo/develop/night/54/",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/",
                        JobStatus.DONE,
                        Result.SUCCESS,
                        ExecutionAcceptance.NEW,
                        null,
                        1L,
                        "develop",
                        "day",
                        1,
                        true,
                        "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                        QualityStatus.INCOMPLETE,
                        "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":16,\"failed\":0,\"passed\":16},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":4,\"failed\":0,\"passed\":4},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":12,\"failed\":0,\"passed\":12},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"INCOMPLETE\"}]",
                        0L,
                        0L
                );

        assertThat(execution.getRuns())
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        assertThat(execution.getCountryDeployments())
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/55/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400007/fr/buildInformation.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenCountryFolderIsEmpty() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581908400008.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution execution = executionRepository.findByProjectIdAndJobUrl(1L, "https://build.company.com/demo/develop/night/54/");

        assertThat(execution)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .contains(
                        "develop",
                        "day",
                        "v3",
                        "34910c9971abebce9f633920d8f8cf90853f38ea",
                        new Date(1581908100000L),
                        new Date(1581908400000L),
                        "https://build.company.com/demo/develop/night/54/",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/",
                        JobStatus.DONE,
                        Result.SUCCESS,
                        ExecutionAcceptance.NEW,
                        null,
                        1L,
                        "develop",
                        "day",
                        1,
                        true,
                        "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                        QualityStatus.INCOMPLETE,
                        "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":16,\"failed\":0,\"passed\":16},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":4,\"failed\":0,\"passed\":4},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":12,\"failed\":0,\"passed\":12},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":32,\"failed\":0,\"passed\":32},\"percent\":100,\"status\":\"INCOMPLETE\"}]",
                        0L,
                        0L
                );

        assertThat(execution.getRuns())
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        assertThat(execution.getCountryDeployments())
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                null,
                                null,
                                null,
                                null
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400008/fr"
                );
    }

    @Test
    public void upload_saveTheExecution_whenNoCountryFolderFound() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581908400009.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution execution = executionRepository.findByProjectIdAndJobUrl(1L, "https://build.company.com/demo/develop/night/54/");

        assertThat(execution)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .contains(
                        "develop",
                        "day",
                        "v3",
                        "34910c9971abebce9f633920d8f8cf90853f38ea",
                        new Date(1581908100000L),
                        new Date(1581908400000L),
                        "https://build.company.com/demo/develop/night/54/",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400009/",
                        JobStatus.DONE,
                        Result.SUCCESS,
                        ExecutionAcceptance.NEW,
                        null,
                        1L,
                        "develop",
                        "day",
                        1,
                        true,
                        "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                        QualityStatus.INCOMPLETE,
                        "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":0,\"failed\":0,\"passed\":0},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":0,\"failed\":0,\"passed\":0},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":0,\"failed\":0,\"passed\":0},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":0,\"failed\":0,\"passed\":0},\"percent\":100,\"status\":\"INCOMPLETE\"}]",
                        0L,
                        0L
                );

        assertThat(execution.getRuns())
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        )
                );

        assertThat(execution.getCountryDeployments())
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                null,
                                null,
                                null,
                                null
                        ),
                        tuple(
                                "us",
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                null,
                                null,
                                null,
                                null
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400009",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400009/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400009/buildInformation.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenTypeFolderEmptyWithBuildInformationFile() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581908400010.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution execution = executionRepository.findByProjectIdAndJobUrl(1L, "https://build.company.com/demo/develop/night/54/");

        assertThat(execution)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .contains(
                        "develop",
                        "day",
                        "v3",
                        "34910c9971abebce9f633920d8f8cf90853f38ea",
                        new Date(1581908100000L),
                        new Date(1581908400000L),
                        "https://build.company.com/demo/develop/night/54/",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/",
                        JobStatus.DONE,
                        Result.SUCCESS,
                        ExecutionAcceptance.NEW,
                        null,
                        1L,
                        "develop",
                        "day",
                        1,
                        true,
                        "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                        QualityStatus.INCOMPLETE,
                        "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":30,\"failed\":0,\"passed\":30},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":22,\"failed\":0,\"passed\":22},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":60,\"failed\":0,\"passed\":60},\"percent\":100,\"status\":\"INCOMPLETE\"}]",
                        0L,
                        0L
                );

        assertThat(execution.getRuns())
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/56/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/57/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/58/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        assertThat(execution.getCountryDeployments())
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/55/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400010/fr/api/buildInformation.json"
                );
    }

    @Test
    public void upload_saveTheExecution_whenTypeFolderIsEmpty() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581908400011.zip");
        executionResource.upload("the-demo-project", "develop", "day", zip);

        Execution execution = executionRepository.findByProjectIdAndJobUrl(1L, "https://build.company.com/demo/develop/night/54/");

        assertThat(execution)
                .extracting(
                        "branch",
                        "name",
                        "release",
                        "version",
                        "buildDateTime",
                        "testDateTime",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "acceptance",
                        "discardReason",
                        "cycleDefinition.projectId",
                        "cycleDefinition.branch",
                        "cycleDefinition.name",
                        "cycleDefinition.branchPosition",
                        "blockingValidation",
                        "qualityThresholds",
                        "qualityStatus",
                        "qualitySeverities",
                        "duration",
                        "estimatedDuration"
                )
                .contains(
                        "develop",
                        "day",
                        "v3",
                        "34910c9971abebce9f633920d8f8cf90853f38ea",
                        new Date(1581908100000L),
                        new Date(1581908400000L),
                        "https://build.company.com/demo/develop/night/54/",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/",
                        JobStatus.DONE,
                        Result.SUCCESS,
                        ExecutionAcceptance.NEW,
                        null,
                        1L,
                        "develop",
                        "day",
                        1,
                        true,
                        "{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}",
                        QualityStatus.INCOMPLETE,
                        "[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":30,\"failed\":0,\"passed\":30},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":8,\"failed\":0,\"passed\":8},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":22,\"failed\":0,\"passed\":22},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":60,\"failed\":0,\"passed\":60},\"percent\":100,\"status\":\"INCOMPLETE\"}]",
                        0L,
                        0L
                );

        assertThat(execution.getRuns())
                .hasSize(6)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                null,
                                null,
                                JobStatus.UNAVAILABLE,
                                "all",
                                null,
                                null,
                                null,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/57/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "fr",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/58/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "api",
                                "api",
                                Technology.POSTMAN,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/60/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/api/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-desktop",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/61/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/firefox-desktop/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        ),
                        tuple(
                                "us",
                                1L,
                                "firefox-mobile",
                                "web",
                                Technology.CUCUMBER,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/62/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/firefox-mobile/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581908400000L),
                                0L,
                                0L,
                                "all",
                                true
                        )
                );

        assertThat(execution.getCountryDeployments())
                .hasSize(2)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/55/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        ),
                        tuple(
                                "us",
                                "integ",
                                "https://build.company.com/demo/deploy/us/59/",
                                "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581908400000L),
                                0L,
                                0L
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/develop",
                        "/opt/ara/data/executions/the-demo-project/develop/day",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/api",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/api/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/api/reports",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/api/reports/result.txt",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/api/reports/pay.postman_collection_fr+us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/api/reports/pay.postman_collection_us.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/us/api/reports/choose-a-product.postman_collection_all.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr/firefox-mobile",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr/firefox-mobile/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr/firefox-mobile/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr/firefox-mobile/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr/firefox-desktop",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr/firefox-desktop/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr/firefox-desktop/report.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr/firefox-desktop/stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/develop/day/incoming/1581908400011/fr/api"
                );
    }

    @Test
    public void upload_saveTheCypressExecution_whenCucumberReport() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581926400000.zip");
        executionResource.upload("the-demo-project", "master", "day", zip);

        Execution execution = executionRepository.findByProjectIdAndJobUrl(1L, "https://build.company.com/demo/master/day/81/");

        assertThat(execution.getBranch()).isEqualTo("master");
        assertThat(execution.getName()).isEqualTo("day");
        assertThat(execution.getRelease()).isEqualTo("v2");
        assertThat(execution.getVersion()).isEqualTo("version-2");
        assertThat(execution.getBuildDateTime()).isEqualTo(new Date(1581926100000L));
        assertThat(execution.getTestDateTime()).isEqualTo(new Date(1581926400000L));
        assertThat(execution.getJobUrl()).isEqualTo("https://build.company.com/demo/master/day/81/");
        assertThat(execution.getJobLink()).isEqualTo("/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/");
        assertThat(execution.getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(execution.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(execution.getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
        assertThat(execution.getDiscardReason()).isNull();
        assertThat(execution.getCycleDefinition().getProjectId()).isEqualTo(1L);
        assertThat(execution.getCycleDefinition().getBranch()).isEqualTo("master");
        assertThat(execution.getCycleDefinition().getName()).isEqualTo("day");
        assertThat(execution.getCycleDefinition().getBranchPosition()).isEqualTo(2);
        assertThat(execution.getBlockingValidation()).isEqualTo(true);
        assertThat(execution.getQualityThresholds()).isEqualTo("{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}");
        assertThat(execution.getQualityStatus()).isEqualTo(QualityStatus.FAILED);
        assertThat(execution.getQualitySeverities()).isEqualTo("[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":2,\"failed\":2,\"passed\":0},\"percent\":0,\"status\":\"FAILED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":0,\"failed\":0,\"passed\":0},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":1,\"failed\":0,\"passed\":1},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":3,\"failed\":2,\"passed\":1},\"percent\":33,\"status\":\"FAILED\"}]");
        assertThat(execution.getDuration()).isEqualTo(100L);
        assertThat(execution.getEstimatedDuration()).isEqualTo(230L);

        runs = new ArrayList<>(execution.getRuns());

        assertThat(runs)
                .hasSize(1)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "cypress-front",
                                "front",
                                Technology.CYPRESS,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/83/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/fr/cypress-front/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581926400000L),
                                50L,
                                30L,
                                "all",
                                true
                        )
                );

        executedScenarios = new ArrayList<>(runs.get(0).getExecutedScenarios());

        assertThat(executedScenarios)
                .hasSize(3)
                .extracting(
                        "featureFile",
                        "featureName",
                        "featureTags",
                        "tags",
                        "severity",
                        "name",
                        "cucumberId",
                        "line",
                        "content",
                        "startDateTime",
                        "screenshotUrl",
                        "videoUrl",
                        "logsUrl",
                        "httpRequestsUrl",
                        "javaScriptErrorsUrl",
                        "diffReportUrl",
                        "cucumberReportUrl",
                        "apiServer",
                        "seleniumNode"
                )
                .containsOnly(
                        tuple(
                                "user-journey-executions-and-errors.feature",
                                "Journey",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Check Runs",
                                "journey;check-runs",
                                23,
                                String.format(
                                        "4:passed:2553000000:Given executions and errors%n"+
                                                "25:passed:587000000:When on the executions and errors page, in the cart \"5\", the user clicks on the run \"fr_api\"%n"+
                                                "26:failed:4545000000:Then on the executions and errors page, in the cart \"5\", on the run \"fr_api\", the team \"28\" is visible%n"+
                                                "27:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_api\", the team \"27\" is visible%n"+
                                                "28:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"26\" is hidden%n"+
                                                "29:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"27\" is hidden%n"+
                                                "30:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"28\" is hidden%n"+
                                                "31:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"29\" is hidden%n"+
                                                "32:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"-404\" is hidden%n"+
                                                "35:skipped:0:When on the executions and errors page, in the cart \"5\", the user clicks on the run \"fr_desktop\"%n"+
                                                "36:skipped:0:Then on the executions and errors page, in the cart \"5\", on the run \"fr_api\", the team \"28\" is visible%n"+
                                                "37:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_api\", the team \"27\" is visible%n"+
                                                "38:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"26\" is visible%n"+
                                                "39:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"27\" is visible%n"+
                                                "40:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"28\" is visible%n"+
                                                "41:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"29\" is visible%n"+
                                                "42:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"-404\" is visible%n"+
                                                "45:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_api\", in the column \"sanity-check\", the number of ok is \"2\", the number of problem is \"0\", the number of ko is \"0\", the progress bar is 100%% of success, 0%% of unhandled and 0%% of failed%n"+
                                                "50:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"sanity-check\", the quality is \"100\", the number of OK is \"9\", the number of KO is \"0\", the color is \"green\"%n"+
                                                "51:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"high\", the quality is \"100\", the number of OK is \"2\", the number of KO is \"0\", the color is \"green\"%n"+
                                                "52:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"medium\", the quality is \"100\", the number of OK is \"7\", the number of KO is \"0\", the color is \"green\"%n"+
                                                "53:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"*\", the quality is \"100\", the number of OK is \"18\", the number of KO is \"0\", the color is \"none\"%n"+
                                                "56:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"sanity-check\", the threshold is \"100\", the color is \"none\"%n"+
                                                "57:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"high\", the threshold is \"95\", the color is \"none\"%n"+
                                                "58:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"medium\", the threshold is \"90\", the color is \"none\""
                                ),
                                null,
                                "https://media.your-company.com/content/images/snapshot3.png",
                                "https://media.your-company.com/content/videos/videos2.mp4",
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/83/cucumber-html-reports/report-feature_user-journey-executions-and-errors-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "user-journey-executions-and-errors.feature",
                                "Journey",
                                "",
                                "@severity-medium",
                                "medium",
                                "Check the Actions Buttons",
                                "journey;check-the-actions-buttons",
                                7,
                                String.format(
                                        "4:passed:4216000000:Given executions and errors%n"+
                                                "8:passed:790000000:When on the executions and errors page, the user clicks on the actions and job reports button \"5\"%n"+
                                                "9:passed:128000000:Then on the executions and errors page, in the actions and job reports list, the \"Actions\" button \"5\" is visible%n"+
                                                "10:passed:133000000:And on the executions and errors page, in the actions and job reports list, the \"Actions\" button \"5\" is disabled%n"+
                                                "11:passed:425000000:And on the executions and errors page, in the actions and job reports list, the \"JobReports\" button \"5\" is visible%n"+
                                                "12:passed:143000000:And on the executions and errors page, in the actions and job reports list, the \"JobReports\" button \"5\" is disabled%n"+
                                                "13:passed:268000000:And on the executions and errors page, in the actions and job reports list, the \"Execution\" button \"5\" is visible%n"+
                                                "14:passed:228000000:And on the executions and errors page, in the actions and job reports list, the \"Execution\" button \"5\" is enabled%n"+
                                                "15:passed:250000000:And on the executions and errors page, in the actions and job reports list, the \"fr_Deployment\" button \"5\" is visible%n"+
                                                "16:passed:156000000:And on the executions and errors page, in the actions and job reports list, the \"fr_Deployment\" button \"5\" is enabled%n"+
                                                "17:passed:249000000:And on the executions and errors page, in the actions and job reports list, the \"fr_api\" button \"5\" is visible%n"+
                                                "18:passed:103000000:And on the executions and errors page, in the actions and job reports list, the \"fr_api\" button \"5\" is enabled%n"+
                                                "19:passed:153000000:And on the executions and errors page, in the actions and job reports list, the \"fr_desktop\" button \"5\" is visible%n"+
                                                "20:passed:324000000:And on the executions and errors page, in the actions and job reports list, the \"fr_desktop\" button \"5\" is enabled"
                                ),
                                null,
                                "https://media.your-company.com/content/images/snapshot2.png",
                                "https://media.your-company.com/content/videos/videos2.mp4",
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/83/cucumber-html-reports/report-feature_user-journey-executions-and-errors-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "user-journey.feature",
                                "Journey",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Check the status of a build",
                                "journey;check-the-status-of-a-build",
                                4,
                                String.format(
                                        "5:failed:9931000000:Given an user with a demo project%n"+
                                                "6:skipped:0:When the user goes to the home page%n"+
                                                "7:skipped:0:Then the top menu is present"
                                ),
                                null,
                                "https://media.your-company.com/content/images/snapshot1.png",
                                "https://media.your-company.com/content/videos/videos1.mp4",
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/83/cucumber-html-reports/report-feature_user-journey-feature.html",
                                null,
                                null
                        )
                );

        countryDeployments = new ArrayList<>(execution.getCountryDeployments());

        assertThat(countryDeployments)
                .hasSize(1)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/82/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581926400000L),
                                90L,
                                170L
                        )
                );

        errors = runs.stream()
                .map(Run::getExecutedScenarios)
                .flatMap(Collection::stream)
                .map(ExecutedScenario::getErrors)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(errors)
                .hasSize(2)
                .extracting(
                        "executedScenario.name",
                        "step",
                        "stepDefinition",
                        "stepLine",
                        "exception"
                )
                .containsOnly(
                        tuple(
                                "Check Runs",
                                "on the executions and errors page, in the cart \"5\", on the run \"fr_api\", the team \"28\" is visible",
                                "^on the executions and errors page, in the cart \"([^\"]*)\", on the run \"([^\"]*)\", the team \"([^\"]*)\" is visible$",
                                26,
                                String.format(
                                        "AssertionError: Timed out retrying: Expected to find element: `[data-nrt='executions_CartRowSubTitle_fr_api_28_5']`, but never found it.%n"+
                                                "    + expected - actual%n%n%n"+
                                                "    at Context.eval (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey-executions-and-errors.feature:65845:61)%n"+
                                                "    at Context.resolveAndRunStepDefinition (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey-executions-and-errors.feature:24567:9)%n"+
                                                "    at Context.eval (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey-executions-and-errors.feature:23936:35)"
                                )
                        ),
                        tuple(
                                "Check the status of a build",
                                "an user with a demo project",
                                "^an user with a demo project$",
                                5,
                                String.format(
                                        "AssertionError: Timed out retrying: Expected to find element: `[data-nrt=deleteDemo]`, but never found it.%n"+
                                                "    + expected - actual%n%n%n"+
                                                "    at Object.reset (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey.feature:65729:50)%n"+
                                                "    at Context.eval (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey.feature:65946:8)%n"+
                                                "    at Context.resolveAndRunStepDefinition (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey.feature:24567:9)%n"+
                                                "    at Context.eval (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey.feature:23936:35)"
                                )
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/master",
                        "/opt/ara/data/executions/the-demo-project/master/day",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/fr",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/fr/cypress-front",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/fr/cypress-front/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/fr/cypress-front/stepDefinitions",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/fr/cypress-front/stepDefinitions/user-journey-executions-and-errors.stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/fr/cypress-front/stepDefinitions/user-journey.stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/fr/cypress-front/reports",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/fr/cypress-front/reports/cucumber",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/fr/cypress-front/reports/cucumber/user-journey.cucumber.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926400000/fr/cypress-front/reports/cucumber/user-journey-executions-and-errors.cucumber.json"
                );
    }

    @Test
    public void upload_saveTheCypressExecution_whenCucumberReportMissAStepDefinitions() throws IOException {
        deleteARADataFolder();
        settingService.clearProjectsValuesCache();

        List<Execution> executions = executionRepository.findAll();
        List<CountryDeployment> countryDeployments = countryDeploymentRepository.findAll();
        List<Run> runs = runRepository.findAll();
        List<ExecutedScenario> executedScenarios = executedScenarioRepository.findAll();
        List<Error> errors = errorRepository.findAll();

        assertThat(executions).isEmpty();
        assertThat(runs).isEmpty();
        assertThat(countryDeployments).isEmpty();
        assertThat(executedScenarios).isEmpty();
        assertThat(errors).isEmpty();

        MultipartFile zip = readZip("src/test/resources/zip/1581926500000.zip");
        executionResource.upload("the-demo-project", "master", "day", zip);

        Execution execution = executionRepository.findByProjectIdAndJobUrl(1L, "https://build.company.com/demo/master/day/81/");

        assertThat(execution.getBranch()).isEqualTo("master");
        assertThat(execution.getName()).isEqualTo("day");
        assertThat(execution.getRelease()).isEqualTo("v2");
        assertThat(execution.getVersion()).isEqualTo("version-2");
        assertThat(execution.getBuildDateTime()).isEqualTo(new Date(1581926100000L));
        assertThat(execution.getTestDateTime()).isEqualTo(new Date(1581926400000L));
        assertThat(execution.getJobUrl()).isEqualTo("https://build.company.com/demo/master/day/81/");
        assertThat(execution.getJobLink()).isEqualTo("/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/");
        assertThat(execution.getStatus()).isEqualTo(JobStatus.DONE);
        assertThat(execution.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(execution.getAcceptance()).isEqualTo(ExecutionAcceptance.NEW);
        assertThat(execution.getDiscardReason()).isNull();
        assertThat(execution.getCycleDefinition().getProjectId()).isEqualTo(1L);
        assertThat(execution.getCycleDefinition().getBranch()).isEqualTo("master");
        assertThat(execution.getCycleDefinition().getName()).isEqualTo("day");
        assertThat(execution.getCycleDefinition().getBranchPosition()).isEqualTo(2);
        assertThat(execution.getBlockingValidation()).isEqualTo(true);
        assertThat(execution.getQualityThresholds()).isEqualTo("{\"sanity-check\":{\"failure\":100,\"warning\":100},\"high\":{\"failure\":95,\"warning\":98},\"medium\":{\"failure\":90,\"warning\":95}}");
        assertThat(execution.getQualityStatus()).isEqualTo(QualityStatus.FAILED);
        assertThat(execution.getQualitySeverities()).isEqualTo("[{\"severity\":{\"code\":\"sanity-check\",\"position\":1,\"name\":\"Sanity Check\",\"shortName\":\"Sanity Ch.\",\"initials\":\"S.C.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":2,\"failed\":2,\"passed\":0},\"percent\":0,\"status\":\"FAILED\"},{\"severity\":{\"code\":\"high\",\"position\":2,\"name\":\"High\",\"shortName\":\"High\",\"initials\":\"High\",\"defaultOnMissing\":true},\"scenarioCounts\":{\"total\":0,\"failed\":0,\"passed\":0},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"medium\",\"position\":3,\"name\":\"Medium\",\"shortName\":\"Medium\",\"initials\":\"Med.\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":1,\"failed\":0,\"passed\":1},\"percent\":100,\"status\":\"PASSED\"},{\"severity\":{\"code\":\"*\",\"position\":2147483647,\"name\":\"Global\",\"shortName\":\"Global\",\"initials\":\"Global\",\"defaultOnMissing\":false},\"scenarioCounts\":{\"total\":3,\"failed\":2,\"passed\":1},\"percent\":33,\"status\":\"FAILED\"}]");
        assertThat(execution.getDuration()).isEqualTo(100L);
        assertThat(execution.getEstimatedDuration()).isEqualTo(230L);

        runs = new ArrayList<>(execution.getRuns());

        assertThat(runs)
                .hasSize(1)
                .extracting(
                        "country.code",
                        "type.projectId",
                        "type.code",
                        "type.source.code",
                        "type.source.technology",
                        "comment",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "countryTags",
                        "startDateTime",
                        "estimatedDuration",
                        "duration",
                        "severityTags",
                        "includeInThresholds"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                1L,
                                "cypress-front",
                                "front",
                                Technology.CYPRESS,
                                null,
                                "integ",
                                "https://build.company.com/demo/test/83/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/fr/cypress-front/",
                                JobStatus.DONE,
                                "all",
                                new Date(1581926400000L),
                                50L,
                                30L,
                                "all",
                                true
                        )
                );

        executedScenarios = new ArrayList<>(runs.get(0).getExecutedScenarios());

        assertThat(executedScenarios)
                .hasSize(3)
                .extracting(
                        "featureFile",
                        "featureName",
                        "featureTags",
                        "tags",
                        "severity",
                        "name",
                        "cucumberId",
                        "line",
                        "content",
                        "startDateTime",
                        "screenshotUrl",
                        "videoUrl",
                        "logsUrl",
                        "httpRequestsUrl",
                        "javaScriptErrorsUrl",
                        "diffReportUrl",
                        "cucumberReportUrl",
                        "apiServer",
                        "seleniumNode"
                )
                .containsOnly(
                        tuple(
                                "user-journey-executions-and-errors.feature",
                                "Journey",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Check Runs",
                                "journey;check-runs",
                                23,
                                String.format(
                                        "4:passed:2553000000:Given executions and errors%n"+
                                                "25:passed:587000000:When on the executions and errors page, in the cart \"5\", the user clicks on the run \"fr_api\"%n"+
                                                "26:failed:4545000000:Then on the executions and errors page, in the cart \"5\", on the run \"fr_api\", the team \"28\" is visible%n"+
                                                "27:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_api\", the team \"27\" is visible%n"+
                                                "28:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"26\" is hidden%n"+
                                                "29:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"27\" is hidden%n"+
                                                "30:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"28\" is hidden%n"+
                                                "31:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"29\" is hidden%n"+
                                                "32:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"-404\" is hidden%n"+
                                                "35:skipped:0:When on the executions and errors page, in the cart \"5\", the user clicks on the run \"fr_desktop\"%n"+
                                                "36:skipped:0:Then on the executions and errors page, in the cart \"5\", on the run \"fr_api\", the team \"28\" is visible%n"+
                                                "37:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_api\", the team \"27\" is visible%n"+
                                                "38:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"26\" is visible%n"+
                                                "39:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"27\" is visible%n"+
                                                "40:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"28\" is visible%n"+
                                                "41:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"29\" is visible%n"+
                                                "42:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_desktop\", the team \"-404\" is visible%n"+
                                                "45:skipped:0:And on the executions and errors page, in the cart \"5\", on the run \"fr_api\", in the column \"sanity-check\", the number of ok is \"2\", the number of problem is \"0\", the number of ko is \"0\", the progress bar is 100%% of success, 0%% of unhandled and 0%% of failed%n"+
                                                "50:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"sanity-check\", the quality is \"100\", the number of OK is \"9\", the number of KO is \"0\", the color is \"green\"%n"+
                                                "51:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"high\", the quality is \"100\", the number of OK is \"2\", the number of KO is \"0\", the color is \"green\"%n"+
                                                "52:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"medium\", the quality is \"100\", the number of OK is \"7\", the number of KO is \"0\", the color is \"green\"%n"+
                                                "53:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"*\", the quality is \"100\", the number of OK is \"18\", the number of KO is \"0\", the color is \"none\"%n"+
                                                "56:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"sanity-check\", the threshold is \"100\", the color is \"none\"%n"+
                                                "57:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"high\", the threshold is \"95\", the color is \"none\"%n"+
                                                "58:skipped:0:And on the executions and errors page, in the cart \"5\", on the header, in the column \"medium\", the threshold is \"90\", the color is \"none\""
                                ),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/83/cucumber-html-reports/report-feature_user-journey-executions-and-errors-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "user-journey-executions-and-errors.feature",
                                "Journey",
                                "",
                                "@severity-medium",
                                "medium",
                                "Check the Actions Buttons",
                                "journey;check-the-actions-buttons",
                                7,
                                String.format(
                                        "4:passed:4216000000:Given executions and errors%n"+
                                                "8:passed:790000000:When on the executions and errors page, the user clicks on the actions and job reports button \"5\"%n"+
                                                "9:passed:128000000:Then on the executions and errors page, in the actions and job reports list, the \"Actions\" button \"5\" is visible%n"+
                                                "10:passed:133000000:And on the executions and errors page, in the actions and job reports list, the \"Actions\" button \"5\" is disabled%n"+
                                                "11:passed:425000000:And on the executions and errors page, in the actions and job reports list, the \"JobReports\" button \"5\" is visible%n"+
                                                "12:passed:143000000:And on the executions and errors page, in the actions and job reports list, the \"JobReports\" button \"5\" is disabled%n"+
                                                "13:passed:268000000:And on the executions and errors page, in the actions and job reports list, the \"Execution\" button \"5\" is visible%n"+
                                                "14:passed:228000000:And on the executions and errors page, in the actions and job reports list, the \"Execution\" button \"5\" is enabled%n"+
                                                "15:passed:250000000:And on the executions and errors page, in the actions and job reports list, the \"fr_Deployment\" button \"5\" is visible%n"+
                                                "16:passed:156000000:And on the executions and errors page, in the actions and job reports list, the \"fr_Deployment\" button \"5\" is enabled%n"+
                                                "17:passed:249000000:And on the executions and errors page, in the actions and job reports list, the \"fr_api\" button \"5\" is visible%n"+
                                                "18:passed:103000000:And on the executions and errors page, in the actions and job reports list, the \"fr_api\" button \"5\" is enabled%n"+
                                                "19:passed:153000000:And on the executions and errors page, in the actions and job reports list, the \"fr_desktop\" button \"5\" is visible%n"+
                                                "20:passed:324000000:And on the executions and errors page, in the actions and job reports list, the \"fr_desktop\" button \"5\" is enabled"
                                ),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/83/cucumber-html-reports/report-feature_user-journey-executions-and-errors-feature.html",
                                null,
                                null
                        ),
                        tuple(
                                "user-journey.feature",
                                "Journey",
                                "",
                                "@severity-sanity-check",
                                "sanity-check",
                                "Check the status of a build",
                                "journey;check-the-status-of-a-build",
                                4,
                                String.format(
                                        "5:failed:9931000000:Given an user with a demo project%n"+
                                                "6:skipped:0:When the user goes to the home page%n"+
                                                "7:skipped:0:Then the top menu is present"
                                ),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "https://build.company.com/demo/test/83/cucumber-html-reports/report-feature_user-journey-feature.html",
                                null,
                                null
                        )
                );

        countryDeployments = new ArrayList<>(execution.getCountryDeployments());

        assertThat(countryDeployments)
                .hasSize(1)
                .extracting(
                        "country.code",
                        "platform",
                        "jobUrl",
                        "jobLink",
                        "status",
                        "result",
                        "startDateTime",
                        "estimatedDuration",
                        "duration"
                )
                .containsOnly(
                        tuple(
                                "fr",
                                "integ",
                                "https://build.company.com/demo/deploy/fr/82/",
                                "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/fr/",
                                JobStatus.DONE,
                                Result.SUCCESS,
                                new Date(1581926400000L),
                                90L,
                                170L
                        )
                );

        errors = runs.stream()
                .map(Run::getExecutedScenarios)
                .flatMap(Collection::stream)
                .map(ExecutedScenario::getErrors)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(errors)
                .hasSize(2)
                .extracting(
                        "executedScenario.name",
                        "step",
                        "stepDefinition",
                        "stepLine",
                        "exception"
                )
                .containsOnly(
                        tuple(
                                "Check Runs",
                                "on the executions and errors page, in the cart \"5\", on the run \"fr_api\", the team \"28\" is visible",
                                "^on the executions and errors page, in the cart \"([^\"]*)\", on the run \"([^\"]*)\", the team \"([^\"]*)\" is visible$",
                                26,
                                String.format(
                                        "AssertionError: Timed out retrying: Expected to find element: `[data-nrt='executions_CartRowSubTitle_fr_api_28_5']`, but never found it.%n"+
                                                "    + expected - actual%n%n%n"+
                                                "    at Context.eval (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey-executions-and-errors.feature:65845:61)%n"+
                                                "    at Context.resolveAndRunStepDefinition (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey-executions-and-errors.feature:24567:9)%n"+
                                                "    at Context.eval (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey-executions-and-errors.feature:23936:35)"
                                )
                        ),
                        tuple(
                                "Check the status of a build",
                                "an user with a demo project",
                                "^an user with a demo project$",
                                5,
                                String.format(
                                        "AssertionError: Timed out retrying: Expected to find element: `[data-nrt=deleteDemo]`, but never found it.%n"+
                                                "    + expected - actual%n%n%n"+
                                                "    at Object.reset (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey.feature:65729:50)%n"+
                                                "    at Context.eval (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey.feature:65946:8)%n"+
                                                "    at Context.resolveAndRunStepDefinition (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey.feature:24567:9)%n"+
                                                "    at Context.eval (http://localhost:8081/__cypress/tests?p=test/cypress/scenarii/user-journey.feature:23936:35)"
                                )
                        )
                );

        List<String> generatedFilesPaths = getARADataFilesAndFoldersPaths();
        assertThat(generatedFilesPaths)
                .contains(
                        "/opt/ara/data",
                        "/opt/ara/data/executions",
                        "/opt/ara/data/executions/the-demo-project",
                        "/opt/ara/data/executions/the-demo-project/master",
                        "/opt/ara/data/executions/the-demo-project/master/day",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/cycleDefinition.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/fr",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/fr/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/fr/cypress-front",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/fr/cypress-front/buildInformation.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/fr/cypress-front/stepDefinitions",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/fr/cypress-front/stepDefinitions/user-journey-executions-and-errors.stepDefinitions.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/fr/cypress-front/reports",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/fr/cypress-front/reports/cucumber",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/fr/cypress-front/reports/cucumber/user-journey.cucumber.json",
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/fr/cypress-front/reports/cucumber/user-journey-executions-and-errors.cucumber.json"
                )
                .doesNotContain(
                        "/opt/ara/data/executions/the-demo-project/master/day/incoming/1581926500000/fr/cypress-front/stepDefinitions/user-journey.stepDefinitions.json"
                );
    }
}
