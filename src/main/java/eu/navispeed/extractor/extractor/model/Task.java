package eu.navispeed.extractor.extractor.model;

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
    CONVERT,
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
  @JsonBackReference
  private Project project;
  private String args;
  private Type type;
  private String message;
  private Integer progress;
}
