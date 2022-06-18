package com.example.csvload.exception.disease;


import com.example.csvload.exception.ApplicationException;

public class FileReadException extends ApplicationException {

    private static final String MESSAGE = "'질병명, 정의, 진료과, 증상, 원인, 치료 ' 순의 csv 파일이 아닙니다.";

    public FileReadException(String message) {
        super(message);
    }

    public FileReadException() {
        this(MESSAGE);
    }
}
