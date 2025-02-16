package com.DigitalMoneyHouse.accountsservice.service;

import com.DigitalMoneyHouse.accountsservice.dto.entry.ActivityFilterEntryDTO;

import com.DigitalMoneyHouse.accountsservice.dto.exit.ActivityOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Activity;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;

import java.io.ByteArrayOutputStream;
import java.util.List;

public interface IActivityService {
    List<ActivityOutDTO> getAllActivitiesByAccountId(Long accountId) throws ResourceNotFoundException;
    ActivityOutDTO getActivityDetail(Long accountId, Long id) throws ResourceNotFoundException;
     List<ActivityOutDTO> filterActivities(ActivityFilterEntryDTO filter);
     Activity getActivityById(Long activityId) throws ResourceNotFoundException;
     ByteArrayOutputStream generateActivityReceipt(Activity activity);
}
