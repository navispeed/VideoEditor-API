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
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Source {

  @Id
  @GeneratedValue
  private Integer id;
  private String url;

  @OneToMany(mappedBy = "url")
  @ToString.Exclude
  @JsonBackReference
  private List<Project> linkedProject;
  private String title;
  @Lob
  private String videoFormats;



}
