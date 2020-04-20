package eu.navispeed.extractor.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
import javax.persistence.OneToOne;
import java.util.UUID;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Task {

  public enum Type {
    DOWNLOAD,
    EXTRACTION,
  }


  public enum State {
    TODO,
    RUNNING,
    DONE,
    DONE_WITH_ERROR
  }


  @Id
  @GeneratedValue
  private UUID id;
  private State state;
  @OneToOne
  private Output output;
  @ManyToOne
  @ToString.Exclude
  @JsonBackReference
  private Project project;
  @Enumerated(value = EnumType.STRING)
  private Type type;
  private String message;
  private Integer progress;
  @OneToOne(cascade = CascadeType.ALL)
  private ExtractionParameter extractionParameter;
  @OneToOne(cascade = CascadeType.ALL)
  private DownloadParameter downloadParameter;

  public Task(UUID id, Integer progress, State state) {
    this.id = id;
    this.progress = progress;
    this.state = state;
  }
}
