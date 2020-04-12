package eu.navispeed.extractor.extractor.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Project {

  @Id
  @GeneratedValue
  private UUID uuid;

  @ManyToOne
  @JsonManagedReference
  private Source url;

  @OneToMany(mappedBy = "project")
  @JsonManagedReference
  @ToString.Exclude
  private List<Output> exports;

  @OneToMany(mappedBy = "project")
  @JsonManagedReference
  @ToString.Exclude
  private List<Task> tasks;
}
