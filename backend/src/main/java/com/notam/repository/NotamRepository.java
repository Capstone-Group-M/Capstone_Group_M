package com.notam.repository;

import com.notam.model.NOTAM;
import java.util.List;

public interface NotamRepository {
    void save(NOTAM notam);
    void saveAll(List<NOTAM> notams);
    List<NOTAM> findByIcaoLocation(String icaoLocation);
}
