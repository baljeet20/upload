package com.test.model;

import org.springframework.stereotype.Component;

@Component
public class BatchRunData {
    private int successRecords;
    private int failedRecords;

    public int getSuccessRecords() {
        return successRecords;
    }

    public void setSuccessRecords(int successRecords) {
        this.successRecords = successRecords;
    }

    public int getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(int failedRecords) {
        this.failedRecords = failedRecords;
    }

    public void increamentSuccessRecords(){
        successRecords++;
    }

    public void increamentFailedRecords(){
        failedRecords++;
    }

    @Override
    public String toString() {
        return "BatchRunData{" +
                "successRecords=" + successRecords +
                ", failedRecords=" + failedRecords +
                '}';
    }

    public void reset() {
        successRecords=0;
        failedRecords=0;
    }
}
