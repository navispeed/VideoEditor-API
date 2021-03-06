package eu.navispeed.extractor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ExtractionParameter {

  public enum Format {
    VIDEO,
    AUDIO
  }

  @Id
  @GeneratedValue
  @JsonIgnore
  private Integer id;

  @ManyToOne(optional = false, cascade = CascadeType.ALL)
  @ToString.Exclude
  private Task extractionTask;

  @Builder.Default
  private Long fromTimeCode = 0L;
  @Builder.Default
  private Long toTimeCode = 0L;
  @Enumerated(value = EnumType.STRING)
  private Format format;
  @ManyToOne
  private Output downloadedInput;
}
