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

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.*;
import com.decathlon.ara.domain.enumeration.*;
import com.decathlon.ara.repository.*;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


@RunWith(SpringRunner.class)
@SpringBootTest
@TransactionalSpringIntegrationTest
@ActiveProfiles("test")
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
                                String.format("-100000:passed:<Pre-Request Script>%n-1:passed:788000000:POST {{baseUrl}}/post%n0:passed:Status code is 200%n1:passed:Response should validate the payment method is indeed By card%n2:passed:Response should indicate a succeed transaction status%n100000:passed:<Test Script>"),// TODO why this has changed ?
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
                                "^the cart page shows (\\d+) products$",
                                22,
                                String.format("java.lang.AssertionError: expected:<[5]> but was:<[1]>%n\tat ara.demo.CartGlue.the_cart_page_shows_products(CartGlue.java:44)%n\tat ✽.Then the cart page shows 5 products(ara/demo/features/buy-a-product.feature:22)%n")
                        ),
                        tuple(
                                "Functionality 2104: Show cart, lots of products",
                                "the cart page shows 1000 products",
                                "^the cart page shows (\\d+) products$",
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
}
