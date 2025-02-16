package com.example.userservice.config.ModelMapperConfig;

import com.example.userservice.dto.entry.UserEntryDto;
import com.example.userservice.dto.exit.UserRegisterOutDto;
import com.example.userservice.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(UserEntryDto.class, User.class);
        modelMapper.typeMap(User.class, UserRegisterOutDto.class);
        return modelMapper;
    }
}
