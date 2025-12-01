package org.example.ia_dashboard.Repository;

import org.example.ia_dashboard.Entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByUserId(Long userId);
}
