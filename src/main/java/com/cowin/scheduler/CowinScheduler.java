package com.cowin.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Component
@EnableScheduling
@Slf4j
public class CowinScheduler {

    private final Map<String, Boolean> map;
    private final UserRepository userRepository;
    private final ExecutorService executorService;

    public CowinScheduler(UserRepository userRepository) {
        this.map = new HashMap<>();
        this.userRepository = userRepository;
        this.executorService = Executors.newFixedThreadPool(10);

    }

    @Scheduled(fixedRate = 60000)
    public void getAlert() {
        log.info("Starting scheduler");
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = sdf.format(date);
        List<Future<Map<String, List<User>>>> futureList = new ArrayList<>();
        getDistinctPinCode().forEach(pinCode -> futureList.add(this.executorService.submit(() -> getUsersByPinCode(pinCode))));
        futureList.forEach(future -> {
            try {
                Map<String, List<User>> userMap = future.get(1, TimeUnit.MINUTES);
                CowinResponse cowinResponse = getResponseFromCowin(new ArrayList<>(userMap.keySet()).get(0), formattedDate);
                new ArrayList<>(userMap.values()).get(0).forEach(user -> Arrays.asList(user.getPreference().split(",")).forEach(preference -> {
                    String[] preferenceArray = preference.split(":");
                    Integer dose = Integer.parseInt(preferenceArray[0]);
                    Integer minAge = Integer.parseInt(preferenceArray[1]);
                    validatePreferenceAndSendEmail(cowinResponse, user, dose, minAge);
                }));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        });
    }

    private Map<String, List<User>> getUsersByPinCode(String pincode) {
        Map<String, List<User>> pinCodeMap = new HashMap<>();
        pinCodeMap.put(pincode, this.userRepository.getUsersByPinCode(pincode));
        return pinCodeMap;
    }

    private List<String> getDistinctPinCode() {
        return this.userRepository.getDistinctPinCode().stream()
                .map(pincode -> pincode.split(",")).map(Arrays::asList)
                .flatMap(Collection::stream).distinct().collect(Collectors.toList());
    }

    private CowinResponse getResponseFromCowin(String pinCode, String formattedDate) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin?pincode=" + pinCode + "&date=" + formattedDate;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("user-agent", "Docker-Client/19.03.5 (linux)");
        HttpEntity<String> httpEntity = new HttpEntity<>("some body", headers);
        ResponseEntity<CowinResponse> response
                = restTemplate.exchange(url, HttpMethod.GET, httpEntity, CowinResponse.class);
        return response.getBody();
    }

    private void validatePreferenceAndSendEmail(CowinResponse cowinResponse, User user, Integer dose, Integer minAge) {
        if (Objects.nonNull(cowinResponse)) {
            ListUtils.emptyIfNull(cowinResponse.getCenters()).forEach(center -> center.getSessions().forEach(session -> {
                String mapKey = center.getCenterId() + user.getEmailId() + session.getDate() + session.getMinAgeLimit();
                if (!map.containsKey(mapKey) && (dose.equals(1) && session.getAvailableCapacityDose1() > 0) ||
                        (dose.equals(2) && session.getAvailableCapacityDose2() > 0) && session.getMinAgeLimit().equals(minAge)) {
                    sendEmail(center, mapKey, session, user);
                }
            }));
        }
    }

    private void sendEmail(Centers center, String mapKey, Session session, User user) {
        map.put(mapKey, true);
        SendEmail.sendEmail(center, session, user.getEmailId());
    }

}
