package org.trv.alex.remotefilebrowser.util.parser;

import org.trv.alex.remotefilebrowser.util.FileProperties;

import java.util.List;

public interface Parser {
    String PARENT_DIR_DOTS = "../";
    List<FileProperties> getFileList(String url);
}
