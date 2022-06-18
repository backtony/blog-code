package com.example.csvload.exception.disease;


import com.example.csvload.exception.ApplicationException;

public class NotCSVFileException extends ApplicationException {

    private static final String MESSAGE = "해당 파일은 CSV 파일이 아닙니다.";

    public NotCSVFileException(String message) {
        super(message);
    }

    public NotCSVFileException() {
        this(MESSAGE);
    }
}
