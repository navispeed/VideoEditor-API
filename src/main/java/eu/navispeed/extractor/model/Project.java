package eu.navispeed.extractor.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonManagedReference
  private Source url;

  @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
  @JsonManagedReference
  @ToString.Exclude
  private List<Task> tasks;

  @Transient
  private List<Output> exports;
}
