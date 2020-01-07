package com.decathlon.ara.cartography;

import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SquashExporter is an Exporter which serialize the functionalities in order to make them importable in SquashTM.
 *
 * @author Sylvain Nieuwlandt
 * @since 4.1.0
 */
@Slf4j
public class SquashExporter extends Exporter {

    static final String PROJECT_NAME = "squash_project_name";
    static final String USER = "squash_user";
    private static final Map<String,String> CRITICALITY_MAPPING = new HashMap<>();
    private static final String[] HEADERS =  {"ACTION", "PROJECT_ID", "PROJECT_NAME", "REQ_PATH", "REQ_NUM", "REQ_VERSION_NUM", "REQ_VERSION_NAME",
            "REQ_VERSION_CRITICALITY", "REQ_VERSION_CATEGORY", "REQ_VERSION_STATUS", "REQ_VERSION_DESCRIPTION", "REQ_VERSION_#_TC",
            "REQ_VERSION_#_ATTACHEMENT", "REQ_VERSION_CREATED_ON", "REQ_VERSION_CREATED_BY", "REQ_VERSION_LAST_MODIFIED_ON",
            "REQ_VERSION_LAST_MODIFIED_BY", "REQ_VERSION_MILESTONE", "REQ_VERSION_CUF_<code du cuf>"};

    static {
        CRITICALITY_MAPPING.put("HIGH", "CRITICAL");
        CRITICALITY_MAPPING.put("MEDIUM", "MAJOR");
        CRITICALITY_MAPPING.put("LOW", "MINOR");
    }

    @Override
    public String getName() {
        return "SquashTM";
    }

    @Override
    public String getDescription() {
        return "Export this cartography to import it as requirements in SquashTM";
    }

    @Override
    public String getFormat() {
        return "xls";
    }

    @Override
    public byte[] generate(List<FunctionalityDTO> functionalities, Map<String, String> requiredInfos) {
        String projectNameInSquash = requiredInfos.get(PROJECT_NAME);
        String userWhichMakesTheImport = requiredInfos.get(USER);

        HSSFWorkbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("REQUIREMENT");

        this.createHeaderRow(sheet);
        Date importDate = new Date();
        int idx = 1;
        for (FunctionalityDTO functionality: functionalities) {
            StringBuilder pathBuilder = new StringBuilder("/");
            if (FunctionalityType.FUNCTIONALITY.name().equals(functionality.getType())) {
                String criticity = CRITICALITY_MAPPING.get(functionality.getSeverity());
                String funcName = functionality.getName();
                pathBuilder.append(projectNameInSquash)
                        .append(this.getFunctionalityPath(functionality, functionalities))
                        .append("/").append(funcName);
                this.addRowToSheet(sheet, idx++, projectNameInSquash,
                        pathBuilder.toString(), functionality.getComment(), criticity,
                        importDate, userWhichMakesTheImport);
            }
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        } catch (IOException ex) {
            log.error("Unable to write the Squash export : {}", ex.getMessage());
            log.debug("Full exception of the Squash export failure", ex);
        }
        return new byte[0];
    }

    @Override
    public List<ExportField> listRequiredFields() {
        List<ExportField> fields = new ArrayList<>();
        fields.add(new ExportField(PROJECT_NAME, "Targeted project's name", "string",
                "The name of targeted project in SquashTM. Note that the project must be created before doing the import."));
        fields.add(new ExportField(USER, "Targeted User's name", "string",
                "The name of the user whom will act as the creator of the requirements in SquashTM. Note that the user must exist before doing the import."));
        return fields;
    }

    void createHeaderRow(Sheet sheet) {
        Row header = sheet.createRow(0);
        int rowIdx = 0;
        for (String headerColumn : HEADERS) {
            this.addCellToRow(header, rowIdx++, headerColumn);
        }
    }

    void addRowToSheet(Sheet sheet, int index, String projectName, String path, String comment, String criticity, Date creationDate, String username) {
        Row row = sheet.createRow(index);
        this.addCellToRow(row, 0, "C");
        this.addCellToRow(row, 2, projectName);
        this.addCellToRow(row, 3, path);
        this.addCellToRow(row, 4, "1");
        this.addCellToRow(row, 5, "1");
        this.addCellToRow(row, 7, criticity);
        this.addCellToRow(row, 8, "CAT_UNDEFINED");
        this.addCellToRow(row, 9, "WORK_IN_PROGRESS");
        this.addCellToRow(row, 10, comment);
        this.addCellToRow(row, 13, creationDate);
        this.addCellToRow(row, 14, username);
        this.addCellToRow(row, 15, creationDate);
        this.addCellToRow(row, 16, username);
    }

    private void addCellToRow(Row row, int position, String value) {
        Cell result = row.createCell(position);
        result.setCellValue(value);
    }

    private void addCellToRow(Row row, int position, Date value) {
        Cell result = row.createCell(position);
        Workbook workbook = row.getSheet().getWorkbook();
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("d/m/yy"));
        result.setCellStyle(dateStyle);
        result.setCellValue(value);
    }

    private String getFunctionalityPath(FunctionalityDTO functionality, List<FunctionalityDTO> functionalities) {
        if (null == functionality.getParentId()) {
            return "";
        }
        Long parentId = functionality.getParentId();
        FunctionalityDTO parent = functionalities.stream()
                .filter(f -> f.getId().equals(parentId))
                .findFirst()
                .orElse(null);
        if (parent == null) {
            return "";
        }
        return this.getFunctionalityPath(parent, functionalities) + "/" + parent.getName();
    }
}
