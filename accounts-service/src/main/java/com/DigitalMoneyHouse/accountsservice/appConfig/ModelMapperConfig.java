package com.DigitalMoneyHouse.accountsservice.appConfig;

import com.DigitalMoneyHouse.accountsservice.dto.entry.CreateCardEntryDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.AccountOutDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.ActivityOutDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.CardOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.Activity;
import com.DigitalMoneyHouse.accountsservice.entities.Card;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        // Configuración de mappings
        modelMapper.typeMap(CreateCardEntryDTO.class, Card.class)
                .addMappings(mapper -> {
                    mapper.map(CreateCardEntryDTO::getNumber, Card::setNumber);
                    mapper.map(CreateCardEntryDTO::getCvc, Card::setCvc);
                    mapper.map(CreateCardEntryDTO::getExpiry, Card::setExpiry);
                    mapper.map(CreateCardEntryDTO::getName, Card::setName);
                    mapper.map(CreateCardEntryDTO::getAccountId, Card::setAccountId);
                });

        modelMapper.createTypeMap(Card.class, CardOutDTO.class)
                .addMappings(mapper -> {
                    mapper.map(Card::getNumber, CardOutDTO::setNumber);
                    mapper.map(Card::getName, CardOutDTO::setName);
                    mapper.map(Card::getExpiry, CardOutDTO::setExpiry);
                    mapper.map(Card::getCvc, CardOutDTO::setCvc);
                    mapper.map(Card::getAccountId, CardOutDTO::setAccountId);
                    mapper.map(Card::getId, CardOutDTO::setId);
                });
        // Configuraciones específicas
        modelMapper.addConverter(new LocalDateTimeToStringConverter());
        modelMapper.typeMap(AccountOutDTO.class, Account.class);

        modelMapper.typeMap(ActivityOutDTO.class, Activity.class);
        modelMapper.typeMap(Account.class, AccountOutDTO.class);
        modelMapper.typeMap(Activity.class, ActivityOutDTO.class)
                .addMappings(mapper -> {
                    mapper.map(Activity::getId, ActivityOutDTO::setId);
                    mapper.map(Activity::getDate, ActivityOutDTO::setDate);
                    mapper.map(Activity::getType, ActivityOutDTO::setType);
                    mapper.map(Activity::getAccountId, ActivityOutDTO::setAccountId);
                    mapper.map(Activity::getAmount, ActivityOutDTO::setAmount);
                    mapper.map(Activity::getDescription, ActivityOutDTO::setCvu);
                });

        return modelMapper;
    }
}
