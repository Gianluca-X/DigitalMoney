package com.DigitalMoneyHouse.accountsservice.exceptions;

import java.nio.file.FileAlreadyExistsException;

public class CardAlreadyExistsException extends FileAlreadyExistsException {
    public CardAlreadyExistsException(String file) {
        super(file);
    }
}
