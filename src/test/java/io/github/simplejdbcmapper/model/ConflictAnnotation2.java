package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.CreatedOn;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;
import io.github.simplejdbcmapper.annotation.UpdatedOn;
import java.time.LocalDateTime;

@Table(name = "annotation_check")
public class ConflictAnnotation2 {
  @Id(type = IdType.AUTO_GENERATED)
  private Integer id;

  @Column private String something;

  @CreatedOn @UpdatedOn private LocalDateTime createdOn1;
}
