package com.xn.ales.data.datimport.csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.RowProcessor;
import com.xn.alex.data.common.CommonConfig;

public class DataRowListProcessor implements RowProcessor {

    private static final String INVALID_VALUE = "#NULL!";

    private static final String REGEX = "-?[0-9]+.*[0-9]*";

    private final List<String[]> rows;

    private String[] headers;

    private final int batchNum;

    private final IDataImport dataImport;

    private List<String> numbericIndexList;

    private int obsoleteLine = 0;

    public DataRowListProcessor(final int batchNum, final IDataImport dataImport) {
        super();
        this.batchNum = batchNum;
        this.dataImport = dataImport;
        rows = new ArrayList<String[]>(batchNum);
        headers = null;
        numbericIndexList = null;
    }

    @Override
    public void processStarted(final ParsingContext context) {
        System.out.println("Started to process rows of data.");
    }

    @Override
    public void rowProcessed(final String[] row, final ParsingContext context) {
        if (headers == null) {
            headers = context.headers();
            ((CSVImport) dataImport).processENColumn(headers);
            numbericIndexList = DataImportFactory.getNumericListColumnIndex(((CSVImport) dataImport).getColumnNames());
        }
        boolean obsolete = false;
        for (String value : row) {
            if (value == null || value.equals(INVALID_VALUE)) {
                value = String.valueOf(CommonConfig.IVALID_VALUE);
            }
            if (numbericIndexList.contains(value)) {
                final Boolean strResult = value.matches(REGEX);
                if (strResult == false) {
                    obsolete = true;
                }
            }
        }
        if (obsolete == true) {
            obsoleteLine++;
        } else {
            rows.add(row);
        }
        if (rows.size() == batchNum) {
            dataImport.load2Db(rows, ((CSVImport) dataImport).getTableName());
            System.out.println("Processed line " + rows.size());
            rows.clear();
        }
    }

    @Override
    public void processEnded(final ParsingContext context) {
        headers = context.headers();
    }

    public List<String[]> getRows() {
        return rows == null ? Collections.<String[]> emptyList() : rows;
    }

    public String[] getHeaders() {
        return headers;
    }

    /**
     * @return the obsoleteLine
     */
    public int getObsoleteLine() {
        return obsoleteLine;
    }

}