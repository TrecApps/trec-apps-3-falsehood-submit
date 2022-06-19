package com.trecapps.falsehoods.submit.repos;

import com.trecapps.falsehoods.submit.models.FalsehoodUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FalsehoodUserRepo extends JpaRepository<FalsehoodUser, String> {
}
