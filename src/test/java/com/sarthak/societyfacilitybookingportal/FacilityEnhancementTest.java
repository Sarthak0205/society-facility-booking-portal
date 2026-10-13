package com.sarthak.societyfacilitybookingportal;

import com.sarthak.societyfacilitybookingportal.entity.*;
import com.sarthak.societyfacilitybookingportal.repository.FacilityRepository;
import com.sarthak.societyfacilitybookingportal.repository.SlotRepository;
import com.sarthak.societyfacilitybookingportal.repository.UserRepository;
import com.sarthak.societyfacilitybookingportal.service.BookingService;
import com.sarthak.societyfacilitybookingportal.service.FacilityService;
import com.sarthak.societyfacilitybookingportal.service.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class FacilityEnhancementTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private SlotService slotService;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private MockHttpSession session;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        testUser = userRepository.findByEmail("test@example.com").orElseGet(() -> {
            User user = new User();
            user.setEmail("test@example.com");
            user.setFullName("Sarthak Test");
            user.setPassword("123456");
            user.setRole(UserRole.RESIDENT);
            return userRepository.save(user);
        });

        session = new MockHttpSession();
        session.setAttribute("userId", testUser.getId());
        session.setAttribute("userName", testUser.getFullName());
        session.setAttribute("userRole", testUser.getRole());
    }

    @Test
    void facilityDetailsPageLoadsWithAttributes() throws Exception {
        Facility facility = facilityService.createFacility(
                "Test Tennis Court",
                "Outdoor tennis court for residents.",
                4,
                "800 sq. ft.",
                "Sports Complex, Ground Floor",
                FacilityCategory.SPORTS,
                FacilityStatus.ACTIVE
        );

        mockMvc.perform(get("/facilities/" + facility.getFacilityId()).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("facility-details"))
                .andExpect(model().attributeExists("facility"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Test Tennis Court")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sports Complex, Ground Floor")));

        // Clean up test facility
        facilityRepository.delete(facility);
    }

    @Test
    void activeFacilityAllowsAccessToSlots() throws Exception {
        Facility activeFacility = facilityService.createFacility(
                "Test Squash Court",
                "Squash court.",
                2,
                "500 sq. ft.",
                "Clubhouse",
                FacilityCategory.SPORTS,
                FacilityStatus.ACTIVE
        );

        LocalDate testDate = LocalDate.now().plusDays(1);

        mockMvc.perform(get("/slots")
                        .param("facilityId", String.valueOf(activeFacility.getFacilityId()))
                        .param("date", testDate.toString())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("slots"))
                .andExpect(model().attributeExists("facility"));

        facilityRepository.delete(activeFacility);
    }

    @Test
    void maintenanceFacilityCannotAccessSlots() throws Exception {
        Facility maintenanceFacility = facilityService.createFacility(
                "Maintenance Pool",
                "Pool undergoing annual maintenance.",
                20,
                "2000 sq. ft.",
                "Clubhouse",
                FacilityCategory.RECREATION,
                FacilityStatus.MAINTENANCE
        );

        LocalDate testDate = LocalDate.now().plusDays(1);

        mockMvc.perform(get("/slots")
                        .param("facilityId", String.valueOf(maintenanceFacility.getFacilityId()))
                        .param("date", testDate.toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/facilities/" + maintenanceFacility.getFacilityId()));

        facilityRepository.delete(maintenanceFacility);
    }

    @Test
    void maintenanceFacilityCannotProceedToBooking() {
        Facility maintenanceFacility = facilityService.createFacility(
                "Maintenance Gym",
                "Gym undergoing renovation.",
                15,
                "1000 sq. ft.",
                "Clubhouse",
                FacilityCategory.FITNESS,
                FacilityStatus.MAINTENANCE
        );

        Slot slot = slotService.createSlot(
                maintenanceFacility.getFacilityId(),
                LocalDate.now().plusDays(2),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                true
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> bookingService.createBooking(testUser, slot)
        );

        assertTrue(exception.getMessage().contains("under maintenance"));

        slotRepository.delete(slot);
        facilityRepository.delete(maintenanceFacility);
    }

    @Test
    void facilityDeletionBlockedWhenSlotsExist() {
        Facility facilityWithSlot = facilityService.createFacility(
                "Yoga Room",
                "Room for yoga and meditation.",
                15,
                "700 sq. ft.",
                "Block C",
                FacilityCategory.FITNESS,
                FacilityStatus.ACTIVE
        );

        Slot slot = slotService.createSlot(
                facilityWithSlot.getFacilityId(),
                LocalDate.now().plusDays(3),
                LocalTime.of(7, 0),
                LocalTime.of(8, 0),
                true
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> facilityService.deleteFacility(facilityWithSlot.getFacilityId())
        );

        assertTrue(exception.getMessage().contains("associated slots"));

        slotRepository.delete(slot);
        facilityService.deleteFacility(facilityWithSlot.getFacilityId());
    }

    @Test
    void facilityDeletionSucceedsWhenNoSlotsExist() {
        Facility emptyFacility = facilityService.createFacility(
                "Temporary Gazebo",
                "Outdoor seating gazebo.",
                10,
                "300 sq. ft.",
                "Garden Area",
                FacilityCategory.RECREATION,
                FacilityStatus.ACTIVE
        );

        Long facilityId = emptyFacility.getFacilityId();
        assertDoesNotThrow(() -> facilityService.deleteFacility(facilityId));
        assertFalse(facilityRepository.findById(facilityId).isPresent());
    }
}
