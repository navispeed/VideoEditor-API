package eu.navispeed.extractor.extractor.domain.provider;

import eu.navispeed.extractor.extractor.model.Source;
import eu.navispeed.extractor.extractor.model.Task;

import java.util.Optional;

public interface VideoProvider {

  String getName();

  Optional<Source> addVideoInfo(Source source);

  void download(Task task);

  String getUrlPattern();
}
