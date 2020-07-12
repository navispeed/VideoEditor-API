package eu.navispeed.extractor.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Output {

  @Id
  @GeneratedValue
  private Integer id;

  private String path;

  @ManyToOne
  @JsonBackReference
  @ToString.Exclude
  private Project project;

  private LocalDateTime creationDate;

  @Builder.Default
  @Getter
  private Boolean isExpired = false;

  @Builder.Default
  @Getter
  private boolean isVideo;

  @Builder.Default
  @Getter
  private boolean isAudio;

}
