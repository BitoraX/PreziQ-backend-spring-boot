package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.repository.CollectionRepository;
import com.bitorax.priziq.service.CollectionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CollectionServiceImp implements CollectionService {
    CollectionRepository collectionRepository;

}
