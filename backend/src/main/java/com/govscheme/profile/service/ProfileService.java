package com.govscheme.profile.service;

import com.govscheme.auth.entity.UserRepository;
import com.govscheme.common.exception.ResourceNotFoundException;
import com.govscheme.common.exception.ValidationException;
import com.govscheme.matching.service.MatchingService;
import com.govscheme.profile.dto.AddressRequest;
import com.govscheme.profile.dto.AddressResponse;
import com.govscheme.profile.dto.ProfileCompleteness;
import com.govscheme.profile.dto.ProfileRequest;
import com.govscheme.profile.dto.ProfileResponse;
import com.govscheme.profile.entity.AddressType;
import com.govscheme.profile.entity.UserAddress;
import com.govscheme.profile.entity.UserAddressRepository;
import com.govscheme.profile.entity.UserProfile;
import com.govscheme.profile.entity.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns the citizen profile. All methods are scoped to the authenticated user's
 * id, so one user can never read or modify another user's profile.
 * <p>
 * Updates use merge semantics: {@code null} request fields leave stored values
 * untouched. Addresses are the exception — when present, the list replaces the
 * stored set (documented on the API).
 */
@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);
    private static final int MAX_ADDRESSES = 5;

    private final UserProfileRepository profileRepository;
    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;
    private final MatchingService matchingService;

    public ProfileService(UserProfileRepository profileRepository,
                          UserAddressRepository addressRepository,
                          UserRepository userRepository,
                          MatchingService matchingService) {
        this.profileRepository = profileRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.matchingService = matchingService;
    }

    @Transactional
    public ProfileResponse getProfile(String username) {
        String userId = userIdFor(username);
        UserProfile profile = getOrCreate(userId);
        List<UserAddress> addresses = addressRepository.findByUserId(userId);
        return toResponse(profile, addresses);
    }

    @Transactional
    public ProfileResponse updateProfile(String username, ProfileRequest request) {
        String userId = userIdFor(username);
        UserProfile profile = getOrCreate(userId);
        merge(profile, request);

        List<UserAddress> addresses;
        if (request.getAddresses() != null) {
            addresses = replaceAddresses(userId, request.getAddresses());
        } else {
            addresses = addressRepository.findByUserId(userId);
        }

        profileRepository.save(profile);
        log.info("PROFILE_UPDATED userId={}", userId);
        try {
            matchingService.recalculateForUser(userId);
        } catch (Exception e) {
            // Matching must never fail a profile save; the error is logged
            // and the next profile write or sync retries the recalculation.
            log.warn("MATCHING_RECALC_FAILED userId={} message={}", userId, e.getMessage());
        }
        return toResponse(profile, addresses);
    }

    private String userIdFor(String username) {
        return userRepository.findByEmailOrPhone(username, username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username))
            .getId();
    }

    private UserProfile getOrCreate(String userId) {
        return profileRepository.findById(userId).orElseGet(() -> {
            UserProfile fresh = new UserProfile();
            fresh.setUserId(userId);
            fresh.setNationality("Indian");
            return profileRepository.save(fresh);
        });
    }

    private void merge(UserProfile profile, ProfileRequest request) {
        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName().trim());
        }
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getNationality() != null) {
            profile.setNationality(request.getNationality().trim());
        }
        if (request.getEducationLevel() != null) {
            profile.setEducationLevel(request.getEducationLevel());
        }
        if (request.getInstitution() != null) {
            profile.setInstitution(request.getInstitution().trim());
        }
        if (request.getStudentStatus() != null) {
            profile.setStudentStatus(request.getStudentStatus());
        }
        if (request.getCourse() != null) {
            profile.setCourse(request.getCourse().trim());
        }
        if (request.getAnnualIncome() != null) {
            profile.setAnnualIncome(request.getAnnualIncome());
        }
        if (request.getIncomeCategory() != null) {
            profile.setIncomeCategory(request.getIncomeCategory());
        }
        if (request.getBplStatus() != null) {
            profile.setBplStatus(request.getBplStatus());
        }
        if (request.getEconomicDistress() != null) {
            profile.setEconomicDistress(request.getEconomicDistress());
        }
        if (request.getCasteCategory() != null) {
            profile.setCasteCategory(request.getCasteCategory());
        }
        if (request.getMinorityStatus() != null) {
            profile.setMinorityStatus(request.getMinorityStatus());
        }
        if (request.getDisabilityStatus() != null) {
            profile.setDisabilityStatus(request.getDisabilityStatus());
            if (Boolean.FALSE.equals(request.getDisabilityStatus())) {
                profile.setDisabilityPercentage(null);
            }
        }
        if (request.getDisabilityPercentage() != null) {
            profile.setDisabilityPercentage(request.getDisabilityPercentage());
        }
        if (request.getOccupation() != null) {
            profile.setOccupation(request.getOccupation());
        }
        if (request.getEmploymentStatus() != null) {
            profile.setEmploymentStatus(request.getEmploymentStatus());
        }
        if (request.getGovtEmployee() != null) {
            profile.setGovtEmployee(request.getGovtEmployee());
        }
        if (request.getMaritalStatus() != null) {
            profile.setMaritalStatus(request.getMaritalStatus());
        }
        if (request.getDependents() != null) {
            profile.setDependents(request.getDependents());
        }
        if (request.getAdditionalAttributes() != null) {
            profile.setAdditionalAttributes(new HashMap<>(request.getAdditionalAttributes()));
        }
    }

    private List<UserAddress> replaceAddresses(String userId, List<AddressRequest> requests) {
        if (requests.size() > MAX_ADDRESSES) {
            throw new ValidationException("Too many addresses",
                Map.of("addresses", "At most " + MAX_ADDRESSES + " addresses are allowed"));
        }
        addressRepository.deleteByUserId(userId);
        int firstPrimary = -1;
        for (int i = 0; i < requests.size(); i++) {
            if (Boolean.TRUE.equals(requests.get(i).getPrimary())) {
                firstPrimary = i;
                break;
            }
        }
        if (firstPrimary == -1 && !requests.isEmpty()) {
            firstPrimary = 0;
        }
        List<UserAddress> saved = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            AddressRequest req = requests.get(i);
            UserAddress address = new UserAddress();
            address.setUserId(userId);
            address.setAddressType(req.getAddressType() != null ? req.getAddressType() : AddressType.PERMANENT);
            address.setPrimary(i == firstPrimary);
            address.setState(trim(req.getState()));
            address.setDistrict(trim(req.getDistrict()));
            address.setBlock(trim(req.getBlock()));
            address.setVillageTown(trim(req.getVillageTown()));
            address.setPincode(trim(req.getPincode()));
            address.setAreaType(req.getAreaType());
            saved.add(addressRepository.save(address));
        }
        return saved;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private ProfileResponse toResponse(UserProfile profile, List<UserAddress> addresses) {
        List<AddressResponse> addressDtos = addresses.stream()
            .map(AddressResponse::from)
            .toList();
        ProfileCompleteness completeness =
            ProfileCompletenessCalculator.calculate(profile, addresses);
        return ProfileResponse.from(profile, addressDtos, completeness);
    }
}
