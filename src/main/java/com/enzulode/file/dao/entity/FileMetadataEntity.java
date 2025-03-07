package com.enzulode.file.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataEntity {

  public enum FileStatus { PROCESSING, FAILED, SUCCEED }



  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String key;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String ownedBy;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FileStatus status;

  public FileMetadataEntity(String key, String name, String ownedBy, FileStatus status) {
    this.key = key;
    this.name = name;
    this.ownedBy = ownedBy;
    this.status = status;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    FileMetadataEntity that = (FileMetadataEntity) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
  }
}
