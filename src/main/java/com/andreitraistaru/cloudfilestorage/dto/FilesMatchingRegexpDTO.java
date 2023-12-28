package com.andreitraistaru.cloudfilestorage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FilesMatchingRegexpDTO {
    private String regexp;
    private List<String> filenames;
}
