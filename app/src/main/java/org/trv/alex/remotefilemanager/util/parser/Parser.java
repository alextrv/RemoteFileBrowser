package org.trv.alex.remotefilemanager.util.parser;

import org.trv.alex.remotefilemanager.util.FileProperties;

import java.util.List;

public interface Parser {
    String PARENT_DIR_DOTS = "../";
    List<FileProperties> getFileList(String url);
}
