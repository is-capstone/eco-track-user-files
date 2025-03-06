package com.enzulode.file.dao.repository;

import com.enzulode.file.dao.entity.FileMetadataEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadataEntity, Long> {

  @Modifying
  @Query("UPDATE FileMetadataEntity fme SET fme.status = 'FAILED' WHERE fme.name = :name AND fme.ownedBy = :ownedBy")
  void markFailedByNameAndOwnedBy(@Param("name") String objName, @Param("ownedBy") String ownedBy);

  @Modifying
  @Query("UPDATE FileMetadataEntity fme SET fme.status = 'SUCCEED' WHERE fme.name = :name AND fme.ownedBy = :ownedBy")
  void markSucceedByNameAndOwnedBy(@Param("name") String objName, @Param("ownedBy") String ownedBy);

  @Query("SELECT fme FROM FileMetadataEntity fme WHERE fme.ownedBy = :ownedBy")
  Page<FileMetadataEntity> findByOwnedBy(@Param("ownedBy") String ownedBy, Pageable pageable);
}
