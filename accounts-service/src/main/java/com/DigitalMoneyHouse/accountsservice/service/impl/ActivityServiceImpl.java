package com.DigitalMoneyHouse.accountsservice.service.impl;

import com.DigitalMoneyHouse.accountsservice.appConfig.LocalDateTimeToStringConverter;
import com.DigitalMoneyHouse.accountsservice.dto.entry.ActivityFilterEntryDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.AccountOutDTO;
import com.DigitalMoneyHouse.accountsservice.dto.exit.ActivityOutDTO;

import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.Activity;
import com.DigitalMoneyHouse.accountsservice.exceptions.BadRequestException;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;
import com.DigitalMoneyHouse.accountsservice.repository.AccountsRepository;
import com.DigitalMoneyHouse.accountsservice.repository.ActivityRepository;
import com.DigitalMoneyHouse.accountsservice.service.IActivityService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements IActivityService {

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    public ActivityServiceImpl(ModelMapper modelMapper,AccountsRepository accountsRepository,ActivityRepository activityRepository) {
        this.modelMapper = modelMapper;
        this.accountsRepository=accountsRepository;
        this.activityRepository=activityRepository;


    }

    // Obtener todas las actividades por cuenta, ordenadas por fecha (descendente)
    public List<ActivityOutDTO> getAllActivitiesByAccountId(Long accountId) throws ResourceNotFoundException {
        // Buscar la cuenta por ID
        Account account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Obtener las actividades de la cuenta
        List<Activity> activities = activityRepository.findAllByAccountIdOrderByDateDesc(accountId);

        // Convertir las entidades Activity a DTO
        return activities.stream()
                .map(activity ->modelMapper.map(activity, ActivityOutDTO.class))  // mapea la lista y Devuelve un coleccion de lista
                .collect(Collectors.toList());
    }

    // Obtener el detalle de una actividad específica (activityId)
    public ActivityOutDTO getActivityDetail(Long accountId, Long id)
            throws ResourceNotFoundException {

        // Buscar la actividad por accountId y activityId
        Activity activity = activityRepository.findByAccountIdAndId(accountId, id)
                .orElseThrow(() -> new ResourceNotFoundException("ActivityID inexistente"));

        // Convertir la actividad a DTO
        return modelMapper.map(activity, ActivityOutDTO.class);
    }

    public List<ActivityOutDTO> filterActivities(ActivityFilterEntryDTO filter) throws BadRequestException {
        if (filter.getStartDate() != null && filter.getEndDate() != null &&
                filter.getStartDate().isAfter(filter.getEndDate())) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la final.");
        }

        // Establecer valores predeterminados para montos si no se proporcionan
        BigDecimal minAmount = filter.getMinAmount() != null ? filter.getMinAmount() : BigDecimal.ZERO;
        BigDecimal maxAmount = filter.getMaxAmount() != null ? filter.getMaxAmount() : BigDecimal.valueOf(1000000000);

        LocalDate startDate = filter.getStartDate();
        LocalDate endDate = filter.getEndDate();
        String activityType = filter.getActivityType();

        // Llamar al repositorio con los filtros aplicados
        List<Activity> activities = activityRepository.filterActivities(
                minAmount,
                maxAmount,
                startDate,
                endDate,
                activityType
        );

        return activities.stream()
                .map(activity -> modelMapper.map(activity, ActivityOutDTO.class))
                .collect(Collectors.toList());
    }

    public Activity getActivityById(Long activityId) throws ResourceNotFoundException {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
    }

    // Método para generar el comprobante de actividad en PDF
    public ByteArrayOutputStream generateActivityReceipt(Activity activity) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Agregar información de la actividad al PDF
            document.add(new Paragraph("Comprobante de Actividad"));
            document.add(new Paragraph("ID de Actividad: " + activity.getId()));
            document.add(new Paragraph("Tipo de Actividad: " + activity.getType()));
            document.add(new Paragraph("Monto: " + activity.getAmount()));
            document.add(new Paragraph("Fecha: " + activity.getDate().toString()));
            document.add(new Paragraph("Descripción: " + activity.getDescription()));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputStream;
    }



}




