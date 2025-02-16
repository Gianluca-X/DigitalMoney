package com.DigitalMoneyHouse.accountsservice;

import com.DigitalMoneyHouse.accountsservice.dto.entry.ActivityFilterEntryDTO;

import com.DigitalMoneyHouse.accountsservice.dto.exit.ActivityOutDTO;
import com.DigitalMoneyHouse.accountsservice.entities.Account;
import com.DigitalMoneyHouse.accountsservice.entities.Activity;
import com.DigitalMoneyHouse.accountsservice.exceptions.ResourceNotFoundException;
import com.DigitalMoneyHouse.accountsservice.repository.AccountsRepository;
import com.DigitalMoneyHouse.accountsservice.repository.ActivityRepository;
import com.DigitalMoneyHouse.accountsservice.service.impl.ActivityServiceImpl;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {
    @Mock
    private AccountsRepository accountsRepository;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ActivityServiceImpl activityService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deberiaDeRetornarTodasLasActividadesPorIdDeCuenta() throws ResourceNotFoundException {
        // Arrange
        Long accountId = 1L;

        // Mock de la cuenta existente
        Account accountMock = new Account();
        accountMock.setId(accountId);

        // Mock de las actividades
        List<Activity> activitiesMock = new ArrayList<>();
        Activity activity1 = new Activity();
        activity1.setId(1L);
        activity1.setAccountId(accountId);
        activity1.setType("pago");
        activity1.setAmount(new BigDecimal("100.00"));
        activity1.setDescription("Pago de servicios");
        activity1.setDate(LocalDateTime.of(2024, 11, 1, 10, 0));
        activitiesMock.add(activity1);

        Activity activity2 = new Activity();
        activity2.setId(2L);
        activity2.setAccountId(accountId);
        activity2.setType("carga");
        activity2.setAmount(new BigDecimal("200.00"));
        activity2.setDescription("Carga de saldo");
        activity2.setDate(LocalDateTime.of(2024, 11, 2, 14, 30));
        activitiesMock.add(activity2);

        // Mock del DTO de salida
        ActivityOutDTO activityOutDTO1 = new ActivityOutDTO();
        activityOutDTO1.setId(1L);
        activityOutDTO1.setAccountId(accountId);
        activityOutDTO1.setType("pago");
        activityOutDTO1.setAmount(new BigDecimal("100.00"));
        activityOutDTO1.setDate("2024-11-01T10:00:00");

        ActivityOutDTO activityOutDTO2 = new ActivityOutDTO();
        activityOutDTO2.setId(2L);
        activityOutDTO2.setAccountId(accountId);
        activityOutDTO2.setType("carga");
        activityOutDTO2.setAmount(new BigDecimal("200.00"));
        activityOutDTO2.setDate("2024-11-02T14:30:00");

        // Configurar los mocks
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(accountMock));
        when(activityRepository.findAllByAccountIdOrderByDateDesc(accountId)).thenReturn(activitiesMock);
        when(modelMapper.map(activity1, ActivityOutDTO.class)).thenReturn(activityOutDTO1);
        when(modelMapper.map(activity2, ActivityOutDTO.class)).thenReturn(activityOutDTO2);

        // Act
        List<ActivityOutDTO> result = activityService.getAllActivitiesByAccountId(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verificar datos del primer DTO
        assertEquals(1L, result.get(0).getId());
        assertEquals(accountId, result.get(0).getAccountId());
        assertEquals("pago", result.get(0).getType());
        assertEquals(new BigDecimal("100.00"), result.get(0).getAmount());
        assertEquals("2024-11-01T10:00:00", result.get(0).getDate());

        // Verificar datos del segundo DTO
        assertEquals(2L, result.get(1).getId());
        assertEquals(accountId, result.get(1).getAccountId());
        assertEquals("carga", result.get(1).getType());
        assertEquals(new BigDecimal("200.00"), result.get(1).getAmount());
        assertEquals("2024-11-02T14:30:00", result.get(1).getDate());

        // Verificar que los métodos del repositorio fueron llamados
        verify(accountsRepository).findById(accountId);
        verify(activityRepository).findAllByAccountIdOrderByDateDesc(accountId);
    }
    @Test
    void deberiaRetornarElDetalleDelaActividadDeUnaCuentaPorId() throws ResourceNotFoundException {
        // Arrange
        Long accountId = 1L;
        Long activityId = 2L;

        // Mock de la actividad
        Activity activityMock = new Activity();
        activityMock.setId(activityId);
        activityMock.setAccountId(accountId);
        activityMock.setType("pago");
        activityMock.setAmount(new BigDecimal("150.00"));
        activityMock.setDescription("Pago de factura");
        activityMock.setDate(LocalDateTime.of(2024, 11, 5, 15, 45));

        // Mock del DTO esperado
        ActivityOutDTO activityOutDTO = new ActivityOutDTO();
        activityOutDTO.setId(activityId);
        activityOutDTO.setAccountId(accountId);
        activityOutDTO.setType("pago");
        activityOutDTO.setAmount(new BigDecimal("150.00"));
        activityOutDTO.setDate("2024-11-05T15:45:00");

        // Configuración de los mocks
        when(activityRepository.findByAccountIdAndId(accountId, activityId))
                .thenReturn(Optional.of(activityMock));
        when(modelMapper.map(activityMock, ActivityOutDTO.class)).thenReturn(activityOutDTO);

        // Act
        ActivityOutDTO result = activityService.getActivityDetail(accountId, activityId);

        // Assert
        assertNotNull(result);
        assertEquals(activityId, result.getId());
        assertEquals(accountId, result.getAccountId());
        assertEquals("pago", result.getType());
        assertEquals(new BigDecimal("150.00"), result.getAmount());
        assertEquals("2024-11-05T15:45:00", result.getDate());

        // Verificar que los métodos del repositorio fueron llamados
        verify(activityRepository).findByAccountIdAndId(accountId, activityId);
        verify(modelMapper).map(activityMock, ActivityOutDTO.class);
    }

    @Test
    void deberiaDeGenerarUnPDFConTodaLaActividadYSuDetalleConLosDatosCorrectosDeLaActividadSeleccionada() throws Exception {
        // Arrange
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setType("pago");
        activity.setAmount(new BigDecimal("150.00"));
        activity.setDate(LocalDateTime.of(2024, 11, 5, 15, 45));
        activity.setDescription("Pago de factura");

        // Act
        ByteArrayOutputStream pdfStream = activityService.generateActivityReceipt(activity);

        // Convertir el PDF en texto para validaciones
        ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfStream.toByteArray());
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(inputStream));
        String pdfContent = PdfTextExtractor.getTextFromPage(pdfDocument.getFirstPage());
        pdfDocument.close();

        // Assert
        assertNotNull(pdfStream);
        assertTrue(pdfStream.size() > 0);
        assertTrue(pdfContent.contains("Comprobante de Actividad"));
        assertTrue(pdfContent.contains("ID de Actividad: 1"));
        assertTrue(pdfContent.contains("Tipo de Actividad: pago"));
        assertTrue(pdfContent.contains("Monto: 150.00"));
        assertTrue(pdfContent.contains("Fecha: 2024-11-05T15:45"));
        assertTrue(pdfContent.contains("Descripción: Pago de factura"));
    }
    @Test
    void deberiaDeDevolverLasActividadesFiltradas() {
        // Arrange
        ActivityFilterEntryDTO filter = ActivityFilterEntryDTO.builder()
                .minAmount(new BigDecimal("50"))
                .maxAmount(new BigDecimal("200"))
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .activityType("pago")
                .build();

        // Crear entidades mock
        Activity activity1 = new Activity();
        activity1.setId(1L);
        activity1.setType("pago");
        activity1.setAmount(new BigDecimal("100"));
        activity1.setDate(LocalDate.of(2024, 5, 20).atStartOfDay());
        activity1.setDescription("Pago de servicio");

        Activity activity2 = new Activity();
        activity2.setId(2L);
        activity2.setType("pago");
        activity2.setAmount(new BigDecimal("150"));
        activity2.setDate(LocalDate.of(2024, 6, 15).atStartOfDay());
        activity2.setDescription("Pago de factura");

        List<Activity> mockActivities = Arrays.asList(activity1, activity2);

        // Crear DTOs esperados
        ActivityOutDTO dto1 = new ActivityOutDTO();
        dto1.setId(1L);
        dto1.setType("pago");
        dto1.setAmount(new BigDecimal("100"));
        dto1.setDate("2024-05-20T00:00");

        ActivityOutDTO dto2 = new ActivityOutDTO();
        dto2.setId(2L);
        dto2.setType("pago");
        dto2.setAmount(new BigDecimal("150"));
        dto2.setDate("2024-06-15T00:00");

        // Configurar mocks
        when(activityRepository.filterActivities(
                new BigDecimal("50"),
                new BigDecimal("200"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "pago"
        )).thenReturn(mockActivities);

        when(modelMapper.map(activity1, ActivityOutDTO.class)).thenReturn(dto1);
        when(modelMapper.map(activity2, ActivityOutDTO.class)).thenReturn(dto2);

        // Act
        List<ActivityOutDTO> result = activityService.filterActivities(filter);

        // Assert
        assertNotNull(result, "La lista de resultados no debe ser null");
        assertEquals(2, result.size(), "El tamaño de la lista debe ser 2");

        assertEquals(dto1, result.get(0), "El primer elemento no coincide");
        assertEquals(dto2, result.get(1), "El segundo elemento no coincide");

        verify(activityRepository, times(1)).filterActivities(
                new BigDecimal("50"),
                new BigDecimal("200"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "pago"
        );

        verify(modelMapper, times(1)).map(activity1, ActivityOutDTO.class);
        verify(modelMapper, times(1)).map(activity2, ActivityOutDTO.class);
    }



}
