package com.decathlon.ara.loader;

import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.service.FunctionalityService;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import com.decathlon.ara.service.dto.request.FunctionalityPosition;
import com.decathlon.ara.service.dto.request.NewFunctionalityDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Load functionalities into the Demo project.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DemoFunctionalityLoader {

    private static final String ALL_COUNTRY_CODES = "fr,us";

    @NonNull
    private final FunctionalityService functionalityService;

    public Map<String, Long> createFunctionalityTree(long projectId, List<TeamDTO> teams)
            throws BadRequestException {

        Long marketingTeamId = teams.get(0).getId();
        Long catalogTeamId = teams.get(1).getId();
        Long buyTeamId = teams.get(2).getId();
        Long accountTeamId = teams.get(3).getId();

        Map<String, Long> functionalityIds = new HashMap<>();

        Long chooseAProductFolderId = createFolder(projectId, null, "Choose a product");
        functionalityIds.put("Marketing-Home", createFunctionality(projectId, chooseAProductFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "Have a friendly home page",
                        ALL_COUNTRY_CODES,
                        marketingTeamId,
                        FunctionalitySeverity.LOW.name(),
                        "v1",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "Everybody love carousels! http://shouldiuseacarousel.com/")));
        functionalityIds.put("Catalog-List", createFunctionality(projectId, chooseAProductFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "List all our useless products",
                        ALL_COUNTRY_CODES,
                        catalogTeamId,
                        FunctionalitySeverity.HIGH.name(),
                        "v1",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "")));
        functionalityIds.put("Catalog-Details", createFunctionality(projectId, chooseAProductFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "Show a product with irresistible details",
                        ALL_COUNTRY_CODES,
                        catalogTeamId,
                        FunctionalitySeverity.MEDIUM.name(),
                        "v1",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "Including its reviews")));
        functionalityIds.put("Marketing-Sales", createFunctionality(projectId, chooseAProductFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "Sales Price on product details page",
                        ALL_COUNTRY_CODES,
                        marketingTeamId,
                        FunctionalitySeverity.MEDIUM.name(),
                        "v2",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "")));

        Long buyAProductFolderId = createFolder(projectId, null, "Buy a product");
        functionalityIds.put("Buy-Add", createFunctionality(projectId, buyAProductFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "Add a product to cart",
                        ALL_COUNTRY_CODES,
                        buyTeamId,
                        FunctionalitySeverity.HIGH.name(),
                        "v1",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "")));
        functionalityIds.put("Buy-Cart", createFunctionality(projectId, buyAProductFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "Show cart",
                        ALL_COUNTRY_CODES,
                        buyTeamId,
                        FunctionalitySeverity.HIGH.name(),
                        "v1",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "")));

        Long deliveryChoosingFolderId = createFolder(projectId, buyAProductFolderId, "Delivery");
        functionalityIds.put("Buy-Delivery", createFunctionality(projectId, deliveryChoosingFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "Choose delivery option",
                        ALL_COUNTRY_CODES,
                        buyTeamId,
                        FunctionalitySeverity.HIGH.name(),
                        "v1",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "")));
        functionalityIds.put("Buy-Pigeon", createFunctionality(projectId, deliveryChoosingFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "By pigeon",
                        "fr",
                        buyTeamId,
                        FunctionalitySeverity.HIGH.name(),
                        "v1",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "We innovate!")));
        functionalityIds.put("Buy-3D", createFunctionality(projectId, deliveryChoosingFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "By 3D Printing",
                        "us",
                        buyTeamId,
                        FunctionalitySeverity.LOW.name(),
                        "v3",
                        Boolean.TRUE, // Started
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "Started writing Gherkins scenario, " +
                                "but we need to be able to create small products to be able to test that.")));
        functionalityIds.put("Buy-Drone", createFunctionality(projectId, deliveryChoosingFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "By drone air-drop",
                        ALL_COUNTRY_CODES,
                        buyTeamId,
                        FunctionalitySeverity.MEDIUM.name(),
                        "v2",
                        Boolean.FALSE,
                        Boolean.TRUE, // Not automatable
                        null,
                        null,
                        null,
                        null,
                        "Too complicated to click that button by a machine: " +
                                "will test manually for a better ROI.")));

        Long payFolderId = createFolder(projectId, buyAProductFolderId, "Pay");
        functionalityIds.put("Buy-Pay", createFunctionality(projectId, payFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "Choose payment option",
                        ALL_COUNTRY_CODES,
                        buyTeamId,
                        FunctionalitySeverity.HIGH.name(),
                        "v1",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "")));
        functionalityIds.put("Buy-Card", createFunctionality(projectId, payFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "By card",
                        ALL_COUNTRY_CODES,
                        buyTeamId,
                        FunctionalitySeverity.HIGH.name(),
                        "v1",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "")));
        functionalityIds.put("Buy-Gift", createFunctionality(projectId, payFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "By gift card",
                        ALL_COUNTRY_CODES,
                        buyTeamId,
                        FunctionalitySeverity.MEDIUM.name(),
                        "v2",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "")));
        functionalityIds.put("Buy-NFC", createFunctionality(projectId, payFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "By mobile NFC",
                        ALL_COUNTRY_CODES,
                        buyTeamId,
                        FunctionalitySeverity.LOW.name(),
                        "v3",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "")));
        functionalityIds.put("Buy-Barter", createFunctionality(projectId, payFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "By barter",
                        "fr",
                        buyTeamId,
                        FunctionalitySeverity.LOW.name(),
                        "v3",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "Innovating again: we will become the new Amazon! Partnering with leboncoin.fr")));

        Long accountFolderId = createFolder(projectId, null, "Account");
        functionalityIds.put("Account-Create", createFunctionality(projectId, accountFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "Create account",
                        ALL_COUNTRY_CODES,
                        accountTeamId,
                        FunctionalitySeverity.HIGH.name(),
                        "v1",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "")));
        functionalityIds.put("Account-Login", createFunctionality(projectId, accountFolderId,
                new FunctionalityDTO(
                        null,
                        null,
                        0,
                        FunctionalityType.FUNCTIONALITY.name(),
                        "Log in",
                        ALL_COUNTRY_CODES,
                        accountTeamId,
                        FunctionalitySeverity.HIGH.name(),
                        "v1",
                        Boolean.FALSE,
                        Boolean.FALSE,
                        null,
                        null,
                        null,
                        null,
                        "")));

        // To demo how a wrong functionality ID is reported in ARA
        functionalityIds.put("Not-Found", Long.valueOf(accountFolderId.longValue() + 1000));

        return functionalityIds;
    }

    private Long createFolder(long projectId, Long parentId, String name) throws BadRequestException {
        final FunctionalityDTO folder = new FunctionalityDTO(null, null, 0,
                FunctionalityType.FOLDER.name(), name,
                null, null, null, null, null, null, null, null, null, null, null);

        return functionalityService.create(projectId, new NewFunctionalityDTO(
                folder, parentId, FunctionalityPosition.LAST_CHILD)).getId();
    }

    private Long createFunctionality(long projectId, Long parentId, FunctionalityDTO functionality)
            throws BadRequestException {
        return functionalityService.create(projectId, new NewFunctionalityDTO(
                functionality, parentId, FunctionalityPosition.LAST_CHILD)).getId();
    }

}
